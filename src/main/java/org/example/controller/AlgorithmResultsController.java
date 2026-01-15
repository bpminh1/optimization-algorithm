package org.example.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.example.model.core.IterationInfo;
import org.example.model.core.Search;
import org.example.model.core.Solution;
import org.example.model.problem.instance.PackingProblem;
import org.example.model.problem.model.Placement;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Controller for the Algorithm Results scene.
 */
public class AlgorithmResultsController {
    @FXML
    private Text algoName;
    @FXML
    private Text detailInfo;
    @FXML
    private GridPane grid;
    @FXML
    private Button changeAlgo;
    @FXML
    private Slider slider;
    @FXML
    private Label sliderLabel;
    @FXML
    private Button skip;
    @FXML
    private Button solutionButton;
    @FXML
    private Button pause;
    @FXML
    private Button slower;
    @FXML
    private Button faster;

    private PackingProblem problem;
    private Solution solution;
    private RenderBoxes boxRenderer;

    // Visualization controls
    private static long VISUALIZATION_DELAY_MS = 650L;
    private final BlockingDeque<IterationInfo> frameQueue = new LinkedBlockingDeque<>();
    private Thread visualizationThread;

    // Control flags
    private volatile boolean visualizationFinished = false;
    private volatile boolean skipRequested = false;
    private volatile boolean pauseRequested = false;
    private boolean sliderReady = false;

    private final Object pauseLock = new Object();
    private List<IterationInfo> history;
    private List<Map<Integer, RectSnapshot>> iterationSnapshots;

    /**
     * Initializes the controller, setting up button actions and visualization parameters.
     */
    @FXML
    public void initialize(){
        changeAlgo.setOnAction(event -> {
            try {
                Stage currentStage = (Stage) changeAlgo.getScene().getWindow();
                new Util().changeSceneToConfigureAlgorithm(this.problem, currentStage);
            } catch (IOException e) {
                Util.showError("Runtime Error", e.getMessage());
            }
        });

        skip.setOnAction(event -> {
            skipRequested = true;

            if(visualizationThread != null && visualizationThread.isAlive()){
                visualizationThread.interrupt();
            }
        });

        solutionButton.setOnAction(event -> boxRenderer.renderBoxes(solution));

        pause.setOnAction(event -> {
            pauseRequested = !pauseRequested;
            if(pauseRequested){
                pause.setText("▶");
            } else{
                pause.setText("⏸");
                // Notify the visualization thread to resume
                synchronized (pauseLock){
                    pauseLock.notifyAll();
                }
            }
        });

        faster.setOnAction(event -> {
            if(VISUALIZATION_DELAY_MS > 100){
                VISUALIZATION_DELAY_MS -= 100L;
            }
        });

        slower.setOnAction(event -> {
            VISUALIZATION_DELAY_MS += 100L;
        });

        visualizationFinished = false;
        skipRequested = false;
        sliderReady = false;
        pauseRequested = false;
        frameQueue.clear();
        VISUALIZATION_DELAY_MS = 650L;
    }

    /**
     * Sets the title and initializes the grid for visualization.
     *
     * @param algo    the name of the algorithm
     * @param problem the packing problem instance
     * @param search  the search instance (LocalSearch or GreedySearch)
     */
    public void setTitleAndGrid
            (String algo, PackingProblem problem, Search search)
    {
        this.problem = problem;
        this.boxRenderer = new RenderBoxes(grid, problem, iterationSnapshots);
        this.algoName.setText("Solving with: " + algo);

        Thread t = getAlgorithmThread(algo, search);
        Platform.runLater(t::start);
    }

