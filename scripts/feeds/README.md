# Feed downloader; used to submit to SCIO

Downloads both full and partial feeds, as well as downloads any pdf,doc,xls,csv,xml files that are refereneced in the feed.

## Add feed

### If it is a full feed (the entire body of information is accessible in the RSS/Atom feed)

Add a line to feeds.txt starting with 'f' (for full), then a space and then the feed url

### If it is a partial feed (the feed only contains the ingress and the actuall feed is online)

Add a line to feeds.txt starting with 'p' (for partial), then a space and then the feed url. This will download the page, trying to extract the text only (to avoid multiple downloads of the same document as far as possible).

### Ignore a file from download

Add the full path of the file to ignore.txt


