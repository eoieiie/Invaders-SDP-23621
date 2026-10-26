# Stage 5: MidBoss

- Health: 2 hits. Score: 100 per hit plus 1000 on defeat (1200 total).
- Movement: bounces horizontally and vertically; Stage 5 uses 5 pixels per update.
- Bounds: top 40; bottom screen height minus 200 and boss height.
- Collision size: 32x20, matching the rendered sprite.
- Attack: one downward bullet, speed 4.
- Shooting interval decreases by 30ms per shot to a 500ms minimum.
- The first shot waits for the configured initial cooldown.
- Destroyed bosses cannot fire or award a second defeat reward.

Increasing the boss baseSpeed makes it move faster. This differs from normal formations, where baseSpeed is a movement interval.
Summoning ordinary enemies is an optional idea and is not implemented here.
