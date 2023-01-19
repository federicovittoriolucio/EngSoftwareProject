package it.unicas.engsoftwareproject;

import java.util.ArrayList;

public class Module {

    private int id;
    final int CONST_NUMFAULTS = 6;
    final int CONST_NUMVSTACKSOC = 2;
    private ArrayList<Double>[] data = null;
    private ArrayList<Boolean>[] faultsdata = null;
    private int numfields;
    private int numrows;
    private Double[] vmax = null;
    private Double[] vmin = null;
    private Double[] vavg = null;
    private Double[] deltav = null;


    public Module(int numvoltsens, int numtempsens, boolean current, boolean faults, int id)
    {
        this.id = id;
        numfields = numvoltsens + numtempsens + (current ? 1:0) + CONST_NUMVSTACKSOC;
        data = new ArrayList[numfields];
        for(int i = 0; i < data.length; i++)
            data[i] = new ArrayList();
        if(faults) {
            faultsdata = new ArrayList[CONST_NUMFAULTS];
            for(int i = 0; i < faultsdata.length; i++)
                faultsdata[i] = new ArrayList();
        }

        vmax = new Double[numvoltsens];
        vmin = new Double[numvoltsens];
        vavg = new Double[numvoltsens];
        deltav = new Double[numvoltsens];
        numrows = 0;
        System.out.println(numfields);
    }

    public void addRow(String[] row)
    {
        for(int i = 0; i < numfields; i++) {
            data[i].add(Double.parseDouble(row[i]));
            System.out.print(data[i].get(numrows) + " ");
        }

        for(int i = 0; i < CONST_NUMFAULTS; i++) {
            faultsdata[i].add(Boolean.parseBoolean(row[i + numfields]));
            System.out.print(faultsdata[i].get(numrows) + " ");
        }

        numrows++;
    }




}
