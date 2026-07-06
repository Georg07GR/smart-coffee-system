package com.smartcoffee;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Basic JavaFX Main application to verify project setup and dependencies.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        Label label = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        
        Scene scene = new Scene(new StackPane(label), 640, 480);
        stage.setScene(scene);
        stage.setTitle("Smart Coffee & Payment System - Setup Verification");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
