package me.djhaskin987.gumshoe;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class GumshoeTest {

    /**
     * Rigorous test.
     */
    @Test
    public void testHappyPath() {

        MockConfigFinder finder = MockConfigFinder.createInstance(Map.of(
                "home/.myprogram/config.properties", "a.b.c=true",
                "/a/b/c/config.properties", "a.b.c=false\na.b.d=animated"));
        Properties systemProperties = new Properties();
        systemProperties.putAll(Map.of("file.separator", "/"));
        Map<String, String> environment = Map.of("MYPROGRAM_A", "a good grade");
        Gumshoe testedInstance = new Gumshoe(finder, systemProperties,
                environment);
        GumshoeReturn result = null;
        try {
            result = testedInstance.gatherOptions("myprogram",
                    Map.of("-z", "--set-a-b-z"),
                    new String[] {"-z", "Lord Zed", "a", "b", "c" });
        } catch (IOException ioe) {
            fail("Couldn't open config files.");
        } catch (Gumshoe.GumshoeException gse) {
            fail("Command line could not be parsed.");
        } catch (Exception all) {
            fail("Some other error happened.");
        }
        assertNotEquals(result, null);
        Properties props = result.getOptionsMap();
        assertEquals(props.getProperty("a.b.c"), "false");
        assertEquals(props.getProperty("a.b.d"), "animated");
        assertEquals(props.getProperty("a"), "a good grade");
        assertEquals(props.getProperty("a.b.z"), "Lord Zed");
        List<String> unusedArgs = result.getUnusedArguments();
        assertArrayEquals(new String[] {"a", "b", "c" }, unusedArgs.toArray());
        assertTrue(true);
    }
}
