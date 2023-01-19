package it.unicas.engsoftwareproject;


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
    
}
