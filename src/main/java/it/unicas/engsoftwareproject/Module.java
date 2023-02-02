package it.unicas.engsoftwareproject;

import eu.hansolo.fx.charts.data.XYChartItem;

import java.util.ArrayList;

public class Module {

    private int id;
    final int CONST_NUMFAULTS = 4;
    final int CONST_CURRENTFAULTS = 2;
    final int CONST_NUMVSTACKSOC = 2;
    final int CONST_NUMSTATS = 4;
    private ArrayList<Double>[] data = null;
    private ArrayList<String>[] faultsdata = null;
    private int numfields;
    private int numrows;
    private int numfaults;
    private int numvoltsens;
    private int numtempsens;
    private boolean current;
    private Double[] vmax = null;
    private Double[] vmin = null;
    private Double[] vavg = null;
    private Double[] vdelta = null;


    public Module(int numvoltsens, int numtempsens, boolean current, boolean faults, int id)
    {
        this.id = id;
        numfields = numvoltsens + numtempsens + (current ? 1:0) + CONST_NUMVSTACKSOC;
        numfaults = CONST_NUMFAULTS+(current ? 1:0)*CONST_CURRENTFAULTS;
        this.numvoltsens = numvoltsens;
        this.numtempsens = numtempsens;
        this.current = current;
        data = new ArrayList[numfields];
        for(int i = 0; i < data.length; i++)
            data[i] = new ArrayList();
        if(faults) {
            faultsdata = new ArrayList[numfaults];
            for(int i = 0; i < faultsdata.length; i++)
                faultsdata[i] = new ArrayList();
        }

        vmax = new Double[numvoltsens];
        vmin = new Double[numvoltsens];
        vavg = new Double[numvoltsens];
        vdelta = new Double[numvoltsens];

        for(int i = 0; i < vmax.length; i++)
            vmax[i] = Double.NEGATIVE_INFINITY;
        for(int i = 0; i < vmin.length; i++)
            vmin[i] = Double.POSITIVE_INFINITY;
        for(int i = 0; i < vavg.length; i++)
            vavg[i] = 0.0;
        for(int i = 0; i < vdelta.length; i++)
            vdelta[i] = 0.0;

        numrows = 0;
        System.out.println(numfields);
    }

    public void addRow(String[] row)
    {
        for(int i = 0; i < numfields; i++) {
            data[i].add(Double.parseDouble(row[i]));
            //System.out.print(data[i].get(numrows) + " ");
        }

        if(faultsdata != null)
            for(int i = 0; i < numfaults; i++) {
                faultsdata[i].add(row[i + numfields]);
                //System.out.print(faultsdata[i].get(numrows) + " ");
            }

        //System.out.println("");

        updateMax();
        updateMin();
        updateAvg();
        updateDelta();

        numrows++;
    }

    private void updateMax(){
        for(int i = 0; i < vmax.length; i++)
            if(vmax[i] < data[i].get(numrows))
                vmax[i] = data[i].get(numrows);
    }
    private void updateMin(){
        for(int i = 0; i < vmin.length; i++)
            if(vmin[i] > data[i].get(numrows))
                vmin[i] = data[i].get(numrows);
    }
    private void updateAvg(){
        for(int i = 0; i < vavg.length; i++)
            vavg[i] = (vavg[i]*(numrows) + data[i].get(numrows))/(numrows+1);

    }
    private void updateDelta(){
        for(int i = 0; i < vdelta.length; i++)
            vdelta[i] = vmax[i] - vmin[i];
    }

    public Double[] getStatsRow(int cell_id){
        Double[] row = new Double[CONST_NUMSTATS];
        row[0] = vmax[cell_id];
        row[1] = vmin[cell_id];
        row[2] = vavg[cell_id];
        row[3] = vdelta[cell_id];

        return row;
    }

    public Double[] getDataRow(int row_id){
        Double[] row = new Double[data.length];
        for(int i = 0; i < data.length; i++){
            row[i] = data[i].get(row_id);
        }
        return row;
    }
    public String[] getFaultsRow(int row_id){
        if(faultsdata == null)
            return null;

        String[] row = new String[faultsdata.length];
        for(int i = 0; i < faultsdata.length; i++)
            row[i] = faultsdata[i].get(row_id);

        return row;
    }

    public int getNumVoltSens() {
        return numvoltsens;
    }
    public int getNumTempSens() {
        return numtempsens;
    }

    public int getNumRows(){
        return numrows;
    }

    public double getVMax()
    {
        double max = Double.NEGATIVE_INFINITY;
        for(int i = 0; i < vmax.length; i++)
            if(max < vmax[i])
                max = vmax[i];
        return max;
    }

    public double getVMin()
    {
        double min = Double.POSITIVE_INFINITY;
        for(int i = 0; i < vmin.length; i++)
            if(min > vmin[i])
                min = vmin[i];
        return min;
    }

    public double getMaxDelta()
    {
        double deltamax = 0;
        for(int i = 0; i < vdelta.length; i++)
            if(deltamax < vdelta[i])
                deltamax = vdelta[i];
        return deltamax;
    }

    public double getVAvg()
    {
        double avg = 0;
        for(int i = 0; i< vavg.length; i++)
            avg += vavg[i];

        avg = avg / vavg.length;

        return avg;
    }

    public Double[] getStats()
    {
        Double[] stats = new Double[4];
        stats[0] = getVMax();
        stats[1] = getVMin();
        stats[2] = getVAvg();
        stats[3] = getMaxDelta();
        return  stats;
    }

    public int getNumfields() {
        return numfields;
    }

    public boolean getCurrentBool() {
        return current;
    }

    public XYChartItem[][] getLastData(int n){

        if(n > numrows)
            n = numrows;

        XYChartItem[][] lastdata = new XYChartItem[numfields][n];
        for(int i = 0; i < numfields; i++)
            for(int j = 0; j < n ; j++)
                lastdata[i][j] = new XYChartItem(j ,data[i].get(j + numrows - n));

        return lastdata;
    }
}
