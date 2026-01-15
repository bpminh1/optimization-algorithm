/**
 * Record to store the result of an algorithm run on a specific instance.
 *
 * @param instanceSeed   Seed of the instance.
 * @param instanceIdx    Index of the instance.
 * @param algorithm      Name of the algorithm used.
 * @param usedBoxes     Number of boxes used in the solution.
 * @param objectiveValue Objective value of the solution.
 * @param time           Time taken to compute the solution.
 */
public record Result(int instanceSeed, int instanceIdx, String algorithm, int usedBoxes, double objectiveValue, double time) {
}
