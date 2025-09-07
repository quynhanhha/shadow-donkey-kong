package game.entities;

import bagel.Image;
import bagel.util.Point;
import game.core.Config;
import game.core.GameObject;

/**
 * Represents a banana projectile thrown by intelligent monkeys.
 * Touching a banana results in a game over for Mario.
 */
public class Banana extends GameObject {
    private static final Image IMG = new Image("res/banana.png");
    private static final double SPEED = 1.8;
    private static final double MAX_DISTANCE = 300;

    private final double velocity;
    private double travelled = 0;

    /**
     * Creates a new banana instance at the specified position
     * @param x The x-coordinate of the banana's starting position
     * @param y The y-coordinate of the banana's starting position
     * @param moveRight Whether the banana should move right (true) or left (false)
     */
    public Banana(double x, double y, boolean moveRight) {
        super("res/banana.png", x, y);
        this.velocity = moveRight ? SPEED : -SPEED;
    }

    /**
     * Updates the banana's position and checks if it should be despawned
     * @return true if the banana has traveled its maximum distance, false otherwise
     */
    public boolean updateAndShouldDespawn() {
        position = new Point(position.x + velocity, position.y);
        travelled += Math.abs(velocity);

        return travelled >= MAX_DISTANCE ||
               position.x < 0 || position.x > Config.getAppInt("window.width");
    }
}
