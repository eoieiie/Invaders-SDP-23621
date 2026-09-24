# Effection

## Team Introduction

Hello everyone! We’re the Effection Team!

Our team is responsible for developing the Visual Effects System for the game. 
We'll be working on several improvements and new features aimed at enhancing the overall visual experience, gameplay feedback, and immersion.

## Members

| Name           | Role                       | GitHub                                          |
|----------------|----------------------------|-------------------------------------------------|
| Khairul Suffie | Team Leader & Collaborator | [KaiSuf](https://github.com/KaiSuf)             |
| Ariq Iqbal     | Assistant Developer        | [ariqiqbal](https://github.com/ariqiqbal)       |
| Alya Fatihah   | UI/UX Designer             | [alyafths](http://github.com/alyafths)          |
| Dian Melissa   | UI/UX Designer             | [dianpines](https://github.com/dianpines)       |
| Najihah        | Project Analyst            | [najihahs](https://github.com/najihahs)         |
| Maisarah       | Documentation              | [maisarah-mg](https://github.com/maisarah-mg)   |
| NurSofia       | Performance & QA Developer | [sofiajourke](https://github.com/sofiajourke)   |
| Elisya Natasha | Project Manager            | [deluluclover](https://github.com/deluluclover) |

---

## Requirements

- The system shall maintain a consistent retro/pixel-art visual style.
- The system shall optimize particle processing to minimize the impact on game performance.
- The system shall reuse VFX components where possible.
- The system shall automatically remove completed effects to prevent unnecessary resource usage.

---

## Aim & Goals

### 1. Combat & Interaction Effects

The system shall provide distinct visual feedback for combat interactions, including weapon firing, hit impacts, enemy destruction, and player death.



### 1.1 Weapon Firing

When the player or enemy fires a projectile, the system shall trigger the appropriate firing effect.

 ```text
Player/Enemy 
  │ 
  └── 🔫 Shoot 
       ↓ 
   Projectile 
       ↓ 
  Small trail effect
```

**Effects:**

- Player bullets shall leave a short visual trail.
- Enemy bullets shall have a visually distinct trail.
- A muzzle flash shall appear when firing.
- Small pixel particles shall be emitted from the weapon.

**Events:**

- `PLAYER_SHOOT`
- `ENEMY_SHOOT`



### 1.2 Hit Impact

When a projectile hits a player or enemy, the system shall display an impact effect at the hit location.

 ```text
Bullet → Enemy
           ↓
       Hit detected
           ↓
      Impact VFX
```

**Effects:**

- A small flash shall appear at the point of impact.
- 3–6 particles shall be emitted.
- A brief sprite animation shall be displayed.
- A small explosion effect shall be displayed.
- The visual effect may be synchronized with the corresponding hit sound.

**Events:**

- `PLAYER_HIT`
- `ENEMY_HIT`



### 1.3 Enemy Destruction

When an enemy is defeated, the system shall trigger an enemy destruction effect.

**Sequence:**

```text
Enemy 
  ↓ 
Flash 
  ↓ 
Expand 
  ↓ 
Particles + debris 
  ↓ 
Fade out 
  ↓ 
Remove
```

**Effects:**

- The enemy shall briefly flash when destroyed.
- The explosion shall expand during the destruction animation.
- Pixel particles and debris shall spread outward.
- The effect shall fade out after the animation.
- The completed effect shall be automatically removed.
- The destruction effect may vary according to the enemy type or level.

**Event:**

- `ENEMY_DESTROYED`



### 1.4 Player Death / Game Over

When the player is destroyed, the VFX system shall display a destruction sequence followed by the Game Over transition.

**Sequence:**

```text
Player hit 
   ↓ 
Player flashes 
   ↓ 
Ship explodes 
   ↓ 
Debris spreads outward 
   ↓ 
Screen shake 
   ↓ 
"GAME OVER" 
   ↓ 
Game Over transition

```

**Effects:**

- The player ship shall flash after being hit.
- An explosion animation shall be triggered.
- Debris shall spread outward from the destroyed ship.
- Screen shake shall be triggered during the destruction sequence.
- `"GAME OVER"` shall be displayed after the destruction effect.
- The VFX shall transition to the Game Over state.

**Events:**

- `PLAYER_DESTROYED`
- `GAME_OVER`



### 2. Environmental Effects

Enhance the game environment and level progression through particle effects and visual transitions, including:

- Floating stars.
- Tiny space particles.
- Sparks from damaged objects.
- Smoke from destroyed ships.
- Debris floating after explosions.
- Level completion effects such as `"STAGE CLEAR"`, pixel fireworks, and screen transitions.



### 3. Synchronization & Accessibility

Provide reusable VFX components and libraries that can be easily accessed and used by other teams, including:

- Reusable VFX components.
- Consistent VFX events.
- Synchronization with game events.
- Easy integration with other game systems.
- Events such as

```text
`PLAYER_SHOOT`, `ENEMY_SHOOT`, `PLAYER_HIT`, `ENEMY_HIT`, `ENEMY_DESTROYED`, `PLAYER_DESTROYED`, `LEVEL_COMPLETED`, `GAME_OVER`, and `STAGE_STARTED`.
```

---


## Dependencies on Other Teams

### 1. Player & Enemy Ship Variety Team

Our Visual Effect System needs access to the player and enemy entities, including their position, sprite/ship type, hitbox, and destruction state. This allows us to correctly attach effects such as enemy hit flashes, explosions, and player-destruction effects to the corresponding ship. The enemy/ship information is also needed so that effects can be positioned correctly and remain consistent with different player and enemy variants.

### 2. Level Design System Team

Our Visual Effect System needs reliable wave/level start, wave completion, and level-transition events. These events allow us to trigger effects such as wave-clear animations, level-transition effects, and new-wave introduction effects at the correct point in the gameplay sequence. Without these events, the visual effects may be triggered at the wrong time or become disconnected from the actual game progression.

### 3. Sound Effects/BGM Team

Our Visual Effect System needs to coordinate with the Sound Effects/BGM Team for shared gameplay events such as player shooting, enemy hits, enemy destruction, and player damage/death. Visual and audio effects should be triggered from the same gameplay event and use consistent timing so that, for example, an enemy explosion and its explosion sound occur together. This coordination will help maintain consistent and responsive gameplay feedback
