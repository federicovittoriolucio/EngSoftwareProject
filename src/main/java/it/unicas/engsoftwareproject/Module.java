package it.unicas.engsoftwareproject;

import eu.hansolo.fx.charts.data.XYChartItem;

import java.util.ArrayList;

/**
 * Class in which every data associated to such module is saved in the right container. Manipulates and manages data on request of DataHandler,
 * It is also used whenever the last N samples are requested.
 * It also manages the presence of current sensor and faults sensors.
 */
public class Module {

    private int id;

    // Constants used to determine the amount of columns.
    final int CONST_NUMFAULTS = 4;
    final int CONST_CURRENTFAULTS = 2;
    final int CONST_NUMVSTACKSOC = 2;
    final int CONST_NUMSTATS = 4;
    /**
     * The container that stores data (Excluding faults).
     */
    private ArrayList<Double>[] data = null;
    /**
     * The container that stores faults data.
     */
    private ArrayList<String>[] faultsdata = null;
    /**
     * Number of fields.
     */
    private int numfields;
    /**
     * Number of rows (elements).
     */
    private int numrows;
    /**
     * Number of faults: depends on the presence of current, voltage and temperature sensors.
     */
    private int numfaults;
    /**
     * Number of voltage sensors (cells).
     */
    private int numvoltsens;
    /**
     * Number of temperature sensors.
     */
    private int numtempsens;
    /**
     * True if current is present, otherwise false.
     */
    private boolean current;
    /**
     * Stores maximum voltage for every cell.
     */
    private Double[] vmax = null;
    /**
     * Stores the minimum voltage for every cell.
     */
    private Double[] vmin = null;

    /**
     * Stores the average voltage for every cell.
     */
    private Double[] vavg = null;
    /**
     * Stores the maximum difference between maximum and minimum voltage per cell.
     */
    private Double[] vdelta = null;


    /** The constructor initialize every container used, and identifies the amount of fields necessary to store data accordingly.
     * @param numvoltsens Number of voltage cells.
     * @param numtempsens Number of temperature sensors.
     * @param current Presence of current.
     * @param faults Presence of faults,
     * @param id Module ID.
     */
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

    /**
     * Adds the new data row at the end of the data array list and calls the necessary methods to update statistical data.
     * @param row String array containing data to be added.
     * @see Module#updateMax()
     * @see Module#updateMin()
     * @see Module#updateAvg()
     * @see Module#updateDelta()
     */
    public void addRow(String[] row)
    {
        for(int i = 0; i < numfields; i++)
            data[i].add(Double.parseDouble(row[i]));

        if(faultsdata != null)
            for(int i = 0; i < numfaults; i++)
                faultsdata[i].add(row[i + numfields]);

        updateMax();
        updateMin();
        updateAvg();
        updateDelta();

        numrows++;
    }

    /**
     * Method that updates maxmimum voltage for every cell.
     */
    private void updateMax(){
        for(int i = 0; i < vmax.length; i++)
            if(vmax[i] < data[i].get(numrows))
                vmax[i] = data[i].get(numrows);
    }

    /**
     * Method that updates minimum voltage for every cell.
     */
    private void updateMin(){
        for(int i = 0; i < vmin.length; i++)
            if(vmin[i] > data[i].get(numrows))
                vmin[i] = data[i].get(numrows);
    }

    /**
     * Method that updates average voltage for every cell.
     */
    private void updateAvg(){
        for(int i = 0; i < vavg.length; i++)
            vavg[i] = (vavg[i]*(numrows) + data[i].get(numrows))/(numrows+1);

    }

    /**
     * Method that updates maxmimum difference between maximum and minimum voltage for every cell.
     */
    private void updateDelta(){
        for(int i = 0; i < vdelta.length; i++)
            vdelta[i] = vmax[i] - vmin[i];
    }

