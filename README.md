# scio-back

## Installation

### Beanstalkd

The scio platform uses the beanstalkd mq. This must be installed and running

### How-to

```bash
git clone https://github.com/mnemonic-no/act-scio.git
cd scio
cd etc
./geonames_download.sh
cd ..
cp etc/scio.ini /etc/scio.ini
cp etc/tlds-* /etc/.
mkdir -p /var/lib/scio-files
cp etc/ISO* etc/*cfg etc/*txt /var/lib/scio-files
mkdir -p /var/lib/opennlp_data
curl http://opennlp.sourceforge.net/models-1.5/da-pos-maxent.bin -o /var/lib/opennlp_data/da-pos-maxent.bin
curl http://opennlp.sourceforge.net/models-1.5/da-pos-perceptron.bin -o /var/lib/opennlp_data/da-pos-perceptron.bin
curl http://opennlp.sourceforge.net/models-1.5/da-sent.bin -o /var/lib/opennlp_data/da-sent.bin
curl http://opennlp.sourceforge.net/models-1.5/da-token.bin -o /var/lib/opennlp_data/da-token.bin
curl http://opennlp.sourceforge.net/models-1.5/de-pos-maxent.bin -o /var/lib/opennlp_data/de-pos-maxent.bin
curl http://opennlp.sourceforge.net/models-1.5/de-pos-perceptron.bin -o /var/lib/opennlp_data/de-pos-perceptron.bin
curl http://opennlp.sourceforge.net/models-1.5/de-sent.bin -o /var/lib/opennlp_data/de-sent.bin
curl http://opennlp.sourceforge.net/models-1.5/de-token.bin -o /var/lib/opennlp_data/de-token.bin
curl http://opennlp.sourceforge.net/models-1.5/en-chunker.bin -o /var/lib/opennlp_data/en-chunker.bin
curl http://opennlp.sourceforge.net/models-1.5/en-ner-date.bin -o /var/lib/opennlp_data/en-ner-date.bin
curl http://opennlp.sourceforge.net/models-1.5/en-ner-location.bin -o /var/lib/opennlp_data/en-ner-location.bin
curl http://opennlp.sourceforge.net/models-1.5/en-ner-money.bin -o /var/lib/opennlp_data/en-ner-money.bin
curl http://opennlp.sourceforge.net/models-1.5/en-ner-organization.bin -o /var/lib/opennlp_data/en-ner-organization.bin
curl http://opennlp.sourceforge.net/models-1.5/en-ner-percentage.bin -o /var/lib/opennlp_data/en-ner-percentage.bin
curl http://opennlp.sourceforge.net/models-1.5/en-ner-person.bin -o /var/lib/opennlp_data/en-ner-person.bin
curl http://opennlp.sourceforge.net/models-1.5/en-ner-time.bin -o /var/lib/opennlp_data/en-ner-time.bin
curl http://opennlp.sourceforge.net/models-1.5/en-ner-vulnerability.bin -o /var/lib/opennlp_data/en-ner-vulnerability.bin
curl http://opennlp.sourceforge.net/models-1.5/en-parser-chunking.bin -o /var/lib/opennlp_data/en-parser-chunking.bin
curl http://opennlp.sourceforge.net/models-1.5/en-pos-maxent.bin -o /var/lib/opennlp_data/en-pos-maxent.bin
curl http://opennlp.sourceforge.net/models-1.5/en-pos-perceptron.bin -o /var/lib/opennlp_data/en-pos-perceptron.bin
curl http://opennlp.sourceforge.net/models-1.5/en-sent.bin -o /var/lib/opennlp_data/en-sent.bin
curl http://opennlp.sourceforge.net/models-1.5/en-token.bin -o /var/lib/opennlp_data/en-token.bin
curl http://opennlp.sourceforge.net/models-1.5/es-ner-location.bin -o /var/lib/opennlp_data/es-ner-location.bin
curl http://opennlp.sourceforge.net/models-1.5/es-ner-misc.bin -o /var/lib/opennlp_data/es-ner-misc.bin
curl http://opennlp.sourceforge.net/models-1.5/es-ner-organization.bin -o /var/lib/opennlp_data/es-ner-organization.bin
curl http://opennlp.sourceforge.net/models-1.5/es-ner-person.bin -o /var/lib/opennlp_data/es-ner-person.bin
curl http://opennlp.sourceforge.net/models-1.5/nl-ner-location.bin -o /var/lib/opennlp_data/nl-ner-location.bin
curl http://opennlp.sourceforge.net/models-1.5/nl-ner-misc.bin -o /var/lib/opennlp_data/nl-ner-misc.bin
curl http://opennlp.sourceforge.net/models-1.5/nl-ner-organization.bin -o /var/lib/opennlp_data/nl-ner-organization.bin
curl http://opennlp.sourceforge.net/models-1.5/nl-ner-person.bin -o /var/lib/opennlp_data/nl-ner-person.bin
curl http://opennlp.sourceforge.net/models-1.5/nl-pos-maxent.bin -o /var/lib/opennlp_data/nl-pos-maxent.bin
curl http://opennlp.sourceforge.net/models-1.5/nl-pos-perceptron.bin -o /var/lib/opennlp_data/nl-pos-perceptron.bin
curl http://opennlp.sourceforge.net/models-1.5/nl-sent.bin -o /var/lib/opennlp_data/nl-sent.bin
curl http://opennlp.sourceforge.net/models-1.5/nl-token.bin -o /var/lib/opennlp_data/nl-token.bin
curl http://opennlp.sourceforge.net/models-1.5/pt-pos-maxent.bin -o /var/lib/opennlp_data/pt-pos-maxent.bin
curl http://opennlp.sourceforge.net/models-1.5/pt-pos-perceptron.bin -o /var/lib/opennlp_data/pt-pos-perceptron.bin
curl http://opennlp.sourceforge.net/models-1.5/pt-sent.bin -o /var/lib/opennlp_data/pt-sent.bin
curl http://opennlp.sourceforge.net/models-1.5/pt-token.bin -o /var/lib/opennlp_data/pt-token.bin
curl http://opennlp.sourceforge.net/models-1.5/se-pos-maxent.bin -o /var/lib/opennlp_data/se-pos-maxent.bin
curl http://opennlp.sourceforge.net/models-1.5/se-pos-perceptron.bin -o /var/lib/opennlp_data/se-pos-perceptron.bin
curl http://opennlp.sourceforge.net/models-1.5/se-sent.bin -o /var/lib/opennlp_data/se-sent.bin
curl http://opennlp.sourceforge.net/models-1.5/se-token.bin -o /var/lib/opennlp_data/se-token.bin
```

### To build

```bash
lein uberjar
lein test
```

## Testing

```bash
lein test
```

## Usage


```bash
java -jar ./target/uberjar/scio-back-[VERSION]-standalone.jar
```

### Bugs



## License

Copyright © 2016-2018 by mnemonic AS <opensource@mnemonic.no>

Permission to use, copy, modify, and/or distribute this software for
any purpose with or without fee is hereby granted, provided that the
above copyright notice and this permission notice appear in all
copies.

THE SOFTWARE IS PROVIDED "AS IS" AND ISC DISCLAIMS ALL WARRANTIES WITH
REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL ISC BE LIABLE FOR ANY
SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT
