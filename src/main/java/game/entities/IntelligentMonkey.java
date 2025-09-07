package game.entities;

import bagel.Image;
import bagel.util.Point;

/**
 * Represents an intelligent monkey that can throw bananas at Mario.
 * Intelligent monkeys patrol platforms like normal monkeys but can also attack by throwing bananas.
 */
public class IntelligentMonkey extends NormalMonkey {
    /** Sprite for intelligent monkey facing left */
    private static final Image IMG_LEFT = new Image("res/intelli_monkey_left.png");
    /** Sprite for intelligent monkey facing right */
    private static final Image IMG_RIGHT = new Image("res/intelli_monkey_right.png");

    /** Number of frames between banana throws */
    private static final int FIRE_INTERVAL_FRAMES = 300;
    /** Counter for banana throw cooldown */
    private int fireCooldown = 0;

    /**
     * Creates a new intelligent monkey at the specified position
     * @param x The x-coordinate of the monkey's starting position
     * @param y The y-coordinate of the monkey's starting position
     * @param direction Initial direction ("left" or "right")
     * @param routeStr Comma-separated list of distances for patrol route
     */
    public IntelligentMonkey(double x, double y, String direction, String routeStr) {
        super(x, y, direction, routeStr);
    }

    /**
     * Updates the monkey's state, including movement and attack cooldown
     */
    @Override
    public void update() {
        super.update();  // this handles movement, snapping, edge turning
        if (!isDestroyed()) {
            fireCooldown++;
        }
    }    

    /**
     * Checks if the monkey should throw a banana
     * @return true if the monkey should throw a banana, false otherwise
     */
    public boolean shouldFireBanana() {
        if (isDestroyed()) return false;
        if (fireCooldown >= FIRE_INTERVAL_FRAMES) {
            fireCooldown = 0;
            return true;
        }
        return false;
    }

    /**
     * Gets the position where a banana should be spawned
     * @return The point where the banana should be created
     */
    public Point getBananaSpawnPoint() {
        return getPosition();
    }

    /**
     * Updates the monkey's sprite based on direction
     */
    @Override
    protected void updateImage() {
        image = facingRight ? IMG_RIGHT : IMG_LEFT;
    }

    /**
     * Renders the monkey if it hasn't been destroyed
     */
    @Override
    public void render() {
        if (!destroyed) {
            super.render();
        }
    }

    /**
     * Gets the monkey's current facing direction
     * @return true if the monkey is facing right, false if facing left
     */
    public boolean isFacingRight() {
        return facingRight;
    }
}


