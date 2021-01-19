package io.github.djhaskin987.gumshoe;

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

/**
 * This looks at configuration files, the environment, and the JVM properties in
 * addition to the given command line arguments and compiles from them a
 * succinct set of properties, captured in an instance of the
 * java.util.Properties class.
 *
 * @since 1.0.0
 */
public class Gumshoe {

    /**
     * This exception is used whenever Gumshoe has a problem.
     */
    public class GumshoeException extends Exception {
        /**
         * Included to avoid compiler errors.
         */
        private static final long serialVersionUID = 5197416338850699110L;

        /**
         * Default constructor, allowing the user to specify a message for this
         * exception, simply calls <code>super</code> with it.
         *
         * @param message
         *                    The message to be displayed with this exception.
         */
        public GumshoeException(final String message) {
            super(message);
        }
    }

    /**
     * This is the normal way to create an instance of Gumshoe.
     *
     * @since 1.0.0
     * @return A new Gumshoe instance that is capable of interacting with the
     *         file system and system environment.
     */
    public static Gumshoe createDefaultInstance() {
        return new Gumshoe(new ConfigFinder() {
            public boolean pathExists(final String path) {
                return Files.exists(Path.of(path));
            }

            public InputStream getInputStream(final String path)
                    throws IOException {
                return Files.newInputStream(Path.of(path));
            }
        }, System.getProperties(), System.getenv());
    }

    /**
     * Object that helps find and open configuration files. It is included in
     * the design of Gumshoe for dependency injection purposes.
     */
    private ConfigFinder finder;
    /**
     * Intended to represent the System properties, but included here as a
     * member for dependency injection purposes.
     */
    private Properties systemProperties;
    /**
     * Intended to represent the System environment, but included here as a
     * member for dependency injection purposes.
     */
    private Map<String, String> environment;

    /**
     * Constructor used to create Gumshoe internally, intended to be used by
     * tests and the method <code>createDefaultInstance</code>.
     *
     * @param givenFinder
     *                                  the ConfigFinder object to be used.
     * @param givenSystemProperties
     *                                  the System properties.
     * @param givenEnvironment
     *                                  the environment to use when finding
     *                                  config files and when looking for
     *                                  properties to add to the returned
     *                                  properties object from the function
     *                                  <code>gatherOptions</code>.
     */
    protected Gumshoe(final ConfigFinder givenFinder,
            final Properties givenSystemProperties,
            final Map<String, String> givenEnvironment) {
        finder = givenFinder;
        systemProperties = givenSystemProperties;
        environment = givenEnvironment;
    }

    /**
     * Check to see if a config file exists, and merge its properties into the
     * properties object <code>props</code> if it does.
     *
     * @param props
     *                  the properties object that is being built.
     * @param path
     *                  the path to the configuration file to check for its
     *                  existence
     * @throws IOException
     *                         An IOException is thrown if the file could not be
     *                         opened.
     */
    private void addFileIfExists(final Properties props, final String path)
            throws IOException {
        if (this.finder.pathExists(path)) {
            Properties intermediate = new Properties();
            InputStream configFile = this.finder.getInputStream(path);
            InputStreamReader utfReader = new InputStreamReader(configFile,
                    Charset.forName("UTF-8"));
            intermediate.load(utfReader);
            props.putAll(intermediate);
        }
    }

    /**
     * Gathers all properties from all configuration files. Looks in the
     * contents of the variable <code>&lt;PROGRAM_NAME&gt;_CONFIG_FILES</code>
     * or, if that is unset, looks in default places depending on the settings
     * in the environment:
     *
     * * If <code>AppData</code> is set in the environment, it looks under
     * <code>%APPDATA%\\&lt;programName&gt;\\config.properties</code>
     *
     * * If <code>XDG_CONFIG_HOME</code> is set in the environment, it looks
     * under
     * <code>${XDG_CONFIG_HOME}/&lt;programName&gt;/config.properties</code>
     *
     * * If <code>HOME</code> is set in the environment, it looks under
     * <code>${HOME}/.&lt;programName&gt;/config.properties</code>
     *
     * * If <code>user.home</code> is set in the System properties but no
     * <code>HOME</code> variable is set in the environment, it looks there
     * instead.
     *
     * @param results
     *                        the Properties object being built up and having
     *                        settings merged into it.
     *
     * @param programName
     *                        the name of the program that is calling Gumshoe.
     *
     * @throws IOException
     *                         IOException is thrown if opening a configuration
     *                         file fails for some reason.
     */

