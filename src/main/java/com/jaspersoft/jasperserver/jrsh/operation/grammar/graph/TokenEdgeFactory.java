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
import org.jgrapht.EdgeFactory;

/**
 * A custom implementation of {@link EdgeFactory} interface. Used to
 * for creating new {@link Token} edges.
 *
 * @author Alexander Krasnyanskiy
 * @since 2.0
 */
public class TokenEdgeFactory
        implements EdgeFactory<Token, TokenEdge<Token>> {

    /**
     * {@inheritDoc}
     */
    @Override
    public TokenEdge<Token> createEdge(Token source, Token target) {
        return new TokenEdge<Token>(source, target);
    }

}
