# Releasing

This guide describes how to publish EDOT Android to
[Maven Central](https://central.sonatype.com/) and the
[Gradle Plugin Portal](https://plugins.gradle.org/). For publishing
configuration details, see the [build-tools README](build-tools/README.md).

## Release steps

A release takes three steps: dispatch the preparation, merge the preparation
PR, merge the release PR. The version is derived from the highest release
tag and the release-note content. Merging the preparation PR is what
publishes.

### 1. Prepare the release

Use either path:

- Ask an agent to prepare the release with the
  [`android-release-wizard`](skills/android-release-wizard/SKILL.md)
  skill. It proposes the notes and resulting version, then dispatches after
  you approve.
- Open the
  [Prepare release workflow](https://github.com/elastic/apm-agent-android/actions/workflows/prepare-release.yml)
  on `main` and provide the release-note JSON.

Both paths dispatch the same workflow with the same input. The workflow
verifies that `gradle.properties` holds the next-minor `-SNAPSHOT` version
after the highest release tag, then creates two branches from the dispatched
`main` commit:

- `releasing/x.y.z`, an unchanged copy of `main` that lives until the
  release is finished.
- `prepare/x.y.z`, with one commit: the release version, the release notes,
  updated documentation versions, and regenerated NOTICE files.

It opens the preparation PR from `prepare/x.y.z` into `releasing/x.y.z`, so
the PR diff shows exactly what the release adds. Review it. Push prose or
NOTICE fixes to `prepare/x.y.z` if needed. For a code fix, land it on `main`,
close the PR, delete both branches, and dispatch again.

#### Release-note JSON

The
[Draft release notes workflow](https://github.com/elastic/apm-agent-android/actions/workflows/draft-release-notes.yml)
prints editable JSON built from the pull requests merged since the last
release. Labels only group the draft; none is required:

- `dependencies` → `dependencies`. Several updates of one dependency collapse
  into the last one merged.
- `enhancement` → `featuresEnhancements`
- `bug` → `fixes`
- anything else → `uncategorized`

Move every `uncategorized` item into a category or delete it. Add
`"breaking": true` to a breaking item. Any breaking item makes the release
major; otherwise it is minor. Prepare release rejects JSON that still has
`uncategorized` items, has no items, or places the rendered breaking prefix
inside `message`.

Each item has a `message`, an optional `prId`, and an optional boolean
`breaking`. See
[`sample.json`](.github/scripts/release/sample.json).

### 2. Merge the preparation PR

When the checks are green and the content is right, merge the preparation PR
into `releasing/x.y.z`. The merge starts the publish workflow, which:

1. Confirms the merged commit carries release version `x.y.z` and that no
   `vx.y.z` tag exists yet.
2. Publishes to Maven Central and the Gradle Plugin Portal once, in Buildkite,
   from the merged commit.
3. Attests the built JAR and AAR files.
4. Creates the `vx.y.z` tag at the merged commit.
5. Creates the GitHub Release.
6. Commits the next `-SNAPSHOT` version on `releasing/x.y.z`.
7. Opens the release PR from `releasing/x.y.z` into `main`.

The team's Slack channel receives the outcome with links to the GitHub Release
and the release PR, or to the failed run.

### 3. Merge the release PR

Review and merge the PR from `releasing/x.y.z` into `main`. It brings the
release notes, documentation versions, NOTICE files, and the next development
version to `main`. Delete the branch after merging if it was not deleted
automatically. Until this PR merges, Prepare release stops with a message
naming the branch that is still in flight.

## Release dry run

The
[Release dry run workflow](https://github.com/elastic/apm-agent-android/actions/workflows/release-dry-run.yml)
runs on every push to `main` and can be dispatched for all targets, Maven
Central, or the Gradle Plugin Portal. It exercises the Buildkite build and
attestation path without publishing.

## Failure and recovery

Use **Re-run failed jobs** on the failed publish run. Every publish and
finalization step checks the real system before acting and skips work that is
already done, so the rerun continues from the failed step.

- Registry visibility can lag. Wait, then rerun. An upload that fails
  because the version is already public counts as done: Maven Central as a
  whole, and each Gradle plugin project on its own.
- If the log names a plugin that is not on the Gradle Plugin Portal although
  its project failed to publish, the upload was interrupted. Rerun once the
  Portal is reachable; if the same plugin keeps failing, ask whoever owns the
  publishing credentials.
- If the run log says the tag already exists, do not push or move it. Rerun so
  the remaining steps finish.
- If a tag exists at another commit, stop. Do not move it.
- If the preparation PR needs different content before it is merged, close
  it, delete both branches, land the correction on `main`, and prepare again.
- If preparation fails after it pushed its branches, for example when opening
  the PR fails, delete `releasing/x.y.z` and `prepare/x.y.z` and dispatch
  again. The next dispatch refuses while a `releasing/*` branch exists.
- If Maven Central or the Gradle Plugin Portal is down, wait for the service
  to recover, then rerun.
- Secret or credential failures require help from whoever owns the pipeline
  secrets.
- For compilation failures, see the
  [build-tools README](build-tools/README.md).
- NOTICE generation failures name the missing license data. Update
  [`manual_licenses_map.txt`](manual_licenses_map.txt) on `main`, then prepare
  again.

