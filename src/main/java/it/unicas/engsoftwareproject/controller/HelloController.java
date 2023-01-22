package it.unicas.engsoftwareproject.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;

import it.unicas.engsoftwareproject.DataHandler;

public class HelloController {
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
    @FXML
    private AnchorPane mainpane;

    final int CONST_SPACING = 5;
    int modulecounter = 0;

    public HelloController(){
        mainpane = new AnchorPane();
        mainpane.setStyle("");
    }

    @FXML
    protected void startSimulation(){
        progressBar.setProgress(0.5);
    }

    @FXML
    protected void addModule()
    {
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
            moduleNameLabels.get(modulecounter).setAlignment(Pos.CENTER);
            moduleNameLabels.get(modulecounter).setFont(new Font(15));
            buttonEraseLabels.add(new Button("x"));
            buttonEraseLabels.get(modulecounter).setOnAction(e -> eraseElement(e));
            buttonEraseLabels.get(modulecounter).getStyleClass().add("btn-sm");
            buttonEraseLabels.get(modulecounter).getStyleClass().add("btn-default");
            buttonEraseLabels.get(modulecounter).prefWidth(6);
            buttonEraseLabels.get(modulecounter).prefHeight(6);
            hbox.add(new HBox(moduleNameLabels.get(modulecounter),buttonEraseLabels.get(modulecounter)));
            hbox.get(modulecounter).setSpacing(CONST_SPACING);
            hbox.get(modulecounter).setAlignment(Pos.CENTER);
            vboxlabels.getChildren().add(hbox.get(modulecounter));
            modulecounter++;
            System.out.println(modulecounter);
        }
    }

    void eraseElement(ActionEvent e)
    {
        vboxlabels.getChildren().remove(((Button)e.getSource()).getParent());
        hbox.remove(((Button)e.getSource()).getParent());
        for(int i = 0; i < buttonEraseLabels.size(); i++)
            if(e.getSource() == buttonEraseLabels.get(i))
            {
                buttonEraseLabels.remove(i);
                moduleNameLabels.remove(i);
            }
        modulecounter--;
        System.out.println(modulecounter);
    }
}