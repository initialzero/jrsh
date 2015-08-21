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

import com.jaspersoft.jasperserver.jrsh.common.exception.CannotCreateStrategyInstanceException;
import com.jaspersoft.jasperserver.jrsh.evaluation.strategy.impl.ScriptEvaluationStrategy;
import com.jaspersoft.jasperserver.jrsh.evaluation.strategy.impl.ShellEvaluationStrategy;
import com.jaspersoft.jasperserver.jrsh.evaluation.strategy.impl.ToolEvaluationStrategy;

import static com.jaspersoft.jasperserver.jrsh.operation.grammar.token.TokenPreconditions.isConnectionString;
import static com.jaspersoft.jasperserver.jrsh.operation.grammar.token.TokenPreconditions.isScriptFileName;

/**
 * An implementation of {@link EvaluationStrategyFactory}
 *
 * @author Alexander Krasnyanskiy
 * @since 2.0
 */
public class EvaluationStrategyFactoryImpl
        implements EvaluationStrategyFactory {

    /**
     * {@inheritDoc}
     */
    public EvaluationStrategy getStrategy(String[] arguments) {
        EvaluationStrategy strategy = null;
        Class<? extends EvaluationStrategy> strategyType;

        if (arguments.length == 1 && isConnectionString(arguments[0])) {
            strategyType = ShellEvaluationStrategy.class;
        } else if (arguments.length == 2
                && "--script".equals(arguments[0])
                && isScriptFileName(arguments[1])) {
            strategyType = ScriptEvaluationStrategy.class;
        } else {
            strategyType = ToolEvaluationStrategy.class;
        }
        try {
            strategy = strategyType.newInstance();
        } catch (Exception ignored) {
            throw new CannotCreateStrategyInstanceException(strategyType);
        }

        return strategy;
    }
}
