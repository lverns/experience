# com.roomkey/experience

## Development

### Prerequisites

- AWS credentials set up under `~/.aws/`
- [Clojure command-line tools](https://clojure.org/guides/deps_and_cli)

### Running tests

`bin/kaocha`

### Releasing Checklist

1. Ensure: all the tests pass.
2. Ensure: everything is committed.
3. Push commits: `git push`
4. Ensure tags are fetched: `git fetch --tags`
5. Tag, build, and push artifact: `bin/release VERSION_CHANGE` (where `VERSION_CHANGE` is `major`, `minor`, `patch`, etc.)
6. Push tags: `git push --tags`

