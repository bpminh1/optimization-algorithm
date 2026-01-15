/**
 * Configuration record for test parameters.
 *
 * @param numInstances   Number of test instances to generate.
 * @param numRectangles  Number of rectangles in each instance.
 * @param minWidth       Minimum width of rectangles.
 * @param maxWidth       Maximum width of rectangles.
 * @param minHeight      Minimum height of rectangles.
 * @param maxHeight      Maximum height of rectangles.
 * @param boxLength      Length of the box to fit rectangles into.
 * @param difficulty     Difficulty level of the test instance.
 */
public record TestConfig(int numInstances, int numRectangles, int minWidth, int maxWidth, int minHeight, int maxHeight, int boxLength, String difficulty) {
}
