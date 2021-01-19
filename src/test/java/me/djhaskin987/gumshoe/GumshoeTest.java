package me.djhaskin987.gumshoe;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.HashMap;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class GumshoeTest {

        /**
         * This test was written to test whether or not the `--add-*` stuff
         * works.
         */
        @Test
        public void testAdd() {
                Map<String, String> mockConfigFiles = Map.of(
                                "/etc/myprogram/config.properties",
                                "do not read",
                                "/home/.myprogram/config.properties",
                                "a.b.c=true",
                                "/a/b/c/.myprogram/config.properties",
                                "a.b.c=false\na.b.d=animated");
                MockConfigFinder finder = MockConfigFinder
                                .createInstance(mockConfigFiles);
                Properties systemProperties = new Properties();
                systemProperties.putAll(Map.of("file.separator", "/",
                                "user.home", "/home", "user.dir", "/a/b/c"));
                Map<String, String> environment = Map.of("MYPROGRAM_A",
                                "a good grade");
                Gumshoe testedInstance = new Gumshoe(finder, systemProperties,
                                environment);
                GumshoeReturn result = null;
                try {
                        result = testedInstance.gatherOptions("myprogram",
                                        new HashMap<String, String>(),
                                        new String[] {"--add-a-b-d",
                                                        "vegitated",
                                                        "--reset-a-b-c",
                                                        "--add-a-b-c", "do",
                                                        "--add-a-b-c", "re",
                                                        "--add-a-b-c", "mi" });
                } catch (IOException ioe) {
                        Assert.fail("Couldn't open config files.");
                } catch (Gumshoe.GumshoeException gse) {
                        Assert.fail("Command line could not be parsed.");
                } catch (Exception all) {
                        Assert.fail("Some other error happened.");
                }
                Assert.assertNotEquals(result, null);
                Properties props = result.getOptionsMap();
                Assert.assertEquals("do,re,mi", props.getProperty("a.b.c"));
                Assert.assertEquals("animated,vegitated",
                                props.getProperty("a.b.d"));
        }

        /**
         * Rigorous test.
         */
        @Test
        public void testHappyPath() {
                Map<String, String> mockConfigFiles = Map.of(
                                "/etc/myprogram/config.properties",
                                "do not read",
                                "/home/.myprogram/config.properties",
                                "a.b.c=true",
                                "/a/b/c/.myprogram/config.properties",
                                "a.b.c=false\na.b.d=animated");
                MockConfigFinder finder = MockConfigFinder
                                .createInstance(mockConfigFiles);
                Properties systemProperties = new Properties();
                systemProperties.putAll(Map.of("file.separator", "/",
                                "user.home", "/home", "user.dir", "/a/b/c"));
                Map<String, String> environment = Map.of("MYPROGRAM_A",
                                "a good grade");
                Gumshoe testedInstance = new Gumshoe(finder, systemProperties,
                                environment);
                GumshoeReturn result = null;
                try {
                        result = testedInstance.gatherOptions("myprogram",
                                        Map.of("-z", "--set-a-b-z"),
                                        new String[] {"-z", "Lord Zed", "a",
                                                        "b", "c" });
                } catch (IOException ioe) {
                        Assert.fail("Couldn't open config files.");
                } catch (Gumshoe.GumshoeException gse) {
                        Assert.fail("Command line could not be parsed.");
                } catch (Exception all) {
                        Assert.fail("Some other error happened.");
                }
                Assert.assertNotEquals(result, null);
                Properties props = result.getOptionsMap();
                Assert.assertEquals("false", props.getProperty("a.b.c"));
                Assert.assertEquals("animated", props.getProperty("a.b.d"));
                Assert.assertEquals("a good grade", props.getProperty("a"));
                Assert.assertEquals("Lord Zed", props.getProperty("a.b.z"));
                List<String> unusedArgs = result.getUnusedArguments();
                Assert.assertArrayEquals(new String[] {"a", "b", "c" },
                                unusedArgs.toArray());
                Assert.assertTrue(true);
                Assert.assertArrayEquals(new String[] {
                                "/home/.myprogram/config.properties",
                                "/a/b/c/.myprogram/config.properties" },
                                finder.getReadPaths().toArray());
                Assert.assertArrayEquals(new String[] {
                                "/home/.myprogram/config.properties",
                                "/a/b/c/.myprogram/config.properties" },
                                finder.getExistenceChecks().toArray());

        }

        /**
         * This test was written to test that the CLI --reset thing works. But
         * also has other sundry assertions.
         */
        @Test
        public void testReset() {

                MockConfigFinder finder = MockConfigFinder
                                .createInstance(new HashMap<String, String>());
                Properties systemProperties = new Properties();
                systemProperties.putAll(Map.of("file.separator", "/",
                                "user.home", "/home", "user.dir", "/a/b/c"));
                Map<String, String> environment = Map.of("MYPROGRAM_A",
                                "a good grade");

                Gumshoe testedInstance = new Gumshoe(finder, systemProperties,
                                environment);
                GumshoeReturn result = null;
                try {
                        result = testedInstance.gatherOptions("myprogram",
                                        new HashMap<String, String>(),
                                        new String[] {"--reset-a" });
                } catch (IOException ioe) {
                        Assert.fail("Couldn't open config files, "
                                        + "but there were none, "
                                        + "so this makes no sense.");
                } catch (Gumshoe.GumshoeException gse) {
                        Assert.fail("Command line could not be parsed.");
                } catch (Exception all) {
                        Assert.fail("Some other error happened.");
                }

                Properties props = result.getOptionsMap();
                Assert.assertNull(props.getProperty("a"));

                Assert.assertArrayEquals(new String[] {},
                                finder.getReadPaths().toArray());
                Assert.assertArrayEquals(new String[] {
                                "/home/.myprogram/config.properties",
                                "/a/b/c/.myprogram/config.properties" },
                                finder.getExistenceChecks().toArray());
        }
}
