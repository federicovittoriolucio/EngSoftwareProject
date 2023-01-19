package it.unicas.engsoftwareproject;


import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DataHandler {
    private static DataHandler instance = null;

    public static synchronized DataHandler getInstance(){
        if(instance == null)
            instance = new DataHandler();
        return instance;
    }

    final int CONST_NUMMODULES = 6;
    private int activemodules = 0;

    private Module[] modules = null;

    private DataHandler(){
        modules = new Module[CONST_NUMMODULES];
    }

    public void addModule(int numvoltsens, int numtempsens, boolean current, boolean faults, int id)
    {
        modules[activemodules] = new Module(numvoltsens, numtempsens, current, faults, id);
        activemodules++;
    }

    public void storeData(String[] splitline, int id_module)
    {
        modules[id_module].addRow(splitline);
    }

    public void writeStatsCSV(int id_module) throws IOException {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String now = dtf.format(LocalDateTime.now());
        FileWriter fw = new FileWriter(System.getProperty("user.dir") + "/CSV_out/" + "stats_" + id_module + "_" + now + ".csv");
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

    public void writeDataCSV(int id_module) throws IOException {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String now = dtf.format(LocalDateTime.now());
        FileWriter fw = new FileWriter(System.getProperty("user.dir") + "/CSV_out/" + "data_" + id_module + "_" + now + ".csv");

        for (int i = 0; i < modules[id_module].getNumRows(); i++) {

            Double[] datarow = modules[id_module].getDataRow(i);
            // todo: DA CONSIDERARE CASO IN CUI NON CI SIANO FAULTS
            String[] faultsrow = modules[id_module].getFaultsRow(i);

            for (int j = 0; j < datarow.length; j++) {
                fw.write(datarow[j].toString());
                fw.write(",");
            }
            for (int j = 0; j < faultsrow.length; j++) {
                fw.write(faultsrow[j]);
                if(j != faultsrow.length - 1)
                    fw.write(",");
            }
            fw.write("\n");
        }
        fw.close();
    }
}
