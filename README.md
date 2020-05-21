# com.roomkey/experience

## Usage

Install via [tools-deps](https://clojure.org/guides/deps_and_cli#_using_git_libraries)
```clojure
com.roomkey/experience
  {:git/url "https://github.com/lverns/experience" :sha "<put HEAD of master here>"}}
```

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
