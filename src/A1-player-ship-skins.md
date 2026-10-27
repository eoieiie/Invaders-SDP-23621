# Player Ship Skins: Fast Attack, Big Bullet, Fast Move, and Two-Way Shot

Team A1 · Player & Enemy Ship Variety · Related: DR-5 (ship-specific sprites), [A1-player-ships.md](A1-player-ships.md)

This document proposes three alternative skins for each of the Fast Attack, Big Bullet, Fast Move, and Two-Way Shot ships. A skin changes only the sprite: every skin uses the same 13x8 grid as the original (26x16 px in game), is left-right symmetric, and keeps the ship's hitbox and stats.

| Ship | Skin | Concept |
|---|---|---|
| Fast Attack | A1 Arrow | Sharper arrowhead |
| Fast Attack | A2 Twin Fang | Twin muzzles |
| Fast Attack | A3 Stealth | Hollow outline |
| Big Bullet | B1 Cannon | Heavy barrel |
| Big Bullet | B2 Mortar | Rounded hull |
| Big Bullet | B3 Fortress | Battlements |
| Fast Move | C1 Jet | Sharper wing tips |
| Fast Move | C2 Triple Thruster | Three thrusters |
| Fast Move | C3 Stealth | Hollow outline |
| Two-Way Shot | D1 Long Barrels | Longer barrels |
| Two-Way Shot | D2 Twin Spike | Thin barrels |
| Two-Way Shot | D3 Hollow | Hollow frame |

In the grids below, `█` is a filled pixel and `·` is empty. The original sprite is shown first for comparison.

## Fast Attack

**Original**

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

### A1 · Arrow

```text
······█······
·····███·····
·····███·····
····█████····
···███████···
█·████·████·█
███·█···█·███
██·········██
```

The nose is one pixel longer and each wing ends in a small spike, for a sharper look.

### A2 · Twin Fang

```text
·············
····█···█····
····██·██····
···███████···
··█████████··
·███·███·███·
███··█·█··███
██·········██
```

The nose splits into two fangs, like twin muzzles for a rapid-fire ship.

### A3 · Stealth

```text
·············
·····███·····
····█···█····
···█··█··█···
··█···█···█··
·███··█··███·
███···█···███
██·········██
```

A hollow outline of the original arrowhead with the centre spine kept, so the silhouette stays the same but looks lighter.

## Big Bullet

**Original**

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

### B1 · Cannon

```text
··██·····██··
··███···███··
···███████···
····█████····
·····███·····
██···███···██
█████████████
·███████████·
```

The muzzle becomes a thick U-shaped barrel and the base widens to the full 13 pixels.

### B2 · Mortar

```text
·············
····█···█····
····█████····
·····███·····
···███████···
·███████████·
█████████████
·██·█···█·██·
```

A short muzzle on a rounded hull with legs underneath, like a mortar.

### B3 · Fortress

```text
···█·····█···
···███████···
····█████····
····█████····
·····███·····
█·█·█████·█·█
█████████████
█████████████
```

A taller muzzle on a solid two-row base with battlements, like a fortress.

## Fast Move

**Original**

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

### C1 · Jet

```text
······█······
·····███·····
····█████····
█··███████··█
█████████████
██·███████·██
·██··███··██·
··█···█···█··
```

The nose is one pixel longer and the wing tips rise into small spikes, like a jet.

### C2 · Triple Thruster

```text
·····███·····
····█████····
···███████···
██·███████·██
█████████████
·███·███·███·
··██·███·██··
··█··███··█··
```

The hull moves up one row, with three thrusters side by side underneath.

### C3 · Stealth

```text
·············
·····███·····
····█·█·█····
·█··█···█··█·
█████████████
█··█·███·█··█
·██··███··██·
··█···█···█··
```

A hollow outline of the original hull, so the ship looks lighter.

## Two-Way Shot

The ship fires its two bullets from the centres of the third and eleventh columns of the grid, so every skin keeps its barrels centred on those two columns.

**Original**

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

### D1 · Long Barrels

```text
·███·····███·
·███·····███·
·███·····███·
·████···████·
·███████████·
█████████████
██·███████·██
█·█·█████·█·█
```

The barrels are one row longer than the original, and small legs under the hull make the ship look like a gun carriage.

### D2 · Twin Spike

```text
··█·······█··
··█·······█··
··██·····██··
·███████████·
█████████████
██·███████·██
█████████████
·███·███·███·
```

The barrels become one-pixel spikes, and the hull has notches along the middle.

### D3 · Hollow

```text
·███·····███·
·█·█·····█·█·
·█·██···██·█·
·█·███████·█·
█████████████
█···█████···█
█████████████
·███████████·
```

The barrels and the upper hull are hollow frames.

## Sprite Data for `res/graphics`

Same format as the ship sprites: `FileManager.loadSprite` reads each sprite column by column, 8 digits per column from top to bottom, starting from the leftmost column (13 x 8 = 104 digits). New lines are appended after the existing entries in `res/graphics`, with matching `SpriteType` values and `spriteMap.put(..., new boolean[13][8])` entries in `DrawManager` in the same order (DR-5).

**A1 · Arrow**

```text
00000111000000110000011000001100000111100111110011111000011111000001111000001100000001100000001100000111
```

**A2 · Twin Fang**

```text
00000011000001110000111000011100011110000011111000011100001111100111100000011100000011100000011100000011
```

**A3 · Stealth**

```text
00000011000001110000111000010100001000000100000001011110010000000010000000010100000011100000011100000011
```

**B1 · Cannon**

```text
00000110000001111100001111100011011100110011111100111111001111110111001111100011110000110000011100000110
```

**B2 · Mortar**

```text
00000010000001110000011100001110011011110011111000111110001111100110111100001110000001110000011100000010
```

**B3 · Fortress**

```text
00000111000000110000011111000011011101110111111101111111011111110111011111000011000001110000001100000111
```
**C1 · Jet**

```text
00011100000011100000101100011100001111000111111011111111011111100011110000011100000010110000111000011100
```

**C2 · Triple Thruster**

```text
00011000000111000000111100111110011110001111111111111111111111110111100000111110000011110001110000011000
```

**C3 · Stealth**

```text
00001100000110100000101100001100001110000100111001101111010011100011100000001100000010110001101000001100
```

**D1 · Long Barrels**

```text
00000111111111101111110111111110000111110000111100001111000011110001111111111110111111011111111000000111
```

**D2 · Twin Spike**

```text
00001110000111111111101100111111000111100001111100011111000111110001111000111111111110110001111100001110
```

**D3 · Hollow**

```text
00001110111110111000101111111011001111110001111100011111000111110011111111111011100010111111101100001110
```

## Open Items

- **Destruction sprites:** Like the original ships, the skins use the shared `ShipDestroyed` sprite when hit.
