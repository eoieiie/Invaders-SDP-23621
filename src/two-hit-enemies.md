# Two-hit enemies

Stages 6 and 9 assign two-hit health to 20% of the formation, rounded to the
nearest whole enemy: 4 of 18 in Stage 6 and 7 of 35 in Stage 9.
Other stages default to zero. Counts are configured in engine/Core.java.

All formation positions are shuffled and the configured count is selected
without replacement. Each start or restart selects fresh positions across
all rows and columns. A repeated layout is possible by chance.

Two-hit enemies start yellow. A standard hit turns them white; the next defeats
them. Surviving hits do not award points, drops or kill achievements.
Movement and shooting are unchanged; Stage 9 retains diagonal movement.

Compile src and tests/TwoHitEnemyTest.java into an isolated build directory,
then run TwoHitEnemyTest with that directory and res on the classpath.
Keep test profile files separate from real player progress.
For manual testing use -Dgame.startLevel=6 or -Dgame.startLevel=9.
