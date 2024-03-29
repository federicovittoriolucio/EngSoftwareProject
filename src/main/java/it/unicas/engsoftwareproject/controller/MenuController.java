package it.unicas.engsoftwareproject.controller;

import it.unicas.engsoftwareproject.BMSMonitor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

import it.unicas.engsoftwareproject.DataHandler;

/**
 * Controller of the Menu Stage, using menu-view fxml file.
 * Manages user interface and interaction of the menu: the user is able to select data sources and sample time for the execution.
 */
public class MenuController {
    // List of sources to analyze
    @FXML
    private ListView<String> listview;

    // Sample time of the simulation
    @FXML
    private TextField timefield;

    /**
     * Method called on action by the "Start Simulation" button, stores and checks the given paths in the list view,
     * notifies the MonitorController class with the settings and finally setups the monitor stage.
     * It also checks if the given sources and sample time are valid.
     * Hides in the background until the monitor stage is closed.
     * @see MonitorController#setSettings(String[], int)
     * @see MenuController#showAlert(String, String)
     */
    @FXML
    protected void startSimulation()
    {
        // Show alerts if exceptions may occur
        if (listview.getItems().size() == 0) {
            showAlert("Alert","You haven't provided any data source.");
            return;
        }
        if (!timefield.getText().matches("\\d*") || timefield.getText().isEmpty()){
            showAlert("Alert","You can't use the specified sample time. Insert a valid sample time.");
            return;
        }
        for(String item : listview.getItems().toArray(new String[0]))
            if(!item.endsWith(".csv")) {
                showAlert("Alert","One of the selected files is not valid.");
                return;
            }

        // Notifies MonitorController about paths and sample time to use for the simulation
        MonitorController.setSettings(listview.getItems().toArray(new String[0]), Integer.parseInt(timefield.getText()));

        // Generates a new stage, loads the required fxml, assigns the new scene and show the newly created stage (Monitor)
        BMSMonitor.stagelist.get(0).hide();
        Stage monitor_stage = new Stage();
        BMSMonitor.stagelist.add(monitor_stage);
        FXMLLoader fxmlLoader = new FXMLLoader(BMSMonitor.class.getResource("monitor-view.fxml"));
        Scene monitor_scene = null;
        try {
            monitor_scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Loads bootstrap and custom stylesheet
        monitor_scene.getStylesheets().add(BMSMonitor.class.getResource("CustomStylesheet.css").toExternalForm());
        monitor_scene.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css");
        monitor_stage.setScene(monitor_scene);
        monitor_stage.setTitle("BMS Monitor");
        monitor_stage.sizeToScene();
        monitor_stage.setMinHeight(720);
        monitor_stage.setMinWidth(1280);
        monitor_stage.show();
    }

    /**
     * Method called on action by the "Add Module" button, adds the specified file path from the user into the list view.
     * @see MenuController#showAlert(String, String)
     */
    @FXML
    protected void addModule()
    {
        if(listview.getItems().size() >= DataHandler.getInstance().CONST_NUMMODULES)
        {
            showAlert("Alert","You can't add more than " + DataHandler.getInstance().CONST_NUMMODULES + " modules.");
            return;
        }

        FileChooser fil_chooser = new FileChooser();
        File file = fil_chooser.showOpenDialog(new Stage());

        if (file != null)
            listview.getItems().add(file.getAbsolutePath());
        System.out.println(listview.getItems().size());
    }

    /**
     * Deletes the selected item from the user in the list view.
     */
    @FXML
    void deleteModule()
    {
        listview.getItems().remove(listview.getSelectionModel().getSelectedItem());
        System.out.println(listview.getItems().size());
    }

    /**
     * Creates a stage alert to be displayed to the user for a given reason.
     * @param title Title to be displayed.
     * @param description Alert content to be displayed (reason).
     */
    private void showAlert(String title, String description)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(description);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css");
        dialogPane.getStyleClass().add("btn-sm");
        alert.show();
    }
}