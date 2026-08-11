# CI, and the route for workflow changes

Changes to `.github/workflows/` are written to `ci/proposed/` and applied by the
owner. This file says why, what belongs where, and how a proposal is applied.

**`ci/proposed/` holds proposal files and nothing else.** This contract sits one
level up, deliberately. It first lived inside `proposed/`, and the first apply
moved the whole directory — reasonably, since the instruction was "move the
proposed CI changes" and this file was sitting in among them. Everything in
`proposed/` is now something that belongs in `.github/workflows/`, so moving all
of it is correct rather than a mistake waiting to happen. That is the fix; a
warning telling people not to move the README would have been the other kind.

The process is [LinkCtrl's](https://github.com/DevOfPie/LinkCtrl), adopted here
on 2026-08-07 because this repository hit the identical wall. Nothing about it
is invented for TradeShop.

## Why a proposal and not a commit

The token building this repository is a fine-grained PAT without the `Workflows`
permission. GitHub refuses the **push**, not the merge:

```
! [remote rejected] ci-minimal-gate -> ci-minimal-gate
  (refusing to allow a Personal Access Token to create or update workflow
   .github/workflows/maven.yml without `workflow` scope)
```

Measured here 2026-08-07. Push access is otherwise real — a probe branch
carrying no workflow file pushed and was deleted. So there is no version of
"open a pull request and let the owner review it" that works: a branch carrying
a workflow change cannot leave the machine at all. That is why the change
arrives as a file at a path that is not `.github/workflows/`.

### Why the permission stays absent

It is not an oversight to be corrected. A workflow file is code that runs with
`GITHUB_TOKEN`, and a workflow's own `permissions:` block overrides the
repository default — that setting is a default, not a ceiling. Write access to
`.github/workflows/` therefore converts into `packages: write`,
`actions: write` (deleting run logs, which is the audit trail) and
`pages: write`, none of which are on the token. A `schedule:` or
`workflow_dispatch:` workflow also keeps running after the token is revoked.

The permission is a lever on all of that, not a lever on YAML. One manual step
per workflow change is the price, and workflow changes are rare by design — see
the split below.

## What lives where

| Change | Where | Needs the owner |
| --- | --- | --- |
| A new check, or a changed one | `ci/build.sh` | No |
| What a check actually does | `ci/*.sh` | No |
| Surefire config, plugin versions, dependency pins | `pom.xml` | No |
| Triggers, `permissions:`, `concurrency:` | `.github/workflows/` | **Yes** |
| `JAVA_VERSION`, `NODE_VERSION`, `runs-on` | `.github/workflows/` | **Yes** |
| Action versions and their pins | `.github/workflows/` | **Yes** |
| Which npm packages the tier-3 client uses | `it-client/package.json` + its lockfile | No |

The left column is the common case and the right column is not, which is what
makes the manual step affordable. Adding a check is a script edit that reaches
the next push; changing what CI *is* takes a proposal.

**This matters for W5.** The test suite's gate — failing the build on skipped
tests, because MockBukkit reports unimplemented API as a skip and a build that
skips everything still exits 0 — is surefire configuration and a script edit.
Neither needs a proposal.

**And it matters for the tier-3 client**, which is where the split stops being
free. `ci/integration.sh` now drives a real Minecraft client, so the run needs a
Node toolchain, and `actions/setup-node` can only be added to
`.github/workflows/` — hence `ci/proposed/maven.yml`. Note what the proposal does
and does not buy: `ubuntu-latest` already ships a Node, so the pin is about
knowing *which* one every run used rather than about making the tier possible at
all. A tier whose toolchain version is whatever the runner image happened to
carry that month is a tier that will one day fail for a reason nobody can name.

`sh ci/workflow-proposals.sh` prints which proposals are pending, with a diff
against the live file. It is deliberately **not** a gate and always exits 0: a
pending proposal is a normal state.

## Applying one

From a checkout with the owner's credentials. **The destination is
`.github/workflows/` — that exact path and no other.** GitHub runs workflows
from there and nowhere else, and a `.yml` under `.github/ISSUE_TEMPLATE/` is not
a workflow that fails loudly, it is a workflow that silently does not exist.

```sh
cp ci/proposed/maven.yml .github/workflows/maven.yml
sh ci/workflow-proposals.sh          # must now report: applied
git add .github/workflows/maven.yml
git commit
```

**Apply on the base branch**, not on the branch that raised the proposal. WC2
was applied to its own proposal branch, so the gate it fixed kept running with
the old triggers on the branch everyone works from, and moving it across cost a
second merge.

**Run the checker before pushing.** It is the entire safety net on the file
itself — it reports `applied` only when a file exists at the live path *and*
matches, so a copy that went elsewhere still reads `pending`. On the first
apply, WC1 landed in `.github/ISSUE_TEMPLATE/workflows/`, the checker was not
run, and the branch pushed green carrying no installed workflow and nothing to
say so.

Both mistakes so far have been about *where*, not *what* — the content was
right each time. That is the step to slow down on.

One `cp` and nothing else. The dead `maven.yml.old` was already removed by the
branch that raised this proposal, because GitHub's guard is on the **workflow
file extension**, not the directory: `.old` is not `.yml`, so the token could
push its deletion. Probed here 2026-08-07 on a throwaway branch that pushed
clean and was deleted. Worth knowing generally — housekeeping under
`.github/workflows/` is only blocked for files GitHub would actually run.

Use `cp` rather than copying the text through an editor: only trailing newlines
are normalised in that comparison, and everything else counts, whitespace
included.

Then close the loop: move the row in the support repository's workflow-changes
tracker to *Applied* with the commit, and **delete the file from this
directory**. A proposal that stays here after being applied becomes a second
copy of the workflow, free to drift from the one that runs — which is the
failure this directory would otherwise invite.

The tracker row lives in the support repository rather than here, because it is
a record and records never cross into this tree. The proposal file lives here
because it is code. That split is the same one that governs everything else in
this project.

## Writing one

- **No header addressed to the reviewer.** A proposal is a whole-file copy and
  applying it is `cp`, so anything the file says about itself lands on the live
  workflow. Do not write "apply this file" into a file that will *become* the
  workflow. The diff *is* the description, and `workflow-proposals.sh` prints
  it.
- **Say what is not changing.** The reviewer's question is always "does this
  alter what runs on push", and the answer belongs in the file's header comment.
  That is a comment about the workflow, not about the proposal, so it survives
  being applied without embarrassing anyone.
- **Change one thing.** A proposal is reviewed by a human reading YAML, and a
  diff that mixes a trigger change with a refactor gets approved for the
  refactor.
- Raise the row in `workflow-changes.md` at the same time, so a waiting change
  is as visible as a waiting defect.
