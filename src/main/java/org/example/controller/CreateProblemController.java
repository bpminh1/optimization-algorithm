package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import org.example.model.problem.instance.PackingProblem;

import java.io.IOException;

/**
 * Controller for the Create Problem scene.
 */
public class CreateProblemController {
    @FXML
    private Spinner<Integer> L;
    @FXML
    private Spinner<Integer> n;
    @FXML
    private Spinner<Integer> minWidth;
    @FXML
    private Spinner<Integer> maxWidth;
    @FXML
    private Spinner<Integer> minHeight;
    @FXML
    private Spinner<Integer> maxHeight;
    @FXML
    private Button next;

    /**
     * Initializes the controller, setting up spinners and button actions.
     */
    @FXML
    public void initialize() {
        L.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 50));
        n.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1000));
        minWidth.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 3));
        maxWidth.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 10));
        minHeight.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 3));
        maxHeight.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 10));

        next.setOnAction(event -> clickNext());
    }

    /**
     * Handles the action when the "Next" button is clicked.
     * Creates a PackingProblem instance and transitions to the Configure Algorithm scene.
     */
    private void clickNext(){
        try{
            PackingProblem problem = new PackingProblem(
                    42,
                    L.getValue(),
                    n.getValue(),
                    minWidth.getValue(),
                    maxWidth.getValue(),
                    minHeight.getValue(),
                    maxHeight.getValue()
            );

            Stage currentStage = (Stage) next.getScene().getWindow();
            new Util().changeSceneToConfigureAlgorithm(problem, currentStage);
        }catch(IllegalArgumentException e){
            Util.showError("Invalid Input", e.getMessage());
        } catch (IOException e) {
            Util.showError("Runtime Error", e.getMessage());
        }
    }
}
