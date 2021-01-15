package me.djhaskin987.gumshoe;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

/**
 * 
 *
 */
public class Gumshoe
{
    private static void addFileIfExists(
            ConfigFinder finder,
            Properties props,
            String path) {
        if (finder.exists(path)) {
            Properties intermediate = new Properties();
            FileInputStream configFile = finder.getInputStream(path);
            InputStreamReader utfReader =
                new InputStreamReader(configFile, Charset.forName("UTF-8"));
            intermediate.load(utfReader);
            props.putAll(intermediate);
        }
    }

    private static void gatherConfigFiles(
            Properties results,
            ConfigFinder finder,
            String programName,
            Properties systemProperties,
            Map<String,String> environment
            ) {
        String operatingSystem = systemProperties.getProperty("os.name").toLowerCase();
        String nextValue = environment.get("AppData");
        if (nextValue != null && !nextValue.equals("")) {
            addFileIfExists(
                    result,
                    String.join(
                        systemProperties.getProperty("file.separator"),
                        nextValue,
                        programName,
                        "config.properties"))
        }
        nextValue = environment.get("XDG_CONFIG_HOME")
        if (nextValue != null && !nextValue.equals("")) {
            addFileIfExists(
                    result,
                    String.join(
                        systemProperties.getProperty("file.separator"),
                        nextValue,
                        programName,
                        "config.properties"))
        }
        nextValue = environment.get("HOME");
        if (nextValue != null && !nextValue.equals("")) {
            addFileIfExists(
                    result,
                    String.join(
                        systemProperties.getProperty("file.separator"),
                        nextValue,
                        "." + programName,
                        "config.properties"))
        }
        nextValue = systemProperties.get("user.home");
        if (nextValue != null && !nextValue.equals("")) {
            addFileIfExists(
                    result,
                    String.join(
                        systemProperties.getProperty("file.separator"),
                        nextValue,
                        "." + programName,
                        "config.properties"));
        }
    }

    public static Properties gatherOptions(
            String programName,
            ConfigFinder finder,
            Properties systemProperties,
            Map<String,String> environment,
            String [] args,
            )
    {
        Properties results = new Properties();
        gatherConfigFiles(results, finder, programName,
                systemProperties, environment);
        gatherEnvironment(results, finder, programName,
                environment);
        gatherCommands(
            

        System.out.println( "Hello World!" );
    }
}
