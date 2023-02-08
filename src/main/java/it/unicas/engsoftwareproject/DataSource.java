package it.unicas.engsoftwareproject;
import java.io.FileNotFoundException;
import java.util.Scanner;

public interface DataSource {
    void start() throws FileNotFoundException;
    void update();
    void pause();
    void resume();
    void stop();
}
