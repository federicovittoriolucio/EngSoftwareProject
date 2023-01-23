package it.unicas.engsoftwareproject.controller;

import it.unicas.engsoftwareproject.BMSMonitor;
import javafx.fxml.FXML;

public class MonitorController {

    @FXML
    protected void backToMenu()
    {
        BMSMonitor.stagelist.get(1).close();
        BMSMonitor.stagelist.remove(1);
        BMSMonitor.stagelist.get(0).show();
    }



}
