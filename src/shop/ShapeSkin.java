package shop;

import java.util.ArrayList;
import java.util.List;

import engine.DrawManager.SpriteType;
import entity.Ship.ShipType;

/**
 * Alternative shapes (sprites) for the player ships (Team A1 - Player &amp;
 * Enemy Ship Variety). A shape skin only changes how a ship looks: its
 * hitbox and stats stay those of its {@link ShipType}. It is sold in the shop
 * and chosen in Customize, and it combines with the colour {@link Skin}.
 *
 * Each ship type has its own three shapes; the Standard ship has none.
 * See src/A1-player-ship-skins.md for the sprites.
 *
 * @author A1 - Player &amp; Enemy Ship Variety
 */
public enum ShapeSkin {
    /** Fast Attack: sharper arrowhead. */
    FAST_ATTACK_ARROW("Arrow", ShipType.FAST_ATTACK, SpriteType.ShipSkinA1,
            "Sharper arrowhead with spiked wings."),
    /** Fast Attack: twin muzzles. */
    FAST_ATTACK_TWIN_FANG("Twin Fang", ShipType.FAST_ATTACK,
            SpriteType.ShipSkinA2, "Twin muzzles for a rapid-fire ship."),
    /** Fast Attack: hollow outline. */
    FAST_ATTACK_STEALTH("Stealth", ShipType.FAST_ATTACK,
            SpriteType.ShipSkinA3, "A light, hollow outline of the hull."),

    /** Big Bullet: heavy barrel. */
    BIG_BULLET_CANNON("Cannon", ShipType.BIG_BULLET, SpriteType.ShipSkinB1,
            "A thick U-shaped barrel on a wide base."),
    /** Big Bullet: rounded hull. */
    BIG_BULLET_MORTAR("Mortar", ShipType.BIG_BULLET, SpriteType.ShipSkinB2,
            "A short muzzle on a rounded hull with legs."),
    /** Big Bullet: battlements. */
    BIG_BULLET_FORTRESS("Fortress", ShipType.BIG_BULLET,
            SpriteType.ShipSkinB3, "A tall muzzle on a solid, battlemented base."),

    /** Fast Move: sharper wing tips. */
    FAST_MOVE_JET("Jet", ShipType.FAST_MOVE, SpriteType.ShipSkinC1,
            "A longer nose and wing tips that rise into spikes."),
    /** Fast Move: three thrusters. */
    FAST_MOVE_TRIPLE_THRUSTER("Triple Thruster", ShipType.FAST_MOVE,
            SpriteType.ShipSkinC2, "Three thrusters side by side."),
    /** Fast Move: hollow outline. */
    FAST_MOVE_STEALTH("Stealth", ShipType.FAST_MOVE, SpriteType.ShipSkinC3,
            "A light, hollow outline of the hull."),

    /** Two-Way Shot: longer barrels. */
    TWO_WAY_LONG_BARRELS("Long Barrels", ShipType.TWO_WAY,
            SpriteType.ShipSkinD1, "Longer barrels on a gun carriage."),
    /** Two-Way Shot: thin barrels. */
    TWO_WAY_TWIN_SPIKE("Twin Spike", ShipType.TWO_WAY, SpriteType.ShipSkinD2,
            "Thin spike barrels and a notched hull."),
    /** Two-Way Shot: hollow frame. */
    TWO_WAY_HOLLOW("Hollow", ShipType.TWO_WAY, SpriteType.ShipSkinD3,
            "Barrels and upper hull as a hollow frame.");

    /** Name shown in the shop and in Customize. */
    private final String displayName;
    /** Ship type this shape belongs to. */
    private final ShipType shipType;
    /** Sprite drawn instead of the ship's own while it is not destroyed. */
    private final SpriteType sprite;
    /** Short description shown in the shop. */
    private final String description;

    ShapeSkin(final String displayName, final ShipType shipType,
              final SpriteType sprite, final String description) {
        this.displayName = displayName;
        this.shipType = shipType;
        this.sprite = sprite;
        this.description = description;
    }

    /** @return Name shown in the shop and in Customize. */
    public String getDisplayName() {
        return this.displayName;
    }

    /** @return Ship type this shape belongs to. */
    public ShipType getShipType() {
        return this.shipType;
    }

    /** @return Sprite drawn while the ship is not destroyed. */
    public SpriteType getSprite() {
        return this.sprite;
    }

    /** @return Short description shown in the shop. */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param type
     *            Ship type.
     * @return The shapes of that ship type, in declaration order; empty for
     *         ships without any (Standard).
     */
    public static List<ShapeSkin> forShip(final ShipType type) {
        List<ShapeSkin> result = new ArrayList<ShapeSkin>();
        for (ShapeSkin skin : values())
            if (skin.shipType == type)
                result.add(skin);
        return result;
    }
}