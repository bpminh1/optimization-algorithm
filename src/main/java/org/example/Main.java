package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.controller.Util;

public class Main extends Application {
    static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        new Util().changeSceneToCreateProblem(stage);
    }
}
