package com.smartcoffee;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application launcher for ByteSized Coffee.
 * Loads the graphical FXML layout and sets up the primary window stage.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load the FXML layout file
        Parent root = FXMLLoader.load(getClass().getResource("/com/smartcoffee/gui/main_layout.fxml"));
        
        // Define dimensions (starts collapsed at 720px: customer touchscreen only)
        Scene scene = new Scene(root, 720, 680);
        
        stage.setScene(scene);
        stage.setTitle("ByteSized Coffee Simulator");
        stage.setResizable(false); // Lock manual resizing to preserve layout, programmatic toggle resizing still works!
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
