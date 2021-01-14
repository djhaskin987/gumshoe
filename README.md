# gumshoe
Grabs all available configuration.

This is a Java library that grabs configuration information, environment
variables, and command line and produces a map of strings to strings to be
consumed by the program.

## Config file format

```
# whole-line comments supported
# set a variable to something
a.b.c=asdf
# unset a variable, remove it from the map
^a.b.c
# define a variable, but don't set it to anything. Equivalent to a single
# equals sign
a.b.c
```

Merges the above configuration file from several configuration files, and the
environment:

```
PROGRAM_NAME_A_B_C=
PROGRAM_NAME_A_B_D=
```

Finally, with the command line:

```
--set-a-b-c <thing>     Sets a-b-c
--unset-a-b-c           Removes from map, resets
--flag-a-b-c            Sets a-b-c flag
```
