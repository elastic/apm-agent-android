---
name: android-release-wizard
description: Guide an EDOT Android main or patch release from current GitHub state. Propose patch candidates or release notes, refine them with the operator, and dispatch the appropriate workflow only after explicit approval. Use when the operator asks to prepare, start, or continue an Android SDK release.
---

# Android release wizard

Work in the conversation only. Do not edit repository files, push, merge,
publish, or tag. Do not dispatch Prepare release or Start patch until the
operator explicitly approves.

The helper scripts in `scripts/` do the mechanical reads, the notes draft,
and the dispatches. Run them from the repository root, use their output, and
do not repeat their steps by hand.

The ideal run is one round: the operator reads your proposal and says "go
ahead". Do the work up front so that is possible.

## Start

1. Ensure the local repository reflects the latest remote state. If this
   skill, its scripts, or the release workflows changed, continue from the
   updated versions.
2. Run `skills/android-release-wizard/scripts/release-state.sh`. It prints
   the `phase`, the release branches, their open pull requests, and the
   operator's `nextActions`.
3. Report every entry of `nextActions` to the operator.
4. If `phase` is `in-flight`, stop: a `releasing/*` branch exists, and the
   next preparation stops until it is gone.
5. If `phase` is `patching`, continue with "Existing patch branch" unless the
   operator asks for something else.

## Inputs

Prepare release takes one input. Every proposal round shows it in readable
form so the operator sees exactly what will run.

- `release_notes`: JSON with `dependencies`, `featuresEnhancements`, `fixes`,
  and `uncategorized` arrays. Each item has `message`, optional `prId`, and
  optional boolean `breaking`. Prepare release rejects leftover
  `uncategorized` items, empty notes, and a `message` that starts with
  `[Breaking]`; the flag adds that prefix when the notes are rendered.

The version follows from the notes. Any `breaking: true` item produces a
major release; otherwise the main flow produces the next minor. A patch
cannot contain a breaking item.

## Draft notes

Draft the release-note JSON on the release ref, `main` or `patching/x.y.z`:

```sh
skills/android-release-wizard/scripts/draft-notes.sh '<main-or-patching/x.y.z>'
```

It dispatches the Draft release notes workflow, waits for the run, and
prints the JSON the run attaches.

- If it reports "The run has no release-notes artifact", the branch runs
  workflows that predate the automation. Point the operator to the
  `RELEASING.md` of the branch's source tag and stop.
- If it fails otherwise, report its message, including the run URL, and
  stop.

## Main release

When the operator wants the next release from `main`:

1. Draft notes on `main`.
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
6. Continue with the approval loop.

## Patch release

When the operator wants a patch and no `patching/*` branch exists:

1. Ask for an `X.Y` release line or accept `latest`.
2. Run the preview, adding any pull request numbers the operator names:

   ```sh
   skills/android-release-wizard/scripts/patch-preview.sh '<X.Y-or-latest>' [<number> ...]
   ```

   It prints the source tag, the patch version, and the cherry-pick
   candidates: `main` pull requests labeled `bug` merged after the source
   tag, plus the named ones. If it fails, report its message and stop.
3. Show the source tag, patch version, and proposed cherry-pick candidates.
   Explain briefly why each candidate belongs.
   - Do not put a candidate with `changesWorkflows: true` in the Start patch
     list. Propose a hand-made pull request into the patch branch for it.
4. After explicit approval, dispatch Start patch:

   ```sh
   skills/android-release-wizard/scripts/dispatch.sh start-patch.yml main -f line='<X.Y>' -f pull_requests='<numbers>'
   ```

   `<numbers>` are pull request numbers separated by commas with no spaces,
   such as `1135,527`.
5. If the run fails, relay the failure message from the script's output
   with the next step:
   - The tag predates the automated release process: patch the release by
     hand as described under "Patch release" in `RELEASING.md`.
   - The patch branch already exists: cherry-pick each selected fix by hand
     through a pull request into that branch.
   - A cherry-pick conflicts, or the push is rejected because a pull request
     changes `.github/workflows/*`: dispatch again without that pull
     request, and add it through a hand-made pull request into the patch
     branch.
6. When the run succeeds, continue with step 2 of "Existing patch branch".

## Existing patch branch

For `patching/x.y.z`:

1. Run the preview for line `x.y`, adding any pull request numbers the
   operator names. Propose the candidates whose fix is not on the branch
   yet. Explain that each selected fix must be cherry-picked by hand through
   a pull request into that branch.
2. Offer to draft notes on `patching/x.y.z`, so the range starts at the
   branch's source tag.
3. After the notes are approved, dispatch Prepare release on
   `patching/x.y.z` as described in "Dispatch".

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

After explicit approval, dispatch Prepare release from the selected ref with
the approved JSON on standard input through a quoted heredoc, so no
character in the notes is interpreted by the shell:

```sh
skills/android-release-wizard/scripts/dispatch.sh prepare-release.yml '<main-or-patching/x.y.z>' -F release_notes=@- <<'EOF'
<json>
EOF
```

`dispatch.sh` dispatches the workflow, waits for the run, and prints the run
ID and URL. Report the run URL. If the run fails, the script prints the
failed steps' log and the run URL; relay the failure message to the
operator.
