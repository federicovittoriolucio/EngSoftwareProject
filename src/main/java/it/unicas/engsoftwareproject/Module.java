package it.unicas.engsoftwareproject;

import eu.hansolo.fx.charts.data.XYChartItem;

import java.util.ArrayList;

/**
 * Class that represents a single module, storing all the necessary data.
 * It manipulates and manages data on demand from DataHandler.
 * Also used whenever the last N samples are needed to update the graphs.
 * It also manages the presence of current sensor and faults sensors.
 */
public class Module
{
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
     * Number of rows (number of values for each field).
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
     * Stores maximum voltage for each cell.
     */
    private Double[] vmax;
    /**
     * Stores the minimum voltage for each cell.
     */
    private Double[] vmin;

    /**
     * Stores the average voltage for each cell.
     */
    private Double[] vavg;
    /**
     * Stores the maximum difference between maximum and minimum voltage for each cell.
     */
    private Double[] vdelta;


    /** Constructor: initializes every container used, and identifies the amount of fields necessary to store data accordingly.
     * @param numvoltsens Number of voltage cells.
     * @param numtempsens Number of temperature sensors.
     * @param current Presence of current.
     * @param faults Presence of faults,
     * @param id Module ID.
     */
    public Module(int numvoltsens, int numtempsens, boolean current, boolean faults, int id)
    {
        // Initalization of members of the class, given them as arguments
        this.id = id;
        numfields = numvoltsens + numtempsens + (current ? 1:0) + CONST_NUMVSTACKSOC;
        numfaults = CONST_NUMFAULTS+(current ? 1:0)*CONST_CURRENTFAULTS;
        this.numvoltsens = numvoltsens;
        this.numtempsens = numtempsens;
        this.current = current;

        // Initiating container for data
        data = new ArrayList[numfields];
        for(int i = 0; i < data.length; i++)
            data[i] = new ArrayList();

        // Initiating container for faults if present
        if(faults) {
            faultsdata = new ArrayList[numfaults];
            for(int i = 0; i < faultsdata.length; i++)
                faultsdata[i] = new ArrayList();
        }

        // Initialization of statistical data
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
     * @param row String array containing the data to be added.
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

        // Updating new statistical data with the new sample
        updateMax();
        updateMin();
        updateAvg();
        updateDelta();

        numrows++;
    }

    /**
     * Updates the maxmimum voltage for each cell.
     */
    private void updateMax()
    {
        for(int i = 0; i < vmax.length; i++)
            if(vmax[i] < data[i].get(numrows))
                vmax[i] = data[i].get(numrows);
    }

    /**
     * Updates the minimum voltage for each cell.
     */
    private void updateMin()
    {
        for(int i = 0; i < vmin.length; i++)
            if(vmin[i] > data[i].get(numrows))
                vmin[i] = data[i].get(numrows);
    }

    /**
     * Updates the average voltage for each cell.
     */
    private void updateAvg()
    {
        for(int i = 0; i < vavg.length; i++)
            vavg[i] = (vavg[i]*(numrows) + data[i].get(numrows))/(numrows+1);
    }

    /**
     * Updates the maxmimum difference between maximum and minimum voltage for each cell.
     */
    private void updateDelta()
    {
        for(int i = 0; i < vdelta.length; i++)
            vdelta[i] = vmax[i] - vmin[i];
    }

    /**
     * Stores the statistical data in an array and returns it.
     * @param cell_id Number identification of the cell.
     * @return Array respectively containing voltage max, voltage min, voltage average and voltage delta.
     */
    public Double[] getStatsRow(int cell_id)
    {
        Double[] row = new Double[CONST_NUMSTATS];
        row[0] = vmax[cell_id];
        row[1] = vmin[cell_id];
        row[2] = vavg[cell_id];
        row[3] = vdelta[cell_id];

        return row;
    }

    /**
     * Stores the raw data at position row_id into an array and returns it.
     * @param row_id Number identification of the row
     * @return The data row at position row_id
     */
    public Double[] getDataRow(int row_id)
    {
        Double[] row = new Double[data.length];

        for(int i = 0; i < data.length; i++)
            row[i] = data[i].get(row_id);

        return row;
    }

    /**
     * Stores the raw faults at postion row_id into an array and returns it.
     * @param row_id Number identification of the row
     * @return The faults row at position row_id, or null if faults aren't included in the module
     */
    public String[] getFaultsRow(int row_id)
    {
        if(faultsdata == null)
            return null;

        String[] row = new String[faultsdata.length];
        for(int i = 0; i < faultsdata.length; i++)
            row[i] = faultsdata[i].get(row_id);

        return row;
    }

    /** Returns the number of voltage sensors (cells).
     * @return The number of voltage sensors (cells).
     */
    public int getNumVoltSens()
    {
        return numvoltsens;
    }

    /** Returns the number of temperature sensors.
     * @return The number of temperature sensors.
     */
    public int getNumTempSens()
    {
        return numtempsens;
    }

    /** Returns the number of rows (number of values for each field) in the module.
     * @return The number of rows (number of values for each field) in the module.
     */
    public int getNumRows()
    {
        return numrows;
    }

    /** Returns the global module maximum voltage.
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

    /** Returns the global module minimum voltage.
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

    /** Returns the global module maximum difference between maximum and minimum voltage.
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

    /** Returns the global module voltage average.
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

    /** Return An array with the global module statistical data, respectively max voltage, min voltage, average voltage and delta voltage.
     * @return An array with the global module statistical data, respectively max voltage, min voltage, average voltage and delta voltage.
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
     * Returns the number of total fields (columns).
     * @return Number of total fields (columns).
     */
    public int getNumfields()
    {
        return numfields;
    }

    /** Returns true if current measurement is present, false otherwise.
     * @return True if current measurement is present, false otherwise.
     */
    public boolean getCurrentBool()
    {
        return current;
    }

    /** This method is used to retrieve the last n data elements to plot them in the associated chart.
     * @param n Last n row samples.
     * @return A n by number of fields XYChartItem matrix containing  the last n samples stored in the module.
     * @see it.unicas.engsoftwareproject.controller.GraphController#updateSeries(int)
     */
    public XYChartItem[][] getLastData(int n)
    {
        if(n > numrows)
            n = numrows;

        XYChartItem[][] lastdata = new XYChartItem[numfields][n];
        for(int i = 0; i < numfields; i++)
            for(int j = 0; j < n ; j++)
                lastdata[i][j] = new XYChartItem(j ,data[i].get(j + numrows - n));

        return lastdata;
    }
}
