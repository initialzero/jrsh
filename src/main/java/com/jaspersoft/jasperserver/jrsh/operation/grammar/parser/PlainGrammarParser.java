/*
 * Copyright (C) 2005 - 2015 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased  a commercial license agreement from Jaspersoft,
 * the following license terms  apply:
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License  as
 * published by the Free Software Foundation, either version 3 of  the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero  General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public  License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaspersoft.jasperserver.jrsh.operation.grammar.parser;

import com.jaspersoft.jasperserver.jrsh.operation.Operation;
import com.jaspersoft.jasperserver.jrsh.operation.annotation.Master;
import com.jaspersoft.jasperserver.jrsh.operation.annotation.Parameter;
import com.jaspersoft.jasperserver.jrsh.operation.annotation.Prefix;
import com.jaspersoft.jasperserver.jrsh.operation.annotation.Value;
import com.jaspersoft.jasperserver.jrsh.operation.grammar.Grammar;
import com.jaspersoft.jasperserver.jrsh.operation.grammar.graph.TokenEdge;
import com.jaspersoft.jasperserver.jrsh.operation.grammar.graph.TokenEdgeFactory;
import com.jaspersoft.jasperserver.jrsh.operation.grammar.rule.PlainRule;
import com.jaspersoft.jasperserver.jrsh.operation.grammar.rule.Rule;
import com.jaspersoft.jasperserver.jrsh.operation.grammar.token.Token;
import com.jaspersoft.jasperserver.jrsh.operation.parser.exception.OperationParseException;
import com.jaspersoft.jasperserver.jrsh.operation.parser.exception.WrongOperationFormatException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.jaspersoft.jasperserver.jrsh.operation.grammar.token.TokenUtils.instantiateToken;

/**
 * Plain implementation of {@link GrammarParser} interface.
 *
 * @author Alexander Krasnyanskiy
 * @since 2.0
 */
public class PlainGrammarParser implements GrammarParser {
    private static Graph<Token, TokenEdge<Token>> graph;
    private static Map<String, Pair<Token, String[]>> dependencies;
    private static Map<String, RuleGroup> groups;
    private static Token root;

    /**
     * Parses operation grammar using Search Graph algorithm for
     * building all possible grammar paths.
     *
     * @param operation operation instance
     * @return operation grammar
     * @throws OperationParseException
     */
    public Grammar parseGrammar(Operation operation) throws OperationParseException {
        graph = new DefaultDirectedGraph<>(new TokenEdgeFactory());
        dependencies = new HashMap<>();
        groups = new HashMap<String, RuleGroup>();

        Grammar grammar = new DefaultGrammar();
        Set<Rule> rules = new HashSet<>();
        Class<?> clazz = operation.getClass();
        Master master = clazz.getAnnotation(Master.class);

        if (master != null) {
            root = instantiateToken(master.tokenClass(), master.name(), master.name(), true, true);

            if (master.tail()) {
                rules.add(new PlainRule(root));
            }

            dependencies.put(root.getName(), new ImmutablePair<>(root, new String[]{}));
            graph.addVertex(root);
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                Prefix prefix = field.getAnnotation(Prefix.class);
                Parameter param = field.getAnnotation(Parameter.class);

                if (param != null) {
                    OperationParameter param1 = new OperationParameter();
                    param1.getTokens().add(root);

                    for (String key : groups.keySet()) {
                        groups.get(key).getParameters().add(param1);
                    }

                    boolean isMandatory = param.mandatory();
                    Value[] values = param.values();
                    OperationParameter param2 = new OperationParameter();

                    for (Value v : values) {
                        Token token = instantiateToken(v.tokenClass(), v.tokenAlias(), v.tokenValue(), isMandatory, v.tail());
                        graph.addVertex(token);

                        if (prefix != null) {
                            Token prefixTkn = instantiateToken(prefix.tokenClass(), prefix.value(), prefix.value(), isMandatory, false);
                            dependencies.put(prefixTkn.getName(), new ImmutablePair<>(prefixTkn, param.dependsOn()));
                            dependencies.put(token.getName(), new ImmutablePair<>(token, new String[]{prefix.value()}));
                            param2.getTokens().add(prefixTkn);
                            graph.addVertex(prefixTkn);
                        } else {
                            dependencies.put(token.getName(), new ImmutablePair<>(token, param.dependsOn()));
                        }

                        param2.getTokens().add(token);
                        String[] ruleGroupKey = param.ruleGroups();

                        for (String groupKey : ruleGroupKey) {
                            RuleGroup ruleGroup = groups.get(groupKey);

                            if (ruleGroup != null) {
                                ruleGroup.getParameters().add(param2);
                            } else {
                                groups.put(groupKey, new RuleGroup(param1, param2));
                            }
                        }
                    }
                }
            }
        }

