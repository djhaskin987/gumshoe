# gumshoe
Grabs all available configuration.

This is a Java library that grabs configuration information, environment
variables, and command line and produces a `java.util.Properties` instance
to be consumed by a java program.

## Quickstart

Maven:

```
<dependency>
  <groupId>io.github.djhaskin987</groupId>
  <artifactId>gumshoe</artifactId>
  <version>1.0.0</version>
</dependency>
```

Usage:

```java
import io.github.djhaskin987.gumshoe.Gumshoe;
import io.github.djhaskin987.gumshoe.GumshoeReturn;

class MyProgram {

    public static void main(String []args) {
        Gumshoe parser = Gumshoe.createInstance();
        GumshoeReturn configResults = parser.gatherOptions(
          "myprogram",
          Map.of("-s", "--enable-short-names",
                 "-a", "--set-alpha-wolf"
                  ),
          args);
        Properties options = parser.getOptionsMap();
        List<String> otherArgs = parser.getUnusedArguments();
        // ...
    }
}
```

## More docs!

Javadocs can be found on [javadoc.io](https://javadoc.io/doc/io.github.djhaskin987/gumshoe).
