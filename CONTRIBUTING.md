# Contributing to Quasporium

Thank you for helping. Issues and pull requests can be written in English or Russian.

## Reporting problems

- **The app does something wrong** — open a *Bug report*.
- **A particular game does not run or looks wrong** — open a *Game compatibility* report and
  include a link to the game. Most of these problems are in the game or the QSP engine, and the
  game file is needed to tell which.
- **An idea** — open a *Feature request*.
- **A security problem** (for example, a game that can read files outside its folder) — do not open
  an issue; follow [SECURITY.md](SECURITY.md).

Before opening an issue, check that it is not already reported and that it happens on the latest
version.

## Development setup

See [Building](README.md#building) in the README. Work on the `foss` flavor: it builds without
Firebase configuration. The `play` flavor needs `app/google-services.json`, which is not public.

## Pull requests

- Keep a pull request to one change. Large changes (new features, refactoring several modules,
  new dependencies) are better discussed in an issue first.
- Follow the [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html) and the
  style of the surrounding code.
- Add or update unit tests for logic you change. Logic that touches Android APIs is easier to test
  when the decision is a plain function (see `planSavesSync` or `nextPageKey`).
- Before pushing, run:

  ```bash
  ./gradlew :app:assembleFossDebug testFossDebugUnitTest testDebugUnitTest
  ```

- Do not add dependencies with proprietary code to the `foss` flavor. Google Play specific code goes
  to `app/src/play` and is added with `playImplementation`.
- Describe in the pull request what you changed and how you checked it (device, Android version,
  games you tried).

## Strings

User-visible strings belong in `res/values/strings.xml` of the module. Translations are welcome.

## License of contributions

By submitting a contribution you agree that it is licensed under the GNU GPL version 3 together with
the [additional permission](LICENSE-EXCEPTION.md) the project uses, the same terms as the rest of
the code.
