package io.github.djhaskin987.gumshoe;

import java.util.List;
import java.util.Properties;

/**
 * A POJO class that houses the return values of Gumshoe's
 * <code>gatherOptions</code> function.
 */
public final class GumshoeReturn {

    /**
     * Represents the arguments that were not parsed by Gumshoe.
     */
    private List<String> unusedArguments;
    /**
     * Represents the options that were gathered by Gumshoe.
     */
    private Properties optionsMap;

    /**
     * Instance factory for the <code>GumshoeReturn</code> class.
     *
     * @param unusedArguments
     *                            the arguments unused by Gumshoe.
     * @param optionsMap
     *                            the options found by Gumshoe.
     * @return a GumshoeReturn object.
     */
    protected static GumshoeReturn createInstance(
            final List<String> unusedArguments, final Properties optionsMap) {
        return new GumshoeReturn(unusedArguments, optionsMap);
    }

    /**
     * Constructor for the <code>GumshoeReturn</code> class.
     *
     * @param givenUnusedArguments
     *                                 the arguments unused by Gumshoe.
     * @param givenOptionsMap
     *                                 the options found by Gumshoe.
     */
    private GumshoeReturn(final List<String> givenUnusedArguments,
            final Properties givenOptionsMap) {
        unusedArguments = givenUnusedArguments;
        optionsMap = givenOptionsMap;
    }

    /**
     * Getter for the unused arguments.
     *
     * @return the unused arguments.
     */
    public List<String> getUnusedArguments() {
        return this.unusedArguments;
    }

    /**
     * Getter for the options map.
     *
     * @return the options map.
     */
    public Properties getOptionsMap() {
        return this.optionsMap;
    }

    /**
     * Setter for the unused arguments.
     *
     * @param givenUnusedArguments
     *                                 the unused arguments.
     */
    protected void setUnusedArguments(final List<String> givenUnusedArguments) {
        unusedArguments = givenUnusedArguments;

    }

    /**
     * Setter for the options map.
     *
     * @param givenOptionsMap
     *                            the options map.
     */
    protected void setOptionsMap(final Properties givenOptionsMap) {
        optionsMap = givenOptionsMap;
    }
}
