package org.example.model.core;

/**
 * Interface representing a solution in an optimization problem.
 */
public interface Solution {
    /**
     * Retrieves the value of the objective function for this solution.
     *
     * @return the objective value
     */
    double getObjectiveValue();

    /**
     * Sets the allowed overlap parameter for this solution.
     *
     * @param overlap the allowed overlap value
     */
    void setAllowedOverlap(double overlap);

    /**
     * Creates a copy of this solution.
     *
     * @return a new Solution instance that is a copy of this one
     */
    Solution copy();
}
