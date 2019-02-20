#!/usr/bin/env sh
set -e

DOWNLOAD_DIR="download"
BACKUP_DIR="bak"
COUNTRY_INFO="countryInfo.txt"
CITIES_1000="cities1000"
CITIES_15000="cities15000"
GEONAMES_URL="http://download.geonames.org/export/dump"
COUNTRIES_WITH_REGION_CODES_URL="https://raw.githubusercontent.com/lukes/ISO-3166-Countries-with-Regional-Codes/master/all/all.json"
COUNTRIES_WITH_REGION_CODES_JSON="ISO-3166-countries-with-regional-codes.json"

function log {
    echo `date "+%F %T"` $1
}

function create_if_not_exists {
    if [ ! -d "$1" ]; then
        log "creating folder $1"
        mkdir $1
    fi
}

function backup_if_exists {
    if [ -f "$1" ]; then
        log "backing up $1"
        mv $1 $BACKUP_DIR/.
    fi
}

function download_from_geonames {
    curl --silent $GEONAMES_URL/$1 -o $DOWNLOAD_DIR/$1
}

function download_cities {
    backup_if_exists "$1.txt"
    download_from_geonames "$1.zip"
    unzip -q $DOWNLOAD_DIR/$1.zip
    rm $DOWNLOAD_DIR/$1.zip
}

function download_country_info {
    backup_if_exists $1
    download_from_geonames $1
    sed -i '' '/^#/d' $DOWNLOAD_DIR/$1
    mv $DOWNLOAD_DIR/$1 .
}

log "checking if all folders exist"
create_if_not_exists $DOWNLOAD_DIR
create_if_not_exists $BACKUP_DIR

log "downloading files"

log "getting $CITIES_1000"
download_cities $CITIES_1000

log "getting $CITIES_15000"
download_cities $CITIES_15000

log "getting $COUNTRY_INFO"
download_country_info $COUNTRY_INFO

log "getting $COUNTRIES_WITH_REGION_CODES"
backup_if_exists $COUNTRIES_WITH_REGION_CODES
curl --silent $COUNTRIES_WITH_REGION_CODES_URL -o $COUNTRIES_WITH_REGION_CODES_JSON

log "removing $DOWNLOAD_DIR"
rm -r $DOWNLOAD_DIR
