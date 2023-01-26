package it.unicas.engsoftwareproject.controller;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import it.unicas.engsoftwareproject.BMSMonitor;
import it.unicas.engsoftwareproject.CSVReader;
import it.unicas.engsoftwareproject.DataHandler;
import javafx.css.Stylesheet;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class MonitorController {

    @FXML
    private TabPane tabpane;

    private ArrayList<HBox[]> hbox_tab;

    @FXML
    protected void backToMenu() throws IOException {

        for (int i = 0; i < readers.length; i++){
            readers[i].stop();
            DataHandler.getInstance().writeDataCSV(i);
            DataHandler.getInstance().writeStatsCSV(i);
        }

        BMSMonitor.stagelist.get(1).close();
        BMSMonitor.stagelist.remove(1);
        BMSMonitor.stagelist.get(0).show();

    }
    CSVReader[] readers = null;
    static private int sampletime;
    static private String[] absolutepaths;

    @FXML
    public void initialize() throws IOException {
        readers = new CSVReader[absolutepaths.length];

        for(int i = 0; i < readers.length; i++)
            readers[i] = new CSVReader(absolutepaths[i], sampletime);

        for(int i = 0; i < readers.length; i++)
            readers[i].start();

        //FXMLLoader[] loader = new FXMLLoader[readers.length];

        //for(int i = 0; i < readers.length; i++)
        //    loader[i] = new FXMLLoader(BMSMonitor.class.getResource("tab-view.fxml"));

        for(int i = 0; i < readers.length; i++) {
            Tab tab = new Tab("Module" + (i+1) );
            HBox window_hbox = new HBox();
            VBox vbox = new VBox();
            HBox volt_hbox = new HBox();
            HBox temp_hbox = new HBox();

            vbox.getChildren().addAll(volt_hbox, temp_hbox);
            window_hbox.getChildren().add(vbox);
            tab.setContent(window_hbox);
            tabpane.getTabs().add(tab);

            // HBox.setHgrow(window_hbox, Priority.ALWAYS); ? pare che non faccia nulla.

            Gauge[] volt_gauge = new Gauge[readers[i].getNumVoltSens()];
            Gauge[] temp_gauge = new Gauge[readers[i].getNumTempSens()];
            for(int j = 0; j < readers[i].getNumVoltSens(); j++) {
                volt_gauge[j] = GaugeBuilder.create()
                        .maxValue(5)
                        .barColor(Color.GREEN)
                        .skinType(Gauge.SkinType.LINEAR)
                        .title("Tensione")
                        .prefSize(120, 300)
                        .build();

                volt_hbox.getChildren().add(volt_gauge[j]);
            }
            for(int j = 0; j < readers[i].getNumTempSens(); j++) {
                temp_gauge[j] = GaugeBuilder.create()
                        .maxValue(100)
                        .barColor(Color.BLUE)
                        .skinType(Gauge.SkinType.LINEAR)
                        .title("Temperatura")
                        .prefSize(120, 300)
                        .build();

                temp_hbox.getChildren().add(temp_gauge[j]);
            }
        }


    }

    static public void setSettings(String[] paths, int T){
        absolutepaths = paths;
        sampletime = T;
    }
}
