module it.unicas.engsoftwareproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires eu.hansolo.medusa;


    opens it.unicas.engsoftwareproject to javafx.fxml;
    exports it.unicas.engsoftwareproject;
    exports it.unicas.engsoftwareproject.controller;
    opens it.unicas.engsoftwareproject.controller to javafx.fxml;
}