package it.unicas.engsoftwareproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setScene(scene);
        stage.setTitle("Titolo");
        stage.sizeToScene();
        stage.show();

        int T = 100;

        CSVReader reader = new CSVReader(System.getProperty("user.dir") + "/CSV/" + "BMS_data.csv", T);
        reader.start();
    }

    public static void main(String[] args) {
        launch();
    }
}