    /**
     * Creates and returns a thread to run the specified search algorithm.
     *
     * @param algo   the name of the algorithm
     * @param search the search instance (LocalSearch or GreedySearch)
     * @return the thread that runs the algorithm
     */
    private Thread getAlgorithmThread(String algo, Search search) {
        long start = System.nanoTime();

        // Create a task to run the search algorithm
        Task<Solution> task = new Task<>(){
            protected Solution call(){
                search.setIterationListener(frameQueue::offer);
                return search.run(Util.getInitialSolution(algo, problem));
            }
        };

        startVisualizationThread(task, algo);

        task.setOnSucceeded(e -> {
            long end = System.nanoTime();
            double time = (end - start)/1_000_000_000.0;

            // Retrieve the final solution and history
            solution = task.getValue();
            history = search.getHistory();
            buildIterationSnapshots();

            skip.setVisible(true);
            skip.setDisable(false);

            // Wait for visualization to finish before setting up UI components
            new Thread(() -> {
                try {
                    while (!visualizationFinished) {
                        Thread.sleep(50);
                    }
                } catch (InterruptedException ignored) {}

                Platform.runLater(() -> {
                    setUpIterationSlider(algo);
                    setUpButtons();
                    boxRenderer.renderBoxes(solution);

                    detailInfo.setText(String.format("Final Solution: time spent=%.3fs\n"
                            + "#Boxes=%d, Objective Value=%.3f",
                            time,
                            ((Placement) solution).getNumBoxes(),
                            solution.getObjectiveValue()));
                });
            }).start();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            Util.showError("Search failed",
                    ex != null? ex.getMessage() : "Unknown error");

            if(visualizationThread != null && visualizationThread.isAlive()){
                visualizationThread.interrupt();
            }
            visualizationFinished = true;

            Platform.runLater(() -> {
                skip.setDisable(true);
                skip.setVisible(false);
            });
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        return t;
    }

    /**
     * Starts the visualization thread to render iterations in real-time.
     *
     * @param task the task running the search algorithm
     * @param algo the name of the algorithm
     */
    private void startVisualizationThread(Task<Solution> task, String algo){
        final Map<Integer, RectSnapshot>[] prevSnapshotHolder = new Map[]{null};

        visualizationThread = new Thread(() -> {
            try{
                while(!skipRequested && (!task.isDone() || !frameQueue.isEmpty())){
                    // Handle pause functionality
                    synchronized (pauseLock){
                        while(pauseRequested && !skipRequested){
                            pauseLock.wait();
                        }
                    }

                    if(skipRequested){
                        break;
                    }

                    IterationInfo info = frameQueue.take();
                    Map<Integer, RectSnapshot> prev = prevSnapshotHolder[0];
                    Map<Integer, RectSnapshot> curr = Util.buildRectSnapshot(info);

                    Platform.runLater(() -> {
                        detailInfo.setText(info.toString());
                        if(algo.contains("Permutation")){
                            boxRenderer.renderBoxes(info.snapshot()); // Without highlighting
                        }else {
                            boxRenderer.renderBoxes(info.snapshot(), prev);
                        }
                        prevSnapshotHolder[0] = curr;
                    });

                    Thread.sleep(VISUALIZATION_DELAY_MS);
                }
            } catch(InterruptedException ignored){
            } finally {
                visualizationFinished = true;
            }
        });

        visualizationThread.setDaemon(true);
        visualizationThread.start();
    }

    /**
     * Sets up the iteration slider for navigating through algorithm iterations.
     *
     * @param algo the name of the algorithm
     */
    private void setUpIterationSlider(String algo){
        if(history == null || history.isEmpty() || slider == null){
            return;
        }

        configureSlider();
        sliderReady = true;

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if(!sliderReady){
                return;
            }

            int iteration = newVal.intValue();
            IterationInfo info = history.get(iteration);

            Platform.runLater(() -> {
                sliderLabel.setText(info.toString());
                if(algo.contains("Permutation")){
                    boxRenderer.renderBoxes(info.snapshot()); // Without highlighting
                }else {
                    boxRenderer.renderBoxes(
                            info.snapshot(),
                            iteration > 0?
                            iterationSnapshots.get(iteration - 1) : null
                    );
                }
            });
        });
    }

    /**
     * Configures the slider properties for iteration navigation.
     */
    private void configureSlider() {
        slider.setVisible(true);
        slider.setDisable(false);
        int maxIteration = history.size() - 1;
        slider.setMin(0);
        slider.setMax(maxIteration);
        slider.setValue(maxIteration);
        slider.setMajorTickUnit((double) maxIteration / 5);
        slider.setBlockIncrement(1);
        slider.setLabelFormatter(new StringConverter<>() { // Convert slider values to integer labels
            @Override
            public String toString(Double value) {
                return Integer.toString(value.intValue());
            }

            @Override
            public Double fromString(String s) {
                return Double.valueOf(s);
            }
        });
    }

    /**
     * Sets up the control buttons for visualization.
     */
    private void setUpButtons(){
        skip.setDisable(true);
        skip.setVisible(false);

        solutionButton.setDisable(false);
        solutionButton.setVisible(true);

        pause.setDisable(true);
        pause.setVisible(false);

        slower.setDisable(true);
        slower.setVisible(false);
        faster.setDisable(true);
        faster.setVisible(false);

        changeAlgo.setVisible(true);
        changeAlgo.setDisable(false);
    }

    /**
     * Builds snapshots of each iteration for visualization.
     */
    private void buildIterationSnapshots(){
        if(history == null || history.isEmpty()){
            iterationSnapshots = List.of();
            return;
        }

        iterationSnapshots = history.stream()
                .map(Util::buildRectSnapshot)
                .toList();

        boxRenderer.setIterationSnapshots(iterationSnapshots);
    }
}