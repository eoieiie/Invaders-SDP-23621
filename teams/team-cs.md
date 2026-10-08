# Team CS

## Team Introduction

Team CS is responsible for designing and implementing the **Item System** for the Space Invaders IC-PBL project. Our goal is to add a flexible, extensible item system (pickups, effects, and inventory-related state) that integrates cleanly with the existing gameplay loop and with the systems owned by other teams (Currency, HUD, Visual Effects, Sound).

### Member Roles

| Member | GitHub | Role |
| --- | --- | --- |
| Kim HyunChan | [whitson1117](https://github.com/whitson1117) | Team Leader |
| Han JaeHyuk | [doha1208](https://github.com/doha1208) | Github Manager, QA Manager |
| Son DongYeol | [playlistSDY](https://github.com/playlistSDY) | Item data model & architecture |
| Cho SeongGil | [ancho040220](https://github.com/ancho040220) | Director, Developer  |
| Choi Jian | [choichoi10](https://github.com/choichoi10) | Director, Developer |
| Kim JiHo | [GTMBB](https://github.com/GTMBB) | Director, Developer |
| Choi JiMin | [zPHf25N](https://github.com/zPHf25N) | Developer + (visual) |
| Kim HanGyeol | [han31415920512](https://github.com/han31415920512) | Developer |

## Team Requirements

Team CS owns the **Item System** requirement: items that spawn during gameplay, can be collected by the player, and modify gameplay state (player stats, score, currency, or ship behavior) when used or applied.

## Detailed Requirements

1. **Item Drop System** — Destroying the red (special) ship guarantees an item drop (100% rate). Destroying a regular alien drops an item with a 15% base probability. These are Team CS's initial values and may be tuned together with the Level Design team as level balancing progresses.
2. **Item Pickup & Storage** — A dropped item is collected when the player's ship touches it. Passive items apply their effect immediately on pickup. Active items are stored and only applied when the player presses a dedicated "use item" key (Mario Kart item-box style).
3. **Active/Passive Item Classification** — Define a data structure that classifies each item as either Active (stored, triggered by key press) or Passive (instant effect on pickup), so other systems can query an item's category without depending on Item System internals.
4. **Life Item** — Grants an extra life. If the player's current lives are below the max-life cap (default: 5), the life is applied instantly; if the player is already at the cap, the pickup instead grants a 500-point score bonus so it is not wasted. The max-life cap is Team CS's initial value and may be adjusted together with the Level Design team.
5. **Shield Item** — On use/pickup, negates exactly one incoming hit within a 10-second window; the effect ends when that window expires or after it absorbs one hit, whichever comes first.
6. **Rapid Fire Item** — Increases the player's firing rate for the rest of the run (it carries over between levels). The first pickup adds +0.5 shots per second on top of the base rate; further pickups stack with diminishing gains (bonus × log2(1 + stacks)), up to 10 stacks. Each stack the player already has lowers the item's drop weight, and it stops dropping at the stack limit.
7. **Bullet Speed Item** — Increases projectile speed for the rest of the run (it carries over between levels). The first pickup adds +1 pixel per frame on top of the base bullet speed; further pickups stack the same way as Rapid Fire (bonus × log2(1 + stacks), up to 10 stacks), with the same drop-weight reduction.
8. **Freeze Item** — On use, disables all enemy movement for 5 seconds.

## Dependencies on Other Teams

1. **Visual Effect System** — Item drop appearance and pickup/activation visuals need to be agreed upon with the Visual Effect (graphics) team.
2. **Level Design System** — The initial drop-probability and max-life-cap values above are Team CS's own working defaults; they need to be reviewed and tuned jointly with the Level Design team as overall level balance is set. All item balance values (drop rates and weights, effect durations, stack bonuses and limits, life cap and bonus score) are kept in `res/item-balance.properties`, so they can be tuned without recompiling.
3. **Gameplay HUD** — Displaying which Active item the player is currently holding (before it is used) requires a shared interface with the HUD team.