        buildGraphEdges();

        if (!(graph.vertexSet().size() == 1 && graph.vertexSet().contains(root))) {
            rules.addAll(buildRules());
        }
        if (!rules.isEmpty()) {
            grammar.addRules(rules);
        } else throw new WrongOperationFormatException();

        return grammar;
    }

    // ---------------------------------------------------------------------
    //                           Helper methods
    // ---------------------------------------------------------------------

    protected Set<Rule> buildRules() {
        KShortestPaths<Token, TokenEdge<Token>> paths = new KShortestPaths<>(graph, root, 2500);
        Set<Token> vertexes = graph.vertexSet();
        Set<Rule> rules = new LinkedHashSet<>();

        for (Token vertex : vertexes) {
            if (!vertex.equals(root)) {
                if (vertex.isTailOfRule()) {
                    val ps = paths.getPaths(vertex);
                    for (val path : ps) {
                        Rule rule = convertGraphPathToRule(path);
                        if (isValidRule(rule)) {
                            rules.add(rule);
                        }
                    }
                }
            }
        }
        return rules;
    }

    protected boolean isValidRule(final Rule rule) {
        List<Token> tokens = rule.getTokens();

        for (RuleGroup group : groups.values()) {
            if (group.getGroupTokens().containsAll(tokens)) {
                val parameters = group.getParameters();

                for (OperationParameter parameter : parameters) {
                    Set<Token> mandatoryTokens = parameter.getOnlyMandatoryTokens();
                    if (mandatoryTokens.size() > 0) {
                        boolean notContains = true;

                        for (Token token : tokens) {
                            if (mandatoryTokens.contains(token)) {
                                notContains = false;
                            }
                        }
                        if (notContains) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    protected Rule convertGraphPathToRule(GraphPath<Token, TokenEdge<Token>> path) {
        List<TokenEdge<Token>> list = path.getEdgeList();
        Rule rule = new PlainRule();
        Set<Token> set = new LinkedHashSet<>();
        for (val edge : list) {
            set.add(edge.getSource());
            set.add(edge.getTarget());
        }
        for (Token token : set) {
            rule.addToken(token);
        }
        return rule;

    }

    protected void buildGraphEdges() {
        for (val entry : dependencies.entrySet()) {
            Pair<Token, String[]> tokenPair = entry.getValue();
            for (String dependencyName : tokenPair.getRight()) {
                Pair<Token, String[]> dependency = dependencies.get(dependencyName);
                graph.addEdge(dependency.getLeft(), tokenPair.getLeft());
            }
        }
    }

    // ---------------------------------------------------------------------
    //                            Nested Classes
    // ---------------------------------------------------------------------

    protected class DefaultGrammar implements Grammar {
        private List<Rule> rules = new ArrayList<>();

        public DefaultGrammar(Rule... rules) {
            Collections.addAll(this.rules, rules);
        }

        @Override
        public List<Rule> getRules() {
            return rules;
        }

        @Override
        public void addRule(Rule rule) {
            rules.add(rule);
        }

        @Override
        public void addRules(Collection<Rule> rules) {
            this.rules.addAll(rules);
        }
    }

    @Data
    @EqualsAndHashCode
    protected class RuleGroup {
        Set<OperationParameter> parameters = new HashSet<>();

        protected RuleGroup(OperationParameter... parameters) {
            Collections.addAll(this.parameters, parameters);
        }

        Set<Token> getGroupTokens() {
            Set<Token> set = new HashSet<>();
            for (val parameter : parameters) {
                set.addAll(parameter.getTokens());
            }
            return set;
        }
    }

    @Data
    @EqualsAndHashCode
    protected class OperationParameter {
        Set<Token> tokens = new HashSet<>();

        Set<Token> getOnlyMandatoryTokens() {
            Set<Token> mandatoryTokens = new HashSet<>();
            for (val token : tokens) {
                if (token.isMandatory()) {
                    mandatoryTokens.add(token);
                }
            }
            return mandatoryTokens;
        }
    }
}