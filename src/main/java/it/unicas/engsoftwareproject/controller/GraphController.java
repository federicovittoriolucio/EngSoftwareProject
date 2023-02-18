package it.unicas.engsoftwareproject.controller;

import eu.hansolo.fx.charts.*;
import eu.hansolo.fx.charts.data.*;
import eu.hansolo.fx.charts.series.XYSeries;
import eu.hansolo.fx.charts.series.XYSeriesBuilder;

import it.unicas.engsoftwareproject.BMSMonitor;
import it.unicas.engsoftwareproject.DataHandler;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;
import java.util.ArrayList;

/**
 * Controller of the Chart/Graph Stage, using graph-view fxml file.
 * manages user interface and interaction with every graphic item in the chart stage (tabs, checkboxes and charts).
 */
public class GraphController {

    // Points displayed in graphs
    final static private int CONST_POINTSNUM = 25;
    // Graphic layout containers
    @FXML
    private TabPane graphtabpane;
    private Tab[] graphtabs;
    private HBox[] tabhbox;
    private ScrollPane[] scrollpane;
    private VBox[] menuvbox;
    // Checkboxes and graph related items
    private CheckBox[][] checkboxes;
    private GridPane[] gridpane;
    private static Axis[][] yaxis;
    private String[] colorarray;
    static private ArrayList<XYSeries>[] series;

