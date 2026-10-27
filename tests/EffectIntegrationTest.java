import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import engine.*;
import entity.*;
import entity.Ship.ShipType;
import screen.*;
import shop.*;

/** Run in an isolated build/save directory, like the other standalone tests. */
public final class EffectIntegrationTest {
    private static int checks;
    private static Field field(Class<?> type, String name) throws Exception {
        Field f = type.getDeclaredField(name); f.setAccessible(true); return f;
    }
    private static Object get(Object target, String name) throws Exception {
        return field(target.getClass(), name).get(target);
    }
    private static Object call(Object target, String name, Class<?>[] types, Object... args) throws Exception {
        Method m = target.getClass().getDeclaredMethod(name, types); m.setAccessible(true);
        return m.invoke(target, args);
    }
    private static void check(boolean value, String message) {
        checks++; if (!value) throw new AssertionError(message);
    }
    private static void buy(PurchaseManager p, ShopItem item) {
        check(p.isOwned(item) || p.purchase(item, shop.Currency.COINS) == PurchaseManager.PurchaseResult.SUCCESS,
                "Purchase " + item.getName());
    }
    private static BufferedImage canvas() throws Exception {
        BufferedImage image = new BufferedImage(448, 520, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        field(DrawManager.class, "backBufferGraphics").set(null, g);
        for (String suffix : new String[]{"Regular", "Big", "Selected"}) {
            Font font = (Font) field(DrawManager.class, "font" + suffix).get(null);
            field(DrawManager.class, "font" + suffix + "Metrics").set(null, g.getFontMetrics(font));
        }
        return image;
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void main(String[] args) throws Exception {
        Core.getLogger().setUseParentHandlers(false);
        DrawManager dm = Core.getDrawManager();
        // Verify every resource's bit pattern against its registered sprite, not just its size.
        Map<DrawManager.SpriteType, boolean[][]> sprites =
                (Map<DrawManager.SpriteType, boolean[][]>) field(DrawManager.class, "spriteMap").get(null);
        java.util.List<String> bits = new ArrayList<>();
        for (String line : Files.readAllLines(Paths.get("res/graphics")))
            if (line.matches("[01]+")) bits.add(line);
        check(bits.size() == sprites.size(), "All sprites registered");
        int index = 0;
        for (Map.Entry<DrawManager.SpriteType, boolean[][]> entry : sprites.entrySet()) {
            StringBuilder actual = new StringBuilder();
            for (boolean[] col : entry.getValue()) for (boolean pixel : col) actual.append(pixel ? '1' : '0');
            check(actual.toString().equals(bits.get(index++)), "Sprite order/data " + entry.getKey());
        }
        CurrencyManager.getInstance().addCoins(10000);
        PurchaseManager p = PurchaseManager.getInstance();
        buy(p, ShopCatalog.findByRef(ShopCategory.SHIP, ShipType.BIG_BULLET.name()));
        ShapeSkin shape = ShapeSkin.BIG_BULLET_CANNON;
        buy(p, ShopCatalog.findByRef(ShopCategory.SHAPE, shape.name()));
        check(p.selectShip(ShipType.BIG_BULLET), "Select Big Bullet");
        check(p.selectShape(ShipType.BIG_BULLET, shape), "Select A1 look");
        for (ShopItem effect : ShopCatalog.getByCategory(ShopCategory.EFFECT)) {
            buy(p, effect);
            if (!p.isEffectOn(effect)) p.toggleEffect(effect);
        }
        check(ShopCatalog.getByCategory(ShopCategory.EFFECT).size() == 4, "All four effects");
        ShopItem ghost = ShopCatalog.findByRef(ShopCategory.EFFECT, "ghost_trail");
        check(p.toggleEffect(ghost) && !p.isEffectOn(ghost), "Switch effect off");
        field(PurchaseManager.class, "instance").set(null, null);
        p = PurchaseManager.getInstance();
        check(!p.isEffectOn(ghost), "Effect off persists");
        check(p.getSelectedShape(ShipType.BIG_BULLET) == shape, "Look persists with effects");
        check(p.getSelectedShip() == ShipType.BIG_BULLET, "Ship persists with effects");
        check(p.isEffectOn("comet_trail"), "Other effect remains on");
        p.toggleEffect(ghost);
        Core.setSelectedShipType(ShipType.BIG_BULLET);

        ShipSelectScreen customize = new ShipSelectScreen(448, 520, 60);
        Class rowType = Class.forName("screen.ShipSelectScreen$Row");
        Object[] rows = rowType.getEnumConstants();
        check(rows.length == 5, "Look and effect rows coexist");
        Object lookRow = Enum.valueOf(rowType, "SHAPE");
        Object effectRow = Enum.valueOf(rowType, "EFFECT");
        call(customize, "change", new Class[]{rowType, int.class}, effectRow, 1);
        check((Integer)get(customize,"effectIndex") == 1, "Effect navigation independent");
        check(p.getSelectedShape(ShipType.BIG_BULLET) == shape, "Effect browse retains look");
        call(customize, "change", new Class[]{rowType, int.class}, lookRow, -1);
        check(p.getSelectedShape(ShipType.BIG_BULLET) == null, "Look navigation still works");
        call(customize, "change", new Class[]{rowType, int.class}, lookRow, 1);
        check(p.getSelectedShape(ShipType.BIG_BULLET) == shape, "Look restored");
        BufferedImage image = canvas();
        dm.drawScreenTitle(customize, "Customize");
        call(customize, "drawPreview", new Class[]{});
        for (Object row : rows) call(customize, "drawRow", new Class[]{rowType}, row);
        field(ShipSelectScreen.class, "row").set(customize, effectRow);
        call(customize, "drawInfo", new Class[]{});
        dm.drawKeyHints(customize, "up/down row | left/right browse | space on/off | esc back");
        if (args.length > 0) ImageIO.write(image, "png", new File(args[0], "customize.png"));

        ShopScreen shop = new ShopScreen(448, 520, 60);
        Class tabType = Class.forName("screen.ShopScreen$Tab");
        Object[] tabs = tabType.getEnumConstants();
        check(tabs.length == 6 && tabs[5].toString().equals("NIGHT_MARKET"), "Six tabs, night market last");
        for (String tab : new String[]{"LOOKS", "EFFECTS"}) {
            call(shop, "showTab", new Class[]{tabType}, Enum.valueOf(tabType, tab));
            check(((java.util.List<?>) get(shop, "entries")).size() == (tab.equals("LOOKS") ? 12 : 4), "Shop entries " + tab);
            image = canvas(); dm.drawScreenTitle(shop, "Shop");
            for (String method : new String[]{"drawBalances","drawTabs","drawList","drawDetail"})
                call(shop, method, new Class[]{});
            dm.drawKeyHints(shop, "arrows/click select | C coins | V diamonds | esc back");
            if (args.length > 0) ImageIO.write(image,"png",new File(args[0], "shop-" + tab + ".png"));
        }
        GameSettings settings = (GameSettings) field(Core.class, "SETTINGS_LEVEL_6").get(null);
        GameScreen game = new GameScreen(new GameState(6,0,3,0,0,0), settings, false,448,520,60);
        game.initialize();
        Ship ship = (Ship)get(game,"ship");
        check(ship.getType() == ShipType.BIG_BULLET && ship.getSpriteType() == shape.getSprite(), "Game keeps ship and look");
        check((Boolean)get(game,"cometTrailOn") && (Boolean)get(game,"lightningOn")
                && get(game,"ghostTrail") != null && get(game,"warpIn") != null, "Four effects initialized with A1 loadout");
        EnemyShipFormation formation = (EnemyShipFormation)get(game,"enemyShipFormation");
        EnemyShip elite = null;
        for (EnemyShip enemy : formation) if(enemy.getHealth() == 2) {elite=enemy; break;}
        check(elite != null, "Two-hit enemy exists with effects");
        Set<Bullet> bullets = (Set<Bullet>)get(game,"bullets");
        LightningChain lightning = (LightningChain)get(game,"lightningChain");
        for(int hit=1;hit<=2;hit++) {
            bullets.add(new Bullet(elite.getPositionX(),elite.getPositionY(),-4));
            call(game,"manageCollisions",new Class[]{});
            check((Integer)get(lightning,"streak") == (hit==1?0:1), "Lightning only on final hit " + hit);
        }
        Bullet big = BulletPool.getCustomBullet(200,300,-6,18,30);
        image = canvas(); dm.drawBulletTrail(big); dm.drawEntity(big,200,300);
        check(big.getWidth()==18 && big.getHeight()==30, "Comet does not change Big Bullet dimensions");
        System.out.println("EffectIntegrationTest: " + checks + " checks passed");
    }
}
