module it.unicas.engsoftwareproject {
    requires javafx.controls;
    requires javafx.fxml;


    opens it.unicas.engsoftwareproject to javafx.fxml;
    exports it.unicas.engsoftwareproject;
}