# Java GNUCash

While there are few Java libraries out there for gnucash, as fas as I know, they don't use gnucash apis. This is understandable since gnucash APIs are implemented in C/C++ 
and rather painful to integrate with Java.

This library uses Java FFM API to generate bindings from gnucash API. These are the same APIs sample python code uses included with gnucash. We are generating full bindings for
all the APIs but work on Java Objects e.g. Account is ongoing. These functions are implemented as and when I need.

Functionality wise, Java code is at par with 

It has been tested on Ubuntu 24.04 and JDK 25 with gnucash version 5.5. To execute sample application follow following steps. Adjust LD_LIBRARY_PATH to whereever gnucash libraries are available on your environement.
```
cd java-gnucash
mvn package
LD_LIBRARY_PATH=/usr/lib/x86_64-linux-gnu/gnucash java --enable-native-access=ALL-UNNAMED -cp target/java-gnucash-1.0-SNAPSHOT.jar net.raohome.gnucash.sample.AccountTotals <path to gnucash file>
```

**As code is still in development phase, highly recommended to make sure you have adequate backup of required files or better make a copy of the file and work with the copy.**

There are few sample utilities built mostly based on my needs

1. AccountsForCommodity - Lists all the account where a commodity is used. I use to to find total balance of same mutual fund invested in multiple accounts.
2. UpdatePrices - Uses Twelve Data to get the latest prices for given stock/quotes. I've no relationship with Twelve Data
3. AccountTotals - Prints all the account totals
4. UpdatePaystub - Reads my paystub from PDF and creates appropriate splits.
5. UpdateMortgageRecord - Reads statement from my mortgage providers and creates appropriate splits.
