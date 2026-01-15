import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.core.Solution;
import org.example.model.core.greedySearch.GreedyConstructor;
import org.example.model.core.greedySearch.GreedyRule;
import org.example.model.core.greedySearch.GreedySearch;
import org.example.model.core.localSearch.LocalSearch;
import org.example.model.problem.greedyConstructors.GreedyConstructorPlace;
import org.example.model.problem.greedyRules.SortByArea;
import org.example.model.problem.greedyRules.SortByMaxSide;
import org.example.model.problem.instance.PackingProblem;
import org.example.model.problem.model.*;
import org.example.model.problem.neighborhoods.GeometryBased;
import org.example.model.problem.neighborhoods.PartialOverlap;
import org.example.model.problem.neighborhoods.PermutationBased;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for running various packing algorithms on different configurations and instances.
 */
public class AlgorithmTest {
    private static final String TEST_CONFIG_FILE = "testInstance_1.json";
    private static final String OUTPUT_FOLDER = "testResults/instance_1";

    /**
     * Main test method that loads configurations, runs algorithms, and collects results.
     *
     * @throws IOException if there is an error loading the configuration
     */
    @Test
    public void test() throws IOException {
        List<TestConfig> configs = loadConfig();
        int baseSeed = 42;

        for(int configIdx = 0; configIdx < configs.size(); configIdx++){
            TestConfig config = configs.get(configIdx);
            int configSeed = baseSeed + configIdx * 10;
            List<Result> results = new ArrayList<>();

            Box.resetIDCounter();
            Rectangle.resetIDCounter();

            System.out.println();
            System.out.println("=== Config " + configIdx + " ===");

            for(int instanceIdx = 0; instanceIdx < config.numInstances(); instanceIdx++){
                System.out.println("---- Instance " + instanceIdx + " ----");
                int instanceSeed = configSeed + instanceIdx;

                PackingProblem problem = new PackingProblem(
                        instanceSeed,
                        config.boxLength(),
                        config.numRectangles(),
                        config.minWidth(),
                        config.maxWidth(),
                        config.minHeight(),
                        config.maxHeight()
                );

                runAllMethods(instanceSeed, instanceIdx, problem, results);

                TestOutput output = new TestOutput(config, results);
                writeResults(output, "config_" + configIdx + ".json");
            }
        }
    }

    /**
     * Loads the test configuration from a JSON file.
     *
     * @return a list of TestConfig objects
     * @throws IOException if there is an error reading the file
     */
    private static List<TestConfig> loadConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        InputStream is = AlgorithmTest.class
                .getClassLoader()
                .getResourceAsStream(TEST_CONFIG_FILE);

        if (is == null) throw new FileNotFoundException(TEST_CONFIG_FILE + " not found in resources");