    /**
     * The initialize method is called after the execution of the constructor of the class, initializes arrays of every graphic container and adds them to the scene
     */
    @FXML
    public void initialize(){

        BMSMonitor.stagelist.get(2).setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                BMSMonitor.stagelist.remove(2);
            }
        });

        int module_number = DataHandler.getInstance().getActiveModules();

        // Color array used to paint series
        colorarray = new String[] { "#ff4545", // light red
                                    "#607dfc", // light blue
                                    "#fcf960", // light yellow
                                    "#7afc60", // light green
                                    "#fc8f60", // sorbet orange
                                    "#60fcfc", // cyan
                                    "#cb60fc", // lilac
                                    "#ed05a7", // pink
                                    "#FFFFFF"};// white

        // Initialization of arrays for graphical containers and graphs related items
        graphtabs = new Tab[module_number];
        tabhbox = new HBox[module_number];
        scrollpane = new ScrollPane[module_number];
        menuvbox = new VBox[module_number];
        checkboxes = new CheckBox[module_number][];
        gridpane = new GridPane[module_number];
        yaxis = new Axis[module_number][];

        series = new ArrayList[module_number];

        // Initialization of graph items, graphical containers and their positioning in the tab
        for(int i = 0; i < module_number; i++) {

            it.unicas.engsoftwareproject.Module module = DataHandler.getInstance().getModule(i);

            graphtabs[i] = new Tab("Module" + (i + 1));
            tabhbox[i] = new HBox();
            scrollpane[i] = new ScrollPane();
            menuvbox[i] = new VBox();
            checkboxes[i] = new CheckBox[module.getNumfields()];
            gridpane[i] = new GridPane();

            RowConstraints rowcon = new RowConstraints();
            rowcon.setVgrow(Priority.ALWAYS);
            rowcon.setFillHeight(true);

            ColumnConstraints colcon = new ColumnConstraints();
            colcon.setHgrow(Priority.ALWAYS);
            colcon.setFillWidth(true);

            gridpane[i].getRowConstraints().addAll(rowcon, rowcon);
            gridpane[i].getColumnConstraints().addAll(colcon, colcon);

            tabhbox[i].getStyleClass().add("window-hbox");
            scrollpane[i].getStyleClass().add("checkboxes-pane");
            menuvbox[i].getStyleClass().add("checkboxes-vbox");
            gridpane[i].setHgap(20);
            gridpane[i].setVgap(20);

            // Checkboxes
            for (int j = 0; j < module.getNumVoltSens(); j++) {
                checkboxes[i][j] = new CheckBox("Cell " + (j + 1) + " voltage");
            }

            checkboxes[i][module.getNumVoltSens()] = new CheckBox("Stack voltage");

            int socindex = module.getNumVoltSens() + 1 + module.getNumTempSens();
            int currindex = socindex + 1;

            for (int j = module.getNumVoltSens() + 1; j < socindex; j++){
                int tempindex = j - module.getNumVoltSens();
                checkboxes[i][j] = new CheckBox("Temperature sensor " + tempindex);
            }

            checkboxes[i][socindex] = new CheckBox("State of Charge");
            if(module.getCurrentBool())
                checkboxes[i][currindex] = new CheckBox("Current");

            for(CheckBox check : checkboxes[i]) {
                check.setSelected(true);
                check.getStyleClass().add("checkbox");
            }

            // Children assignment
            menuvbox[i].getChildren().addAll(checkboxes[i]);
            scrollpane[i].setContent(menuvbox[i]);
            tabhbox[i].getChildren().addAll(scrollpane[i], gridpane[i]);
            graphtabs[i].setContent(tabhbox[i]);

            HBox.setHgrow(gridpane[i], Priority.ALWAYS);

            // Series initialization
            initSeries(i);

            int num_charts = 3;
            if(module.getCurrentBool())
                num_charts = 4;

            XYChart[] charts;
            charts = createGraphs(i);

            gridpane[i].add(charts[0], 0,0);
            gridpane[i].add(charts[1], 1,0);
            gridpane[i].add(charts[2], 0,1);
            if(num_charts == 4)
                gridpane[i].add(charts[3], 1,1);

            updateSeries(i);
        }

        graphtabpane.getTabs().addAll(graphtabs);

    }

    /**
     * Creates a xaxis with certain settings given by the library.
     * @return The xaxis built in the method (sample axis).
     */
    private Axis createXAxis(){
        return AxisBuilder  .create(Orientation.HORIZONTAL, Position.TOP)
                            .type(AxisType.LINEAR)
                            .minValue(0)
                            .maxValue(CONST_POINTSNUM)
                            .autoScale(true)
                            .axisColor(Color.web("#85949B"))
                            .tickLabelColor(Color.web("#85949B"))
                            .tickMarkColor(Color.web("#85949B"))
                            .tickMarksVisible(true)
                            .titleColor(Color.LIGHTGRAY)
                            .autoTitleFontSize(false)
                            .titleFontSize(10)
                            .build();
    }

    /**
     * Creates a yaxis with certain settings given by the library.
     * @return The yaxis built in the method (values axis).
     */
    private Axis createYAxis(){
        return AxisBuilder  .create(Orientation.VERTICAL, Position.LEFT)
                            .type(AxisType.LINEAR)
                            .minValue(0)
                            .maxValue(110)
                            .autoScale(true)
                            .axisColor(Color.web("#85949B"))
                            .tickLabelColor(Color.web("#85949B"))
                            .tickMarkColor(Color.web("#85949B"))
                            .tickMarksVisible(true)
                            .zeroColor(Color.web("#85949B"))
                            .build();
    }

    /**
     * Creates a grid given both axis with certain settings given by the library.
     * @param x Axis of samples.
     * @param y Axis of values.
     * @return Grid with given axis and settings.
     */
    private Grid createGrid(Axis x, Axis y){
        return GridBuilder  .create(x, y)
                            .gridLinePaint(Color.web("#384C57"))
                            .minorHGridLinesVisible(false)
                            .mediumHGridLinesVisible(false)
                            .minorVGridLinesVisible(false)
                            .mediumVGridLinesVisible(false)
                            .gridLineDashes(4, 4)
                            .build();
    }

    /**
     * Creates an overlapping chart for voltage sensors, an overlapping chart for temperature sensors, a chart for state of charge and a chart for currect if present.
     * @param module_id Module identification for creation of the graphs.
     * @return An array of charts (3 or 4) for the module with ID module_id.
     */
    private XYChart[] createGraphs(int module_id) {
        it.unicas.engsoftwareproject.Module module = DataHandler.getInstance().getModule(module_id);
        int num_charts = 3;
        if(module.getCurrentBool())
            num_charts = 4;

        Axis[] xaxis = new Axis[num_charts];
        for(int i = 0; i < num_charts; i++)
            xaxis[i] = createXAxis();

        yaxis[module_id] = new Axis[num_charts];

        for(int i = 0; i < num_charts; i++)
            yaxis[module_id][i] = createYAxis();

        yaxis[module_id][0].setDecimals(1);
        xaxis[0].setTitle("Voltage (V)");
        xaxis[1].setTitle("Temperature (°C)");
        xaxis[2].setTitle("State of Charge (%)");
        if(num_charts == 4)
            xaxis[3].setTitle("Current (A)");

        Grid[] grids = new Grid[num_charts];
        for(int i = 0; i < num_charts; i++)
            grids[i] = createGrid(xaxis[i], yaxis[module_id][i]);

        int indexvoltsens = module.getNumVoltSens() + 1;
        int indextempsens = indexvoltsens + module.getNumTempSens();

        XYPane voltpane = new XYPane(series[module_id].subList(0, indexvoltsens));
        XYPane temppane = new XYPane(series[module_id].subList(indexvoltsens, indextempsens));
        XYPane socpane  = new XYPane(series[module_id].get(indextempsens));
        XYPane currpane;

        XYChart[] charts = new XYChart[num_charts];
        charts[0] = new XYChart(voltpane, grids[0], yaxis[module_id][0], xaxis[0]);
        charts[1] = new XYChart(temppane, grids[1], yaxis[module_id][1], xaxis[1]);
        charts[2] = new XYChart(socpane, grids[2], yaxis[module_id][2], xaxis[2]);

        if(num_charts == 4) {
            currpane = new XYPane(series[module_id].get(indextempsens + 1));
            charts[3] = new XYChart(currpane, grids[3], yaxis[module_id][3], xaxis[3]);
        }

        return charts;
    }

    /**
     * Creates series for a module with id module_id and using certain settings.
     * It also set an action event for every checkbox in the module tab for their visibility.
     * @param module_id Module identification used to initialize series of module module_id.
     * @see GraphController#updateVisibility(int)
     */
    private void initSeries(int module_id){

        // Initialization of series, and assignment to his checkbox
        series[module_id] = new ArrayList<>();
        int indexvoltsens = DataHandler.getInstance().getModule(module_id).getNumVoltSens() + 1;
        int indextempsens = indexvoltsens + DataHandler.getInstance().getModule(module_id).getNumTempSens();

        for(int i = 0; i < DataHandler.getInstance().getModule(module_id).getNumfields(); i++){

            series[module_id].add(
                    XYSeriesBuilder .create()
                                    .chartType(ChartType.LINE)
                                    .stroke(Color.web(colorarray[8]))
                                    .symbolFill(Color.web(colorarray[8]))
                                    .symbolStroke(Color.web(colorarray[8]))
                                    .symbolSize(5)
                                    .strokeWidth(1)
                                    .symbolsVisible(true)
                                    .build()
            );

            checkboxes[module_id][i].setOnAction(event -> updateVisibility(module_id));

        }

        for(int i = 0; i < indexvoltsens; i++) {

            String color = "#FFFFFF";
            if(i < colorarray.length)
                color = colorarray[i];

            series[module_id].get(i).setStroke(Color.web(color));
            series[module_id].get(i).setSymbolFill(Color.web(color));
            series[module_id].get(i).setSymbolStroke(Color.web(color));
            checkboxes[module_id][i].setStyle("-fx-text-fill: " + color);
        }

        for(int i = indexvoltsens; i < indextempsens; i++) {

            String color = "#FFFFFF";
            if(i-indexvoltsens < colorarray.length)
                color = colorarray[i-indexvoltsens];

            series[module_id].get(i).setStroke(Color.web(color));
            series[module_id].get(i).setSymbolFill(Color.web(color));
            series[module_id].get(i).setSymbolStroke(Color.web(color));
            checkboxes[module_id][i].setStyle("-fx-text-fill: " + color);
        }

        series[module_id].get(indextempsens).setChartType(ChartType.AREA);
        series[module_id].get(indextempsens).setFill(Color.web("#FFFFFF", 0.3));
        series[module_id].get(indextempsens).setStrokeWidth(4);
        series[module_id].get(indextempsens).setSymbolSize(12);

        if(indextempsens+1 < series[module_id].size()) {
            series[module_id].get(indextempsens + 1).setChartType(ChartType.AREA);
            series[module_id].get(indextempsens + 1).setFill(Color.web("#FFFFFF", 0.3));
            series[module_id].get(indextempsens + 1).setStrokeWidth(4);
            series[module_id].get(indextempsens + 1).setSymbolSize(12);
        }
    }

    /**
     * Whenever this method is called, updates visibility of series, with respect to the choices of the checkboxes.
     * @param module_id Module identification used for the module to update series visibility.
     * @see GraphController#updateSeries(int)
     * @see GraphController#initSeries(int)
     */
    private void updateVisibility(int module_id) {
        for(int i = 0; i < checkboxes[module_id].length; i++)
            series[module_id].get(i).setVisible(checkboxes[module_id][i].isSelected());
        updateSeries(module_id);
    }

    /**
     * Updates series whenever new values are obtainable from the DataHandler singleton class.
     * It also calls setYAxisRange to adjust the YAxis range for displayed series.
     * @param module_id Module identification used for the module to update series values.
     * @see DataHandler#updateData(String[], int)
     * @see GraphController#setYAxisRange(int, int, int, int)
     */
    static public void updateSeries(int module_id){

        // Obtain last "CONST_POINTSNUM" points and updates their series
        XYChartItem[][] data = DataHandler.getInstance().getModule(module_id).getLastData(CONST_POINTSNUM);

        int indexvoltsens = DataHandler.getInstance().getModule(module_id).getNumVoltSens() + 1;
        int indextempsens = indexvoltsens + DataHandler.getInstance().getModule(module_id).getNumTempSens();

        for(int i = 0; i < data.length; i++)
            series[module_id].get(i).setItems(data[i]);

        // Updating ranges
        setYAxisRange(0, indexvoltsens, 0, module_id);
        setYAxisRange(indexvoltsens, indextempsens, 1, module_id);

        // If current chart is present, updates range
        if(DataHandler.getInstance().getModule(module_id).getCurrentBool())
            setYAxisRange(indextempsens+1,indextempsens+2,3, module_id);
    }

    /**
     * Updates YAxis ranges with given parameters.
     * @param begin Begin sublist for YAxis series update.
     * @param end End sublist for YAxis series update.
     * @param graph_id Chart id subject to changes.
     * @param module_id Module identification for the module to update YAxis ranges.
     */
    static private void setYAxisRange(int begin, int end, int graph_id, int module_id)
    {
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;
        boolean flag = false;

        for(XYSeries s : series[module_id].subList(begin, end))
            if(s.isVisible()) {
                if (max < s.getMaxY()) {
                    max = s.getMaxY();
                }
                if (min > s.getMinY()) {
                    min = s.getMinY();
                }
                flag = true;
            }

        // If series are visible, updates yaxis to adapt to new constraints
        if(flag) {
            if(min > yaxis[module_id][graph_id].getMaxValue()) {
                yaxis[module_id][graph_id].setMaxValue(max + 0.1 * max);
                yaxis[module_id][graph_id].setMinValue(min - 0.1 * Math.abs(min));
            }
            else {
                yaxis[module_id][graph_id].setMinValue(min - 0.1 * Math.abs(min));
                yaxis[module_id][graph_id].setMaxValue(max + 0.1 * max);
            }
            yaxis[module_id][graph_id].setTickLabelsVisible(true);
        }
    }
}
