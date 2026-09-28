# Releasing

This guide describes how to publish EDOT Android to
[Maven Central](https://central.sonatype.com/) and the
[Gradle Plugin Portal](https://plugins.gradle.org/). For publishing
configuration details, see the [build-tools README](build-tools/README.md).

## Release steps

A release from `main` takes three steps: dispatch the preparation, merge the
preparation PR, then merge the resulting PR into `main`. The version is
derived from the highest release tag and the release-note content. Merging
the preparation PR is what publishes.

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
`uncategorized` items, has no items, or has a `message` that starts with
`[Breaking]`; the flag adds that prefix when the notes are rendered.

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
6. For a release from `main`, commits the next `-SNAPSHOT` version on
   `releasing/x.y.z` and opens its PR into `main`.
7. For a patch, opens a notes-only PR into `main`.

The team's Slack channel receives the outcome with links to the GitHub Release
and the release PR, or to the failed run.

### 3. Merge the PR into main

For a release from `main`, review and merge the PR from `releasing/x.y.z`.
It brings the release notes, documentation versions, NOTICE files, and the
next development version to `main`. Delete the branch after merging if it was
not deleted automatically. Until this PR merges, Prepare release stops with a
message naming the branch that is still in flight.

For a patch, review and merge the notes-only PR from
`patch-notes/x.y.z`, then delete the `patching/x.y.z` and `releasing/x.y.z`
branches. Until `releasing/x.y.z` is deleted, Prepare release stops with a
message naming it.

## Patch release

Use the release wizard or the
[Start patch workflow](https://github.com/elastic/apm-agent-android/actions/workflows/start-patch.yml)
on `main`. Supply the `X.Y` release line and, optionally, merged `main` pull
requests to cherry-pick. The wizard proposes `bug`-labeled candidates merged
after the source tag.

Start patch selects the highest `vX.Y.Z` tag, creates
`patching/X.Y.(Z+1)` from it, and cherry-picks the selected fixes. It pushes
only after every cherry-pick succeeds. Add later fixes by opening pull
requests into the patch branch. The branch releases with the tooling of its
tag, so only releases made by this automation can be patched this way.

Draft release notes with the wizard or by dispatching the Draft release notes
workflow on `patching/x.y.z`; the draft then covers only that branch's
changes. Approve them, then dispatch Prepare release from that branch. Patch preparation leaves `applies_to` metadata
unchanged. Review and merge the preparation PR to publish. Finally, merge the
notes-only PR into `main` and delete `patching/x.y.z` and `releasing/x.y.z`.

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
- Start patch stops when the line has no release, the newest tag predates this
  automation, or the patch branch already exists. For an existing branch, add
  fixes through pull requests into it.
- If a Start patch cherry-pick conflicts, dispatch again without that pull
  request. Cherry-pick it by hand through a pull request into the named patch
  branch.