        return mapper.readValue(is, new TypeReference<List<TestConfig>>() {});
    }

    /**
     * Writes the test results to a JSON file.
     *
     * @param output   the TestOutput object containing configuration and results
     * @param fileName the name of the output file
     * @throws IOException if there is an error writing to the file
     */
    private static void writeResults(TestOutput output, String fileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        File outputFolder = new File(System.getProperty("user.dir"), OUTPUT_FOLDER);
        if(!outputFolder.exists()){
            outputFolder.mkdirs();
        }
        File outputFile = new File(outputFolder, fileName);

        mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, output);
    }

    /**
     * Runs all packing algorithms on the given problem instance and collects results.
     *
     * @param instanceSeed the seed used for generating the instance
     * @param instanceIdx  the index of the instance
     * @param problem      the packing problem instance
     * @param results      the list to collect results
     */
    private static void runAllMethods(int instanceSeed, int instanceIdx, PackingProblem problem, List<Result> results){
        System.out.println("Geometry Based Local Search:");
        Box.resetIDCounter();
        geometryBased(instanceSeed, instanceIdx, problem, results);
        System.out.println("Done");

        System.out.println("Permutation Based Local Search:");
        Box.resetIDCounter();
        permutationBased(instanceSeed, instanceIdx, problem, results);
        System.out.println("Done");

        System.out.println("Partial Overlap Local Search:");
        Box.resetIDCounter();
        partialOverlap(instanceSeed, instanceIdx, problem, results);
        System.out.println("Done");

        System.out.println("Greedy Area Based:");
        Box.resetIDCounter();
        greedyArea(instanceSeed, instanceIdx, problem, results);
        System.out.println("Done");

        System.out.println("Greedy Max Side Based:");
        Box.resetIDCounter();
        greedyMaxSide(instanceSeed, instanceIdx, problem, results);
        System.out.println("Done");
    }

    /**
     * Runs the Geometry Based Local Search algorithm and records the results.
     *
     * @param instanceSeed the seed used for generating the instance
     * @param instanceIdx  the index of the instance
     * @param problem      the packing problem instance
     * @param results      the list to collect results
     */
    private static void geometryBased(int instanceSeed, int instanceIdx, PackingProblem problem, List<Result> results){
        LocalSearch geometry = new LocalSearch(new GeometryBased());

        Placement initialPlacement = (Placement) problem.getInitialSolution();
        results.add(new Result(
                instanceSeed,
                instanceIdx,
                "Initial Solution",
                initialPlacement.getNumBoxes(),
                initialPlacement.getObjectiveValue(),
                0.0
        ));

        long start = System.nanoTime();
        Solution solution = geometry.run(problem.getInitialSolution());
        long end = System.nanoTime();
        double time = (end - start) / 1_000_000_000.0;

        Placement placement = (Placement) solution;
        results.add(new Result(
                instanceSeed,
                instanceIdx,
                "Geometry Based",
                placement.getNumBoxes(),
                solution.getObjectiveValue(),
                time));
    }

    /**
     * Runs the Permutation Based Local Search algorithm and records the results.
     *
     * @param instanceSeed the seed used for generating the instance
     * @param instanceIdx  the index of the instance
     * @param problem      the packing problem instance
     * @param results      the list to collect results
     */
    private static void permutationBased(int instanceSeed, int instanceIdx, PackingProblem problem, List<Result> results){
        LocalSearch permutation = new LocalSearch(new PermutationBased(problem.getL(), problem.getInitialSolution()));

        long start = System.nanoTime();
        Solution solution = permutation.run(problem.getInitialSolution());
        long end = System.nanoTime();
        double time = (end - start) / 1_000_000_000.0;

        Placement placement = (Placement) solution;
        results.add(new Result(
                instanceSeed,
                instanceIdx,
                "Permutation Based",
                placement.getNumBoxes(),
                solution.getObjectiveValue(),
                time));
    }

    /**
     * Runs the Partial Overlap Local Search algorithm and records the results.
     *
     * @param instanceSeed the seed used for generating the instance
     * @param instanceIdx  the index of the instance
     * @param problem      the packing problem instance
     * @param results      the list to collect results
     */
    private static void partialOverlap(int instanceSeed, int instanceIdx, PackingProblem problem, List<Result> results){
        GreedyConstructorPlace constructor = new GreedyConstructorPlace(problem.getL());
        GreedySearch<Rectangle> greedySearch = new GreedySearch<>(
                items -> items, constructor, problem.getItems());

        PlacementOverlap emptySolution = new PlacementOverlap(problem.getL(), 1.0);
        emptySolution.addBox();

        PlacementOverlap initialSolution = (PlacementOverlap) greedySearch.run(emptySolution);

        LocalSearch partialOverlap = new LocalSearch(new PartialOverlap());

        long start = System.nanoTime();
        Solution solution = partialOverlap.run(initialSolution);
        long end = System.nanoTime();
        double time = (end - start) / 1_000_000_000.0;

        assertNoOverlap((PlacementOverlap) solution);
        Placement placement = (Placement) solution;
        results.add(new Result(
                instanceSeed,
                instanceIdx,
                "Partial Overlap",
                placement.getNumBoxes(),
                solution.getObjectiveValue(),
                time));
    }

    /**
     * Runs the Greedy Area Based algorithm and records the results.
     *
     * @param instanceSeed the seed used for generating the instance
     * @param instanceIdx  the index of the instance
     * @param problem      the packing problem instance
     * @param results      the list to collect results
     */
    private static void greedyArea(int instanceSeed, int instanceIdx, PackingProblem problem, List<Result> results){
        GreedyConstructor<Rectangle> constructor = new GreedyConstructorPlace(problem.getL());
        GreedyRule<Rectangle> areaBased = new SortByArea();
        GreedySearch<Rectangle> areaBasedSearch = new GreedySearch<>(areaBased, constructor, problem.getItems());

        Placement emptyPlacement = new Placement(problem.getL());
        emptyPlacement.addBox();

        long start = System.nanoTime();
        Solution solution = areaBasedSearch.run(emptyPlacement);
        long end = System.nanoTime();
        double time = (end - start) / 1_000_000_000.0;

        Placement placement = (Placement) solution;

        results.add(new Result(
                instanceSeed,
                instanceIdx,
                "Greedy Area Based",
                placement.getNumBoxes(),
                solution.getObjectiveValue(),
                time));
    }

    /**
     * Runs the Greedy Max Side Based algorithm and records the results.
     *
     * @param instanceSeed the seed used for generating the instance
     * @param instanceIdx  the index of the instance
     * @param problem      the packing problem instance
     * @param results      the list to collect results
     */
    private static void greedyMaxSide(int instanceSeed, int instanceIdx, PackingProblem problem, List<Result> results){
        GreedyConstructor<Rectangle> constructor = new GreedyConstructorPlace(problem.getL());
        GreedyRule<Rectangle> maxSideBased = new SortByMaxSide();
        GreedySearch<Rectangle> maxSideBasedSearch = new GreedySearch<>(maxSideBased, constructor, problem.getItems());

        Placement emptyPlacement = new Placement(problem.getL());
        emptyPlacement.addBox();

        long start = System.nanoTime();
        Solution solution = maxSideBasedSearch.run(emptyPlacement);
        long end = System.nanoTime();
        double time = (end - start) / 1_000_000_000.0;

        Placement placement = (Placement) solution;

        results.add(new Result(
                instanceSeed,
                instanceIdx,
                "Greedy Max Side Based",
                placement.getNumBoxes(),
                solution.getObjectiveValue(),
                time));
    }

    /**
     * Asserts that there is no overlap between rectangles in the given solution.
     * Used for Partial Overlap Local Search verification.
     *
     * @param solution the solution to check for overlaps
     */
    private static void assertNoOverlap(PlacementOverlap solution){
        BoxOverlap boxWithOverlap = solution.findBoxWithOverlap();
        if(boxWithOverlap != null){
            throw new AssertionError("Overlap detected in box ID: " + boxWithOverlap.getId());
        }
    }
}
