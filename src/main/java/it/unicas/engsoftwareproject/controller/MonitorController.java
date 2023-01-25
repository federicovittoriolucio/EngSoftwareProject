package it.unicas.engsoftwareproject.controller;

import it.unicas.engsoftwareproject.BMSMonitor;
import it.unicas.engsoftwareproject.CSVReader;
import it.unicas.engsoftwareproject.DataHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MonitorController {

    @FXML
    private TabPane tabpane;

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
    public void initialize() throws FileNotFoundException {
        readers = new CSVReader[absolutepaths.length];

        for(int i = 0; i < readers.length; i++)
            readers[i] = new CSVReader(absolutepaths[i], sampletime);

        for(int i = 0; i < readers.length; i++)
            readers[i].start();

        for(int i = 0; i < readers.length; i++)
            tabpane.getTabs().add(new Tab("Module " + (i+1)));


    }

    static public void setSettings(String[] paths, int T){
        absolutepaths = paths;
        sampletime = T;
    }
}
