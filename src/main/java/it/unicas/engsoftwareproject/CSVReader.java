package it.unicas.engsoftwareproject;

import it.unicas.engsoftwareproject.controller.HelloController;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;


public class CSVReader {

    private ArrayList<String> fields;
    private Double[][] values;
    private int numcols;
    private int numvoltsens;
    private int numtempsens;
    private int sampletime; // ms
    private boolean current;
    private boolean faults;
    private String path;

    private int samplenum;

    public CSVReader(String pathname, int T) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(pathname));
        path = pathname;
        sampletime = T;
        System.out.println(path);
        samplenum = 0;
        sc.useDelimiter(",");   //sets the delimiter pattern
        fields = new ArrayList<String>();
        String linefield = sc.nextLine();
        String[] field = linefield.split(",");
        for(String item : field)
            fields.add(item);

        System.out.println("");

        /*while(sc.hasNextLine())
        {
            String line = sc.nextLine();
            String[] rowline = line.split(",");
            list.add(rowline);
        }

        values = new Double[numrows][numcols];
        for(int i = 0; i < numrows; i++)
        {
            for (int j = 0; j < numcols; j++)
                values[i][j] = Double.parseDouble(list.get(i)[j]);
        }*/

        numvoltsens = 0;
        numtempsens = 0;
        current = false;
        faults = false;
        numcols = fields.size();

        for(int i = 0; i < numcols; i++) {
            if (fields.get(i).equals("Vstack"))
                numvoltsens = i;

            if (fields.get(i).equals("Soc"))
                numtempsens = i - numvoltsens - 1;

            if(fields.get(i).equals("I"))
                current = true;

            if(fields.get(i).equals("OV"))
                faults = true;
        }

        System.out.println("Number of volt sensors: " + numvoltsens);
        System.out.println("Number of temp sensors: " + numtempsens);
        System.out.println("Current: " + current);
        System.out.println("Faults: " + faults);

        sc.close();
    }

    /*public void printCSV()
    {
        for(String item : fields)
        {
            System.out.print(item + " ");
        }
        System.out.println("");
        for(int j = 0; j < numrows; j++)
        {
            for (int i = 0; i < numcols; i++)
                System.out.print(list.get(j)[i] + " ");

            System.out.println("");
        }
    }*/

    public Double getValue(int row, int column)
    {
        return values[row][column];
    }

    public Double[] getRow(int row)
    {
        Double[] vec = new Double[numcols];
        for(int i = 0; i < numcols; i++)
            vec[i] = values[row][i];
        return vec;
    }

    public Double[][] getValues()
    {
        return values;
    }

    public int getNumVoltSens()
    {
        return numvoltsens;
    }

    public int getNumTempSens()
    {
        return numtempsens;
    }

    public boolean getCurrentBool()
    {
        return current;
    }

    public boolean getFaultsBool()
    {
        return faults;
    }

    public void start()
    {
        Scanner sc = new Scanner(path);
        sc.nextLine();
        Timer timer = new Timer();
        // Viene eseguito il task, runnando update() ogni sampletime millisecondi
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                update(sc);
            }
        }, 0, sampletime);

    }

    public void update(Scanner sc)
    {
        if(sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] rowline = line.split(",");
            for (int i = 0; i < numcols; i++) {
                values[samplenum][i] = Double.parseDouble(rowline[i]);
                System.out.print(values[samplenum][i] + " ");
            }
            System.out.println("");
            samplenum++;
        }
        else {
            sc.close();
        }
    }
}
