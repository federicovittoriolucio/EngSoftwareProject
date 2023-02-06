package it.unicas.engsoftwareproject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;


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
    private int samplenum;
    private Timer timer;
    private Scanner reader;

    enum State {INACTIVE, RUNNING, PAUSE, END}
    State state;

    public CSVReader(String pathname, int T) throws FileNotFoundException {
        id = INSTANCE_COUNTER;
        INSTANCE_COUNTER++;
        state = State.INACTIVE;
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

    public void start() throws FileNotFoundException
    {
        reader = new Scanner(new File(path));
        reader.nextLine();
        state = State.RUNNING;
        timer = new Timer();
        // Viene eseguito il task, runnando update() ogni sampletime millisecondi
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                update(reader);
            }
        }, 0, sampletime);
        // Status = running

    }

    public void update(Scanner reader)
    {
        if(reader.hasNextLine()) {
            String line = reader.nextLine();
            String[] rowline = line.split(",");
            DataHandler.getInstance().updateData(rowline,id);
            samplenum++;

        }
        else {
            state = State.END;
            stop();
        }
    }

    public void pause()
    {
        if(state == State.RUNNING) {
            state = State.PAUSE;
            timer.cancel();
            timer.purge();
            System.out.println("Blocco il timer");
        }
    }

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
                    update(reader);
                }
            }, 0, sampletime);
        }
    }

    public void stop()
    {
        timer.cancel();
        timer.purge();
        reader.close();
        // Status = stopped
    }
    static public void resetCounter()
    {
        INSTANCE_COUNTER = 0;
    }

}
