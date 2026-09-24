# ECE

## Team Introduction

Team ECE is responsible for building the Gameplay HUD for the Invaders-SDP-23621 project. Our goal is to give players clear, real-time feedback on their score, lives, level, and equipped power-ups without ever cluttering or slowing down the on-screen action. Team roles and GitHub accounts are listed below.

## Members

| Name | Role | GitHub |
|------|------|--------|
| Maxence Morcillo | Team Leader | [] |
| Thomas Duval | UI/Overlay | [TraderX-2](https://github.com/TraderX-2) |
| Thomas Favre | QA/Testeur | [fvpok](https://github.com/fvpok) |
| Hugo Fedoroff | HUD Developer (Score and Lives) | [Hugofedoroff](https://github.com/Hugofedoroff) |
| Roch Le Pere De Graveron | Dependency Integration | [Rochdgrvrn](https://github.com/Rochdgrvrn) |
| Robin Sénéchal | Documentation and Communication | [Robinsène](https://github.com/Robinsène) |
| Eliott Siquier | HUD Developer (Level and Weapon/Power-up) | [eliottlemaxibg](https://github.com/eliottlemaxibg) |
| Ghali Benharbit | Gameplay integration | [GhaliBenh](https://github.com/GhaliBenh) |

## Team Requirements

Our team is responsible for **Requirement 8: Gameplay HUD**: the on-screen interface that shows the player real-time game state (score, lives, level, etc.) during gameplay, without obstructing play.

## Detailed Requirements

- **Score display**: Show the current score in a fixed HUD zone, updating in real time as enemies are destroyed.
- **Lives/health indicator**: Show remaining player lives (icon-based counter) or a health bar if the ship has hit points instead of discrete lives.
- **Wave/level indicator**: Display the current wave or level number, updating on level transitions.
- **Weapon/power-up status**: Show the player's currently equipped weapon or active power-up and where relevant, a cooldown/duration indicator.
- **Pause overlay**: Provide a pause button/key that shows a pause overlay (resume, restart, quit) without leaving the game screen.
- **Boss health bar**: Display a dedicated health bar for boss encounters, separate from the player's own indicator.

## Dependencies on Other Teams

- **Currency System**: Need the current currency/coin value exposed so the HUD can display it.
- **Item System**: Need item/power-up identifiers and icons so the HUD can render the correct active-item indicator.
- **Player & Enemy Ship Variety**: Need access to the player ship's stats (max lives/health, weapon type) so HUD elements reflect the selected ship correctly.
