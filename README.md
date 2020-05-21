# com.roomkey/experience

A Clojure library for compactly expressing and selecting combinations
of experiences for use in A/B tests.

## Installation

Install via [tools-deps](https://clojure.org/guides/deps_and_cli#_using_git_libraries).
```clojure
com.roomkey/experience
  {:git/url "https://github.com/lverns/experience" :sha "<put HEAD of master here>"}}
```

## Usage

This library supports A/B tests as well as A/B/C... as many variations
as you want to test at once. Additionally, It supports nested
experiences. For example, you might wish to test a two versions of a
dialog, but that dialog is only present on one branch of a running A/B
test. This effectively gives you 3 different experiences that a user
could have:
- experience without dialog
- experience with dialog, dialog version 1
- experience with dialog, dialog version 2

See the namespace docstring in `roomkey.experience` as well as
`flattened-tree` and `outcomes` in the namespace for more details.

See the test suite for example usage.

## Development

### Prerequisites

- AWS credentials set up under `~/.aws/`
- [Clojure command-line tools](https://clojure.org/guides/deps_and_cli)

### Running tests

`bin/kaocha`

### Releasing Checklist

1. Ensure all tests are passing
2. Ensure [the changelog](./CHANGELOG.md) is up to date
3. Tag commit with the new version number & push

## License

Copyright © 2020 Hotel JV Services, LLC

Unless otherwise noted, all files in this repository are released
under the 3-Clause BSD License. See LICENSE for terms.
