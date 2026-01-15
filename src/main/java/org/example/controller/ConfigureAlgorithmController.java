package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.example.model.core.Search;
import org.example.model.core.greedySearch.GreedyConstructor;
import org.example.model.core.greedySearch.GreedyRule;
import org.example.model.core.greedySearch.GreedySearch;
import org.example.model.core.localSearch.LocalSearch;
import org.example.model.problem.greedyConstructors.GreedyConstructorPlace;
import org.example.model.problem.greedyRules.SortByArea;
import org.example.model.problem.greedyRules.SortByMaxSide;
import org.example.model.problem.instance.PackingProblem;
import org.example.model.problem.model.Rectangle;
import org.example.model.problem.neighborhoods.GeometryBased;
import org.example.model.problem.neighborhoods.PartialOverlap;
import org.example.model.problem.neighborhoods.PermutationBased;

import java.io.IOException;

/**
 * Controller for the Configure Algorithm scene.
 */
public class ConfigureAlgorithmController {
    private PackingProblem problem;
    @FXML
    private Text problemInfo;
    @FXML
    private Button GeometryBased;
    @FXML
    private Button Permutation;
    @FXML
    private Button PartialOverlap;
    @FXML
    private Button MaxArea;
    @FXML
    private Button MaxSide;
    @FXML
    private Button backButton;

    /**
     * Initializes the controller, setting up button actions for algorithm selection.
     */
    @FXML
    public void initialize(){
        GeometryBased.setOnAction(event -> {
            boolean confirmed = Util.showInformation(
                    "Geometry Based Local Search " +
                            "\nA neighborhood is created by one of these operations: shifting in the same box, moving to another box, rotating a rectangle.",
                    Util.buildTextContentLocalSearch());
            if(!confirmed){
                return;
            }

            LocalSearch localSearch = localSearch("geometry");
            try {
                setNextScene("Geometry Based Local Search", localSearch);
            } catch (IOException e) {
                Util.showError("Runtime Error", e.getMessage());
            }
        });

        Permutation.setOnAction(event -> {
            boolean confirmed = Util.showInformation(
                    "Permutation Local Search " +
                            "\nThe rectangles are shuffled randomly and placed again using a greedy constructor.",
                    new TextFlow(new Text("Each iteration creates a new permutation of the rectangles and constructs a new solution based on that permutation.")));
            if(!confirmed){
                return;
            }

            LocalSearch localSearch = localSearch("permutation");
            try {
                setNextScene("Permutation Based Local Search", localSearch);
            } catch (IOException e) {
                Util.showError("Runtime Error", e.getMessage());
            }
        });

        PartialOverlap.setOnAction(event -> {
            boolean confirmed = Util.showInformation(
                    """
                            Partial Overlap Local Search \
                            
                            The allowed overlap ratio is in the beginning equal to 1.0 and decreases gradually to 0.0 during the search process.\
                            
                            A neighborhood is created by one of these operations: \
                            
                            if one of the boxes has an overlap ratio higher than the allowed overlap ratio, resolves this first\
                            
                            shifting in the same box, moving to another box, rotating a rectangle""",
                    Util.buildTextContentLocalSearch());
            if(!confirmed){
                return;
            }

            LocalSearch localSearch = localSearch("overlap");
            try {
                setNextScene("Partial Overlap Local Search", localSearch);
            } catch (IOException e) {
                Util.showError("Runtime Error", e.getMessage());
            }
        });

        MaxArea.setOnAction(event -> {
            boolean confirmed = Util.showInformation(
                    "Greedy Search Max Area First" +
                            "\nRectangles are sorted by their area (width * height) and placed in descending order.",
                    Util.buildTextContentGreedySearch());
            if(!confirmed){
                return;
            }

            GreedySearch<Rectangle> greedySearch = greedySearch("area");
            try {
                setNextScene("Greedy Max Area First ", greedySearch);
            } catch (IOException e) {
                Util.showError("Runtime Error", e.getMessage());
            }
        });

        MaxSide.setOnAction(event -> {
            boolean confirmed = Util.showInformation(
                    "Greedy Search Max Side First " +
                            "\nRectangles are sorted by their maximum side length (max(width, height)) and placed in descending order.",
                    Util.buildTextContentGreedySearch());
            if(!confirmed){
                return;
            }

            GreedySearch<Rectangle> greedySearch = greedySearch("maxside");
            try {
                setNextScene("Greedy Max Side First", greedySearch);
            } catch (IOException e) {
                Util.showError("Runtime Error", e.getMessage());
            }
        });

        backButton.setOnAction(event -> {
            Stage currentStage = (Stage) backButton.getScene().getWindow();

            try {
                new Util().changeSceneToCreateProblem(currentStage);
            } catch (IOException e) {
                Util.showError("Runtime Error", e.getMessage());
            }
        });
    }

    /**
     * Sets the next scene to display the algorithm results.
     *
     * @param algo   the name of the algorithm
     * @param search the search instance (LocalSearch or GreedySearch)
     * @throws IOException if loading the FXML file fails
     */
    public void setNextScene(String algo, Search search)
            throws IOException
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/AlgorithmResults.fxml"));
        Parent root = loader.load();

        AlgorithmResultsController controller = loader.getController();
        controller.setTitleAndGrid(algo, problem, search);

        Stage currentStage = (Stage) problemInfo.getScene().getWindow();
        currentStage.setTitle("Algorithm Results");

        Scene scene = new Scene(root);
        currentStage.setScene(scene);
        currentStage.setMaximized(true);
        currentStage.show();
    }

    /**
     * Sets the packing problem for the controller.
     *
     * @param problem the packing problem instance
     */
    public void setProblem(PackingProblem problem){
        this.problem = problem;
        if(problem != null) {
            problemInfo.setText("Problem created: " + problem);
        }
    }

    /** Creates a LocalSearch instance based on the selected algorithm.
     *
     * @param algo the selected local search algorithm
     * @return the LocalSearch instance
     */
    private LocalSearch localSearch(String algo){
        return new LocalSearch(algo.equals("geometry")?
                new GeometryBased() : algo.equals("permutation")?
                new PermutationBased(problem.getL(), problem.getInitialSolution()) : new PartialOverlap());
    }

    /** Creates a GreedySearch instance based on the selected rule.
     *
     * @param rule the selected greedy rule
     * @return the GreedySearch instance
     */
    private GreedySearch<Rectangle> greedySearch(String rule){
        GreedyConstructor<Rectangle> constructor = new GreedyConstructorPlace(problem.getL());
        GreedyRule<Rectangle> greedyRule = rule.equals("area")?
                new SortByArea() : new SortByMaxSide();

        return new GreedySearch<>(greedyRule, constructor, problem.getItems());
    }
}
