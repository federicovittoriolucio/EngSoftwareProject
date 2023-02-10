package it.unicas.engsoftwareproject.controller;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;

import it.unicas.engsoftwareproject.BMSMonitor;
import it.unicas.engsoftwareproject.CSVReader;
import it.unicas.engsoftwareproject.DataHandler;
import it.unicas.engsoftwareproject.DataSource;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.IOException;

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

    static private Tab[] tabs;
    static private Gauge[][] volt_gauges;
    static private Gauge[][] temp_gauges;
    static private Gauge[] curr_gauges;
    static private Gauge[] vstack_gauges;
    static private Gauge[] soc_gauges;
    static private TextField[] vmax_textfield;
    static private TextField[] vmin_textfield;
    static private TextField[] vavg_textfield;
    static private TextField[] vdelta_textfield;

    static DataSource[] sources = null;
    static private int sampletime;
    static private String[] absolutepaths;

    public static void updateGraphics(Double[] data, String[] faults, Double[] stats, int id_module) {

        it.unicas.engsoftwareproject.Module m = DataHandler.getInstance().getModule(id_module);

        int numvolt = m.getNumVoltSens();
        int numtemp = m.getNumTempSens();
        int vstackindex = numvolt;
        int socindex = numvolt+numtemp+1;
        int currindex = numvolt+numtemp+2;

        for(int i = 0; i < numvolt; i++)
            volt_gauges[id_module][i].valueProperty().set(data[i]);

        vstack_gauges[id_module].valueProperty().set(data[vstackindex]);

        for(int i = 0; i < numtemp; i++)
            temp_gauges[id_module][i].valueProperty().set(data[i+numvolt+1]);

        soc_gauges[id_module].valueProperty().set(data[socindex]);
        if(m.getCurrentBool())
            curr_gauges[id_module].valueProperty().set(data[currindex]);

        vmax_textfield[id_module].setText(String.format("%.3f", stats[0]));
        vmin_textfield[id_module].setText(String.format("%.3f", stats[1]));
        vavg_textfield[id_module].setText(String.format("%.3f", stats[2]));
        vdelta_textfield[id_module].setText(String.format("%.3f", stats[3]));

        if (faults != null) {

            boolean flag = false;

            for (int i = 0; i < numvolt; i++)
                if (faults[0].charAt(i) == '1') {
                    volt_gauges[id_module][i].setLedColor(Color.RED);
                    volt_gauges[id_module][i].setLedOn(true);
                    flag = true;
                } else if (faults[1].charAt(i) == '1') {
                    volt_gauges[id_module][i].setLedColor(Color.YELLOW);
                    volt_gauges[id_module][i].setLedOn(true);
                    flag = true;
                } else {
                    volt_gauges[id_module][i].setLedColor(Color.BLACK);
                    volt_gauges[id_module][i].setLedOn(false);
                }

            for (int i = 0; i < numtemp; i++)
                if (faults[2].charAt(i) == '1') {
                    temp_gauges[id_module][i].setLedColor(Color.RED);
                    temp_gauges[id_module][i].setLedOn(true);
                    flag = true;
                } else if (faults[3].charAt(i) == '1') {
                    temp_gauges[id_module][i].setLedColor(Color.YELLOW);
                    temp_gauges[id_module][i].setLedOn(true);
                    flag = true;
                } else {
                    temp_gauges[id_module][i].setLedColor(Color.BLACK);
                    temp_gauges[id_module][i].setLedOn(false);
                }

            if (m.getCurrentBool() == true)
                if (faults[4].compareTo("1") == 0) {
                    curr_gauges[id_module].setLedColor(Color.MAGENTA);
                    curr_gauges[id_module].setLedOn(true);
                    flag = true;
                } else if (faults[5].compareTo("1") == 0) {
                    curr_gauges[id_module].setLedColor(Color.ORANGE);
                    curr_gauges[id_module].setLedOn(true);
                    flag = true;
                } else {
                    curr_gauges[id_module].setLedColor(Color.BLACK);
                    curr_gauges[id_module].setLedOn(false);
                }

            if(flag)
                tabs[id_module].getStyleClass().set(1,"tab-pane-alert");
            else
                tabs[id_module].getStyleClass().set(1,"tab-pane");
        }
    }


    @FXML
    protected void backToMenu() throws IOException {

        for (int i = 0; i < sources.length; i++){
            sources[i].stop();
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
        sources[id_module].pause();
    }

    @FXML
    protected void resumeModule()
    {
        int id_module = Integer.parseInt(tabpane.getSelectionModel().getSelectedItem().getId());
        sources[id_module].resume();
    }

    @FXML
    protected void showGraphWindow(){

        if(BMSMonitor.stagelist.size() > 2) {
            BMSMonitor.stagelist.get(2).requestFocus();
            return;
        }

        Stage graph_stage = new Stage();
        BMSMonitor.stagelist.add(graph_stage);
        FXMLLoader fxmlLoader = new FXMLLoader(BMSMonitor.class.getResource("graph-view.fxml"));
        Scene graph_scene = null;
        try {
            graph_scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        graph_scene.getStylesheets().add(BMSMonitor.class.getResource("CustomStylesheet.css").toExternalForm());
        graph_scene.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css");
        graph_stage.setScene(graph_scene);
        graph_stage.setTitle("Graphs");
        graph_stage.sizeToScene();
        graph_stage.setMinHeight(720);
        graph_stage.setMinWidth(1280);
        graph_stage.show();
    }

    @FXML
    public void initialize() throws IOException {

        sources = DataHandler.getInstance().genReaders(absolutepaths, sampletime);

        initGraphics();

        for(int i = 0; i < sources.length; i++)
            sources[i].start();

        BMSMonitor.stagelist.get(1).setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
    }

    static public void setSettings(String[] paths, int T){
        absolutepaths = paths;
        sampletime = T;
    }

    private void initGraphics()
    {
        window_hbox = new HBox[sources.length];
        indicators_vbox = new VBox[sources.length];
        stats_vbox = new VBox[sources.length];
        volt_hbox = new HBox[sources.length];
        temp_hbox = new HBox[sources.length];
        curr_hbox = new HBox[sources.length];
        container_hbox = new HBox[sources.length];
        tabs = new Tab[sources.length];
        volt_gauges = new Gauge[sources.length][];
        temp_gauges = new Gauge[sources.length][];
        curr_gauges = new Gauge[sources.length];
        vstack_gauges = new Gauge[sources.length];
        soc_gauges = new Gauge[sources.length];
        vmax_textfield = new TextField[sources.length];
        vmin_textfield = new TextField[sources.length];
        vavg_textfield = new TextField[sources.length];
        vdelta_textfield = new TextField[sources.length];



        for(int i = 0; i < sources.length; i++) {
            it.unicas.engsoftwareproject.Module m = DataHandler.getInstance().getModule(i);

            tabs[i] = new Tab("Module" + (i+1));
            tabs[i].setId(Integer.toString(i));

            window_hbox[i] = new HBox();
            indicators_vbox[i] = new VBox();
            stats_vbox[i] = new VBox();
            volt_hbox[i] = new HBox();
            temp_hbox[i] = new HBox();
            curr_hbox[i] = new HBox();
            container_hbox[i] = new HBox();
            volt_gauges[i] = new Gauge[m.getNumVoltSens()];
            temp_gauges[i] = new Gauge[m.getNumTempSens()];

            tabs[i].setContent(window_hbox[i]);
            tabpane.getTabs().add(tabs[i]);

            window_hbox[i].getStyleClass().add("window-hbox");
            volt_hbox[i].getStyleClass().add("gauge-hbox");
            temp_hbox[i].getStyleClass().add("gauge-hbox");
            stats_vbox[i].getStyleClass().add("stats-vbox");
            curr_hbox[i].getStyleClass().add("gauge-hbox");
            tabs[i].getStyleClass().add("tab-pane-alert");

            indicators_vbox[i].setSpacing(20);
            container_hbox[i].setSpacing(20);

            window_hbox[i].setAlignment(Pos.CENTER);

            HBox.setHgrow(window_hbox[i], Priority.ALWAYS);
            HBox.setHgrow(curr_hbox[i], Priority.ALWAYS);


            if(m.getCurrentBool()) {
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
                    .maxValue(m.getNumVoltSens()*5)
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
                    .barColor(Color.LIMEGREEN)
                    .skinType(Gauge.SkinType.BATTERY)
                    .title("SoC (%)")
                    .decimals(1)
                    .titleColor(Color.WHITE)
                    .valueColor(Color.WHITE)
                    .tickLabelColor(Color.WHITE)
                    .build();

            for(int j = 0; j < m.getNumVoltSens(); j++) {
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
            for(int j = 0; j < m.getNumTempSens(); j++) {
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

            Label vmax_label = new Label("Max Voltage");
            Label vmin_label = new Label("Min Voltage");
            Label vavg_label = new Label("Avg Voltage");
            Label vdelta_label = new Label("Delta Voltage");

            vmax_label.setTextFill(Color.WHITE);
            vmin_label.setTextFill(Color.WHITE);
            vavg_label.setTextFill(Color.WHITE);
            vdelta_label.setTextFill(Color.WHITE);

            vmax_textfield[i] = new TextField();
            vmin_textfield[i] = new TextField();
            vavg_textfield[i] = new TextField();
            vdelta_textfield[i] = new TextField();

            vmax_textfield[i].setEditable(false);
            vmin_textfield[i].setEditable(false);
            vavg_textfield[i].setEditable(false);
            vdelta_textfield[i].setEditable(false);

            stats_vbox[i].getChildren().addAll( soc_gauges[i],
                                                new VBox(vmax_label, vmax_textfield[i]),
                                                new VBox(vmin_label, vmin_textfield[i]),
                                                new VBox(vavg_label, vavg_textfield[i]),
                                                new VBox(vdelta_label, vdelta_textfield[i]));

            container_hbox[i].getChildren().addAll(temp_hbox[i], curr_hbox[i]);
            indicators_vbox[i].getChildren().addAll(volt_hbox[i], container_hbox[i]);
            window_hbox[i].getChildren().addAll(indicators_vbox[i],stats_vbox[i]);

            for(Node child : indicators_vbox[i].getChildren()) {
                VBox.setVgrow(child, Priority.ALWAYS);
            }

        }

    }
}
