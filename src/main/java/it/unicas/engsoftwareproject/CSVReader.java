package it.unicas.engsoftwareproject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Implementation of DataSource, it reads data from CSV files.
 */
public class CSVReader implements DataSource
{
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
     * Constructor: Initializes the reader, storing the amount of fields, and adds a new module representing the reader into DataHandler.
     * @param pathname CSV system path.
     * @param T Sample time of the simulation.
     * @throws FileNotFoundException Exception thrown when the system path is invalid.
     * @see DataHandler
     */
    public CSVReader(String pathname, int T) throws FileNotFoundException
    {
        // Initalization of members of the class (reads file to obtain fields properties)
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

        // Creates a new module if the CSV reading is successful
        DataHandler.getInstance().addModule(numvoltsens,numtempsens,current,faults,id);

        sc.close();
    }

    /**
     * Implementation of the start method, once started updates data every [sample time] milliseconds scheduling a timer, which is an attribute of the class.
     * @throws FileNotFoundException Exception thrown when the system path is not valid.
     * @see CSVReader#update()
     */
    public void start() throws FileNotFoundException
    {
        // Starts reading file at location "path" and schedules a timer for update every "sampletime"
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
     * Implementation of the update method; whenever called, reads the next line of data and updates the DataHandler singleton.
     * If it reaches the end of the file, it calls stop() and updates state.
     * @see DataHandler
     */
    public void update()
    {
        // Reads new line when called and organizes it in a string array (otherwise stops execution)
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
     * Implementation of the pause method, whenever called, it pauses the reader execution and updates the state.
     */
    public void pause()
    {
        // If reading, pauses execution
        if(state == State.RUNNING) {
            state = State.PAUSE;
            timer.cancel();
            timer.purge();
        }
    }

    /**
     * Implementation of the resume method, whenever called, it resumes the reader and updates state.
     */
    public void resume()
    {
        // if paused, resume execution
        if(state == State.PAUSE) {
            state = State.RUNNING;
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    update();
                }
            }, 0, sampletime);
        }
    }

    /**
     * Implementation of the stop method, whenever called, it stops the reader execution and closes the file reader.
     */
    public void stop()
    {
        // Purges timer and closes reader
        timer.cancel();
        timer.purge();
        reader.close();
    }

    /**
     * Resets counter whenever called (used to reset CSV Readers after stopping the simulation)
     * @see it.unicas.engsoftwareproject.controller.MonitorController
     */
    static public void resetCounter()
    {
        INSTANCE_COUNTER = 0;
    }
}