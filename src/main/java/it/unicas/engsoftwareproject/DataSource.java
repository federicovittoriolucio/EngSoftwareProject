package it.unicas.engsoftwareproject;

import java.io.FileNotFoundException;

/**
 * Interface that requires implementation based on the data source type.
 * @see CSVReader
 */
public interface DataSource
{
    /**
     * Starts the acquisition of data from the source.
     * @throws FileNotFoundException Exception thrown if the source cannot be found.
     */
    void start() throws FileNotFoundException;

    /**
     * Updates the acquisition of data from the source.
     */
    void update();

    /**
     * Pauses the acquisition of data from the source.
     */
    void pause();

    /**
     * Resumes the acquisition of data from the source.
     */
    void resume();

    /**
     * Stops the acquisition of data from the source.
     */
    void stop();
}
