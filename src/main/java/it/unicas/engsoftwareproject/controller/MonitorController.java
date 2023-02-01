package it.unicas.engsoftwareproject.controller;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.tilesfx.Tile;

import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.skins.BarChartItem;
import eu.hansolo.tilesfx.tools.BarChartItemBuilder;
import it.unicas.engsoftwareproject.BMSMonitor;
import it.unicas.engsoftwareproject.CSVReader;
import it.unicas.engsoftwareproject.DataHandler;
import it.unicas.engsoftwareproject.DataSource;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MonitorController {

    @FXML
    private TabPane tabpane;

    private HBox[] window_hbox;
    private VBox[] indicators_vbox;
    private VBox[] stats_vbox;
    private HBox[] volt_hbox;
    private HBox[] temp_hbox;
    private HBox[] curr_hbox;
    private HBox[] container_hbox;
    static private Gauge[][] volt_gauges;
    static private Gauge[][] temp_gauges;
    static private Gauge[] curr_gauges;
    static private Gauge[] vstack_gauges;
    static private Gauge[] soc_gauges;
    /*static private Gauge[] vmax_gauges;
    static private Gauge[] vmin_gauges;
    static private Gauge[] vavg_gauges;
    static private Gauge[] vdelta_gauges;*/
    static private Label[] vmax_labels;
    static private Label[] vmin_labels;
    static private Label[] vavg_labels;
    static private Label[] vdelta_labels;

    static DataSource[] readers = null;
    static private int sampletime;
    static private String[] absolutepaths;

    public static void updateGraphics(Double[] data, String[] faults, Double[] stats, int id_module) {

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

        vmax_labels[id_module].setText("Max Voltage: " + String.format("%.3f", stats[0]));
        vmin_labels[id_module].setText("Min Voltage: " + String.format("%.3f", stats[1]));
        vavg_labels[id_module].setText("Avg Voltage: " + String.format("%.3f", stats[2]));
        vdelta_labels[id_module].setText("Delta Voltage: " + String.format("%.3f", stats[3]));

        if (faults != null) {

            for (int i = 0; i < numvolt; i++)
                if (faults[0].charAt(i) == '1') {
                    volt_gauges[id_module][i].setLedColor(Color.RED);
                    volt_gauges[id_module][i].setLedOn(true);
                    //volt_gauges[id_module][i].barColorProperty().setValue(Color.LIGHTGREEN);
                } else if (faults[1].charAt(i) == '1') {
                    volt_gauges[id_module][i].setLedColor(Color.YELLOW);
                    volt_gauges[id_module][i].setLedOn(true);
                    //volt_gauges[id_module][i].barColorProperty().setValue(Color.DARKGREEN);
                } else {
                    volt_gauges[id_module][i].setLedColor(Color.BLACK);
                    volt_gauges[id_module][i].setLedOn(false);
                    //volt_gauges[id_module][i].barColorProperty().setValue(Color.GREEN);
                }

            for (int i = 0; i < numtemp; i++)
                if (faults[2].charAt(i) == '1') {
                    temp_gauges[id_module][i].setLedColor(Color.RED);
                    temp_gauges[id_module][i].setLedOn(true);
                    //temp_gauges[id_module][i].barColorProperty().setValue(Color.PINK);
                } else if (faults[3].charAt(i) == '1') {
                    temp_gauges[id_module][i].setLedColor(Color.YELLOW);
                    temp_gauges[id_module][i].setLedOn(true);
                    //temp_gauges[id_module][i].barColorProperty().setValue(Color.LIGHTBLUE);
                } else {
                    temp_gauges[id_module][i].setLedColor(Color.BLACK);
                    temp_gauges[id_module][i].setLedOn(false);
                    //temp_gauges[id_module][i].barColorProperty().setValue(Color.RED);
                }

            if (readers[id_module].getCurrentBool() == true)
                if (faults[4].compareTo("1") == 0) {
                    curr_gauges[id_module].setLedColor(Color.RED);
                    curr_gauges[id_module].setLedOn(true);
                } else if (faults[5].compareTo("1") == 0) {
                    curr_gauges[id_module].setLedColor(Color.YELLOW);
                    curr_gauges[id_module].setLedOn(true);
                } else {
                    curr_gauges[id_module].setLedColor(Color.BLACK);
                    curr_gauges[id_module].setLedOn(false);
                }
        }
    }


    @FXML
    protected void backToMenu() throws IOException {

        for (int i = 0; i < readers.length; i++){
            readers[i].stop();
            DataHandler.getInstance().writeDataCSV(i);
            DataHandler.getInstance().writeStatsCSV(i);
        }

        CSVReader.resetCounter();
        DataHandler.getInstance().resetActivemodules();

        BMSMonitor.stagelist.get(1).close();
        BMSMonitor.stagelist.remove(1);
        BMSMonitor.stagelist.get(0).show();

    }
    
    @FXML
    protected void pauseModule()
    {
        int id_module = Integer.parseInt(tabpane.getSelectionModel().getSelectedItem().getId());
        readers[id_module].pause();
    }

    @FXML
    protected void resumeModule()
    {
        int id_module = Integer.parseInt(tabpane.getSelectionModel().getSelectedItem().getId());
        readers[id_module].resume();
    }

    @FXML
    public void initialize() throws IOException {

        readers = DataHandler.getInstance().genReaders(absolutepaths, sampletime);

        this.getClass().getResource("CustomStylesheet.css");
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
        curr_hbox = new HBox[readers.length];
        container_hbox = new HBox[readers.length];
        volt_gauges = new Gauge[readers.length][];
        temp_gauges = new Gauge[readers.length][];
        curr_gauges = new Gauge[readers.length];
        vstack_gauges = new Gauge[readers.length];
        soc_gauges = new Gauge[readers.length];
        vmax_labels = new Label[readers.length];
        vmin_labels = new Label[readers.length];
        vavg_labels = new Label[readers.length];
        vdelta_labels = new Label[readers.length];


        for(int i = 0; i < readers.length; i++) {
            Tab tab = new Tab("Module" + (i+1) );
            tab.setId(Integer.toString(i));

            window_hbox[i] = new HBox();
            indicators_vbox[i] = new VBox();
            stats_vbox[i] = new VBox();
            volt_hbox[i] = new HBox();
            temp_hbox[i] = new HBox();
            curr_hbox[i] = new HBox();
            container_hbox[i] = new HBox();
            volt_gauges[i] = new Gauge[readers[i].getNumVoltSens()];
            temp_gauges[i] = new Gauge[readers[i].getNumTempSens()];

            tab.setContent(window_hbox[i]);
            tabpane.getTabs().add(tab);

            window_hbox[i].getStyleClass().add("window-hbox");
            volt_hbox[i].getStyleClass().add("gauge-hbox");
            temp_hbox[i].getStyleClass().add("gauge-hbox");
            stats_vbox[i].getStyleClass().add("gauge-hbox");
            curr_hbox[i].getStyleClass().add("gauge-hbox");

            indicators_vbox[i].setSpacing(20);
            container_hbox[i].setSpacing(20);

            HBox.setHgrow(window_hbox[i], Priority.ALWAYS);

            if(readers[i].getCurrentBool()) {
                curr_gauges[i] = GaugeBuilder.create()
                        .maxValue(100)
                        .barColor(Color.BLUE)
                        .ledVisible(true)
                        .ledColor(Color.BLACK)
                        .skinType(Gauge.SkinType.LINEAR)
                        .title("Current (A)")
                        .decimals(3)
                        .tickMarkColor(Color.LIGHTGRAY)
                        .titleColor(Color.WHITE)
                        .valueColor(Color.WHITE)
                        .tickLabelColor(Color.WHITE)
                        .build();
                curr_hbox[i].getChildren().add(curr_gauges[i]);
            }

            vstack_gauges[i] = GaugeBuilder.create()
                    .maxValue(readers[i].getNumVoltSens()*5)
                    .barColor(Color.GREEN)
                    .skinType(Gauge.SkinType.LINEAR)
                    .title("Vstack (V)")
                    .decimals(3)
                    .tickMarkColor(Color.LIGHTGRAY)
                    .titleColor(Color.WHITE)
                    .valueColor(Color.WHITE)
                    .tickLabelColor(Color.WHITE)
                    .build();
            curr_hbox[i].getChildren().add(vstack_gauges[i]);

            soc_gauges[i] = GaugeBuilder.create()
                    .maxValue(100)
                    .barColor(Color.LIGHTGRAY)
                    .skinType(Gauge.SkinType.BATTERY)
                    .title("SoC (%)")
                    .decimals(1)
                    .tickMarkColor(Color.LIGHTGRAY)
                    .titleColor(Color.WHITE)
                    .valueColor(Color.WHITE)
                    .tickLabelColor(Color.WHITE)
                    .build();

            //stats_hbox[i].getChildren().addAll(vstack_gauges[i]);

            for(int j = 0; j < readers[i].getNumVoltSens(); j++) {
                volt_gauges[i][j] = GaugeBuilder.create()
                        .maxValue(5)
                        .barColor(Color.GREEN)
                        .ledVisible(true)
                        .ledColor(Color.BLACK)
                        .skinType(Gauge.SkinType.LINEAR)
                        .title("Cell " + (j+1) + " (V)")
                        .decimals(3)
                        .tickMarkColor(Color.LIGHTGRAY)
                        .titleColor(Color.WHITE)
                        .valueColor(Color.WHITE)
                        .tickLabelColor(Color.WHITE)
                        .build();

                volt_hbox[i].getChildren().add(volt_gauges[i][j]);
            }
            for(int j = 0; j < readers[i].getNumTempSens(); j++) {
                temp_gauges[i][j] = GaugeBuilder.create()
                        .maxValue(70)
                        .barColor(Color.RED)
                        .ledVisible(true)
                        .ledColor(Color.BLACK)
                        .skinType(Gauge.SkinType.LINEAR)
                        .title("Temp " + (j+1) + " (°C)")
                        .decimals(3)
                        .tickMarkColor(Color.LIGHTGRAY)
                        .titleColor(Color.WHITE)
                        .valueColor(Color.WHITE)
                        .tickLabelColor(Color.WHITE)
                        .build();

                temp_hbox[i].getChildren().add(temp_gauges[i][j]);
            }

            vmax_labels[i] = new Label();
            vmin_labels[i] = new Label();
            vavg_labels[i] = new Label();
            vdelta_labels[i] = new Label();

            vmax_labels[i].setTextFill(Color.WHITE);
            vmin_labels[i].setTextFill(Color.WHITE);
            vavg_labels[i].setTextFill(Color.WHITE);
            vdelta_labels[i].setTextFill(Color.WHITE);

            stats_vbox[i].getChildren().addAll(vmax_labels[i], vmin_labels[i], vavg_labels[i], vdelta_labels[i], soc_gauges[i]);
            container_hbox[i].getChildren().addAll(temp_hbox[i], curr_hbox[i]);
            indicators_vbox[i].getChildren().addAll(volt_hbox[i], container_hbox[i]);
            window_hbox[i].getChildren().addAll(indicators_vbox[i],stats_vbox[i]);

            for(Node child : indicators_vbox[i].getChildren()) {
                VBox.setVgrow(child, Priority.ALWAYS);
            }

            //for(Node child : window_hbox[i].getChildren()) {
            //    HBox.setHgrow(child, Priority.ALWAYS);
            //}

        }

    }
}
