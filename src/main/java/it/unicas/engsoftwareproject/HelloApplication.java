package it.unicas.engsoftwareproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("ui.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setScene(scene);
        stage.setTitle("Titolo");
        stage.sizeToScene();
        stage.show();

        int T = 100;

        CSVReader reader = new CSVReader(System.getProperty("user.dir") + "/CSV/" + "BMS_data.csv", T);
        CSVReader reader2 = new CSVReader(System.getProperty("user.dir") + "/CSV/" + "BMS_data2.csv", T);

        reader.start();
        reader2.start();

        Thread.sleep(15000);
        DataHandler.getInstance().writeStatsCSV(0);
        DataHandler.getInstance().writeDataCSV(0);
        DataHandler.getInstance().writeStatsCSV(1);
        DataHandler.getInstance().writeDataCSV(1);
    }

    public static void main(String[] args) {
        launch();
    }
}