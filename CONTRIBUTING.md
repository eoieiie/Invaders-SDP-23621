# Contributing — Hello World

## Branches
- `main` — protected. No direct push. Changes arrive only through PRs.
- `hello-world/<feature>` — one branch per feature. Lowercase, hyphens. Delete after merge.
  - `hello-world/settings-screen`, `hello-world/mouse-support`

## Commits
- Format: `<type>: <subject>` — see `.gitmessage.txt`. Enable it: `git config commit.template .gitmessage.txt`
- Types: `feat` `fix` `refactor` `docs` `test` `ci` `chore` `style`
- Subject ≤ 50 chars, imperative ("add", not "added"), English.
- Small commits. If you need "and" in the subject, split it.

## Pull requests
- One feature per PR. Target ~400 changed lines or less.
- Fill the template. Screenshots for anything visible.
- Base branch: **our fork `main`** for daily work. Upstream only when a requirement is complete and after the leaders' channel has been told.
- Rebase/merge latest `main` before requesting review. PRs with conflicts are not reviewed.
- Request **1–2 reviewers**, not everyone.
- Merge method: **Squash and merge**. Delete the branch.

## Reviews
Tag every comment with a priority so the author knows what is required:

| Tag | Meaning | Reviewer action |
|---|---|---|
| **P1** | Must fix — breaks behaviour or violates a rule | Request changes |
| **P2** | Should fix — strong recommendation | Request changes |
| **P3** | Consider — fix now or leave a follow-up note | Comment |
| **P4** | Optional | Approve |
| **P5** | Nit / opinion | Approve |

Review focus, in order: (1) does it work, (2) does it touch shared files more than needed, (3) consistency with existing code, (4) naming.

Approval must say **what you checked** in one line. "LGTM" alone is not a review.

## Shared files — extra care
`engine/DrawManager.java`, `engine/Core.java`, `screen/GameScreen.java` are used by every team.
Any change there must be mentioned in the PR description and reviewed by the menu-framework owner.

## Definition of Done
- Code compiles (CI green) and the game runs
- Reviewed and approved
- Screen reachable from the menu and exits with ESC (for screen work)
- Docs/wiki updated if behaviour changed
