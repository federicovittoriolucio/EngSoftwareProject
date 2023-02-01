package it.unicas.engsoftwareproject.controller;

import eu.hansolo.fx.charts.*;
import eu.hansolo.fx.charts.data.*;
import eu.hansolo.fx.charts.series.XYSeries;
import eu.hansolo.fx.charts.series.XYSeriesBuilder;
import eu.hansolo.fx.charts.series.XYZSeriesBuilder;
import it.unicas.engsoftwareproject.DataHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.chart.Chart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;

import javafx.scene.paint.Color;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GraphController {

    @FXML
    private TabPane graphtabpane;

    private Tab[] graphtabs;
    private HBox[] tabhbox;
    private ScrollPane[] scrollpane;
    private VBox[] menuvbox;
    private CheckBox[][] checkboxes;
    private GridPane[] gridpane;

    @FXML
    public void initialize(){

        int module_number = DataHandler.getInstance().getActiveModules();

        graphtabs = new Tab[module_number];
        tabhbox = new HBox[module_number];
        scrollpane = new ScrollPane[module_number];
        menuvbox = new VBox[module_number];
        checkboxes = new CheckBox[module_number][];
        gridpane = new GridPane[module_number];


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

            gridpane[i].add(createGraph(i), 0,0);
            gridpane[i].add(createGraph(i), 1,0);
            gridpane[i].add(createGraph(i), 0,1);
            gridpane[i].add(createGraph(i), 1,1);

        }


        graphtabpane.getTabs().addAll(graphtabs);

    }

    private XYChart createGraph(int module_id){

        int pointsnum = 50;
        int endvalue = DataHandler.getInstance().getSampletime() * pointsnum;

        ArrayList<XYChartItem> points  = new ArrayList<>();
        points.add(new XYChartItem(0, 0));
        //for(int i = 0; i < 30; i++){
        //    points.add(new TYChartItem(LocalDateTime.now().plusSeconds(i), i));
        //}

        Axis xaxis;
        Axis yaxis;

        XYSeries series = XYSeriesBuilder.create()
                .chartType(ChartType.SMOOTH_LINE)
                .fill(Color.web("#00AEF520"))
                .stroke(Color.web("#00AEF5"))
                .symbolFill(Color.web("#00AEF5"))
                .symbolStroke(Color.web("#293C47"))
                .symbolSize(10)
                .strokeWidth(3)
                .symbolsVisible(true)
                .build();

        xaxis = AxisBuilder.create(Orientation.HORIZONTAL, Position.BOTTOM)
                .type(AxisType.LINEAR)
                .minValue(0)
                .maxValue(endvalue)
                .autoScale(true)
                .build();

        xaxis.setVisible(false);

        yaxis = AxisBuilder.create(Orientation.VERTICAL, Position.LEFT)
                .type(AxisType.LINEAR)
                .minValue(0)
                .maxValue(11)
                .autoScale(true)
                .axisColor(Color.web("#85949B"))
                .tickLabelColor(Color.web("#85949B"))
                .tickMarkColor(Color.web("#85949B"))
                .tickMarksVisible(true)
                .build();

        Grid grid = GridBuilder.create(xaxis, yaxis)
                .gridLinePaint(Color.web("#384C57"))
                .minorHGridLinesVisible(false)
                .mediumHGridLinesVisible(false)
                .minorVGridLinesVisible(false)
                .mediumVGridLinesVisible(false)
                .gridLineDashes(4, 4)
                .build();

        XYPane lineChartPane = new XYPane(series);

        XYChart lineChart = new XYChart<>(lineChartPane, grid, yaxis, xaxis);

        ScheduledExecutorService scheduledExecutorService;
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.scheduleAtFixedRate(() -> {

            Platform.runLater(() -> {
                points.add(new XYChartItem(points.get(points.size()-1).getX() + DataHandler.getInstance().getSampletime(),
                                            Math.random()*10));
                if(points.size() > pointsnum) {
                    points.remove(0);
                    xaxis.setMinValue(points.get(0).getX());
                    xaxis.setMaxValue(points.get(points.size()-1).getX());
                }
                series.setItems(points);
            });
        }, 0, DataHandler.getInstance().getSampletime(), TimeUnit.MILLISECONDS);

        return lineChart;
    }

}
