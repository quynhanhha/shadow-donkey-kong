package game.entities;

import bagel.Image;
import bagel.util.Point;
import game.core.GameObject;

/**
 * Represents a hammer that Mario can pick up and use to defeat Donkey Kong.
 * The hammer has a limited duration and can be used to destroy enemies.
 */
public class Hammer extends GameObject {
    private boolean collected = false;

    /**
     * Creates a new hammer instance at the specified position
     * @param x The x-coordinate of the hammer's position
     * @param y The y-coordinate of the hammer's position
     */
    public Hammer(double x, double y) {
        super("res/hammer.png", x, y);
    }

    /**
     * Updates the hammer's state and handles collection by Mario
     * @param mario The player character that can collect the hammer
     */
    public void update(Mario mario) {
        if (!collected && intersects(mario)) {
            collected = true;
            mario.collectHammer();
        }
    }

    /**
     * Renders the hammer if it hasn't been collected yet
     */
    @Override
    public void render() {
        if (!collected) {
            super.render();
        }
    }

    private boolean intersects(GameObject other) {
        return super.intersects(other, 5.0, 5.0);
    }

    /**
     * Checks if Mario is close enough to pick up the hammer
     * @param mario The Mario instance to check distance from
     * @return true if Mario is within pickup range, false otherwise
     */
    public boolean isMarioCloseEnoughToPickUp(Mario mario) {
        return Math.abs(mario.getX() - getX()) < 30;
    }
}
