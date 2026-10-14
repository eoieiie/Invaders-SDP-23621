# Team Sound (SFX/BGM) — Git Workflow

## 1. Selected Git Workflow and Rationale

**Selected workflow:** Sub-Feature Branch Workflow (hybrid), with an intermediate integration layer of sub-branches (`sfx`, `bgm`, `engine`).

**Rationale:**
- Our team is responsible for the **Sound Effects/BGM** requirement inside a larger multi-team project. We do not have direct write access to the global repository, so a **fork-based** model is required for how we integrate with the global project (`origin` = our team fork, `upstream` = global project repo).
- Inside our own team, the sound work naturally splits into **two sub-teams (SFX and BGM)** plus a shared **engine/integration layer** (audio manager, mixing, loading). Using dedicated sub-branches for each area lets both sub-teams work in parallel without blocking each other, while still integrating frequently within the team fork.
- Individual features (a specific sound effect, a specific music track, a specific engine hook) are naturally short, self-contained units of work, which fits a **Feature Branch** model with a bounded lifetime (4–8 days) to avoid long-lived, hard-to-merge branches.
- This two-level structure (feature → sub-branch → team main → global main) gives us local autonomy and fast iteration for sub-teams, while still producing a single reviewed, stable contribution to the rest of the project.

---

## 2. Branch Strategy

| Branch | Location | Role |
|---|---|---|
| `main` (global) | Global project repository (`upstream`) | Final integrated project. Only receives reviewed PRs from each team's fork `main`. |
| `main` (team) | Our team fork (`origin`) | Stable, integrated state of the Sound team's work. Source of truth for our team; only receives merges from `sfx`, `bgm`, and `engine`. |
| `sfx` | Team fork | Long-lived sub-branch integrating all completed sound-effect features before they reach team `main`. |
| `bgm` | Team fork | Long-lived sub-branch integrating all completed background-music features before they reach team `main`. |
| `engine` | Team fork | Long-lived sub-branch integrating shared audio engine/integration work (audio manager, mixer, loaders) before it reaches team `main`. Maintained by a mix of SFX and BGM developers as needed, rather than a fixed dedicated sub-team. |
| `feature/<sfx-bgm-engine>/<short-description>` | Team fork | Short-lived branch for one specific feature (e.g. one sound effect, one track, one engine hook), created by a single developer or small pair. |

**Creation rules:**
- `sfx`, `bgm`, `engine` are created once, at project start, from team `main`, and stay for the whole project.
- A `feature/...` branch is created from the relevant sub-branch (`sfx`, `bgm`, or `engine`) at the start of a task, and is expected to live **4–8 days maximum**. If a feature needs more time, it should be split into smaller features.

**Merge rules:**
- `feature/...` → its sub-branch (`sfx`/`bgm`/`engine`): once the feature works, is tested in-game, and has passed review from at least one other member of the same sub-team.
- `sfx` / `bgm` / `engine` → team `main`: once the sub-branch is stable and its content has been reviewed by the sub-team lead (batched, not necessarily one PR per feature).
- team `main` → global `main`: once our sub-team leader has verified the team fork's `main` builds and runs correctly, via a PR to the global repository.

**Deletion rules:**
- `feature/...` branches are deleted immediately after being merged into their sub-branch.
- `sfx`, `bgm`, `engine`, and team `main` are kept for the whole project and are never deleted.

---

## 3. Commit Rules

**What belongs in one commit:**
- One commit = one coherent, working change (e.g. "add footstep sound effect," "fix boss theme loop point," "hook audio manager into game state"). Avoid mixing unrelated sounds/features in a single commit.
- Each commit should leave the game in a runnable state whenever possible.
- Binary asset commits (audio files) should be grouped with the code/config that uses them, not committed separately in a way that breaks the build.

**Commit message format (Conventional Commits style):**
```
<type>(<scope>): <short description>

[optional longer description]
```
- `type`: `feat`, `fix`, `refactor`, `chore`, `docs`
- `scope`: `sfx`, `bgm`, or `engine`

Examples:
- `feat(sfx): add explosion sound effect for enemy ship destruction`
- `fix(bgm): correct loop point in boss theme`
- `chore(engine): update audio manager volume scaling`

---

## 4. Pull Request and Code Review Rules

**When to open a PR:**
- When a `feature/...` branch is complete, tested locally in-game, and ready to be merged into its sub-branch (`sfx`, `bgm`, or `engine`).
- When a sub-branch (`sfx`, `bgm`, or `engine`) is ready to be merged into team `main`.
- When team `main` is ready to be merged into the global project `main`.

**Review and approval conditions:**
- Feature → sub-branch: **at least 1 approval** from another developer on the same sub-team (SFX or BGM).
- Sub-branch → team `main`: **approval from the sub-team lead**, who checks integration and consistency across features.
- Team `main` → global `main`: approval required from the global project's designated reviewer/integration manager (per the class's cross-team PR rules), including at least one cross-team review.

**Direct pushes to `main`:** **Not allowed**, at any level (team `main` or global `main`). All changes must go through a reviewed PR.

---

## 5. Merge Strategy

| Merge direction | Method | Reason |
|---|---|---|
| `feature/...` → sub-branch | **Squash merge** | Keeps sub-branch history clean and readable, since feature branches may contain many small "work in progress" commits. |
| sub-branch (`sfx`/`bgm`/`engine`) → team `main` | **Merge commit** | Preserves traceability of which sub-branch/feature set was integrated and when. |
| team `main` → global `main` | **Merge commit** (via PR, per class-wide agreement) | Matches the cross-team integration rules agreed in the leaders meeting. |

**Conflict resolution:**
- Conflicts inside a feature branch (against its sub-branch) are resolved by the **feature's author**, with help from the reviewer if needed.
- Conflicts between sub-branches (`sfx`/`bgm`/`engine`) when merging into team `main` are resolved by the **sub-team leads together**, since they concern shared engine/integration code.
- Conflicts at the global `main` level are resolved by our **team leader**, in coordination with the other team leaders during the leaders meeting.

---

## 6. Overall Development Workflow

```mermaid
flowchart TD
    subgraph Feature Work
        F1[feature/sfx/explosion] -->|squash merge, 1 review| SFX[sfx branch]
        F2[feature/bgm/boss-theme] -->|squash merge, 1 review| BGM[bgm branch]
        F3[feature/engine/audio-manager] -->|squash merge, 1 review| ENG[engine branch]
    end

    SFX -->|merge commit, lead review| TM[Team main - our fork]
    BGM -->|merge commit, lead review| TM
    ENG -->|merge commit, lead review| TM

    TM -->|PR, cross-team review| GM[Global project main]
```

**Process summary:**
1. A developer creates a `feature/<sfx|bgm|engine>/<name>` branch from the relevant sub-branch.
2. The developer works on the feature for up to 4–8 days, committing regularly with clear messages.
3. Once complete and tested, the developer opens a PR into the sub-branch; at least one sub-team peer reviews and approves; PR is squash-merged; feature branch is deleted.
4. Periodically, the sub-team lead opens a PR from the sub-branch into team `main`; reviews integration; merges with a merge commit.
5. Once team `main` is stable, our team leader opens a PR from team `main` into the global project `main`, following the cross-team review and approval rules agreed in the leaders meeting.
