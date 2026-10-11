# Player Ship Designs: Fast Attack and Big Bullet, Fast Move, Two-Way Shot

Team A1 · Player & Enemy Ship Variety · Related: DR-2 (player ship types), DR-5 (ship-specific sprites)

This document introduces four new player ship sprites for the selectable ship roster. Both are drawn on a 13x8 grid, the same size as the current `Ship` sprite (26x16 px in game), so they keep the existing ship size and hitbox.

| Ship | Concept | Grid |
|---|---|---|
| Fast Attack | High rate of fire | 13x8 |
| Big Bullet | Large projectile | 13x8 |

In the grids below, `█` is a filled pixel and `·` is empty.

## Fast Attack

```text
·············
·····███·····
····█████····
···███████···
··█████████··
·████·█·████·
████··█··████
███·······███
```

An arrowhead hull narrows to a three-pixel nose. Below it, a single-pixel spine runs down the centre between two swept-back wings.

## Big Bullet

```text
·············
···█·····█···
···███████···
····█████····
·····███·····
·█···███···█·
·███████████·
·███████████·
```

A heavy two-row base with a small fin at each end. Above it, a funnel widens upward into a seven-pixel mouth with two raised tips, like a wide muzzle for a large shot.


## Fast Move

```text
·············
·····███·····
····█████····
·█·███████·█·
█████████████
██·███████·██
·██·█████·██·
··█···█···█··
```

The fuselage starts with a three-pixel-wide nose and gradually widens downward until it meets the 13-pixel-wide wings. The wings on either side taper inward as they extend downward, while three one-pixel protrusions at the bottom—positioned on the left, center, and right—give the appearance of thrusters.

## Two-Way Shot

```text
·███·····███·
·███·····███·
·████···████·
·███████████·
█████████████
█████████████
█████████████
█████████████
```

The bottom four rows form a wide, solid fuselage that spans the entire width. At the upper left and right, three-pixel-wide gun barrels extend upward, with an empty space between them, fitting the concept of a craft capable of firing projectiles in both directions.


## Gameplay Stats

Fast Move and Two-Way Shot are implemented as `ShipType` values in `src/entity/Ship.java`. Fast Attack and Big Bullet are not finalised yet. The Standard column shows the current ship for comparison.

| Stat | Standard (current) | Fast Attack (planned) | Big Bullet (planned) | Fast Move | Two-Way Shot |
|---|---|---|---|---|---|
| Movement speed (`speed`, px per frame) | 2 | 2 | 2 | 4 | 1 |
| Shooting interval (`shootingInterval`, ms) | 750 | 375 | 1000 | 750 | 1000 |
| Bullet speed (`BULLET_SPEED`, px per frame, negative = up) | -6 | -6 | -6 | -6 | -6 |
| Bullets per shot | 1 | 1 | 1 | 1 | 2 |
| Bullet size (sprite pixels, px in game) | 3x5 (6x10 px) | 3x5 (6x10 px) | 7x7 (14x14 px) | 3x5 (6x10 px) | 3x5 (6x10 px) |
| Max health (hits) | 1 | 1 | 1 | 1 | 1 |
| Movement (px per second at 60 FPS) | 120 | 120 | 120 | 240 | 60 |
| Bullets per second | 1.33 | 2.67 | 1.00 | 1.33 | 2.00 |

The last two rows are derived from the rows above (px per frame x 60 FPS, and bullets per shot / shooting interval) to make the ships easier to compare. Two-Way Shot fires 1.5 times as many bullets per second as Standard, so it moves at half the Standard speed and shoots every 1000 ms to offset this.

## Sprite Data for `res/graphics`

`FileManager.loadSprite` reads a sprite column by column: each group of 8 digits is one column from top to bottom, starting from the leftmost column (13 columns x 8 digits = 104 digits). Per DR-5, append these after the existing entries in `res/graphics`, and add the matching `SpriteType` values and `spriteMap.put` entries in `DrawManager` in the same order.

**Fast Attack**

```text
00000011000001110000111100011110001111000111100001111110011110000011110000011110000011110000011100000011
```

**Big Bullet**

```text
00000000000001110000001101100011001100110011111100111111001111110011001101100011000000110000011100000000
```

**Fast Move**

```text
00001100000111100000101100011100001111100111111001111111011111100011111000011100000010110001111000001100
```

**Two-Way Shot**

```text
00001111111111111111111111111111001111110001111100011111000111110011111111111111111111111111111100001111
```

## Open Items

- **Destruction sprites:** DR-5 requires an idle and a destruction sprite for each ship. Only the idle sprites are designed so far.
- **Big Bullet projectile:** `Bullet` is a fixed 3x5 sprite (6x10 px) and `BulletPool` reuses those objects, so a larger bullet needs its own sprite and size handling when bullets are reused.
