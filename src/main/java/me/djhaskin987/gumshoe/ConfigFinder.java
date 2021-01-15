package me.djhaskin987.gumshoe;

public interface ConfigFinder {
    public boolean pathExists(String path);
    public InputStream getInputStream(String path);
}
