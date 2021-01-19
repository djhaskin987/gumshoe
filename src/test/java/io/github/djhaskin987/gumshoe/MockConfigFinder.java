package io.github.djhaskin987.gumshoe;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Mock ConfigFinder class for use in testing.
 */
public class MockConfigFinder implements ConfigFinder {

    /**
     * List of paths that were read, in the order that they were read.
     */
    private List<String> readPaths;
    /**
     * List of paths that were checked for existence, in the order they were
     * checked.
     */
    private List<String> existenceChecks;

    /**
     * Paths that should exist in the test, together with their contents.
     */
    private Map<String, String> pathContents;

    /**
     * Create a mock instance.
     *
     * @param givenPathContents
     *                              a list of paths that should exist in the
     *                              test, together with their contents.
     * @return a MockConfigFinder.
     */
    public static MockConfigFinder createInstance(
            final Map<String, String> givenPathContents) {
        return new MockConfigFinder(givenPathContents);

    }

    /**
     * Create a mock instance.
     *
     * @param givenPathContents
     *                              a list of paths that should exist in the
     *                              test, together with their contents.
     */
    protected MockConfigFinder(final Map<String, String> givenPathContents) {
        pathContents = givenPathContents;
        readPaths = new ArrayList<String>();
        existenceChecks = new ArrayList<String>();
    }

    /**
     * Returns StringIO input stream for the path.
     */
    @Override
    public InputStream getInputStream(final String path) throws IOException {
        readPaths.add(path);
        String contents = pathContents.get(path);
        if (contents == null) {
            throw new IOException("Path does not exist.");
        }

        return new ByteArrayInputStream(
                contents.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Checks to see if the path is in the set of paths which exist.
     */
    @Override
    public boolean pathExists(final String path) {
        existenceChecks.add(path);
        return pathContents.containsKey(path);
    }

    /**
     * Allows the tester to sense what files were checked for existence.
     *
     * @return a list of Paths the existence of which was checked.
     */
    public List<String> getExistenceChecks() {
        return existenceChecks;
    }

    /**
     * Allows the tester to sense what files were read.
     *
     * @return a list of paths that were read.
     */
    public List<String> getReadPaths() {
        return readPaths;
    }

}
