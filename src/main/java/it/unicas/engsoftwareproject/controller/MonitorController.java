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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.IOException;

/**
 * Controller of the Monitor Stage, using monitor-view fxml file.
 * Manages user interface and interaction with every graphic item such as tabs, labels, gauges, and LEDs on the stage. Updating displayed data according to the sample time.
 */
public class MonitorController {

    @FXML
    private TabPane tabpane;

    // Graphic layout items
    private HBox[] window_hbox;
    private VBox[] indicators_vbox;
    private VBox[] stats_vbox;
    private HBox[] volt_hbox;
    private HBox[] temp_hbox;
    private HBox[] curr_hbox;
    private HBox[] container_hbox;
    private VBox[][] volt_gauges_vbox;

    // Graphic items
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
    static private TextField[][] vmax_cell_textfield;
    static private TextField[][] vmin_cell_textfield;

    // Sources and settings
    static DataSource[] sources = null;
    static private int sampletime;
    static private String[] absolutepaths;

    /**
     * Updates every single dynamic graphic item with the newest values obtained by the DataHandler class.
     * @param id_module Module ID to be updated.
     * @see DataHandler#updateData(String[], int)
     */
    public static void updateGraphics(int id_module) {

        // Obtaining statistical data to display
        it.unicas.engsoftwareproject.Module m = DataHandler.getInstance().getModule(id_module);
        int row = m.getNumRows() - 1;
        Double[] data = m.getDataRow(row);
        String[] faults = m.getFaultsRow(row);
        Double[] stats = m.getStats();

        // Obtaining fields and index data
        int numvolt = m.getNumVoltSens();
        int numtemp = m.getNumTempSens();
        int vstackindex = numvolt;
        int socindex = numvolt+numtemp+1;
        int currindex = numvolt+numtemp+2;

        // Updating cell gauge data and his statistical data
        for(int i = 0; i < numvolt; i++) {
            volt_gauges[id_module][i].valueProperty().set(data[i]);
            vmax_cell_textfield[id_module][i].setText(String.format("%.3f",m.getStatsRow(i)[0]));
            vmin_cell_textfield[id_module][i].setText(String.format("%.3f",m.getStatsRow(i)[1]));
        }

        // Updating VStack gauge data
        vstack_gauges[id_module].valueProperty().set(data[vstackindex]);

        // Updating temperature gauge data
        for(int i = 0; i < numtemp; i++)
            temp_gauges[id_module][i].valueProperty().set(data[i+numvolt+1]);

        // Updating State of Charge data
        soc_gauges[id_module].valueProperty().set(data[socindex]);
        // Updating current gauge data if present
        if(m.getCurrentBool())
            curr_gauges[id_module].valueProperty().set(data[currindex]);

        // Updating global module data
        vmax_textfield[id_module].setText(String.format("%.3f", stats[0]));
        vmin_textfield[id_module].setText(String.format("%.3f", stats[1]));
        vavg_textfield[id_module].setText(String.format("%.3f", stats[2]));
        vdelta_textfield[id_module].setText(String.format("%.3f", stats[3]));

        // Updating LEDs faults for every gauge if faults are present
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

            // Updating color of the tab label if fault has occurred in this specific module
            if(flag)
                tabs[id_module].getStyleClass().set(1,"tab-pane-alert");
            else
                tabs[id_module].getStyleClass().set(1,"tab-pane");
        }
        else
            tabs[id_module].getStyleClass().set(1,"tab-pane");
    }


    /**
     * Method called on action for "Save and Exit" button selection, stores data obtained until that point, closes every connection, resets active module counter and kills the stage monitor window.
     * @throws IOException Exception thrown by the DataHandler writing methods.
     */
    @FXML
    protected void backToMenu() throws IOException {

        // Stores analyzed data until backToMenu() has been called
        for (int i = 0; i < sources.length; i++){
            sources[i].stop();
            DataHandler.getInstance().writeDataCSV(i);
            DataHandler.getInstance().writeStatsCSV(i);
        }

        // Resets static attributes used to keep track of the modules
        CSVReader.resetCounter();
        DataHandler.getInstance().resetActivemodules();

        // Closes stage and re-shows Menu Stage
        BMSMonitor.stagelist.get(1).close();
        BMSMonitor.stagelist.remove(1);
        BMSMonitor.stagelist.get(0).show();

    }

    /**
     * Method called on action for "Pause Module" button selection, pauses the selected Tab module if in running state.
     * @see DataSource#pause()
     */
    @FXML
    protected void pauseModule()
    {
        // Pauses module selected in tab-pane
        int id_module = Integer.parseInt(tabpane.getSelectionModel().getSelectedItem().getId());
        sources[id_module].pause();
    }

    /**
     * Method called on action for "Resume Module" button selection, resumes the selected Tab module in pause state.
     * @see DataSource#resume()
     */
    @FXML
    protected void resumeModule()
    {
        // Resumes module selected in tab-pane
        int id_module = Integer.parseInt(tabpane.getSelectionModel().getSelectedItem().getId());
        sources[id_module].resume();
    }

    /**
     * Builds and shows the charts stage and setting up the scene: if it has already been created, it will focus on the chart stage.
     * @see GraphController
     */
    @FXML
    protected void showGraphWindow(){

        // Focuses on graph stage if already present
        if(BMSMonitor.stagelist.size() > 2) {
            BMSMonitor.stagelist.get(2).requestFocus();
            return;
        }

        // Generates a new stage, loads the required fxml, assigns the new scene and show the newly created stage (Graph)
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

    /**The initialize method is called after the execution of the constructor of the class, initializes graphics and generates the data readers.
     * @throws IOException Exception thrown at the initialization of the data sources.
     */
    @FXML
    public void initialize() throws IOException {

        // Generates data readers (Check this method if source has to change)
        sources = DataHandler.getInstance().genReaders(absolutepaths, sampletime);

        // Initiate graphics
        initGraphics();

        // Starts data readers
        for(int i = 0; i < sources.length; i++)
            sources[i].start();

        // Set on close request to completely shut down the application
        BMSMonitor.stagelist.get(1).setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
    }

    /**
     * Sets all the necessary settings obtained by the Menu Controller.
     * @param paths Paths of the sources to be retrieved.
     * @param T Sample time of the execution.
     * @see MenuController#startSimulation()
     */
    static public void setSettings(String[] paths, int T) {
        absolutepaths = paths;
        sampletime = T;
    }

    /**
     * Initialize and place every single graphic element inside the Monitor Stage according to the amount of items, dynamically (Gauges, Labels, TextBoxes, LEDs, ect.)
     */
    private void initGraphics()
    {
        // Initialization of arrays for graphical containers, Medusa's gauges and statistical data TextFields
        window_hbox = new HBox[sources.length];
        indicators_vbox = new VBox[sources.length];
        stats_vbox = new VBox[sources.length];
        volt_hbox = new HBox[sources.length];
        temp_hbox = new HBox[sources.length];
        curr_hbox = new HBox[sources.length];
        container_hbox = new HBox[sources.length];
        volt_gauges_vbox = new VBox[sources.length][];
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
        vmax_cell_textfield = new TextField[sources.length][];
        vmin_cell_textfield = new TextField[sources.length][];


        // Initialization and positioning of graphical containers
        for(int i = 0; i < sources.length; i++) {
            it.unicas.engsoftwareproject.Module m = DataHandler.getInstance().getModule(i);

            tabs[i] = new Tab("Module" + (i+1));
            tabs[i].setId(Integer.toString(i));

            // Containers
            window_hbox[i] = new HBox();
            indicators_vbox[i] = new VBox();
            stats_vbox[i] = new VBox();
            volt_hbox[i] = new HBox();
            temp_hbox[i] = new HBox();
            curr_hbox[i] = new HBox();
            container_hbox[i] = new HBox();
            volt_gauges[i] = new Gauge[m.getNumVoltSens()];
            volt_gauges_vbox[i] = new VBox[m.getNumVoltSens()];
            temp_gauges[i] = new Gauge[m.getNumTempSens()];
            vmax_cell_textfield[i] = new TextField[m.getNumVoltSens()];
            vmin_cell_textfield[i] = new TextField[m.getNumVoltSens()];

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

            // Gauges and cell statistical containers
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

                volt_gauges_vbox[i][j] = new VBox(volt_gauges[i][j]);
                volt_gauges_vbox[i][j].setAlignment(Pos.CENTER);

                vmax_cell_textfield[i][j] = new TextField();
                vmax_cell_textfield[i][j].setMaxWidth(0.4*volt_gauges[i][j].getPrefWidth());
                vmax_cell_textfield[i][j].setEditable(false);
                vmin_cell_textfield[i][j] = new TextField();
                vmin_cell_textfield[i][j].setMaxWidth(0.4*volt_gauges[i][j].getPrefWidth());
                vmin_cell_textfield[i][j].setEditable(false);

                // Cell statistical containers
                Label label = new Label("Max: ");
                label.setTextFill(Color.WHITE);
                HBox hbox = new HBox(label, vmax_cell_textfield[i][j]);
                volt_gauges_vbox[i][j].getChildren().add(hbox);
                hbox.setAlignment(Pos.CENTER);
                label = new Label("Min: ");
                label.setTextFill(Color.WHITE);
                hbox = new HBox(label, vmin_cell_textfield[i][j]);
                volt_gauges_vbox[i][j].getChildren().add(hbox);
                hbox.setAlignment(Pos.CENTER);
                volt_gauges_vbox[i][j].setSpacing(3);
                volt_hbox[i].getChildren().add(volt_gauges_vbox[i][j]);
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

            // Labels and Textfields
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
