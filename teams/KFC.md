# KFC

## 1. Team Introduction

Team KFC is responsible for designing a progressive and enjoyable level system for the Space Invaders game.

Our goal is to provide players with a smooth difficulty progression from the tutorial to Endless Mode.

### Members

- Member 1: 이동원 (Team Leader / Project Manager)
- Member 2: 진자영 (Lead Level Designer)
- Member 3: 사호철 (Tutorial Stage Designer)
- Member 4: 장윤수 (Stage Progression Designer)
- Member 5: 류동희 (Boss Level Designer)
- Member 6: 김동정 (Endless Mode Designer)
- Member 7: 박성현 (Game Balance & Difficulty Designer)
- Member 8: Jeremie GABOVICH (GitHub Integration & QA Tester)

## 2. Team Requirements

Our team is responsible for the **Level Design System** of the Space Invaders game.

The game will begin with a tutorial stage that teaches the player the basic controls, how to attack enemies, and how to clear a stage.

After completing the tutorial, the player will progress through **Stage 1 to Stage 10**.

The difficulty will gradually increase as the player progresses through the stages. The number of enemies and their movement speed will increase according to the stage number.

**Stage 5** will include a special mid-boss, and **Stage 10** will include the final boss.

After clearing Stage 10, an **Endless Mode** will be unlocked.

In Endless Mode, the player's objective is to survive for as long as possible and achieve a high score based on survival time and the number of enemies defeated.

The difficulty of Endless Mode will increase over time.

A boss will appear when the player survives for **90 seconds** or defeats **30 enemies**, whichever condition is satisfied first. After the boss is defeated, the boss trigger conditions will reset.

Defeating an Endless Mode boss will provide one randomly selected upgrade from a predefined reward list.

The exact balance values may be adjusted during development and playtesting while maintaining the progression rules defined in this document.

## 3. Detailed Requirements

### 1. Tutorial Stage

The tutorial stage will teach players the basic controls and the basic flow of the game.

The tutorial will guide the player through the following actions:

- Move the player ship.
- Fire a projectile.
- Defeat at least one enemy.
- Clear the tutorial enemy wave.

The tutorial will also explain that stronger enemies or bosses may appear during later stages.

The tutorial is considered complete when the player successfully clears the tutorial enemy wave after performing the required basic actions.

After completing the tutorial, the player will proceed to Stage 1.

---

### 2. Mid-Boss and Final Boss

Stage 5 will include a special **Mid-Boss**, and Stage 10 will include the **Final Boss**.

The Mid-Boss will be stronger than normal enemies and will provide a noticeable increase in difficulty compared with Stage 4.

The Final Boss will be stronger than the Mid-Boss and will represent the highest difficulty level of the normal stage progression.

The following rules will be used:

- The Stage 5 Mid-Boss must have higher health than normal enemies.
- The Stage 5 Mid-Boss must use at least one enhanced attack or movement pattern.
- The Stage 10 Final Boss must have higher health than the Stage 5 Mid-Boss.
- The Stage 10 Final Boss must use at least two attack or movement patterns.

The exact health, movement speed, and attack values may be adjusted during playtesting.

Stage 5 is considered complete after the Mid-Boss is defeated.

Stage 10 is considered complete after the Final Boss is defeated.

Clearing Stage 10 will unlock Endless Mode.

---

### 3. Endless Mode

After the player clears Stage 10, **Endless Mode** will be unlocked.

Unlike the normal stages, Endless Mode does not have a final stage.

The player's main goal is to survive for as long as possible and achieve the highest possible score.

The player's score will be calculated using both survival time and the number of enemies defeated.

The initial scoring formula will be:

**Score = (Survival Time in Seconds × 10) + (Enemies Defeated × 100)**

For example:

- Survival Time: 120 seconds
- Enemies Defeated: 20

The resulting score will be:

**(120 × 10) + (20 × 100) = 3,200 points**

The player's score will continue to increase until the player loses all available lives or the game ends.

The exact score values may be adjusted during balancing, but both survival time and enemy defeats must remain part of the scoring system.

---

### 4. Progressive Difficulty System

The difficulty of the game will gradually increase from Stage 1 to Stage 10.

Stage 1 will be used as the baseline difficulty.

For the initial implementation, the following progression rules will be used:

- Each new stage will increase the number of enemies by **2** compared with the previous stage.
- Enemy movement speed will increase by approximately **5% per stage** compared with the Stage 1 baseline.
- Stage 5 will introduce the Mid-Boss.
- Stage 10 will introduce the Final Boss.

For example:

- Stage 1: Base enemy count and base movement speed.
- Stage 2: Stage 1 enemy count + 2, approximately 5% increased movement speed.
- Stage 3: Stage 1 enemy count + 4, approximately 10% increased movement speed.
- ...
- Stage 10: Highest normal-stage enemy count and movement speed, followed by the Final Boss.

These values may be adjusted during playtesting if the difficulty increases too quickly or too slowly.

However, later stages must remain more difficult than earlier stages based on measurable gameplay parameters such as enemy count, enemy speed, enemy health, or boss encounters.

The purpose of this system is to provide a smooth difficulty progression so that players can gradually improve their skills while continuing to face new challenges.

---

### 5. Endless Mode Boss and Reward System

In Endless Mode, difficulty will continue to increase based on survival time.

For the initial implementation:

- Enemy movement speed will increase by approximately **5% every 30 seconds**.
- The number of enemies appearing in a wave may increase after each completed difficulty interval.
- The difficulty increase will continue as long as the player survives.

An Endless Mode boss will appear when either of the following conditions is satisfied:

1. The player survives for **90 seconds** after the previous boss encounter, or
2. The player defeats **30 enemies** after the previous boss encounter.

The condition that is satisfied first will trigger the boss encounter.

After the boss is defeated:

- The survival-time boss counter will reset.
- The enemy-defeat boss counter will reset.
- A new boss trigger cycle will begin.

When the player defeats an Endless Mode boss, the player will receive **one randomly selected gameplay upgrade**.

The initial reward pool will include:

- **Attack Damage Upgrade:** Basic attack damage increases by 10%.
- **Movement Speed Upgrade:** Player movement speed increases by 10%.
- **Projectile Upgrade:** The number of projectiles fired at once increases by 1.

Only one reward will be granted for each defeated Endless Mode boss.

The specific reward values may be adjusted during playtesting, but boss defeats must always provide a gameplay benefit that helps the player survive the increasing difficulty.

---

## 4. Dependencies on Other Teams

### 1. Player & Enemy Ship Variety

Our level design requires enemies and bosses with different gameplay characteristics, including health, movement speed, movement patterns, and attack patterns.

Therefore, we may depend on the team responsible for **Player & Enemy Ship Variety** for enemy and boss variants required by the level progression system.

Our team will define when and where these enemy types are used within the level progression.

---

### 2. Gameplay HUD

The following information needs to be displayed to the player:

- Current stage number
- Tutorial instructions
- Survival time
- Current score
- Number of enemies defeated
- Endless Mode status
- Boss encounters
- Player upgrades

Therefore, we may depend on the **Gameplay HUD** team to display the necessary gameplay information.

---

### 3. Records & Achievements System

Endless Mode uses survival time, number of enemies defeated, and total score as important gameplay records.

Therefore, we may depend on the **Records & Achievements System** team to save and display:

- Highest Endless Mode score
- Longest survival time
- Number of enemies defeated

Our team will provide the scoring and progression rules, while the Records & Achievements System may be responsible for storing and displaying the resulting records.
