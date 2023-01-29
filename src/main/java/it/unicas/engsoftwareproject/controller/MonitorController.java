package it.unicas.engsoftwareproject.controller;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.tilesfx.skins.LedTileSkin;
import it.unicas.engsoftwareproject.BMSMonitor;
import it.unicas.engsoftwareproject.CSVReader;
import it.unicas.engsoftwareproject.DataHandler;
import it.unicas.engsoftwareproject.DataSource;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.IOException;

public class MonitorController {

    @FXML
    private TabPane tabpane;

    private HBox[] window_hbox;
    private VBox[] indicators_vbox;
    private HBox[] stats_hbox;
    private HBox[] volt_hbox;
    private HBox[] temp_hbox;
    static private Gauge[][] volt_gauges;
    static private Gauge[][] temp_gauges;
    static private Gauge[] curr_gauges;
    static private Gauge[] vstack_gauges;
    static private Gauge[] soc_gauges;
    static private Gauge[] vmax_gauges;
    static private Gauge[] vmin_gauges;
    static private Gauge[] vavg_gauges;
    static private Gauge[] vdelta_gauges;

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

        vmax_gauges[id_module].valueProperty().set(stats[0]);
        vmin_gauges[id_module].valueProperty().set(stats[1]);
        vavg_gauges[id_module].valueProperty().set(stats[2]);
        vdelta_gauges[id_module].valueProperty().set(stats[3]);

        if (faults != null) {

            for (int i = 0; i < numvolt; i++)
                if (faults[0].charAt(i) == '1') {
                    volt_gauges[id_module][i].setLedColor(Color.RED);
                    volt_gauges[id_module][i].setLedOn(true);
                    volt_gauges[id_module][i].barColorProperty().setValue(Color.LIGHTGREEN);
                } else if (faults[1].charAt(i) == '1') {
                    volt_gauges[id_module][i].setLedColor(Color.YELLOW);
                    volt_gauges[id_module][i].setLedOn(true);
                    volt_gauges[id_module][i].barColorProperty().setValue(Color.DARKGREEN);
                } else {
                    volt_gauges[id_module][i].setLedColor(Color.BLACK);
                    volt_gauges[id_module][i].setLedOn(false);
                    volt_gauges[id_module][i].barColorProperty().setValue(Color.GREEN);
                }

            for (int i = 0; i < numtemp; i++)
                if (faults[2].charAt(i) == '1') {
                    temp_gauges[id_module][i].setLedColor(Color.RED);
                    temp_gauges[id_module][i].setLedOn(true);
                    temp_gauges[id_module][i].barColorProperty().setValue(Color.PINK);
                } else if (faults[3].charAt(i) == '1') {
                    temp_gauges[id_module][i].setLedColor(Color.YELLOW);
                    temp_gauges[id_module][i].setLedOn(true);
                    temp_gauges[id_module][i].barColorProperty().setValue(Color.LIGHTBLUE);
                } else {
                    temp_gauges[id_module][i].setLedColor(Color.BLACK);
                    temp_gauges[id_module][i].setLedOn(false);
                    temp_gauges[id_module][i].barColorProperty().setValue(Color.RED);
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
        stats_hbox = new HBox[readers.length];
        volt_hbox = new HBox[readers.length];
        temp_hbox = new HBox[readers.length];
        volt_gauges = new Gauge[readers.length][];
        temp_gauges = new Gauge[readers.length][];
        curr_gauges = new Gauge[readers.length];
        vstack_gauges = new Gauge[readers.length];
        soc_gauges = new Gauge[readers.length];
        vmax_gauges = new Gauge[readers.length];
        vmin_gauges = new Gauge[readers.length];
        vavg_gauges = new Gauge[readers.length];
        vdelta_gauges = new Gauge[readers.length];


        for(int i = 0; i < readers.length; i++) {
            Tab tab = new Tab("Module" + (i+1) );
            tab.setId(Integer.toString(i));
            window_hbox[i] = new HBox();
            indicators_vbox[i] = new VBox();
            stats_hbox[i] = new HBox();
            volt_hbox[i] = new HBox();
            temp_hbox[i] = new HBox();
            volt_gauges[i] = new Gauge[readers[i].getNumVoltSens()];
            temp_gauges[i] = new Gauge[readers[i].getNumTempSens()];
            indicators_vbox[i].getChildren().addAll(volt_hbox[i], temp_hbox[i]);
            window_hbox[i].getChildren().addAll(indicators_vbox[i],stats_hbox[i]);
            tab.setContent(window_hbox[i]);
            tabpane.getTabs().add(tab);

            indicators_vbox[i].setSpacing(50);

            if(readers[i].getCurrentBool()) {
                curr_gauges[i] = GaugeBuilder.create()
                        .maxValue(100)
                        .barColor(Color.BLUE)
                        .ledVisible(true)
                        .ledColor(Color.BLACK)
                        .skinType(Gauge.SkinType.LINEAR)
                        .title("Current (A)")
                        .prefSize(100, 300)
                        .decimals(3)
                        .build();
                stats_hbox[i].getChildren().add(curr_gauges[i]);
            }

            vstack_gauges[i] = GaugeBuilder.create()
                    .maxValue(readers[i].getNumVoltSens()*5)
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

            stats_hbox[i].getChildren().addAll(vstack_gauges[i], soc_gauges[i]);

            for(int j = 0; j < readers[i].getNumVoltSens(); j++) {
                volt_gauges[i][j] = GaugeBuilder.create()
                        .maxValue(5)
                        .barColor(Color.GREEN)
                        .ledVisible(true)
                        .ledColor(Color.BLACK)
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
                        .barColor(Color.RED)
                        .ledVisible(true)
                        .ledColor(Color.BLACK)
                        .skinType(Gauge.SkinType.LINEAR)
                        .title("Temp " + (j+1) + " (°C)")
                        .prefSize(100, 300)
                        .decimals(3)
                        .build();

                temp_hbox[i].getChildren().add(temp_gauges[i][j]);
            }

            vmax_gauges[i] = GaugeBuilder.create()
                    .maxValue(5)
                    .barColor(Color.GREEN)
                    .skinType(Gauge.SkinType.LINEAR)
                    .title("MaxV (V)")
                    .prefSize(100, 300)
                    .decimals(3)
                    .build();

            vmin_gauges[i] = GaugeBuilder.create()
                    .maxValue(5)
                    .barColor(Color.GREEN)
                    .skinType(Gauge.SkinType.LINEAR)
                    .title("MinV (V)")
                    .prefSize(100, 300)
                    .decimals(3)
                    .build();

            vavg_gauges[i] = GaugeBuilder.create()
                    .maxValue(5)
                    .barColor(Color.GREEN)
                    .skinType(Gauge.SkinType.LINEAR)
                    .title("AvgV (V)")
                    .prefSize(100, 300)
                    .decimals(3)
                    .build();

            vdelta_gauges[i] = GaugeBuilder.create()
                    .maxValue(5)
                    .barColor(Color.GREEN)
                    .skinType(Gauge.SkinType.LINEAR)
                    .title("DeltaV (V)")
                    .prefSize(100, 300)
                    .decimals(3)
                    .build();

            stats_hbox[i].getChildren().addAll(vmax_gauges[i], vmin_gauges[i], vavg_gauges[i], vdelta_gauges[i]);

        }
    }
}
