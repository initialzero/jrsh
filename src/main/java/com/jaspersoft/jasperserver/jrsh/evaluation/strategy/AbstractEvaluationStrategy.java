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
package com.jaspersoft.jasperserver.jrsh.evaluation.strategy;

import com.jaspersoft.jasperserver.jrsh.operation.grammar.lexer.PathConsideringLexer;
import com.jaspersoft.jasperserver.jrsh.operation.grammar.parser.PlainGrammarParser;
import com.jaspersoft.jasperserver.jrsh.operation.parser.LL1OperationParser;
import com.jaspersoft.jasperserver.jrsh.operation.parser.OperationParser;
import lombok.Data;

/**
 * Abstract base class for {@link EvaluationStrategy} implementations.
 *
 * @author Alexander Krasnyanskiy
 */
@Data
public abstract class AbstractEvaluationStrategy
        implements EvaluationStrategy {
    protected OperationParser parser;

    public AbstractEvaluationStrategy() {
        this.parser = new LL1OperationParser(
                new PathConsideringLexer(),
                new PlainGrammarParser());
    }
}
