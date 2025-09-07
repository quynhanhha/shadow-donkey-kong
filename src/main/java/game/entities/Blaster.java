package game.entities;

import game.core.GameObject;

/**
 * Represents a blaster power-up that can be collected by Mario.
 * When collected, it provides Mario with additional bullets.
 */
public class Blaster extends GameObject {
    private boolean collected = false;

    /**
     * Creates a new Blaster at the specified position.
     *
     * @param x The x-coordinate of the blaster
     * @param y The y-coordinate of the blaster
     */
    public Blaster(double x, double y) {
        super("res/blaster.png", x, y);
    }

    /**
     * Updates the blaster's state and handles collection by Mario.
     * When collected, adds 5 bullets to Mario's count.
     *
     * @param mario The player character that can collect the blaster
     */
    public void update(Mario mario) {
        if (!collected && intersects(mario)) {
            collected = true;
            
            // Add 5 bullets to Mario's count (this will accumulate if Mario already has a blaster)
            mario.collectBlaster(5);
        }
    }

    /**
     * Renders the blaster if it hasn't been collected yet.
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
}
