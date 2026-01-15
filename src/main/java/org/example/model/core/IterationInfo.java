package org.example.model.core;

/**
 *
 * Information about a specific iteration in an optimization process.
 * @param iteration the iteration number
 * @param objectiveValue the value of the objective function at this iteration
 * @param allowedOverlap the allowed overlap parameter at this iteration
 * @param snapshot the solution snapshot at this iteration
 */
public record IterationInfo(int iteration, double objectiveValue, double allowedOverlap, Solution snapshot) {
    @Override
    public String toString() {
        return String.format("Iteration %d, Allowed Overlap = %.3f, Objective Value = %.3f",
                iteration,
                allowedOverlap,
                objectiveValue
        );
    }
}
