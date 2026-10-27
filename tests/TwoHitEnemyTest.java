import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import engine.Core;
import engine.CoinDropManager;
import engine.GameSettings;
import engine.GameState;
import engine.DrawManager.SpriteType;
import entity.Bullet;
import entity.EnemyShip;
import entity.EnemyShipFormation;
import screen.GameScreen;

/** Standalone regression checks; run with isolated build/profile files. */
public final class TwoHitEnemyTest {
    private static int checks;

    private static Object get(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void call(Object target, String name) throws Exception {
        Method method = target.getClass().getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(target);
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    @SuppressWarnings("unchecked")
    private static Set<Bullet> bullets(GameScreen screen) throws Exception {
        return (Set<Bullet>) get(screen, "bullets");
    }

    private static void hit(GameScreen screen, EnemyShip enemy) throws Exception {
        bullets(screen).add(new Bullet(enemy.getPositionX(), enemy.getPositionY(), -4));
        call(screen, "manageCollisions");
    }

    public static void main(String[] args) throws Exception {
        Core.getLogger().setUseParentHandlers(false);
        for (int level = 1; level <= 10; level++) {
            Field field = Core.class.getDeclaredField("SETTINGS_LEVEL_" + level);
            field.setAccessible(true);
            GameSettings settings = (GameSettings) field.get(null);
            check(settings.isDiagonalMovement() == (level == 3 || level == 9),
                "Diagonal stage placement " + level);
            EnemyShipFormation formation = new EnemyShipFormation(settings);
            int elites = 0;
            int ordinary = 0;
            for (EnemyShip enemy : formation) {
                if (enemy.getHealth() == 2) elites++;
                else ordinary++;
            }
            check(elites == (level == 6 ? 4 : level == 9 ? 7 : 0),
                "Stage placement " + level);
            check(ordinary + elites == settings.getFormationWidth()
                * settings.getFormationHeight(), "Formation size " + level);
        }
        for (int level : new int[] {6, 9}) {
            Field field = Core.class.getDeclaredField("SETTINGS_LEVEL_" + level);
            field.setAccessible(true);
            GameSettings settings = (GameSettings) field.get(null);
            Set<String> layouts = new java.util.HashSet<>();
            boolean upperRowElite = false;
            for (int attempt = 0; attempt < 50; attempt++) {
                int elites = 0;
                StringBuilder layout = new StringBuilder();
                for (EnemyShip enemy : new EnemyShipFormation(settings)) {
                    if (enemy.getHealth() == 2) {
                        elites++;
                        layout.append(enemy.getPositionX()).append(',')
                            .append(enemy.getPositionY()).append(';');
                        upperRowElite |= enemy.getPositionY()
                            < 100 + 40 * (settings.getFormationHeight() - 1);
                    }
                }
                check(elites == (level == 6 ? 4 : 7), "Exact random count " + level);
                layouts.add(layout.toString());
            }
            check(layouts.size() > 1, "Layouts vary across starts " + level);
            check(upperRowElite, "Selection includes upper rows " + level);
        }
        EnemyShip strongHit = new EnemyShip(0, 0, SpriteType.EnemyShipA1, 2);
        check(!strongHit.takeDamage(0), "Zero damage must not kill");
        check(strongHit.takeDamage(2), "Damage amount must be respected");
        check(!strongHit.takeDamage(1), "Lethal hit cannot be repeated");

        GameSettings combined = new GameSettings(3, 2, 10, 1000, true, 2);
        check(combined.isDiagonalMovement() && combined.getTwoHitEnemyCount() == 2,
            "Both settings can be enabled together");
        EnemyShipFormation combinedFormation = new EnemyShipFormation(combined);
        combinedFormation.attach(new screen.Screen(448, 520, 60));
        java.util.Map<EnemyShip, int[]> positions = new java.util.HashMap<>();
        int combinedElites = 0;
        for (EnemyShip enemy : combinedFormation) {
            positions.put(enemy, new int[] {enemy.getPositionX(), enemy.getPositionY()});
            if (enemy.getHealth() == 2) combinedElites++;
        }
        check(combinedElites == 2, "Combined formation has two elites");
        for (int frame = 0; frame < 20; frame++) combinedFormation.update();
        for (EnemyShip enemy : combinedFormation) {
            int[] before = positions.get(enemy);
            check(enemy.getPositionX() - before[0] == 8
                && enemy.getPositionY() - before[1] == 4,
                "Combined formation moves diagonally with spacing preserved");
        }

        GameScreen screen = new GameScreen(new GameState(6, 0, 3, 0, 0, 0),
            new GameSettings(3, 2, 10, 1000, 1), false, 448, 520, 60);
        screen.initialize();
        ((CoinDropManager) get(screen, "coinDropManager")).setDropChance(1);
        EnemyShipFormation formation = (EnemyShipFormation) get(screen, "enemyShipFormation");
        EnemyShip elite = null;
        EnemyShip ordinary = null;
        for (EnemyShip enemy : formation) {
            if (enemy.getHealth() == 2) elite = enemy;
            else ordinary = enemy;
        }
        check(elite != null && Color.YELLOW.equals(elite.getColor()), "Elite is yellow");
        int killsBefore = Core.getAchievementManager().getTotalEnemiesKilled();
        boolean couldShoot = ((java.util.List<?>) get(formation, "shooters")).contains(elite);
        hit(screen, elite);
        check(elite.getHealth() == 1 && !elite.isDestroyed(), "First hit survives");
        check(Color.WHITE.equals(elite.getColor()), "First hit changes color");
        check(bullets(screen).isEmpty(), "Nonlethal bullet consumed");
        check((Integer) get(formation, "shipCount") == 6, "First hit keeps formation count");
        check(((java.util.List<?>) get(formation, "shooters")).contains(elite) == couldShoot,
            "First hit keeps shooting eligibility");
        check(screen.getGameState().getScore() == 0, "No score for first hit");
        check(screen.getGameState().getShipsDestroyed() == 0, "No kill for first hit");
        check(((Set<?>) get(screen, "coins")).isEmpty(), "No coin for first hit");
        check(Core.getAchievementManager().getTotalEnemiesKilled() == killsBefore,
            "No achievement progress for first hit");
        call(screen, "manageCollisions");
        check(elite.getHealth() == 1, "Consumed bullet cannot hit next frame");

        hit(screen, elite);
        check(elite.isDestroyed(), "Second hit destroys");
        check(screen.getGameState().getScore() == elite.getPointValue(), "One score reward");
        check(screen.getGameState().getShipsDestroyed() == 1, "One kill");
        check(((Set<?>) get(screen, "coins")).size() == 1, "One coin reward");
        check(Core.getAchievementManager().getTotalEnemiesKilled() == killsBefore + 1,
            "One achievement increment");
        check((Integer) get(formation, "shipCount") == 5, "Count reduced only on death");
        check(!((java.util.List<?>) get(formation, "shooters")).contains(elite),
            "Dead shooter replaced");
        hit(screen, elite);
        check(screen.getGameState().getShipsDestroyed() == 1, "Dead enemy cannot reward twice");
        bullets(screen).clear();
        hit(screen, ordinary);
        check(ordinary.isDestroyed(), "Ordinary enemy remains one-hit");

        // Preserve existing wide-bullet multi-target collisions, once per enemy.
        call(screen, "restartLevel");
        formation = (EnemyShipFormation) get(screen, "enemyShipFormation");
        int left = Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        for (EnemyShip enemy : formation) {
            left = Math.min(left, enemy.getPositionX());
            top = Math.min(top, enemy.getPositionY());
        }
        bullets(screen).add(new Bullet(left, top, -4, 200, 100));
        call(screen, "manageCollisions");
        int totalHealth = 0;
        for (EnemyShip enemy : formation) totalHealth += enemy.getHealth();
        check(totalHealth == 1, "Wide bullet damages each overlapping enemy once");
        check(bullets(screen).isEmpty(), "Wide bullet consumed");
        call(screen, "restartLevel");
        formation = (EnemyShipFormation) get(screen, "enemyShipFormation");
        for (EnemyShip enemy : formation) {
            hit(screen, enemy);
            if (!enemy.isDestroyed()) hit(screen, enemy);
        }
        check((Integer) get(formation, "shipCount") == 0, "Wave can be cleared");
        check(screen.getGameState().getShipsDestroyed() == 6, "Mixed wave counts six kills");
        System.out.println("PASS: " + checks + " checks");
    }
}
