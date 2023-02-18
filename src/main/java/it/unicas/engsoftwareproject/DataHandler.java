package it.unicas.engsoftwareproject;

import it.unicas.engsoftwareproject.controller.GraphController;
import it.unicas.engsoftwareproject.controller.MonitorController;

import javafx.application.Platform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Singleton class that holds responsibility of managing and updating incoming data and store such data in files.
 */
public class DataHandler {
    private static DataHandler instance = null;

    /**
     * Used to reference the singleton class object.
     * @return Singleton object
     */
    public static synchronized DataHandler getInstance(){
        if(instance == null)
            instance = new DataHandler();
        return instance;
    }

    // Max amount of modules
    final public int CONST_NUMMODULES = 6;
    // Number of active modules
    private int activemodules = 0;

    // Modules container
    private Module[] modules = null;

    /**
     * Initialize the array with the maximum amount of modules instantiable.
     */
    private DataHandler(){
        modules = new Module[CONST_NUMMODULES];
    }

    /** Generates a module with fields given by the arguments and increases the amount of active modules (the maximum amount of modules is fixed).
     * @param numvoltsens Number of voltage sensors.
     * @param numtempsens Number of temperature sensors.
     * @param current Presence of current.
     * @param faults Presence of faults.
     * @param id Module ID.
     */
    public void addModule(int numvoltsens, int numtempsens, boolean current, boolean faults, int id)
    {
        if(activemodules < CONST_NUMMODULES) {
            modules[activemodules] = new Module(numvoltsens, numtempsens, current, faults, id);
            activemodules++;
        }

    }

    /**
     * Updates data logically, and graphically in the Module class and in the displaying containers through the Monitor classes.
     * @param datarow Array of strings containing the data to be updated and displayed on the interface.
     * @param id_module Module ID to be updated.
     * @see MonitorController#updateGraphics(int)
     * @see GraphController#updateSeries(int)
     * @see Module#addRow(String[])
     */
    public void updateData(String[] datarow, int id_module)
    {
        Module m = modules[id_module];
        // adds data row to the specific module
        m.addRow(datarow);
        // updates graphics
        Platform.runLater(() -> MonitorController.updateGraphics(id_module));
        if(BMSMonitor.stagelist.size() > 2)
            Platform.runLater(() -> GraphController.updateSeries(id_module));
    }

    /**
     * Creates a CSV file with the statistical data of a given Module object through module ID.
     * @param id_module Module ID to be manipulated.
     * @throws IOException Exception thrown if the CSV file can't be built.
     */
    public void writeStatsCSV(int id_module) throws IOException
    {
        // Building stats file
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd_HH.mm.ss");
        String now = dtf.format(LocalDateTime.now());
        String path = System.getProperty("user.dir") + "/CSV_out/";

        if(!Files.exists(Paths.get(path)))
            new File(path).mkdirs();
        FileWriter fw = new FileWriter(path + "stats_" + now + "_mod_" +  id_module + ".csv");
        // Writing stats fields
        fw.write("vmax, vmin, vavg, vdelta\n");

        // Writing actual stats
        for (int i = 0; i < modules[id_module].getNumVoltSens(); i++) {

            Double[] row = modules[id_module].getStatsRow(i);
            for (int j = 0; j < row.length; j++) {
                fw.write(row[j].toString());
                if(j != row.length - 1)
                    fw.write(",");
            }
            fw.write("\n");
        }
        fw.close();
    }

    /**
     * Creates a CSV file with the data stores in a given Module object through module ID.
     * @param id_module Module ID to be manipulated.
     * @throws IOException Exception thrown if the CSV file can't be built.
     */
    public void writeDataCSV(int id_module) throws IOException {

        // Building data file
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd_HH.mm.ss");
        String now = dtf.format(LocalDateTime.now());
        String path = System.getProperty("user.dir") + "/CSV_out/";

        if(!Files.exists(Paths.get(path)))
            new File(path).mkdirs();
        FileWriter fw = new FileWriter(path + "data_" + now + "_mod_" + id_module + ".csv");

        // Writing data fields
        for(int j = 0; j < modules[id_module].getNumVoltSens(); j++)
            fw.write("Vcell" + (j+1) +",");

        fw.write("Vstack,");

        for(int j = 0; j < modules[id_module].getNumTempSens(); j++)
            fw.write("Temp" + (j+1) +",");

        fw.write("SoC");

        if(modules[id_module].getCurrentBool())
            fw.write(",I");

        if(modules[id_module].getFaultsRow(0) != null) {
            fw.write(",OV,UV,OT,UT");

            if(modules[id_module].getCurrentBool())
                fw.write(",W,A");

        }

        fw.write("\n");

        // Writing actual data
        for (int i = 0; i < modules[id_module].getNumRows(); i++) {

            Double[] datarow = modules[id_module].getDataRow(i);
            String[] faultsrow = modules[id_module].getFaultsRow(i);

            for (int j = 0; j < datarow.length; j++) {
                fw.write(datarow[j].toString());
                fw.write(",");
            }
            if(faultsrow != null)
                for (int j = 0; j < faultsrow.length; j++) {
                    fw.write(faultsrow[j]);
                    if(j != faultsrow.length - 1)
                        fw.write(",");
                }
            fw.write("\n");
        }
        fw.close();
    }

    /**
     * Return number of active modules.
     * @return Number of active modules.
     */
    public int getActiveModules() {return activemodules;}

    /**
     * Instantiate readers using the given paths and sample time.
     * @param paths  An array containing system paths of the data source.
     * @param sampletime Sample time of the execution.
     * @return An array of DataSource class with instanced readers.
     * @throws FileNotFoundException Exception thrown if one or more paths are not valid.
     * @see MonitorController#initialize()
     */
    public DataSource[] genReaders(String[] paths, int sampletime) throws FileNotFoundException {

        // Generating data readers
        // ***IMPORTANT***
        // You need to change this part of the code in a way that it works for the desired data source

        // Generating CSVReaders
        CSVReader[] readers = new CSVReader[paths.length];

        for (int i = 0; i < readers.length; i++)
            readers[i] = new CSVReader(paths[i], sampletime);

        return readers;
    }

    /**
     * Resets active modules to zero.
     */
    public void resetActivemodules() {
        activemodules = 0;
    }

    /**
     * @param id_module Module ID to return.
     * @return Module with identification id_module.
     */
    public Module getModule(int id_module){
        return modules[id_module];
    }
}
