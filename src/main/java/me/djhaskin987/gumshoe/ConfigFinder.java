package me.djhaskin987.gumshoe;

import java.io.InputStream;

public interface ConfigFinder {
    public boolean pathExists(String path);
    public InputStream getInputStream(String path);
}
