# scio-back

## Requirements

SCIO requires a working clojure environment to build and beanstalkd/java to run.

### Beanstalkd

The scio platform uses the beanstalkd mq. This must be installed and running.

## Installation

### Clone repository

```bash
git clone https://github.com/mnemonic-no/act-scio.git
cd act-scio
```

### Download vendor files to vendor/ (OpenNLP models, Geo names and TLDs)

```bash

script/get-vendor-files.sh
```

### To run locally

In repository, run this command to replace all directories in ini-file to point to our local repository.

This is also needed to run the tests.

```bash
sed "s#/opt/scio#$(pwd)#g" etc/scio.ini > etc/scio.ini.local
```

Create directoy for storing documents.

```bash
mkdir documents
```

### System wide installation: copy files

```bash
mkdir -p /opt/scio/documents
cp -r etc vendor /opt/scio
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
java -jar ./target/uberjar/scio-back-[VERSION]-standalone.jar --config etc/scio.ini.local
```

Config file defaults to /etc/scio.ini if not specified.

### Bugs


## License

Copyright © 2016-2019 by mnemonic AS <opensource@mnemonic.no>

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
