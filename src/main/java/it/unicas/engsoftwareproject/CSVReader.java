package it.unicas.engsoftwareproject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Implementation of DataSource, manages reading CSV files.
 */
public class CSVReader implements DataSource {

    private ArrayList<String> fields;

    private static int INSTANCE_COUNTER = 0;

    private int id;
    private int numcols;
    private int numvoltsens;
    private int numtempsens;
    private int sampletime; // ms
    private boolean current;
    private boolean faults;
    private String path;
    private Timer timer;
    private Scanner reader;

    /**
     * Enumerator used to keep track of every feasible state of the reader.
     */
    enum State {INACTIVE, RUNNING, PAUSE, END}
    State state;

    /**
     * Initialize the reader, storing the amount of fields, and add such active module to DataHandler.
     * @param pathname CSV system path.
     * @param T Sample time of the simulation.
     * @throws FileNotFoundException Exception thrown when the system path is not valid.
     * @see DataHandler
     */
    public CSVReader(String pathname, int T) throws FileNotFoundException {
        id = INSTANCE_COUNTER;
        INSTANCE_COUNTER++;
        state = State.INACTIVE;
        Scanner sc = new Scanner(new File(pathname));
        path = pathname;
        sampletime = T;
        System.out.println(path);
        sc.useDelimiter(",");   // sets the delimiter pattern
        fields = new ArrayList<String>();
        String linefield = sc.nextLine();
        String[] field = linefield.split(",");
        for(String item : field)
            fields.add(item);

        System.out.println("");

        numvoltsens = 0;
        numtempsens = 0;
        current = false;
        faults = false;
        numcols = fields.size();

        for(int i = 0; i < numcols; i++) {
            if (fields.get(i).contains("Vcell"))
                numvoltsens++;

            if (fields.get(i).contains("Temp"))
                numtempsens++;

            if(fields.get(i).equals("I"))
                current = true;

            if(fields.get(i).equals("OV"))
                faults = true;
        }

        System.out.println("Number of volt sensors: " + numvoltsens);
        System.out.println("Number of temp sensors: " + numtempsens);
        System.out.println("Current: " + current);
        System.out.println("Faults: " + faults);

        DataHandler.getInstance().addModule(numvoltsens,numtempsens,current,faults,id);

        sc.close();
    }

    /**
     * Implementation of the start method, once started updates data every sample time milliseconds scheduling an attribute timer of the class.
     * @throws FileNotFoundException Exception thrown when the system path is not valid.
     * @see CSVReader#update()
     */
    public void start() throws FileNotFoundException
    {
        reader = new Scanner(new File(path));
        reader.nextLine();
        state = State.RUNNING;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, 0, sampletime);
    }

    /**
     * Implementation of the update method, whenever called, updates data reading calling the DataHandler singleton.
     * If reaches end of file, calls stop() and updates state.
     * @see DataHandler
     */
    public void update()
    {
        if(reader.hasNextLine()) {
            String line = reader.nextLine();
            String[] rowline = line.split(",");
            DataHandler.getInstance().updateData(rowline,id);
        }
        else {
            state = State.END;
            stop();
        }
    }

    /**
     * Implementation of the pause method, whenever called, pauses the reader execution and updates state.
     */
    public void pause()
    {
        if(state == State.RUNNING) {
            state = State.PAUSE;
            timer.cancel();
            timer.purge();
            System.out.println("Blocco il timer");
        }
    }

    /**
     * Implementation of the resume method, whenever called, resume reading and updates state.
     */
    public void resume()
    {
        if(state == State.PAUSE) {
            // Ripristina il timer associato allo scanner principale
            state = State.RUNNING;
            timer = new Timer();
            System.out.println("Riattivo il timer");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    update();
                }
            }, 0, sampletime);
        }
    }

    /**
     * Implementation of the stop method, whenever called, stops the reader execution and close the file reader.
     */
    public void stop()
    {
        timer.cancel();
        timer.purge();
        reader.close();
    }

    /**
     * Resets counter whenever called (used to reset CSV Readers stopping the simulation)
     * @see it.unicas.engsoftwareproject.controller.MonitorController
     */
    static public void resetCounter()
    {
        INSTANCE_COUNTER = 0;
    }

}