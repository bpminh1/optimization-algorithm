package org.example.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.example.model.core.IterationInfo;
import org.example.model.core.Solution;
import org.example.model.core.greedySearch.GreedySearch;
import org.example.model.problem.greedyConstructors.GreedyConstructorPlace;
import org.example.model.problem.instance.PackingProblem;
import org.example.model.problem.model.Box;
import org.example.model.problem.model.Placement;
import org.example.model.problem.model.PlacementOverlap;
import org.example.model.problem.model.Rectangle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for scene management and common dialog operations in the GUI.
 */
public class Util {
    /**
     * Changes the current scene to the Create Problem scene.
     *
     * @param currentStage the current stage
     * @throws IOException if loading the FXML file fails
     */
    public void changeSceneToCreateProblem(Stage currentStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/CreateProblem.fxml"));
        Parent root = loader.load();

        currentStage.setTitle("Create Packing Problem");

        Scene scene = new Scene(root);
        currentStage.setScene(scene);
        currentStage.setMaximized(true);
        currentStage.show();
    }

    /**
     * Changes the current scene to the Configure Algorithm scene.
     *
     * @param problem      the packing problem to configure
     * @param currentStage the current stage
     * @throws IOException if loading the FXML file fails
     */
    public void changeSceneToConfigureAlgorithm(PackingProblem problem, Stage currentStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/ConfigureAlgorithm.fxml"));
        Parent root = loader.load();

        ConfigureAlgorithmController controller = loader.getController();
        controller.setProblem(problem);

        currentStage.setTitle("Configure Algorithm");

        Scene scene = new Scene(root);
        currentStage.setScene(scene);
        currentStage.setMaximized(true);
        currentStage.show();
    }

    /**
     * Displays an error alert with the specified header and message.
     *
     * @param header  the header text of the alert
     * @param message the content message of the alert
     */
    public static void showError(String header, String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays an information alert with the specified header and content.
     *
     * @param header  the header text of the alert
     * @param content the content of the alert as a TextFlow
     * @return true if the user clicked OK, false otherwise
     */
    public static boolean showInformation(String header, TextFlow content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(header);
        alert.getDialogPane().setContent(content);

        return alert
                .showAndWait()
                .filter(response -> response == ButtonType.OK)
                .isPresent();
    }

    /**
     * Builds the TextFlow content for the local search information dialog.
     *
     * @return a TextFlow containing the formatted information
     */
    public static TextFlow buildTextContentLocalSearch(){
        Text oldPosition = new Text("light blue");
        Text t1 = new Text(" old position of a rectangle");
        Text newPosition = new Text("\ncorn flower blue");
        Text t2 = new Text(" new position of a rectangle");
        Text boxBorder = new Text("\ndodger blue");
        Text t3 = new Text(" boxes contain changed rectangle");
        Text t4 = new Text("""
                .\
                
                
                The live search process will be visualized.\
                
                ▶ resume the live visualization. \
                
                ⏸ pause the live visualization. \
                
                ⏪ slow down the live visualization. \
                
                ⏩ speed up the live visualization. \
                
                ⏭ skip the live visualization. \
                
                'SOLUTION' shows the final solution found by the algorithm. \
                
                After the live visualization ends, there is a slider to go through the iterations of the local search.""");

        oldPosition.setFill(Color.LIGHTBLUE);
        newPosition.setFill(Color.CORNFLOWERBLUE);
        boxBorder.setFill(Color.DODGERBLUE);

        return new TextFlow(oldPosition, t1, newPosition, t2, boxBorder, t3, t4);
    }

    /**
     * Builds the TextFlow content for the greedy search information dialog.
     *
     * @return a TextFlow containing the formatted information
     */
    public static TextFlow buildTextContentGreedySearch(){
        Text newPosition = new Text("corn flower blue");
        Text t1 = new Text(" newly placed rectangle");
        Text boxBorder = new Text("\ndodger blue");
        Text t2 = new Text(" box where rectangle is placed");
        Text t3 = new Text("""
                .\
                
                
                The live search process will be visualized.\
                
                ▶ resume the live visualization. \
                
                ⏸ pause the live visualization. \
                
                ⏪ slow down the live visualization. \
                
                ⏩ speed up the live visualization. \
                
                ⏭ skip the live visualization. \
                
                'SOLUTION' shows the final solution found by the algorithm. \
                
                After the live visualization ends, there is a slider to go through the iterations of the local search.""");

        newPosition.setFill(Color.CORNFLOWERBLUE);
        boxBorder.setFill(Color.DODGERBLUE);

        return new TextFlow(newPosition, t1, boxBorder, t2, t3);
    }

    /**
     * Builds a mapping of rectangle IDs to their snapshots from the given iteration information.
     *
     * @param iterationInfo the iteration information containing the snapshot
     * @return a map of rectangle IDs to their corresponding RectSnapshot objects
     */
    public static Map<Integer, RectSnapshot> buildRectSnapshot(IterationInfo iterationInfo){
        Placement placement = (Placement) iterationInfo.snapshot();
        Map<Integer, RectSnapshot> rectSnapshotMap = new HashMap<>();
        for(Box box : placement.getBoxes()){
            for(Rectangle rect : box.getRects()){
                RectSnapshot rectSnapshot = new RectSnapshot(
                        rect.getRectID(), box.getId(),
                        rect.getX(), rect.getY(),
                        rect.getWidth(), rect.getHeight(),
                        rect.isRotated()
                );
                rectSnapshotMap.put(rect.getRectID(), rectSnapshot);
            }
        }
        return rectSnapshotMap;
    }

    /**
     * Generates the initial solution based on the specified algorithm type.
     *
     * @param algo    the algorithm type
     * @param problem the packing problem instance
     * @return the initial solution as a Solution object
     */
    public static Solution getInitialSolution(String algo, PackingProblem problem){
        Placement initialSolution;

        if(algo.contains("Overlap")){
            GreedyConstructorPlace constructor = new GreedyConstructorPlace(problem.getL());
            GreedySearch<Rectangle> greedySearch = new GreedySearch<>(
                    items -> items, constructor, problem.getItems());

            PlacementOverlap emptySolution = new PlacementOverlap(problem.getL(), 1.0);
            Box.resetIDCounter();
            emptySolution.addBox();

            initialSolution = (PlacementOverlap) greedySearch.run(emptySolution);
        } else if(algo.contains("Greedy")){
            initialSolution = new Placement(problem.getL());
            Box.resetIDCounter();
            initialSolution.addBox();
        } else{
            initialSolution = (Placement) problem.getInitialSolution();
        }

        return initialSolution;
    }
}
