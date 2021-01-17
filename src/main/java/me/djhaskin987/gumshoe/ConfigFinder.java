package me.djhaskin987.gumshoe;

import java.nio.file.Path;
import java.io.InputStream;
import java.io.IOException;

public interface ConfigFinder {
    public boolean pathExists(String path);
    public InputStream getInputStream(String path) throws IOException;
}
