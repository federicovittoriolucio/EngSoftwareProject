package it.unicas.engsoftwareproject.controller;

import it.unicas.engsoftwareproject.BMSMonitor;
import it.unicas.engsoftwareproject.CSVReader;
import it.unicas.engsoftwareproject.DataHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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

        FXMLLoader[] loader = new FXMLLoader[readers.length];

        for(int i = 0; i < readers.length; i++)
            loader[i] = new FXMLLoader(BMSMonitor.class.getResource("tab-view.fxml"));

        for(int i = 0; i < readers.length; i++) {
            Tab tab = new Tab("Module" + (i+1) );
            tab.setContent(loader[i].load());
            tabpane.getTabs().add(tab);
        }
    }

    static public void setSettings(String[] paths, int T){
        absolutepaths = paths;
        sampletime = T;
    }
}
