#!/bin/sh

# Exit on error
set -e

DATA_HOME=`dirname $0`/../vendor
GEO_HOME=$DATA_HOME/geonames
OPENNLP_LIB=$DATA_HOME/opennlp
TLD=$DATA_HOME/tld

mkdir -p $GEO_HOME $OPENNLP_LIB $TLD

curl https://data.iana.org/TLD/tlds-alpha-by-domain.txt -o $TLD/tlds-alpha-by-domain.txt

# Geo Names
curl http://download.geonames.org/export/dump/cities1000.zip -o $GEO_HOME/cities1000.zip
unzip -fo $GEO_HOME/cities1000.zip cities1000.txt -d $GEO_HOME
curl http://download.geonames.org/export/dump/cities15000.zip -o $GEO_HOME/cities15000.zip
unzip -fo $GEO_HOME/cities15000.zip cities15000.txt -d $GEO_HOME
curl http://download.geonames.org/export/dump/countryInfo.txt -o $GEO_HOME/countryInfo.txt
sed -i '/^#/d' $GEO_HOME/countryInfo.txt

# Remove downloaded zip files
rm -f $GEO_HOME/cities1*zip

curl https://raw.githubusercontent.com/lukes/ISO-3166-Countries-with-Regional-Codes/master/all/all.json -o $GEO_HOME/ISO-3166-countries-with-regional-codes.json

# Models
curl http://opennlp.sourceforge.net/models-1.5/da-pos-maxent.bin -o $OPENNLP_LIB/da-pos-maxent.bin
curl http://opennlp.sourceforge.net/models-1.5/da-pos-perceptron.bin -o $OPENNLP_LIB/da-pos-perceptron.bin
curl http://opennlp.sourceforge.net/models-1.5/da-sent.bin -o $OPENNLP_LIB/da-sent.bin
curl http://opennlp.sourceforge.net/models-1.5/da-token.bin -o $OPENNLP_LIB/da-token.bin
curl http://opennlp.sourceforge.net/models-1.5/de-pos-maxent.bin -o $OPENNLP_LIB/de-pos-maxent.bin
curl http://opennlp.sourceforge.net/models-1.5/de-pos-perceptron.bin -o $OPENNLP_LIB/de-pos-perceptron.bin
curl http://opennlp.sourceforge.net/models-1.5/de-sent.bin -o $OPENNLP_LIB/de-sent.bin
curl http://opennlp.sourceforge.net/models-1.5/de-token.bin -o $OPENNLP_LIB/de-token.bin
curl http://opennlp.sourceforge.net/models-1.5/en-chunker.bin -o $OPENNLP_LIB/en-chunker.bin
curl http://opennlp.sourceforge.net/models-1.5/en-ner-date.bin -o $OPENNLP_LIB/en-ner-date.bin
curl http://opennlp.sourceforge.net/models-1.5/en-ner-location.bin -o $OPENNLP_LIB/en-ner-location.bin
curl http://opennlp.sourceforge.net/models-1.5/en-ner-money.bin -o $OPENNLP_LIB/en-ner-money.bin
curl http://opennlp.sourceforge.net/models-1.5/en-ner-organization.bin -o $OPENNLP_LIB/en-ner-organization.bin
curl http://opennlp.sourceforge.net/models-1.5/en-ner-percentage.bin -o $OPENNLP_LIB/en-ner-percentage.bin
curl http://opennlp.sourceforge.net/models-1.5/en-ner-person.bin -o $OPENNLP_LIB/en-ner-person.bin
curl http://opennlp.sourceforge.net/models-1.5/en-ner-time.bin -o $OPENNLP_LIB/en-ner-time.bin
curl http://opennlp.sourceforge.net/models-1.5/en-ner-vulnerability.bin -o $OPENNLP_LIB/en-ner-vulnerability.bin
curl http://opennlp.sourceforge.net/models-1.5/en-parser-chunking.bin -o $OPENNLP_LIB/en-parser-chunking.bin
curl http://opennlp.sourceforge.net/models-1.5/en-pos-maxent.bin -o $OPENNLP_LIB/en-pos-maxent.bin
curl http://opennlp.sourceforge.net/models-1.5/en-pos-perceptron.bin -o $OPENNLP_LIB/en-pos-perceptron.bin
curl http://opennlp.sourceforge.net/models-1.5/en-sent.bin -o $OPENNLP_LIB/en-sent.bin
curl http://opennlp.sourceforge.net/models-1.5/en-token.bin -o $OPENNLP_LIB/en-token.bin
curl http://opennlp.sourceforge.net/models-1.5/es-ner-location.bin -o $OPENNLP_LIB/es-ner-location.bin
curl http://opennlp.sourceforge.net/models-1.5/es-ner-misc.bin -o $OPENNLP_LIB/es-ner-misc.bin
curl http://opennlp.sourceforge.net/models-1.5/es-ner-organization.bin -o $OPENNLP_LIB/es-ner-organization.bin
curl http://opennlp.sourceforge.net/models-1.5/es-ner-person.bin -o $OPENNLP_LIB/es-ner-person.bin
curl http://opennlp.sourceforge.net/models-1.5/nl-ner-location.bin -o $OPENNLP_LIB/nl-ner-location.bin
curl http://opennlp.sourceforge.net/models-1.5/nl-ner-misc.bin -o $OPENNLP_LIB/nl-ner-misc.bin
curl http://opennlp.sourceforge.net/models-1.5/nl-ner-organization.bin -o $OPENNLP_LIB/nl-ner-organization.bin
curl http://opennlp.sourceforge.net/models-1.5/nl-ner-person.bin -o $OPENNLP_LIB/nl-ner-person.bin
curl http://opennlp.sourceforge.net/models-1.5/nl-pos-maxent.bin -o $OPENNLP_LIB/nl-pos-maxent.bin
curl http://opennlp.sourceforge.net/models-1.5/nl-pos-perceptron.bin -o $OPENNLP_LIB/nl-pos-perceptron.bin
curl http://opennlp.sourceforge.net/models-1.5/nl-sent.bin -o $OPENNLP_LIB/nl-sent.bin
curl http://opennlp.sourceforge.net/models-1.5/nl-token.bin -o $OPENNLP_LIB/nl-token.bin
curl http://opennlp.sourceforge.net/models-1.5/pt-pos-maxent.bin -o $OPENNLP_LIB/pt-pos-maxent.bin
curl http://opennlp.sourceforge.net/models-1.5/pt-pos-perceptron.bin -o $OPENNLP_LIB/pt-pos-perceptron.bin
curl http://opennlp.sourceforge.net/models-1.5/pt-sent.bin -o $OPENNLP_LIB/pt-sent.bin
curl http://opennlp.sourceforge.net/models-1.5/pt-token.bin -o $OPENNLP_LIB/pt-token.bin
curl http://opennlp.sourceforge.net/models-1.5/se-pos-maxent.bin -o $OPENNLP_LIB/se-pos-maxent.bin
curl http://opennlp.sourceforge.net/models-1.5/se-pos-perceptron.bin -o $OPENNLP_LIB/se-pos-perceptron.bin
curl http://opennlp.sourceforge.net/models-1.5/se-sent.bin -o $OPENNLP_LIB/se-sent.bin
curl http://opennlp.sourceforge.net/models-1.5/se-token.bin -o $OPENNLP_LIB/se-token.bin
