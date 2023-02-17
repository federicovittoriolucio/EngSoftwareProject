package it.unicas.engsoftwareproject;

import it.unicas.engsoftwareproject.controller.GraphController;
import it.unicas.engsoftwareproject.controller.MonitorController;

import javafx.application.Platform;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Singleton class that holds responsibility of managing and updating incoming data and store such data in files.
 */
public class DataHandler {
    private static DataHandler instance = null;
    private int sampletime;

    /**
     * Used to reference the singleton class object.
     * @return Singleton object
     */
    public static synchronized DataHandler getInstance(){
        if(instance == null)
            instance = new DataHandler();
        return instance;
    }

    final public int CONST_NUMMODULES = 6;
    private int activemodules = 0;

    private Module[] modules = null;

    /**
     * Initialize the array with the maximum amount of modules instantiable.
     */
    private DataHandler(){
        modules = new Module[CONST_NUMMODULES];
    }

    /** Generates a module with fields such given the arguments and increases the amount of active modules.
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
    public void updateData(String[] datarow, int id_module) {
        Module m = modules[id_module];
        m.addRow(datarow);
        Platform.runLater(() -> MonitorController.updateGraphics(id_module));
        if(BMSMonitor.stagelist.size() > 2)
            Platform.runLater(() -> GraphController.updateSeries(id_module));
    }

    /**
     * Creates a CSV file with the statistical data of a given Module object through module ID.
     * @param id_module Module ID to be manipulated.
     * @throws IOException Exception thrown if the CSV file can't be built.
     */
    public void writeStatsCSV(int id_module) throws IOException {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd_HH.mm.ss");
        String now = dtf.format(LocalDateTime.now());
        FileWriter fw = new FileWriter(System.getProperty("user.dir") + "/CSV_out/" + "stats_" + now + "_mod_" +  id_module + ".csv");
        fw.write("vmax, vmin, vavg, vdelta\n");

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

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd_HH.mm.ss");
        String now = dtf.format(LocalDateTime.now());
        FileWriter fw = new FileWriter(System.getProperty("user.dir") + "/CSV_out/" + "data_" + now + "_mod_" + id_module + ".csv");

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

        CSVReader[] readers = new CSVReader[paths.length];

        for (int i = 0; i < readers.length; i++)
            readers[i] = new CSVReader(paths[i], sampletime);

        this.sampletime = sampletime;
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
