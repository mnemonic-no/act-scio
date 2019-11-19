# Feed downloader; used to submit to SCIO

Downloads both full and partial feeds, as well as downloads any pdf,doc,xls,csv,txt,json,xml files that are refereneced in the feed.

## Installation

### Install requirements

```bash
pip install requests justext urllib3 feedparser bs4
```

### Copy all scripts to /opt/scio_feeds

Optionally replace scio with the user that will run the scripts in the `chown` command.

```bash
cp -r scripts/feeds /opt/scio_feeds
mkdir /opt/auto_report_download
chown -R scio /opt/auto_report_download /opt/scio_feeds
```

### Add to cron (for the same user owning the files in /opt/scio_feeds)

Feeds

```bash
0 * * * * * /opt/scio_feeds/run-feeds.sh
```

Report repositories (optionally)

```bash
0 5 * * * * /opt/scio_feeds/run-reports.sh
```

## Add feed

### If it is a full feed (the entire body of information is accessible in the RSS/Atom feed)

Add a line to feeds.txt starting with 'f' (for full), then a space and then the feed url

### If it is a partial feed (the feed only contains the ingress and the actuall feed is online)

Add a line to feeds.txt starting with 'p' (for partial), then a space and then the feed url. This will download the page, trying to extract the text only (to avoid multiple downloads of the same document as far as possible).

### Ignore a file from download

Add the full path of the file to ignore.txt


