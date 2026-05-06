# Java GNUCash

This library uses Java FFM API to generate bindings from gnucash API. There is a sample class net.raohome.gnucash.sample.AccountTotals that mimics the functionality of export_account_totals.py found in python/example_scripts of gnucash sources.

This code is still work in progress. While code generation does the complete code generation and functionality is available, Java wrapper classes and it's methods are implemented as and when need arises, mostly based on sample python scripts.

It has been tested on Ubuntu 24.04 and JDK 25. To execute sample application follow following steps. Adjust LD_LIBRARY_PATH to whereever gnucash libraries are available on your environement.
```
cd java-gnucash
mvn package
LD_LIBRARY_PATH=/usr/lib/x86_64-linux-gnu/gnucash java --enable-native-access=ALL-UNNAMED -cp target/java-gnucash-1.0-SNAPSHOT.jar net.raohome.gnucash.sample.AccountTotals <path to gnucash file>
```

As code is still in development phase, highly recommended to make sure you have adequate backup of required files or better make a copy of the file and work with the copy.
