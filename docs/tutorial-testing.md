# Tutorial stage

Every new game starts with a safe tutorial before Stage 1. Use A/D or the
left/right arrows to move, then SPACE to shoot. Clear the one-enemy practice
wave and press ENTER to start Stage 1. Press S at any tutorial step to skip
directly to Stage 1, including immediately after opening the tutorial.
ESC returns to the main menu; it does not skip into the game.
Practice does not award points or consume lives in the normal game. Completing
or skipping the tutorial starts Stage 1 with the normal initial game state.
The tutorial remains available on every new game; completion is not saved.

S must be released and pressed again if held while entering from the menu.
If S and ESC are pressed together, returning to the menu takes priority.

## Manual checks

1. Run `engine.Core` with `res` on the classpath (in IntelliJ, mark `res` as
   Resources Root). Select Play. The tutorial should appear before Stage 1.
2. Press SPACE without moving: the movement step must remain active.
3. Move with A/D or the arrow keys: the shooting prompt and practice enemy appear.
4. Shoot away from the enemy: the wave must not complete until a shot hits it.
5. Align below the enemy and shoot: the completion message explains later enemies
   and bosses. Holding SPACE must not skip this message.
6. Press ENTER: Stage 1 begins with a fresh score and three lives.
7. Start a new game and press ESC during the tutorial: return to the menu without
   entering Stage 1 or the score screen. Starting again resets the tutorial.
8. Move against each edge: the player must stay inside the play area.

## Skip and regression checks

Use a new game for each case. Record the tested commit, result, and screenshots
when reporting a manual test; the checklist below is not a claim that it was run.

| Case | Action | Expected result |
| --- | --- | --- |
| Initial skip | Press S immediately on entering the tutorial, before moving. | Stage 1 begins; no need to wait for the input delay. |
| Shooting step | Move, then press S before firing. | Stage 1 begins. |
| Active bullets | Fire away from the enemy, then press S while a bullet is visible. | Stage 1 begins without leftover tutorial bullets or enemies. |
| Completion | Clear the practice wave, then press S. | Same Stage 1 entry as ENTER. |
| Held menu key | Hold S while selecting Play with SPACE. | Tutorial stays visible until S is released and pressed again. |
| Return to menu | Press ESC at each step, including completion. | Main menu appears, with no score screen or new game started. |
| Conflicting keys | Press S and ESC together. | Return to menu. |
| Normal completion | Move, fire, defeat the enemy, release then press ENTER. | Existing completion path still starts Stage 1. |
| Fresh game state | Compare a skipped run with a completed tutorial run. | Both start at Stage 1 with score 0, three lives and no practice kill/shot credit. |
| Repeat | Return to menu and select Play again. | Tutorial resets; skip remains available. |

Capture the full tutorial window showing `S: skip to Stage 1 | ESC: menu`,
then Stage 1 after skipping. Confirm the hint fits and remains readable.

## Integration

`src/engine/Core.java` is a shared file. This change inserts `TutorialScreen`
before the existing game loop; mention this in the PR and obtain the required
shared-file review. No stage balance or boss behavior is changed.

The skip follow-up only changes `TutorialScreen.java` and this document.
It returns the existing start-game code (2), so Core's normal Stage 1 setup
is reused. The existing `run()` finally block recycles practice bullets on
both skip and menu exits. This addresses the tutorial replay feedback on
upstream PR #90; other integration feedback is outside this change.

## Automated offscreen checks (Java 17)

The optional test harness stays in the [KFC fork](https://github.com/1322528197ldy-tech/KFC-Invaders-SDP-23621/tree/47b9f1196e12de6b7196d8d39d348023104974f4/tests/kfc/KfcTutorialChecks.java), rather than adding a team-specific test directory to the course repository. It is not part of the game or the shared CI configuration.
Compile production sources into a fresh temporary classes directory, then compile the linked harness against those classes. Run `KfcTutorialChecks <temporary-screenshot-directory>` with that classes directory and `res` on the classpath. The temporary location isolates generated saves. Java 17 test-only Unsafe warnings are expected. These checks do not replace a human playthrough.
