package org.example.model.core;

import java.util.List;

/**
 * Represents a problem to be solved in an optimization context.
 *
 * @param <T> the type of items involved in the problem
 */
public interface Problem<T> {
    /**
     * Retrieves the list of items involved in the problem.
     *
     * @return a list of items
     */
    List<T> getItems();

    /**
     * Retrieves the initial solution for the problem.
     *
     * @return the initial solution
     */
    Solution getInitialSolution();
}
