package org.example.model.core.localSearch;

import org.example.model.core.Solution;

/**
 * Interface representing a neighborhood structure for local search algorithms.
 */
public interface Neighborhood {
    /**
     * Generates a neighboring solution based on the current solution.
     *
     * @param currentSolution the current solution
     * @return a neighboring solution
     */
    Solution getNeighbor(Solution currentSolution);

    /**
     * Retrieves the current allowed overlap parameter.
     *
     * @return the current allowed overlap value
     */
    double getCurrentAllowedOverlap();

    /**
     * Updates the allowed overlap parameter based on the neighborhood strategy.
     */
    void updateAllowedOverlap();
}
