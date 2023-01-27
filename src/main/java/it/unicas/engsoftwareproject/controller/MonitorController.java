package it.unicas.engsoftwareproject.controller;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import it.unicas.engsoftwareproject.BMSMonitor;
import it.unicas.engsoftwareproject.CSVReader;
import it.unicas.engsoftwareproject.DataHandler;
import it.unicas.engsoftwareproject.DataSource;
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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class MonitorController {

    @FXML
    private TabPane tabpane;

    private HBox[] window_hbox;
    private VBox[] indicators_vbox;
    private VBox[] stats_vbox;
    private HBox[] volt_hbox;
    private HBox[] temp_hbox;
    static private Gauge[][] volt_gauges;
    static private Gauge[][] temp_gauges;
    static private Gauge[] curr_gauges;
    static private Gauge[] vstack_gauges;
    static private Gauge[] soc_gauges;
    static DataSource[] readers = null;
    static private int sampletime;
    static private String[] absolutepaths;

    public static void updateGraphics(Double[] data, String[] faults, int id_module) {

        int numvolt = readers[id_module].getNumVoltSens();
        int numtemp = readers[id_module].getNumTempSens();
        int vstackindex = numvolt;
        int socindex = numvolt+numtemp+1;
        int currindex = numvolt+numtemp+2;

        for(int i = 0; i < numvolt; i++)
            volt_gauges[id_module][i].valueProperty().set(data[i]);

        vstack_gauges[id_module].valueProperty().set(data[vstackindex]);

        for(int i = 0; i < numtemp; i++)
            temp_gauges[id_module][i].valueProperty().set(data[i+numvolt+1]);

        soc_gauges[id_module].valueProperty().set(data[socindex]);
        if(readers[id_module].getCurrentBool())
            curr_gauges[id_module].valueProperty().set(data[currindex]);

    }


    @FXML
    protected void backToMenu() throws IOException {

        for (int i = 0; i < readers.length; i++){
            readers[i].stop();
            DataHandler.getInstance().writeDataCSV(i);
            DataHandler.getInstance().writeStatsCSV(i);
        }

        CSVReader.resetCounter();
        BMSMonitor.stagelist.get(1).close();
        BMSMonitor.stagelist.remove(1);
        BMSMonitor.stagelist.get(0).show();

    }

    @FXML
    public void initialize() throws IOException {

        readers = DataHandler.getInstance().genReaders(absolutepaths, sampletime);

        initGraphics();

        for(int i = 0; i < readers.length; i++)
            readers[i].start();


    }

    static public void setSettings(String[] paths, int T){
        absolutepaths = paths;
        sampletime = T;
    }

    private void initGraphics()
    {
        window_hbox = new HBox[readers.length];
        indicators_vbox = new VBox[readers.length];
        stats_vbox = new VBox[readers.length];
        volt_hbox = new HBox[readers.length];
        temp_hbox = new HBox[readers.length];
        volt_gauges = new Gauge[readers.length][];
        temp_gauges = new Gauge[readers.length][];
        curr_gauges = new Gauge[readers.length];
        vstack_gauges = new Gauge[readers.length];
        soc_gauges = new Gauge[readers.length];


        for(int i = 0; i < readers.length; i++) {
            Tab tab = new Tab("Module" + (i+1) );
            window_hbox[i] = new HBox();
            indicators_vbox[i] = new VBox();
            stats_vbox[i] = new VBox();
            volt_hbox[i] = new HBox();
            temp_hbox[i] = new HBox();
            volt_gauges[i] = new Gauge[readers[i].getNumVoltSens()];
            temp_gauges[i] = new Gauge[readers[i].getNumTempSens()];
            indicators_vbox[i].getChildren().addAll(volt_hbox[i], temp_hbox[i]);
            window_hbox[i].getChildren().addAll(indicators_vbox[i],stats_vbox[i]);
            tab.setContent(window_hbox[i]);
            tabpane.getTabs().add(tab);

            indicators_vbox[i].setSpacing(50);

            if(readers[i].getCurrentBool()) {
                curr_gauges[i] = GaugeBuilder.create()
                        .maxValue(100)
                        .barColor(Color.GREEN)
                        .skinType(Gauge.SkinType.LINEAR)
                        .title("Current (A)")
                        .prefSize(100, 300)
                        .decimals(3)
                        .build();
                stats_vbox[i].getChildren().add(curr_gauges[i]);
            }

            vstack_gauges[i] = GaugeBuilder.create()
                    .maxValue(readers[i].getNumTempSens()*5)
                    .barColor(Color.CYAN)
                    .skinType(Gauge.SkinType.LINEAR)
                    .title("Vstack (V)")
                    .prefSize(100, 300)
                    .decimals(3)
                    .build();

            soc_gauges[i] = GaugeBuilder.create()
                    .maxValue(100)
                    .barColor(Color.MAGENTA)
                    .skinType(Gauge.SkinType.LINEAR)
                    .title("SoC (%)")
                    .prefSize(100, 300)
                    .decimals(3)
                    .build();

            stats_vbox[i].getChildren().addAll(vstack_gauges[i], soc_gauges[i]);

            for(int j = 0; j < readers[i].getNumVoltSens(); j++) {
                volt_gauges[i][j] = GaugeBuilder.create()
                        .maxValue(5)
                        .barColor(Color.GREEN)
                        .skinType(Gauge.SkinType.LINEAR)
                        .title("Cell " + (j+1) + " (V)")
                        .prefSize(100, 300)
                        .decimals(3)
                        .build();

                volt_hbox[i].getChildren().add(volt_gauges[i][j]);
            }
            for(int j = 0; j < readers[i].getNumTempSens(); j++) {
                temp_gauges[i][j] = GaugeBuilder.create()
                        .maxValue(70)
                        .barColor(Color.BLUE)
                        .skinType(Gauge.SkinType.LINEAR)
                        .title("Temp " + (j+1) + " (°C)")
                        .prefSize(100, 300)
                        .decimals(3)
                        .build();

                temp_hbox[i].getChildren().add(temp_gauges[i][j]);
            }
        }
    }
}