    private void gatherConfigFiles(final Properties results,
            final String programName) throws IOException {

        List<String> candidates = new ArrayList<String>();
        String predefinedLocations = this.environment
                .get(programName.toUpperCase() + "_CONFIG_FILES");
        if (predefinedLocations != null) {
            for (String location : predefinedLocations.split(",")) {
                candidates.add(location);
            }
        } else {
            if (this.systemProperties.get("file.separator") == null) {
                return;
            }
            String nextValue = this.environment.get("AppData");
            if (nextValue != null && !nextValue.equals("")) {
                candidates.add(String.join(
                        this.systemProperties.getProperty("file.separator"),
                        nextValue, programName, "config.properties"));
            }
            nextValue = this.environment.get("XDG_CONFIG_HOME");
            if (nextValue != null && !nextValue.equals("")) {
                candidates.add(String.join(
                        this.systemProperties.getProperty("file.separator"),
                        nextValue, programName, "config.properties"));
            }
            nextValue = this.environment.get("HOME");
            if (nextValue == null) {
                nextValue = this.systemProperties.getProperty("user.home");
            }
            if (nextValue != null && !nextValue.equals("")) {
                candidates.add(String.join(
                        this.systemProperties.getProperty("file.separator"),
                        nextValue, "." + programName, "config.properties"));
            }
            nextValue = this.systemProperties.getProperty("user.dir");
            if (nextValue != null && !nextValue.equals("")) {
                candidates.add(String.join(
                        this.systemProperties.getProperty("file.separator"),
                        nextValue, "." + programName, "config.properties"));
            }
        }
        for (String candidate : candidates) {
            addFileIfExists(results, candidate);
        }
    }

    /**
     * Gather properties from the environment and merge them into the properties
     * object being built.
     *
     * This method looks for environment variables of the form
     * <code>&lt;PROGRAMNAME&gt;_PROPERTY_NAME</code> and takes its value,
     * setting a property named <code>property.name</code> in the properties
     * object to the value of the found variable.
     *
     * @param results
     *                        the Properties object being built.
     * @param programName
     *                        the name of the program calling Gumshoe.
     */

    private void gatherEnvironment(final Properties results,
            final String programName) {
        Pattern findProgramName = Pattern
                .compile("^" + programName.toUpperCase() + "_(.*)$");
        this.environment.forEach((String key, String value) -> {
            Matcher inspect = findProgramName.matcher(key);
            if (inspect.matches()) {
                String propertyName = inspect.group(1).toLowerCase()
                        .replace('_', '.');
                results.setProperty(propertyName, value);
            }
        });

    }

