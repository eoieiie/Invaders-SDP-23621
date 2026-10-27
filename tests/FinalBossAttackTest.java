import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import engine.Core;
import engine.Cooldown;
import engine.GameSettings;
import engine.GameState;
import engine.DrawManager.SpriteType;
import entity.Bullet;
import entity.BulletPool;
import entity.FinalBoss;
import screen.GameScreen;

/** Standalone checks for the Final Boss aimed shot, bullet rain and teleport. */
public final class FinalBossAttackTest {
    private static final int WIDTH = 448;
    private static final int HEIGHT = 520;
    private static final GameSettings SETTINGS = new GameSettings(5, 4, 8, 500);
    private static int checks;

    private static Object get(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object call(Object target, String name, Object... args) throws Exception {
        for (Method method : target.getClass().getDeclaredMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == args.length) {
                method.setAccessible(true);
                return method.invoke(target, args);
            }
        }
        throw new NoSuchMethodException(name);
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static FinalBoss boss(int x, int y) {
        return new FinalBoss(x, y, SpriteType.FinalBoss1);
    }

    @SuppressWarnings("unchecked")
    private static List<Integer> rainTargets(FinalBoss boss) throws Exception {
        return (List<Integer>) get(boss, "rainTargets");
    }

    public static void main(String[] args) throws Exception {
        Core.getLogger().setUseParentHandlers(false);
        mixedBulletSizesStaySeparate();
        recycledBulletsLoseSidewaysSpeed();
        aimedShotHeadsForThePlayer();
        bulletRainWarnsThenDropsOnePerLane();
        teleportWarnsThenJumpsAndHoldsFire();
        pausePreservesBossWarningsAndGracePeriod();
        specialAttacksNeverRepeat();
        destroyClearsPendingAttacks();
        System.out.println("PASS: " + checks + " checks");
    }

    @SuppressWarnings("unchecked")
    private static void mixedBulletSizesStaySeparate() throws Exception {
        Field poolField = BulletPool.class.getDeclaredField("pool");
        poolField.setAccessible(true);
        Set<Bullet> pool = (Set<Bullet>) poolField.get(null);
        pool.clear();

        Bullet large = BulletPool.getCustomBullet(100, 100, -6, 18, 30);
        large.setSpeedX(4);
        Set<Bullet> used = new HashSet<Bullet>();
        used.add(large);
        BulletPool.recycle(used);

        Bullet enemy = BulletPool.getBullet(200, 100, 5);
        check(enemy != large && enemy.getWidth() == Bullet.DEFAULT_WIDTH
                && enemy.getHeight() == Bullet.DEFAULT_HEIGHT,
                "Enemy shot does not reuse a large player bullet");
        check(enemy.getPositionX() == 200 - Bullet.DEFAULT_WIDTH / 2
                && enemy.getSpeedX() == 0, "Normal bullet is centered and reset");

        Bullet custom = BulletPool.getCustomBullet(150, 100, -6, 18, 30);
        check(custom == large && custom.getSpeedX() == 0,
                "Matching large bullet is reused without sideways speed");
        used.clear();
        used.add(enemy);
        used.add(custom);
        BulletPool.recycle(used);

        Bullet player = BulletPool.getBullet(50, 100, -6);
        check(player == enemy && player.getWidth() == Bullet.DEFAULT_WIDTH
                && player.getHeight() == Bullet.DEFAULT_HEIGHT,
                "Normal player shot keeps the normal collision size");
        pool.clear();
    }

    private static void recycledBulletsLoseSidewaysSpeed() {
        Bullet bullet = BulletPool.getBullet(100, 100, 5);
        bullet.setSpeedX(3);
        bullet.update();
        check(bullet.getPositionX() == 100 - bullet.getWidth() / 2 + 3,
                "Sideways speed moves the bullet");
        Set<Bullet> used = new HashSet<Bullet>();
        used.add(bullet);
        BulletPool.recycle(used);
        check(BulletPool.getBullet(50, 50, 5).getSpeedX() == 0,
                "Recycled bullet starts with no sideways speed");
    }

    private static void aimedShotHeadsForThePlayer() throws Exception {
        int[] targets = {400, 30, 224};
        for (int targetX : targets) {
            FinalBoss boss = boss(200, 60);
            int targetY = 480;
            boss.setTarget(targetX, targetY);
            Set<Bullet> bullets = new HashSet<Bullet>();
            call(boss, "shootAimed", bullets);
            check(bullets.size() == 3, "Aimed shot fires three bullets");

            int startX = 200 + 24;
            for (Bullet bullet : bullets) {
                check(bullet.getSpeed() >= 2, "Aimed bullets travel downward");
                check(Integer.signum(bullet.getSpeedX())
                        == Integer.signum(targetX - startX),
                        "Aimed bullets drift toward the player at x=" + targetX);
            }
            // Follow the lead bullet down to the player's height.
            Bullet lead = null;
            for (Bullet bullet : bullets)
                if (lead == null || bullet.getPositionY() > lead.getPositionY())
                    lead = bullet;
            while (lead.getPositionY() < targetY)
                lead.update();
            int centerX = lead.getPositionX() + lead.getWidth() / 2;
            check(Math.abs(centerX - targetX) <= 40,
                    "Aimed bullet reaches the player (x=" + centerX
                    + ", target " + targetX + ")");
        }
    }

    private static void bulletRainWarnsThenDropsOnePerLane() throws Exception {
        FinalBoss boss = boss(200, 60);
        boss.moveBoss(WIDTH, HEIGHT, SETTINGS);
        check((Boolean) call(boss, "canStartRain"), "Rain is available");
        call(boss, "startBulletRain");
        List<Integer> targets = rainTargets(boss);
        check(targets.size() == 5, "Rain has five drops");
        int laneWidth = WIDTH / 5;
        for (int lane = 0; lane < 5; lane++)
            check(targets.get(lane) / laneWidth == lane,
                    "Rain drop " + lane + " stays in its own lane");
        check(!(Boolean) call(boss, "canStartRain"),
                "No second rain while one is pending");

        Set<Bullet> bullets = new HashSet<Bullet>();
        check(!(Boolean) call(boss, "dropPendingRain", bullets)
                && bullets.isEmpty(), "Rain waits for its warning");
        Thread.sleep(900);
        check((Boolean) call(boss, "dropPendingRain", bullets),
                "Rain falls after the warning");
        check(bullets.size() == 5, "All five drops fall");
        for (Bullet bullet : bullets)
            check(bullet.getPositionY() == 45 && bullet.getSpeed() == 4
                    && bullet.getSpeedX() == 0, "Rain falls straight down from the top");
        check(rainTargets(boss).isEmpty(), "Rain warning cleared after falling");
    }

    private static void teleportWarnsThenJumpsAndHoldsFire() throws Exception {
        FinalBoss boss = boss(200, 100);
        call(boss, "startTeleport", WIDTH, HEIGHT);
        check((Boolean) get(boss, "teleporting"), "Teleport warning started");
        int destX = (Integer) get(boss, "teleportX");
        int destY = (Integer) get(boss, "teleportY");
        check(Math.hypot(destX - 200, destY - 100) >= 100,
                "Teleport destination is far enough away");
        check(destX >= 0 && destX <= WIDTH - 48 && destY >= 40
                && destY <= HEIGHT - 200 - 32, "Teleport destination is on screen");

        boolean seenHidden = false;
        boolean seenShown = false;
        long until = System.currentTimeMillis() + 300;
        while (System.currentTimeMillis() < until) {
            if (boss.isVisible()) seenShown = true; else seenHidden = true;
            boss.moveBoss(WIDTH, HEIGHT, SETTINGS);
            Thread.sleep(10);
        }
        check(seenHidden && seenShown, "Boss flickers during the warning");
        check(boss.getPositionX() == 200 && boss.getPositionY() == 100,
                "Boss stays put during the warning");
        Set<Bullet> bullets = new HashSet<Bullet>();
        check(!boss.shoot(bullets, SETTINGS) && bullets.isEmpty(),
                "Boss holds fire during the warning");

        Thread.sleep(500);
        boss.moveBoss(WIDTH, HEIGHT, SETTINGS);
        check(boss.getPositionX() == destX && boss.getPositionY() == destY,
                "Boss jumps to the destination");
        check(!(Boolean) get(boss, "teleporting") && boss.isVisible(),
                "Boss is visible again after the jump");
        check(!boss.shoot(bullets, SETTINGS) && bullets.isEmpty(),
                "Boss holds fire right after the jump");
        Thread.sleep(600);
        check(boss.shoot(bullets, SETTINGS) && !bullets.isEmpty(),
                "Boss fires again after the grace period");
    }

    private static void pausePreservesBossWarningsAndGracePeriod() throws Exception {
        GameScreen screen = new GameScreen(new GameState(10, 0, 3, 0, 0, 0),
                SETTINGS, false, WIDTH, HEIGHT, 60);
        screen.initialize();
        FinalBoss boss = (FinalBoss) get(screen, "midBoss");
        boss.moveBoss(WIDTH, HEIGHT, SETTINGS);
        call(boss, "startBulletRain");
        call(boss, "startTeleport", WIDTH, HEIGHT);
        int startX = boss.getPositionX();
        int startY = boss.getPositionY();

        set(screen, "paused", true);
        set(screen, "pauseStartedAt", System.currentTimeMillis());
        Thread.sleep(900);
        call(screen, "confirmPauseSelection");
        check(!(Boolean) get(screen, "paused"), "Pause menu resumes gameplay");
        Set<Bullet> bullets = new HashSet<Bullet>();
        check(!(Boolean) call(boss, "dropPendingRain", bullets)
                && bullets.isEmpty(), "Rain warning survives a long pause");
        boss.moveBoss(WIDTH, HEIGHT, SETTINGS);
        check((Boolean) get(boss, "teleporting")
                && boss.getPositionX() == startX && boss.getPositionY() == startY,
                "Teleport warning survives a long pause");

        call(boss, "finishTeleport");
        Cooldown holdFire = (Cooldown) get(boss, "holdFireCooldown");
        Thread.sleep(600);
        boss.resumeAfterPause(600);
        check(!holdFire.checkFinished(),
                "Post-teleport hold-fire time survives a long pause");
    }

    private static void specialAttacksNeverRepeat() throws Exception {
        FinalBoss boss = boss(200, 60);
        boss.moveBoss(WIDTH, HEIGHT, SETTINGS);
        Set<Bullet> bullets = new HashSet<Bullet>();
        Object previous = null;
        Set<Object> seen = new HashSet<Object>();
        for (int i = 0; i < 60; i++) {
            call(boss, "fireSpecialAttack", bullets);
            Object attack = get(boss, "lastSpecialAttack");
            check(attack != previous, "Special attack " + attack + " not repeated");
            seen.add(attack);
            previous = attack;
            rainTargets(boss).clear();
        }
        check(seen.size() == 3, "All three special attacks are used");
    }

    private static void destroyClearsPendingAttacks() throws Exception {
        FinalBoss boss = boss(200, 60);
        boss.moveBoss(WIDTH, HEIGHT, SETTINGS);
        call(boss, "startBulletRain");
        call(boss, "startTeleport", WIDTH, HEIGHT);
        boss.destroy();
        check(rainTargets(boss).isEmpty(), "Destroy cancels pending rain");
        check(!(Boolean) get(boss, "teleporting"), "Destroy cancels the teleport");
        Set<Bullet> bullets = new HashSet<Bullet>();
        check(!boss.shoot(bullets, SETTINGS) && bullets.isEmpty(),
                "Destroyed boss does not fire");
    }
}
