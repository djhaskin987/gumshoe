# gumshoe
Grabs all available configuration.

This is a Java library that grabs configuration information, environment
variables, and command line and produces a `java.util.Properties` instance
to be consumed by a java program.


This makes it *dead simple* to get options implemented in your program. Just
use Gumshoe, and you don't even have to define CLI options -- it's easy for
your callers to simply set properties in the Properties instance using the
command line.
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
        System.out.println(options.getProperty("alpha.wolf");
    }
}
```

Your callers would be able to set configuration in, e.g. `~/.myprogram/config.properties` (among other places):

```
alpha.wolf="harry"
```

In Environment Variables:

```
export MYPROGRAM_ALPHA_WOLF="larry"
```

Or, on the command line:

```
myprogram --set-alpha-wolf "nod"
```

Or, since an alias `-s` was specified in the above code snippet, this is equivalent:

```
myprogram -s "nod"
```

Command line wins over environment variables, and environment variables wins
over configuration files. For example, if all of the above were defined, the
output of the program would be "nod". If no argumens on the command line were
given, it would be "larry" and if no environment variables were defined, the
result would be "harry".

## More docs!

Javadocs can be found on [javadoc.io](https://javadoc.io/doc/io.github.djhaskin987/gumshoe).
