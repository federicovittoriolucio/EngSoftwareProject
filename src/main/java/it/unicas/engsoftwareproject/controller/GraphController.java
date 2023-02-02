package it.unicas.engsoftwareproject.controller;

import eu.hansolo.fx.charts.*;
import eu.hansolo.fx.charts.data.*;
import eu.hansolo.fx.charts.series.XYSeries;
import eu.hansolo.fx.charts.series.XYSeriesBuilder;
import eu.hansolo.medusa.tools.Data;
import it.unicas.engsoftwareproject.DataHandler;
import it.unicas.engsoftwareproject.Module;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GraphController {

    @FXML
    private TabPane graphtabpane;
    final static private int CONST_POINTSNUM = 25;
    private Tab[] graphtabs;
    private HBox[] tabhbox;
    private ScrollPane[] scrollpane;
    private VBox[] menuvbox;
    private CheckBox[][] checkboxes;
    private GridPane[] gridpane;

    //Graph stuff
    static private ArrayList<XYSeries>[] series;

    @FXML
    public void initialize(){

        int module_number = DataHandler.getInstance().getActiveModules();

        graphtabs = new Tab[module_number];
        tabhbox = new HBox[module_number];
        scrollpane = new ScrollPane[module_number];
        menuvbox = new VBox[module_number];
        checkboxes = new CheckBox[module_number][];
        gridpane = new GridPane[module_number];

        series = new ArrayList[module_number];

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

            for (int j = 0; j < module.getNumVoltSens(); j++)
                checkboxes[i][j] = new CheckBox("Cell " + (j+1) + " voltage");

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

            for(CheckBox check : checkboxes[i])
                check.getStyleClass().add("checkbox");

            menuvbox[i].getChildren().addAll(checkboxes[i]);
            scrollpane[i].setContent(menuvbox[i]);
            tabhbox[i].getChildren().addAll(scrollpane[i], gridpane[i]);
            graphtabs[i].setContent(tabhbox[i]);

            HBox.setHgrow(gridpane[i], Priority.ALWAYS);

            initSeries(i);
            int num_charts = 3;
            if(module.getCurrentBool())
                num_charts = 4;

            XYChart[] charts = new XYChart[num_charts];
            charts = createGraphs(i);

            gridpane[i].add(charts[0], 0,0);
            gridpane[i].add(charts[1], 1,0);
            gridpane[i].add(charts[2], 0,1);
            if(num_charts == 4)
                gridpane[i].add(charts[3], 1,1);

        }

        graphtabpane.getTabs().addAll(graphtabs);

    }

    private Axis createXAxis(){
        return AxisBuilder  .create(Orientation.HORIZONTAL, Position.BOTTOM)
                            .type(AxisType.LINEAR)
                            .minValue(0)
                            .maxValue(CONST_POINTSNUM)
                            .autoScale(true)
                            .axisColor(Color.web("#85949B"))
                            .tickLabelColor(Color.web("#85949B"))
                            .tickMarkColor(Color.web("#85949B"))
                            .tickMarksVisible(true)
                            .build();
    }

    private Axis createYAxis(){
        return AxisBuilder  .create(Orientation.VERTICAL, Position.LEFT)
                            .type(AxisType.LINEAR)
                            .minValue(0)
                            .maxValue(100)
                            .autoScale(true)
                            .axisColor(Color.web("#85949B"))
                            .tickLabelColor(Color.web("#85949B"))
                            .tickMarkColor(Color.web("#85949B"))
                            .tickMarksVisible(true)
                            .build();
    }

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

    private XYChart[] createGraphs(int module_id) {
        it.unicas.engsoftwareproject.Module module = DataHandler.getInstance().getModule(module_id);
        int num_charts = 3;
        if(module.getCurrentBool())
            num_charts = 4;

        Axis[] xaxis = new Axis[num_charts];
        for(int i = 0; i < num_charts; i++)
            xaxis[i] = createXAxis();

        Axis[] yaxis = new Axis[num_charts];
        for(int i = 0; i < num_charts; i++)
            yaxis[i] = createYAxis();

        Grid[] grids = new Grid[num_charts];
        for(int i = 0; i < num_charts; i++)
            grids[i] = createGrid(xaxis[i], yaxis[i]);

        int indexvoltsens = module.getNumVoltSens() + 1;
        int indextempsens = indexvoltsens + module.getNumTempSens();

        XYPane voltpane = new XYPane(series[module_id].subList(0, indexvoltsens));
        XYPane temppane = new XYPane(series[module_id].subList(indexvoltsens, indextempsens));
        XYPane socpane  = new XYPane(series[module_id].get(indextempsens));
        XYPane currpane;

        XYChart[] charts = new XYChart[num_charts];
        charts[0] = new XYChart(voltpane, grids[0], yaxis[0], xaxis[0]);
        charts[1] = new XYChart(temppane, grids[1], yaxis[1], xaxis[1]);
        charts[2] = new XYChart(socpane, grids[2], yaxis[2], xaxis[2]);

        if(num_charts == 4) {
            currpane = new XYPane(series[module_id].get(indextempsens + 1));
            charts[3] = new XYChart(currpane, grids[3], yaxis[3], xaxis[3]);
        }

        return charts;
        /*
        ScheduledExecutorService scheduledExecutorService;
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.scheduleAtFixedRate(() -> {

            Platform.runLater(() -> {

                if(points.size() <= CONST_POINTSNUM)
                    points.add(new XYChartItem(points.size(), Math.random()*10));
                else{
                    for(int i = 0; i < points.size() - 1; i++)
                        points.get(i).setY(points.get(i+1).getY());
                    points.get(points.size() - 1).setY(Math.random()*10);
                }
                series.setItems(points);

            });
        }, 1000, DataHandler.getInstance().getSampletime(), TimeUnit.MILLISECONDS);

        return linechart;
        */
    }

    private void initSeries(int module_id){

        series[module_id] = new ArrayList<>();
        for(int i = 0; i < DataHandler.getInstance().getModule(module_id).getNumfields(); i++){

            series[module_id].add(
                    XYSeriesBuilder .create()
                                    .chartType(ChartType.LINE)
                                    .fill(Color.web("#00AEF520"))
                                    .stroke(Color.web("#00AEF5"))
                                    .symbolFill(Color.web("#00AEF5"))
                                    .symbolStroke(Color.web("#293C47"))
                                    .symbolSize(10)
                                    .strokeWidth(1)
                                    .symbolsVisible(true)
                                    .build()
            );
        }
    }

    static public void updateSeries(int module_id){

        XYChartItem[][] data = DataHandler.getInstance().getModule(module_id).getLastData(CONST_POINTSNUM);

        for(int i = 0; i < data.length; i++)
            series[module_id].get(i).setItems(data[i]);

    }
}
