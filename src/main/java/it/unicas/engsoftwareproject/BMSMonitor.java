package it.unicas.engsoftwareproject;

import eu.hansolo.medusa.FGauge;
import eu.hansolo.medusa.FGaugeBuilder;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class BMSMonitor extends Application {
    public static ArrayList<Stage> stagelist;
    @Override
    public void start(Stage stage) throws IOException, InterruptedException {

        stagelist = new ArrayList<>();
        stagelist.add(stage);
        FXMLLoader fxmlLoader = new FXMLLoader(BMSMonitor.class.getResource("menu-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        // To use bootstrap:
        scene.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css");
        scene.getStylesheets().getClass().getResource("CustomStylesheet.css");
        stage.setScene(scene);
        stage.setTitle("Titolo");
        stage.sizeToScene();
        stage.setResizable(false);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}