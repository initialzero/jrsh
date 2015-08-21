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
package com.jaspersoft.jasperserver.jrsh.operation.grammar.graph;

import com.jaspersoft.jasperserver.jrsh.operation.grammar.token.Token;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

/**
 * An implementation of {@link DefaultEdge} in a {@link Graph}.
 *
 * @param <T> token type
 * @author Alexander Krasnyanskiy
 * @since 2.0
 */
public class TokenEdge<T extends Token> extends DefaultEdge {

    private T source;
    private T target;

    /**
     * Constructs a new {@link TokenEdge}.
     *
     * @param source an edge source
     * @param target an edge target
     */
    public TokenEdge(T source, T target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public T getSource() {
        return source;
    }

    @Override
    public T getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "(" + source + " : " + target + ")";
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

}