    /**
     * Gather arguments from the command line and merge the results from them
     * into the properties object being built.
     *
     * This method looks for arguments of different forms and based on what it
     * finds it sets properties in the Properties object.
     *
     * * When it sees arguments like
     * <code>--set-property-name &lt;value&gt;</code> it sets
     * <code>property.name</code> in the properties object to <code>value</code>
     *
     * * When it sees arguments like <code>--enable-property-name</code> it sets
     * the property <code>property.name</code> to <code>true</code>
     *
     * * When it sees arguments like <code>--disable-property-name</code> it
     * sets the property <code>property.name</code> to <code>false</code>
     *
     * * When it sees arguments like
     * <code>--add-property-name &lt;value&gt;</code> it sets
     * <code>property.name</code> in the properties object to <code>value</code>
     * if it doesn't exist in the map, or adds its value to whatever is already
     * is there, separated by a comma (<code>,</code>)
     *
     * * When it sees arguments like <code>--reset-poperty-name</code> it
     * removes <code>property.name</code> from the properties object
     *
     * @param results
     *                        the Properties object being built.
     * @param programName
     *                        the name of the program calling Gumshoe.
     * @param aliases
     *                        Aliases specified by the calling program. Any
     *                        string in the command line matching one of the
     *                        keys in this map is replaced by its corresponding
     *                        value before it is examined by the above rules.
     * @param arguments
     *                        the command line arguments to be examined.
     * @throws GumshoeException
     *                              GumshoeException is thrown when the command
     *                              line parsing fails for some reason.
     * @return a GumshoeReturn object containing the finished properties object
     *         and any unparsed arguments from the command line.
     */
    private GumshoeReturn gatherArguments(final Properties results,
            final String programName, final Map<String, String> aliases,
            final String[] arguments) throws GumshoeException {
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
                String property = inspect.group(2).toLowerCase().replace('-',
                        '.');
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
                            results.setProperty(property, String.join(",",
                                    priorProperty, nextArgument));
                        }
                    }
                }
            } else {
                unusedArguments.add(usedArgument);
            }
            index = index + 1;
        }
        return GumshoeReturn.createInstance(unusedArguments, results);
    }

    /**
     * <p>
     * This is the main function of Gumshoe. It gathers information from the
     * System properties, environment, configuration files, and command line
     * arguments and produces a return value from which a single merged set of
     * properties may be obtained.
     * </p>
     *
     * <p>
     * First, it gathers all properties from all configuration files. Looks in
     * the contents of the variable
     * <code>&lt;PROGRAMNAME&gt;_CONFIG_FILES</code> or, if that is unset, looks
     * in default places depending on the settings in the environment:
     * </p>
     *
     * <ul>
     * <li>If <code>AppData</code> is set in the environment, it looks under
     * <code>%APPDATA%\\&lt;programName&gt;\\config.properties</code></li>
     *
     * <li>If <code>XDG_CONFIG_HOME</code> is set in the environment, it looks
     * under
     * <code>${XDG_CONFIG_HOME}/&lt;progName&gt;/config.properties</code></li>
     *
     * <li>If <code>HOME</code> is set in the environment, it looks under
     * <code>${HOME}/.&lt;programName&gt;/config.properties</code></li>
     *
     * <li>If <code>user.home</code> is set in the System properties but no
     * <code>HOME</code> variable is set in the environment, it looks there
     * instead.</li>
     * </ul>
     *
     * <p>
     * Next, it gathers properties from the environment and merges them into the
     * properties object that will be returned.
     * </p>
     *
     * <p>
     * It looks for environment variables of the form
     * <code>&lt;PROGRAMNAME&gt;_PROPERTY_NAME</code> and takes its value,
     * setting a property named <code>property.name</code> in the properties
     * object to the value of the found variable.
     * </p>
     *
     * <p>
     * Finally, this method gathers arguments from the command line and merge
     * the results from them into the properties object being built.
     * </p>
     *
     * <p>
     * This method looks for arguments of different forms and based on what it
     * finds it sets properties in the Properties object.
     * </p>
     *
     * <ul>
     * <li>When it sees arguments like
     * <code>--set-property-name &lt;value&gt;</code> it sets
     * <code>property.name</code> in the properties object to <code>value</code>
     * </li>
     *
     * <li>When it sees arguments like <code>--enable-property-name</code> it
     * sets the property <code>property.name</code> to <code>true</code></li>
     *
     * <li>When it sees arguments like <code>--disable-property-name</code> it
     * sets the property <code>property.name</code> to <code>false</code></li>
     *
     * <li>When it sees arguments like
     * <code>--add-property-name &lt;value&gt;</code> it sets
     * <code>property.name</code> in the properties object to <code>value</code>
     * if it doesn't exist in the map, or adds its value to whatever is already
     * is there, separated by a comma (<code>,</code>)</li>
     *
     * <li>When it sees arguments like <code>--reset-poperty-name</code> it
     * removes <code>property.name</code> from the properties object.</li>
     * </ul>
     *
     * Then, this method returns the built <code>Properties</code> object
     * together with any arguments that were not parsed and houses them in a
     * <code>GumshoeReturn</code> object.
     *
     * @param programName
     *                        The name of the program that is using this
     *                        library.
     * @param aliases
     *                        a list of search-and-replace aliases that will be
     *                        used as shortnames for command line arguments. Any
     *                        time one of the keys in this map is encountered in
     *                        the arguments, it is replaced with that entry's
     *                        value.
     * @param arguments
     *                        the arguments given to this tool over the command
     *                        line.
     * @throws IOException
     *                              throws IOException if a configuration file
     *                              could not be opened.
     * @throws GumshoeException
     *                              throws GumshoeException if the command line
     *                              could not be parsed.
     *
     * @return a GumshoeReturn object, from which the unparsed arguments and the
     *         merged Properties instance can be obtained.
     * @since 1.0.0
     */
    public GumshoeReturn gatherOptions(final String programName,
            final Map<String, String> aliases, final String[] arguments)
            throws IOException, GumshoeException {
        Properties results = new Properties();
        gatherConfigFiles(results, programName);
        gatherEnvironment(results, programName);
        return gatherArguments(results, programName, aliases, arguments);
    }
}
