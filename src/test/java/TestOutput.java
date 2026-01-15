import java.util.List;

/**
 * TestOutput record to hold test configuration and results.
 *
 * @param config the test configuration
 * @param result the list of results
 */
public record TestOutput (TestConfig config, List<Result> result) {
}
