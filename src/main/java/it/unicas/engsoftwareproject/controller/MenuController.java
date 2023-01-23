package it.unicas.engsoftwareproject.controller;

import it.unicas.engsoftwareproject.BMSMonitor;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import it.unicas.engsoftwareproject.DataHandler;

public class MenuController {
    @FXML
    private ArrayList<Label> moduleNameLabels = null;
    @FXML
    private ArrayList<Button> buttonEraseLabels = null;
    @FXML
    private ArrayList<HBox> hbox = null;
    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button addModuleButton;
    @FXML
    private VBox vboxlabels;

    final int CONST_SPACING = 12;
    int modulecounter = 0;

    @FXML
    protected void startSimulation(ActionEvent event){
        progressBar.setProgress(0.5);
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
        // To use bootstrap:
        monitor_scene.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css");
        monitor_scene.getStylesheets().getClass().getResource("CustomStylesheet.css");
        monitor_stage.setScene(monitor_scene);
        monitor_stage.setTitle("Monitor");
        monitor_stage.sizeToScene();
        monitor_stage.show();
    }

    @FXML
    protected void addModule()
    {
        if(modulecounter >= DataHandler.getInstance().CONST_NUMMODULES)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Alert");
            alert.setHeaderText("You can't add more than " + DataHandler.getInstance().CONST_NUMMODULES + " modules.");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css");
            dialogPane.getStyleClass().add("btn-sm");
            alert.show();
            return;
        }

        FileChooser fil_chooser = new FileChooser();
        File file = fil_chooser.showOpenDialog(new Stage());

        if (file != null) {

            if(moduleNameLabels == null)
            {
                moduleNameLabels = new ArrayList<>();
                buttonEraseLabels = new ArrayList<>();
                hbox = new ArrayList<>();
            }

            moduleNameLabels.add(new Label(file.getName()));
            moduleNameLabels.get(modulecounter).setAlignment(Pos.CENTER_LEFT);
            moduleNameLabels.get(modulecounter).setFont(new Font(14));
            buttonEraseLabels.add(new Button("x"));
            buttonEraseLabels.get(modulecounter).setAlignment(Pos.CENTER_RIGHT);
            buttonEraseLabels.get(modulecounter).setOnAction(e -> eraseElement(e));
            buttonEraseLabels.get(modulecounter).getStyleClass().add("btn-xs");
            buttonEraseLabels.get(modulecounter).getStyleClass().add("btn-default");

            hbox.add(new HBox(moduleNameLabels.get(modulecounter),buttonEraseLabels.get(modulecounter)));
            hbox.get(modulecounter).setSpacing(CONST_SPACING);
            hbox.get(modulecounter).setAlignment(Pos.CENTER_LEFT);
            vboxlabels.getChildren().add(hbox.get(modulecounter));
            vboxlabels.setSpacing(CONST_SPACING);
            modulecounter++;
            System.out.println(modulecounter);
        }
    }

    void eraseElement(ActionEvent e)
    {
        moduleNameLabels.remove(((Button)e.getSource()).getParent().getChildrenUnmodifiable().get(0));
        vboxlabels.getChildren().remove(((Button)e.getSource()).getParent());
        hbox.remove(((Button)e.getSource()).getParent());
        buttonEraseLabels.remove(e.getSource());
        modulecounter--;
        System.out.println(modulecounter);
    }
}