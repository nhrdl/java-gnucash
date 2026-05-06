#!/bin/env bash

#Ensure jextract is on your path. jextract can be downloaded from
# https://jdk.java.net/jextract/

#sudo apt-get install libglib2.0-dev

cat <<EOF > /tmp/gnucash_files.h
/* Files used for generating java code */
#include <gnucash/Account.h>
#include <gnucash/FreqSpec.h>
#include <gnucash/Query.h>
#include <gnucash/QuickFill.h>
#include <gnucash/Recurrence.h>
#include <gnucash/SchedXaction.h>
#include <gnucash/Scrub2.h>
#include <gnucash/Scrub3.h>
#include <gnucash/ScrubBudget.h>
#include <gnucash/ScrubBusiness.h>
#include <gnucash/Scrub.h>
#include <gnucash/Split.h>
#include <gnucash/SX-book.h>
#include <gnucash/SX-ttinfo.h>
#include <gnucash/Transaction.h>
#include <gnucash/TransLog.h>
EOF

#Step 1, to create the dump of includes
jextract --dump-includes /tmp/all-includes `pkg-config --cflags glib-2.0` -I/usr/include/gnucash --header-class-name GCashBinding /tmp/gnucash_files.h

grep -e "gnucash\|GList\|GDate\|_GObjectClass\|_GObject\|GTypeClass\|_GSList\|_GTypeInstance" /tmp/all-includes > /tmp/gnucash.includes

jextract '@/tmp/gnucash.includes' --output "../java-gnucash/src/main/java" -t net.raohome.gnucash.gen  `pkg-config --cflags glib-2.0` -I/usr/include/gnucash -l libgnc-engine -l libgnc-core-utils  --header-class-name GNUCashBinding /tmp/gnucash_files.h 
