package org.example.model.core;

import java.util.List;

/**
 * Interface representing a search algorithm for optimization problems.
 */
public interface Search {
    /**
     * Runs the search algorithm starting from the given initial solution.
     *
     * @param initialSolution the initial solution to start the search from
     * @return the best solution found by the search algorithm
     */
    Solution run(Solution initialSolution);

    /**
     * Sets the iteration listener to receive updates during the search process.
     *
     * @param listener the iteration listener
     */
    void setIterationListener(IterationListener listener);

    /**
     * Retrieves the history of iterations performed during the search.
     *
     * @return a list of iteration information
     */
    List<IterationInfo> getHistory();
}
