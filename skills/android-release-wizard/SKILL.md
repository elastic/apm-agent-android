---
name: android-release-wizard
description: Propose the release notes of the Prepare release workflow for EDOT Android, including which items are breaking and the version that follows, refine them with the operator, and dispatch once the operator explicitly approves. Use when the operator asks to prepare or start an Android SDK release.
---

# Android release wizard

Work in the conversation only. Do not edit files, push, merge, publish, or
tag. Do not run the dispatch until the operator explicitly approves.

The ideal run is one round: the operator reads your proposal and says "go
ahead". Do the work up front so that is possible.

Before you start, ensure the local repository reflects the latest remote
state. If this skill or the release scripts changed, continue from the
updated versions.

## Inputs

Prepare release takes one input. Every proposal round shows it in readable
form so the operator sees exactly what will run.

- `release_notes`: JSON with `dependencies`, `featuresEnhancements`, `fixes`,
  and `uncategorized` arrays. Each item has `message`, optional `prId`, and
  optional boolean `breaking`. Prepare release rejects leftover
  `uncategorized` items, empty notes, and a `message` that starts with
  `[Breaking]`; the flag adds that prefix when the notes are rendered.

The version follows from the notes. Any `breaking: true` item produces a
major release; otherwise the next minor.

## Propose

1. Run from the repository root, with `<main-ref>` as the up-to-date `main`
   ref, normally `origin/main`:

   ```sh
   GITHUB_REPOSITORY=elastic/apm-agent-android .github/scripts/release/draft-release-notes.sh <main-ref>
   ```

2. Read the last two or three versions in `docs/release-notes/index.md` and
   match their style: short, user-facing, one line per change.
3. For each pull request, write the message you believe belongs in the
   notes: what changed for users of the SDK. Read the pull request
   description and, when the title is not enough, its diff. Keep the title
   only when it already reads like a release note.
4. Place each `uncategorized` item in the group you believe fits, or propose
   deleting it when it does not belong in user-facing notes, for example
   release bookkeeping or CI-only changes. Mark each such decision as
   proposed and say why in a few words.
5. Mark an item `breaking: true` when it looks breaking and state the reason.
   Show the release version that follows from the complete notes. If the
   operator asks for a major release and no item is breaking, explain that a
   major requires a breaking item and ask which change is breaking.
6. Show the summary described below.

## Refine until approved

Repeat until the operator explicitly approves:

1. Show the summary of every input as it stands:
   - The release notes as readable Markdown, not JSON: a list per group,
     one line per item as `message (#prId)`, with breaking items marked and
     listed first.
   - Items proposed for deletion, each with its reason.
   - Each proposed `breaking` flag and its reason.
   - The release version that follows.
   - Anything still marked as proposed and not yet confirmed.
2. Ask the operator to confirm, or to say what to change.
3. Apply the requested changes in the conversation. Use the operator's
   wording as given.

Treat only an explicit go-ahead as approval, such as "looks good",
"approved", "continue", "go ahead", or "dispatch". A go-ahead confirms every
proposal shown in that summary. Treat a question, a comment, partial
feedback, or silence as more feedback. When unsure whether the operator
approved, ask.

## Dispatch

1. Record the time, then dispatch with the approved JSON on standard input
   through a quoted heredoc, so no character in the notes is interpreted by
   the shell:

   ```sh
   since=$(date -u +%Y-%m-%dT%H:%M:%SZ)
   gh workflow run prepare-release.yml -R elastic/apm-agent-android --ref main -F release_notes=@- <<'EOF'
   <json>
   EOF
   ```

2. Report the URL of the run created after that time. The run can take a few
   seconds to appear; repeat the command until it prints a URL:

   ```sh
   gh run list -R elastic/apm-agent-android --workflow prepare-release.yml --event workflow_dispatch --limit 5 --json url,createdAt --jq ".[] | select(.createdAt > \"$since\") | .url"
   ```
