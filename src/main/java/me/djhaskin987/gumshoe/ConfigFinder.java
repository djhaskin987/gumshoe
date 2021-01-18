package me.djhaskin987.gumshoe;

import java.io.InputStream;
import java.io.IOException;

/**
 * This class is intended to be used for dependency injection purposes and test
 * facilitation of the main Gumshoe class.
 */
public interface ConfigFinder {
    /**
     * Check to see that a path exists on the file system.
     *
     * @param path
     *                 the path of the file to open.
     * @return whether or not the path exists.
     */
    boolean pathExists(String path);

    /**
     * Open the file and return an InputStream to it.
     *
     * @param path
     *                 the path of the file to open.
     * @return the <code>InputStream</code> to the open file.
     * @throws IOException
     *                         thrown if the file could not be opened.
     */
    InputStream getInputStream(String path) throws IOException;
}
