# Hello World — Git Workflow

## Terms

| Term | Meaning |
|---|---|
| **upstream** | `oh-gnues/Invaders-SDP-23621`. The real game where all 9 teams merge. We never edit it directly |
| **fork** | `eoieiie/Invaders-SDP-23621`. Our team's copy of upstream. Our workspace |
| **branch** | A line of work for one feature. Deleted when done |
| **PR** | "Put this branch into that branch." The arrow direction is everything |
| **access holder** | The one person per team who can merge on upstream. Ours is **Yongtae** |

## Structure

```
upstream main (oh-gnues)      ← all 9 teams meet only here
      ▲ PR ②  (when a requirement is done)
our fork main (eoieiie)       ← upstream + our finished work
      ▲ PR ①  (daily)
feature branches              ← one per person, one per feature
```

## Who can do what

| | fork | upstream |
|---|---|---|
| push | all 9 members | nobody (Yongtae only presses merge) |
| open PR | all 9 members | all 9 members (no permission needed) |
| approve | all 9 members | the 9 access holders |
| merge | all 9 members (1 approval required) | Yongtae |

## Daily routine (PR ①)

```bash
git checkout main
git pull                                  # update fork main
git checkout -b hello-world/feature-name
# work, commit
git push -u origin hello-world/feature-name
```

Open a PR on GitHub → **check that base is `eoieiie/…`** → 1 teammate approves → merge → delete branch.

If a branch lives more than a day, every morning:
```bash
git checkout main && git pull
git checkout hello-world/feature-name && git merge main
```

## Sending to upstream (PR ②)

```
1. Yongtae: fork page → "Sync fork" → pull latest upstream
2. If conflicts: resolve in the fork, run the game to confirm
3. Yongtae cuts a snapshot branch at the feature's squash commit and opens the PR from it:
   `git checkout -b to-upstream/<feature> <sha>` → push → PR `to-upstream/<feature>` → upstream main
   (Not from fork `main` — that branch keeps moving, and anything merged later would ride along into the open PR.)
   If it depends on an upstream PR that isn't merged yet, first line of the body: `Depends on: #N`
   Title `type(requirement): subject`, e.g. `feat(main-menu): ...`. Body: `Depends on: #N` first if needed
   First upstream PR from a person? CI shows "workflow awaiting approval" — Yongtae clicks **Approve and run**. Automatic after that
4. One access holder from another team approves
5. Yongtae merges with **Create a merge commit** — not squash. A fork-main PR carries several people's commits; squash would collapse them under one name
6. If other teams need it, post one line in the leaders channel
```

- One PR = one requirement. Do not batch
- Yongtae opens and merges upstream PRs; approval comes from another team, so this is fine
- Other teams never look at our fork. They pull from upstream only

## Rules

- Branch name: `hello-world/feature-name`
- Commit message: `feat: what you did` / `fix:` / `docs:` / `refactor:` / `chore:`
- One PR = one feature. Over 400 lines → split
- Upstream PRs must have zero conflicts
- No merge on fork without 1 approval
- `DrawManager` · `Core` · `GameScreen` are shared by every team — review carefully
- Meeting notes and records go to the Wiki, not a folder in the repo (it would travel with PRs)

## Common mistakes

1. **PR arrow** — GitHub defaults the base to upstream. A team-internal PR lands on upstream. Check the base
2. **`git add .` with IDE files** — never commit `.idea/` `.vscode/` `bin/` `out/`
3. **Working on main** — branch first, always. main is pull-only
4. **`git pull` on a feature branch** — updates only that branch. To update main, be on main