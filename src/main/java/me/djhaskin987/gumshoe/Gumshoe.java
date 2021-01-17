package me.djhaskin987.gumshoe;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** This looks at configuration files, the environment, and the JVM properties
 * in addition to the given command line arguments and compiles from them
 * a succinct set of properties, captured in an instance of the java.util.Properties
 * class.
 * 
 * @since 1.0.0
 */
public class Gumshoe
{
    public class GumshoeException extends Exception {
        public GumshoeException(String message) {
            super(message);
        }
    }
    /** This is the normal way to create an instance of Gumshoe.
     *  @since 1.0.0
     *  @return A new Gumshoe instance that is capable of interacting
     *          with the file system and system environment.
     */
    public static Gumshoe createDefaultInstance() throws IOException {
        return new Gumshoe(new ConfigFinder() {
            public boolean pathExists(String path) {
                return Files.exists(Path.of(path));
            }
            public InputStream getInputStream(String path) throws IOException {
                return Files.newInputStream(Path.of(path));
            } 
        },
        System.getProperties(),
        System.getenv());
    }
    
    private ConfigFinder finder;
    private Properties systemProperties;
    private Map<String, String> environment;

    protected Gumshoe(
            ConfigFinder finder,
            Properties systemProperties,
            Map<String,String> environment) {
        this.finder = finder;
        this.systemProperties = systemProperties;
        this.environment = environment;
    }
    
    private void addFileIfExists(
            Properties props,
            String path) throws IOException {
        if (this.finder.pathExists(path)) {
            Properties intermediate = new Properties();
            InputStream configFile = this.finder.getInputStream(path);
            InputStreamReader utfReader =
                new InputStreamReader(configFile, Charset.forName("UTF-8"));
            intermediate.load(utfReader);
            props.putAll(intermediate);
        }
    }

    private void gatherConfigFiles(
            Properties results,
            String programName
            ) throws IOException {
        List<String> candidates = new ArrayList<String>();
        String predefinedLocations = this.environment.get(programName.toUpperCase() + "_CONFIG_FILES");
        if (predefinedLocations != null) {
            for (String location : predefinedLocations.split(",")) {
                candidates.add(location);
            }
        } else {
            String nextValue = this.environment.get("AppData");
            if (nextValue != null && !nextValue.equals("")) {
                candidates.add(String.join(this.systemProperties.getProperty("file.separator"),
                    nextValue,
                    programName,
                    "config.properties"));
            }
            nextValue = this.environment.get("XDG_CONFIG_HOME");
            if (nextValue != null && !nextValue.equals("")) {
                candidates.add(String.join(this.systemProperties.getProperty("file.separator"),
                    nextValue,
                    programName,
                    "config.properties"));
            }
            nextValue = this.environment.get("HOME");
            if (nextValue == null) {
                nextValue = this.systemProperties.getProperty("user.home");
            }
            if (nextValue != null && !nextValue.equals("")) {
                candidates.add(String.join(this.systemProperties.getProperty("file.separator"),
                    nextValue,
                    "." + programName,
                    "config.properties"));
            }
            nextValue = this.systemProperties.getProperty("user.dir");
            if (nextValue != null && !nextValue.equals("")) {
                candidates.add(String.join(this.systemProperties.getProperty("file.separator"),
                    nextValue,
                    "." + programName,
                    "config.properties"));
            }
        }
        for (String candidate : candidates) {
            addFileIfExists(results, candidate);
        }
    }
                    
    private void gatherEnvironment(
        Properties results,
        String programName) {
        Pattern findProgramName = Pattern.compile(
            "^" + 
            programName.toUpperCase() + 
            "_(.*)$");
        this.environment.forEach( (String key, String value) -> {
            Matcher inspect = findProgramName.matcher(key);
            if (inspect.matches()) {
                String propertyName = inspect.group(1).toLowerCase().replace('_','.');
                results.setProperty(propertyName, value);
            }
        });
            
    }

    private GumshoeReturn gatherArguments(
        Properties results,
        String programName,
        Map<String, String> aliases,
        String [] arguments) 
        throws GumshoeException {
        Pattern findParts = Pattern.compile("^--([^-]+)-(.+)$");
        int index = 0;
        List<String> unusedArguments = new ArrayList<String>();
        while (index < arguments.length) {
            String argument = arguments[index];
            String usedArgument = aliases.get(argument);
            if (usedArgument == null) { 
                usedArgument = argument;
            }
            Matcher inspect = findParts.matcher(usedArgument);
            if (inspect.matches()) {
                String verb = inspect.group(1);
                String property = inspect.group(2).toLowerCase().replace('-', '.');
                if (verb.equals("enable")) {
                    results.setProperty(property, "true");
                } else if (verb.equals("disable")) {
                    results.setProperty(property, "false");
                } else if (verb.equals("reset")) {
                    results.remove(property);
                } else {
                    String nextArgument;
                    if (index + 1 >= arguments.length) {
                        throw new GumshoeException("Not enough arguments.");
                    } else {
                        index = index + 1;
                        nextArgument = arguments[index];
                    }
                    if (verb.equals("set")) {
                        results.setProperty(property, nextArgument);
                    } else if (verb.equals("add")) {
                        String priorProperty = results.getProperty(property);
                        if (priorProperty == null) {
                            results.setProperty(property, nextArgument);
                        } else {
                            results.setProperty(property,
                            String.join("," , priorProperty, nextArgument));
                        }
                    }
                }
            } else {
                unusedArguments.add(usedArgument);
            }
        }
        return GumshoeReturn.createInstance(
            unusedArguments,
            results
        );
    }
  
    /** This is the main function of Gumshoe. It gathers information
     *  from the System properties, environment, configuration files,
     *  and command line arguments and produces a return value from
     *  which a single merged set of properties may be obtained.
     * 
     * @param programName The name of the program that is using this library.
     * @param aliases a list of search-and-replace aliases that will be used
     * as shortnames for command line arguments. Any time one of the keys
     * in this map is encountered in the arguments, it is replaced with
     * that entry's value.
     * @param arguments the arguments given to this tool over the command line.
     * @throws IOException, Gumshoe.GumshoeException
     * @return a GumshoeReturn object, from which the unparsed arguments and
     * the merged Properties instance can be obtained.
     * @since 1.0.0
     */
    public GumshoeReturn gatherOptions(
            String programName,
            Map<String,String> aliases,
            String [] arguments)
        throws IOException, GumshoeException
    {
        Properties results = new Properties();
        gatherConfigFiles(results, programName);
        gatherEnvironment(results, programName);
        return gatherArguments(results, programName, aliases, arguments);
    }
}