    /**
     * Stores the statistical data in an array and returns it.
     * @param cell_id Number identification of the cell.
     * @return Array of double respectively containing voltage max, voltage min, voltage average and voltage delta.
     */
    public Double[] getStatsRow(int cell_id){
        Double[] row = new Double[CONST_NUMSTATS];
        row[0] = vmax[cell_id];
        row[1] = vmin[cell_id];
        row[2] = vavg[cell_id];
        row[3] = vdelta[cell_id];

        return row;
    }

    /**
     * Stores the raw data at postion row_id into an array and returns it.
     * @param row_id Number identification of the row
     * @return The data row at position row_id
     */
    public Double[] getDataRow(int row_id){
        Double[] row = new Double[data.length];
        for(int i = 0; i < data.length; i++){
            row[i] = data[i].get(row_id);
        }
        return row;
    }

    /**
     * Stores the raw faults at postion row_id into an array and returns it.
     * @param row_id Number identification of the row
     * @return The faults row at position row_id
     */
    public String[] getFaultsRow(int row_id){
        if(faultsdata == null)
            return null;

        String[] row = new String[faultsdata.length];
        for(int i = 0; i < faultsdata.length; i++)
            row[i] = faultsdata[i].get(row_id);

        return row;
    }

    /** Return The number of voltage sensors (cells).
     * @return The number of voltage sensors (cells).
     */
    public int getNumVoltSens() {
        return numvoltsens;
    }

    /** Return The number of temperature sensors.
     * @return The number of temperature sensors.
     */
    public int getNumTempSens() {
        return numtempsens;
    }

    /** Return Number of rows (elements) in the module.
     * @return Number of rows (elements) in the module.
     */
    public int getNumRows(){
        return numrows;
    }

    /** Return The global module maximum voltage.
     * @return The global module maximum voltage.
     */
    public double getVMax()
    {
        double max = Double.NEGATIVE_INFINITY;
        for(int i = 0; i < vmax.length; i++)
            if(max < vmax[i])
                max = vmax[i];
        return max;
    }

    /** Return The global module minimum voltage.
     * @return The global module minimum voltage.
     */
    public double getVMin()
    {
        double min = Double.POSITIVE_INFINITY;
        for(int i = 0; i < vmin.length; i++)
            if(min > vmin[i])
                min = vmin[i];
        return min;
    }

    /** Return The global module maximum difference between maximum and minimum voltage.
     * @return The global module maximum difference between maximum and minimum voltage.
     */
    public double getMaxDelta()
    {
        double deltamax = 0;
        for(int i = 0; i < vdelta.length; i++)
            if(deltamax < vdelta[i])
                deltamax = vdelta[i];
        return deltamax;
    }

    /** Return The global module voltage average.
     * @return The global module voltage average.
     */
    public double getVAvg()
    {
        double avg = 0;
        for(int i = 0; i< vavg.length; i++)
            avg += vavg[i];

        avg = avg / vavg.length;

        return avg;
    }

    /** Return A double array with the global module statistical data, respectively max voltage, min voltage, average voltage and delta voltage.
     * @return A double array with the global module statistical data, respectively max voltage, min voltage, average voltage and delta voltage.
     * @see Module#getVMax()
     * @see Module#getVMin()
     * @see Module#getVAvg()
     * @see Module#getMaxDelta()
     */
    public Double[] getStats()
    {
        Double[] stats = new Double[4];
        stats[0] = getVMax();
        stats[1] = getVMin();
        stats[2] = getVAvg();
        stats[3] = getMaxDelta();
        return  stats;
    }

    /**
     * Returns number of total fields (columns).
     * @return Number of total fields (columns).
     */
    public int getNumfields() {
        return numfields;
    }

    /** Return true if current is present, false otherwise.
     * @return True if current is present, false otherwise.
     */
    public boolean getCurrentBool() {
        return current;
    }

    /** This method is used to retrieve the last n data elements to plot in the associated chart.
     * @param n Last n row samples.
     * @return A n by number of fields XYChartItem matrix containing  the last n samples stored in the module.
     * @see it.unicas.engsoftwareproject.controller.GraphController#updateSeries(int)
     */
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
