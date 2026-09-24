# Mantis

## Team Introduction

Our team focuses on developing the Records & Achievements System for Space Invaders. We aim to make the game more engaging by tracking player performance and rewarding players when they reach different milestones and goals. Our system will include persistent records and achievements based on high scores, lives, coins, gameplay goals, and accuracy.

## Members

| Name | Role | GitHub |
| --- | --- | --- |
| Parth | Team Leader | [himmm69](https://github.com/himmm69) |
| Marko | Game logic | [markovorkapic](https://github.com/markovorkapic) |
| Adib | Collaborator | [Adiboba](https://github.com/Adiboba)| 
| Aizat | Logic Programmer & Back-end | [AizatIqbal](https://github.com/AizatIqbal) |
| Farhana | System Architect & Database | [hana-hanif](https://github.com/hana-hanif)  |
|Hessa | UI/UX | [srhessaroslan-ux](https://github.com/srhessaroslan-ux) | 
| Aiman | Cross-System Integration Engineer | [Irfnaimann02](https://github.com/Irfnaimann02) |
| Dina | QA & Balance Tester | [dinaadaniii](https://github.com/dinaadaniii) | 
| Syafiq | Front-end | [Fiq](https://github.com/syafiqnorfandhi) |

## Requirements
- Records: Persistent tracking of player performance data (username, currency, hit accuracy per run, unlocked achievements, unlocked items/skins/models, unlocked modes, time taken per run), stored per save file.
- A milestone-based reward system that unlocks in-game rewards (coin bonuses, and premium currency for Gold-tier clears) as players cross score, survival, and progression milestones.
- Score achievements: unlock at 1,000 / 5,000 / 10,000 points reached in a single run, each rewarding 1,000 coins.
- No-hit score achievements: unlock at 1,000 / 10,000 points reached in a single run without the player taking any damage.
- Tiered achievement system (5 criteria × 3 tiers = 15 achievements):
  - **Damage/Survival** — Bronze: finish a run without dying. Silver: finish with more than 1 heart remaining. Gold: finish without taking any damage.
  - **Hit Accuracy** — Bronze/Silver/Gold thresholds TBD, pending confirmation with Gameplay team on how accuracy is tracked.
  - **Items Unlocked** — Bronze: 10% of items unlocked. Silver: 50%. Gold: 100%.
  - **Total Enemies Killed (lifetime)** — Bronze: 100. Silver: 500. Gold: 1,000.
  - **Currency Collected** — Bronze/Silver/Gold thresholds TBD, pending confirmation with Currency team on whether this is a lifetime total or per-run amount.
- Non-tiered achievements:
  - defeat the first enemy
  - unlock endless mode
  - beat level 10 with every ship model
  -beat the game using the starter (weakest) ship
- Hidden achievements:
  - unlock all other achievements (normal + hidden)


## Dependencies on Other Teams

### 1. Level Design System (KFC)
Need level count and endless-mode existence confirmed to implement "beat level 10 with every ship model" and endless-mode achievements.

### 2. Coin System (GOG)
Need a public method to add coins (e.g. `addCoins(int amount)`) for achievement rewards, and a way to query total currency collected for the Currency Collected tier.

### 3. Main Menu (Hello World)
Main Menu team requested an Achievements tab — need to expose a method that returns the current unlock status list for display.
