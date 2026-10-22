FinalBoss Class Analysis

This document analyzes the operations, constants, and attack patterns of the FinalBoss class.

Important Notes (Must Read Before Modifying)

1. Boss Movement Speed (baseSpeed)

The initial movement speed of the boss is determined by the baseSpeed value in Core's GameSettings.

Crucial: Increasing baseSpeed in game settings directly increases the boss's movement speed. Keep this in mind when balancing stage difficulty.

2. Shooting Rate & Burst Pattern

Accelerating Frequency: The firing interval decreases over time by DECLINE_RATE down to MIN_SHOOTING_RATE.

3-Bullet Burst Attack: After a randomized number of single shots (between MIN_BURST_SHOTS and MAX_BURST_SHOTS), shoot3Burst triggers, launching 3 vertically aligned bullets with a BULLET_GAP offset.

Detailed Code & Component Explanation

1. Constants & State Variables

private static final int FINAL_BOSS_POINTS = 5000;    // Bonus score on destruction
private static final int FINAL_BOSS_POINTS_HIT = 300; // Score granted per bullet hit
private static final int FINAL_BOSS_HP = 5;          // Total health points
private static final int MIN_SHOOTING_RATE = 250;    // Minimum cooldown limit (ms)
private static final int DECLINE_RATE = 50;          // Cooldown reduction per shot
private static int BULLET_SPEED = 5;                 // Bullet velocity

// Burst Attack Pattern Constants
private static final int MAX_BURST_SHOTS = 5;        // Max shots before triggering burst
private static final int MIN_BURST_SHOTS = 3;        // Min shots before triggering burst
private static final int BULLET_GAP = 15;            // Vertical gap (px) between burst bullets

private int shotCount = 0;                           // Current shot counter
private int targetShotsForBurst;                      // Target count for next burst
private Random random = new Random();


2. Constructor & Counter Reset

public FinalBoss(final int positionX, final int positionY, final SpriteType spriteType)


Sets boss dimensions, sprite, score values, and HP (FINAL_BOSS_HP = 5).

Invokes resetBurstCounter() to initialize the first random target shot count for the burst pattern.

3. Movement Logic (moveBoss)

public final void moveBoss(final int screenWidth, final int screenHeight, final GameSettings gameSettings)


Moves smoothly inside screen boundaries (topBoundary = 40, bottomBoundary = screenHeight - 200 - bossHeight).

Bounces upon hitting left/right or top/bottom boundary limits.

4. Shooting & Burst Pattern Logic (shoot, shoot3Burst, resetBurstCounter)

public final boolean shoot(final Set<Bullet> bullets, final GameSettings gameSettings)


Increments shotCount on each valid shooting cycle.

Executes shoot3Burst(bullets) when shotCount >= targetShotsForBurst, spawning 3 vertically spaced bullets in the same shooting cycle and invoking resetBurstCounter().

Otherwise, fires a standard single bullet.

Helper Methods

resetBurstCounter(): Resets shotCount = 0 and calculates next target using random.nextInt(range) + MIN_BURST_SHOTS.

shoot3Burst(Set<Bullet> bullets): Spawns 3 bullets from the boss center with Y-axis offsets (startY, startY - BULLET_GAP, startY - 2 * BULLET_GAP).
Integration note: each burst creates three downward bullets on one X coordinate, spaced 15 pixels vertically. It is a projectile train, not an angled spread shot or a timed sequence of three separate shoot calls. Splitting and summoning remain optional ideas.
