# URL Crawler

Recursively crawls a URL and displays an overview of all status codes

## Usage

```bash
$ ./gradlew run --args=URL
```

## Example

```bash
$ ./gradlew run --args=http://localhost/
✓ Scanning URLs

Results:
HTTP 500: 5
HTTP 404: 1
HTTP 301: 4
HTTP 200: 252
```

## How it works

The crawler first takes the given URL and looks for links on that page. Then for each link the crawler found it'll do the same: look for more links on those pages. When it's finished it will display an overview of all the status codes it came across.

However, there are a few things to consider:
- each URL only ever gets crawled once
- only URLs with the same domain are processed

## License

Copyright (C) Oliver Amann

This project is licensed under the GNU Affero General Public License Version 3 (AGPL-3.0-only). Please see [LICENSE](./LICENSE) for more information.
