package entity;

import java.awt.Color;

/** A tiny sparkle that flies outward and fades away. */
public class Particle {

    private float x, y;              // position
    private float speedX, speedY;    // pixels per frame
    private int size;                // square size in pixels
    private Color color;
    private long born;               // creation time
    private int lifetime;            // milliseconds

    public Particle(final float x, final float y, final Color color) {
        double angle = Math.random() * Math.PI * 2;      // random direction
        float speed = 0.5f + (float) Math.random() * 2f; // random speed
        this.x = x;
        this.y = y;
        this.speedX = (float) Math.cos(angle) * speed;
        this.speedY = (float) Math.sin(angle) * speed;
        this.size = Math.random() < 0.5 ? 2 : 3;
        this.color = color;
        this.born = System.currentTimeMillis();
        this.lifetime = 400 + (int) (Math.random() * 300);
    }

    /** Moves the particle; it slows down a little each frame. */
    public final void update() {
        this.x += this.speedX;
        this.y += this.speedY;
        this.speedX *= 0.96f;
        this.speedY *= 0.96f;
    }

    /** 1 when new, 0 when fully faded. */
    public final float getAlpha() {
        float age = (System.currentTimeMillis() - this.born)
                / (float) this.lifetime;
        return Math.max(0f, 1f - age);
    }

    public final boolean isDead() { return getAlpha() <= 0f; }
    public final int getX() { return Math.round(this.x); }
    public final int getY() { return Math.round(this.y); }
    public final int getSize() { return this.size; }
    public final Color getColor() { return this.color; }
}