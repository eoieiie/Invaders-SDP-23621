# Stage progression and balance

A new run starts at Stage 1 and progresses through Stage 10. Early formations are 5x2, 5x3, 5x4 and 6x4; Stages 6-9 are 6x3, 6x4, 7x4 and 7x5. Higher stages are wider multi-row formations, not single horizontal lines.

This focused PR keeps Stage 5 as the upstream normal wave. Stage 10 temporarily uses the Stage 9 normal-wave settings. The dependent boss PR replaces those two encounters and sets their movement to 5/8 pixels per update. Normal-wave baseSpeed is a movement interval, not pixels per update.

Check transitions through all 10 stages, extra lives, currency cash-out, final-stage score/win achievement, and no diamond reward on death. Boss and tutorial behavior are submitted separately.

## Automated offscreen checks (Java 17)

Run in a fresh temporary classes directory so generated player profiles are isolated from real saves. The harness uses an offscreen frame adapter; it is not a human playthrough. Compile production sources first, then the [KFC fork test harness](https://github.com/1322528197ldy-tech/KFC-Invaders-SDP-23621/tree/47b9f1196e12de6b7196d8d39d348023104974f4/tests/kfc/KfcProgressionChecks.java) against them, and run `KfcProgressionChecks <temporary-screenshot-directory>` with `res` on the classpath. Test-only Unsafe warnings are expected on Java 17.

## Achievement compatibility

The course main update from Mantis PR #170 now sets Infinity Void to Stage 10 and Fleet Master to the final stage reported by Core.getNumLevels(). Humble Beginnings follows the existing game-win hook after Stage 10. The boss integration preserves these upstream achievement rules and existing saved achievements.
