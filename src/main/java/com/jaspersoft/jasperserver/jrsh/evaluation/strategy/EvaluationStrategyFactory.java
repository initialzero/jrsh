package com.jaspersoft.jasperserver.jrsh.evaluation.strategy;

/**
 * Factory interface that can be used to create {@link EvaluationStrategy}s.
 *
 * @author Alexander Krasnyanskiy
 */
public interface EvaluationStrategyFactory {

    /**
     * Gets a proper strategy to evaluate operations in corresponding mode.
     * @param arguments application arguments to analyze
     * @return strategy
     */
    EvaluationStrategy getStrategy(String[] arguments);
}
