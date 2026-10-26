# Boss integration testing

Stage 5 uses MidBoss (2 HP, 100 per hit + 1000 defeat = 1200 total).
Stage 10 uses FinalBoss (5 HP, 300 per hit + 5000 defeat = 6500 total),
with a 24x16 graphic scaled to a matching 48x32 collision box.
Both bounce within the play area. FinalBoss fires one downward shot normally,
then a three-projectile vertical train every random 3–5 firing cycles.
The three projectiles share one X coordinate and are spaced 15 pixels vertically.
The firing cooldown accelerates with floors of 500ms (mid) and 250ms (final).

## Manual checks

- Play Stage 5 and Stage 10; verify correct boss graphic, movement, hits and score.
- Check single-shot/train patterns, stage transition and one clear reward.
- Verify purchased ships/skins, pause/resume/restart, item freeze/shield,
  ordinary enemy drops/VFX and coin refund on restart.
- Verify first-boss achievement and game over without normal formation errors.
- Human balance testing remains necessary; splitting/summoning are not included.

## Automated checks

Java 17 production compilation; KfcBossChecks (62), KfcSyncChecks (27),
KfcBurstChecks (61): 150 checks on this isolated boss/progression tree.
Use a fresh temporary classes/save directory with res on the classpath.
The tutorial is deliberately excluded; it remains in upstream PR #140.
This branch includes the progression dependency from #141.
Achievement conditions follow the latest Mantis #170 integration: Infinity Void and Fleet Master now target the final Stage 10. Boss clears also carry run-level totals and flawless-level progress; restart refunds remove attempt coins from the per-run total.

Authors: Donghee's internal KFC #22/#23 and earlier KFC boss integration.
See Boss_Final.md and Boss_Mid.md for the technical details.

The optional harnesses remain in the [KFC fork](https://github.com/1322528197ldy-tech/KFC-Invaders-SDP-23621/tree/47b9f1196e12de6b7196d8d39d348023104974f4/tests/kfc); they are not included in the course repository or its CI. Compile KfcBossChecks, KfcSyncChecks and KfcBurstChecks against this branch in a temporary classes directory. KfcSyncChecks and KfcBurstChecks extend KfcBossChecks. Do not run KfcProgressionChecks on this boss tree: it intentionally tests the standalone normal-wave version of #141.

## October 18 upstream synchronization

Merged course main 5de5f8d. Retained the Stage 10 boss settings (movement speed 8, initial shooting interval 500ms), all tier achievement icons in the upstream loading order, and the normal-wave meteor integration. Meteor spawning is called only for ordinary formations; boss stages do not create an empty formation just to spawn meteors.

The three existing harnesses were adapted to the new GameState constructor and passed all 150 checks. An additional 46 cross-team checks cover every loaded sprite, boss clear counters and flawless progress, normal meteor spawning, no null formation on boss updates, restart coin totals and the tier achievement page. Total: 196 automated checks; manual difficulty testing remains separate. These optional harnesses remain outside the course PR.
