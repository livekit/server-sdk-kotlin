# Releasing server-sdk-kotlin

This repository releases with [verwalter](https://github.com/livekit/verwalter).
The commands that are the same in every repository are in the
[verwalter cheat sheet](https://github.com/livekit/verwalter/blob/main/docs/cheat-sheet.md).
This file holds what is specific to this repository.

## Packages

| Package | Tag | Goes to |
|---|---|---|
| `server-sdk-kotlin` | `v<version>` | Maven Central as `io.livekit:livekit-server` |

The version is in `gradle.properties` (`VERSION_NAME`) and in the two
install snippets in `README.md`. The release PR moves all three.

## Workflows

| File | Starts when | Does |
|---|---|---|
| `changeset-check.yml` | A PR is opened or updated | Fails a PR that changes the package without a changeset. On the release PR, it runs a Gradle publish to the local Maven repository and fails the PR if the build breaks. |
| `release-pr.yml` | A push to `main` | Keeps the release PR `release/main` current. |
| `release.yml` | The release PR merges | Tags, creates the GitHub release, publishes to Maven Central, waits until the version is on repo1.maven.org. |
| `pre-release.yml` | Run by hand | Opens an rc PR, such as `0.18.0-rc.1`. |

## Release

1. Merge the release PR.
2. Watch the run of `release.yml`: `gh run list --workflow release.yml --limit 1`.

A complete release has these two things:

- A public GitHub release for the tag `v<version>`.
- A 200 answer from `https://repo1.maven.org/maven2/io/livekit/livekit-server/<version>/livekit-server-<version>.pom`.

## Pre-release

An rc replaces the old `-SNAPSHOT` builds. Use it to test unreleased
changes from `main` before the final release.

1. Run `gh workflow run pre-release.yml --ref main`.
2. Merge the rc PR `release/main-rc`. It carries every pending changeset,
   for example as `0.18.0-rc.1`.
3. `release.yml` tags the rc and publishes it to Maven Central.
4. Depend on it as `io.livekit:livekit-server:0.18.0-rc.1`.

The changesets stay pending. The next rc is `rc.2`, and the release PR
still releases `0.18.0`. Maven Central keeps every rc, and nobody can
remove one from it.

## A release failed

Find the cause first. The paths by cause are in the
[recovery section](https://github.com/livekit/verwalter/blob/main/docs/release-workflows.md#recovery).

| Situation | Command |
|---|---|
| A job failed for a transient reason | `gh run rerun <run-id> --failed` |
| The workflow file needed a fix, and the fix is on `main` | `gh workflow run release.yml -f ref=<tag>` |
| The released commit is wrong | `verwalter release --abandon <tag>`, then a changeset and the next version |

The publish job skips a version that Maven Central already has, so a
re-run is safe. If the Sonatype staging repository closed but did not
release, release it in the Sonatype Central portal, then re-run the
failed jobs.

## Who to ask

The LiveKit server SDK maintainers in the `livekit/server-sdk-kotlin`
CODEOWNERS file.
