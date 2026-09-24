# Sound Effects

## Team Introduction

Our team wants to incorporate sound effects to enhance player engagement and create a more immersive experience.

## Members

| Name                               | Role                   | GitHub                                                      |
|------------------------------------|------------------------|-------------------------------------------------------------|
| Jung Seungwoo                      | Team Leader            | [swjung2313](https://github.com/swjung2313)                 |
| Muhammed Ali Garanli               | Software Developer/Git | [AyeJay21](https://github.com/AyeJay21)                     |
| Axel Jeremy Raphael Agat Tutkovics | Background effects     | [Axel-ece](https://github.com/Axel-ece)                     |
| Elio Ellini                        | Background effects     | [elioeln](https://github.com/elioeln)                       |
| Massyl Ait Ali Belkacem            | Soundeffect            | [Ma55yl](https://github.com/Ma55yl)                         |
| Valentin HEBRAS LECLERC            | Software Developer     | [Valentin HEBRAS LECLERC](https://github.com/Valzzzzzzzzzz) |
| Erwan GUILLEM                      | Soundeffect            | [R2D2-4997](https://github.com/R2D2-4997)                   |
| Solal Coupin Dagnet                | Background effects     | [solal67](https://github.com/solal67)                       |
| Lee Inseob                           | Software Developer     | [liss1110](https://github.com/liss1110)                      |

## Team Requirements
Our team is responsible for the overall design, integration, and management of the project's audio system. This work involves setting up an audio framework to manage playback, volume, and audio channels across the application.
The team is also in charge of creating and triggering sound effects for gameplay actions, UI elements, and state changes. Finally, the role includes managing audio files, loading them, and handling memory allocation to ensure smooth, lag-free performance.

## Detailed Requirements

* **Weapon & Combat SFX**
    * **Shooting:** Crisp audio feedback for firing projectiles.
    * **Player Damage & Death:** Distinct sound effects for taking damage and player destruction/death sequence.
    * **Enemy Destruction:** Dynamic explosion sounds when defeating standard enemies or bosses.
* **Dynamic BGM & Atmospheric Audio**
    * **Background Music (BGM):** Looping tracks tailored to main menu, active gameplay, and victory states.
    * **Intensive Phase:** Dynamic music transitions during high-difficulty moments (e.g., remaining enemies speed up).
    * **Event Tracks:** Dedicated audio sequences for "Level Complete" and "Game Over" screens.
* **Interactions & Power-Ups**
    * **Level-Up & Pickups:** Audio cues for picking up items, activating temporary boosts, and leveling up.

## Dependencies on Other Systems

* **Visual Effect System:** Audio triggers must synchronize with visual animations (e.g., explosions, screen shakes, and hit flashes).
* **Level Design System:** Needs integration with level progression logic to seamlessly switch BGM tracks and trigger intensive phases.
* **Gameplay HUD & UI:** Requires event listeners for UI interactions (e.g., button clicks, pause menu toggle, and health bar notifications).
