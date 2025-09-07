package game.entities;

import game.core.GameObject;
import game.core.Config;
import bagel.util.Point;

/**
 * Represents a bullet projectile that can be fired by Mario's blaster.
 * Bullets travel horizontally and can destroy monkeys or damage Donkey Kong.
 */
public class Bullet extends GameObject {
    private final double velocity;
    private double distanceTravelled = 0;
    private static final double MAX_DISTANCE = 300;

    /**
     * Creates a new bullet at the specified position
     * @param x The x-coordinate of the bullet's starting position
     * @param y The y-coordinate of the bullet's starting position
     * @param facingRight Whether the bullet should move right (true) or left (false)
     */
    public Bullet(double x, double y, boolean facingRight) {
        super(facingRight ? "res/bullet_right.png" : "res/bullet_left.png", x, y);
        this.velocity = facingRight ? 3.8 : -3.8;
    }

    /**
     * Updates the bullet's position and checks if it should be removed
     * @return true if the bullet should be removed (reached max distance or left screen), false otherwise
     */
    public boolean updateAndShouldDespawn() {
        position = new Point(position.x + velocity, position.y);
        distanceTravelled += Math.abs(velocity);
        return distanceTravelled >= MAX_DISTANCE ||
               position.x < 0 || position.x > Config.getAppInt("window.width");
    }
}
