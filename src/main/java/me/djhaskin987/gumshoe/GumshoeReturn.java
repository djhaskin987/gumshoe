package me.djhaskin987.gumshoe;

import java.util.List;
import java.util.Properties;

public class GumshoeReturn {
    private List<String> unusedArguments;
    private Properties optionsMap;

    protected static GumshoeReturn createInstance(List<String> unusedArguments, Properties optionsMap) {
        return new GumshoeReturn(unusedArguments, optionsMap);
    }
    
    private GumshoeReturn(List<String> unusedArguments, Properties optionsMap) {
        this.unusedArguments = unusedArguments;
        this.optionsMap = optionsMap;
    }

    public List<String> getUnusedArguments() {
        return this.unusedArguments;
    }

    public Properties getOptionsMap() {
        return this.optionsMap;
    }

    protected void setUnusedArguments(List<String> unusedArguments) {
        this.unusedArguments = unusedArguments;
        
    }

    protected void setOptionsMap(Properties optionsMap) {
        this.optionsMap = optionsMap;
    }
}