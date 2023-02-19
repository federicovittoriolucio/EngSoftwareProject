package it.unicas.engsoftwareproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Runnable class initialized at the beginning of execution of the program.
 */
public class BMSMonitor extends Application
{
    public static ArrayList<Stage> stagelist;

    /**
     * Given the initial stage, loads the menu fxml into the scene and displays the Menu Stage.
     * @see it.unicas.engsoftwareproject.controller.MenuController
     * @param stage Primary stage displayed at launch. (Menu Monitor)
     * @throws IOException Expection thrown loading the menu-view fxml.
     */
    @Override
    public void start(Stage stage) throws IOException
    {
        // Generates a new stage, loads the required fxml, assigns the new scene and shows the newly created stage (Menu)
        stagelist = new ArrayList<>();
        stagelist.add(stage);
        FXMLLoader fxmlLoader = new FXMLLoader(BMSMonitor.class.getResource("menu-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css");
        scene.getStylesheets().getClass().getResource("CustomStylesheet.css");
        stage.setScene(scene);
        stage.setTitle("Main Menu");
        stage.sizeToScene();
        stage.setResizable(false);
        stage.show();
    }

    /**
     * The main calls the launch() method (Application Class) which implicitly calls start.
     * @see BMSMonitor#start(Stage)
     * @param args Arguments not used.
     */
    public static void main(String[] args)
    {
        launch();
    }
}