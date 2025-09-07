package game.entities;

import bagel.Image;
import bagel.util.Point;
import game.core.GameObject;
import java.util.List;

/**
 * Represents a barrel that Mario can jump over or destroy with a hammer.
 */
public class Barrel extends GameObject {
    // Physics constants
    private static final double GRAVITY = 0.4;
    private static final double MAX_FALL_SPEED = 5;

    // State variables
    private double velocityY = 0;
    private boolean destroyed = false;
    private boolean scoredThisFrame = false;

    /**
     * Creates a new barrel at the specified position
     */
    public Barrel(double x, double y) {
        super("res/barrel.png", x, y);
    }

    /**
     * Updates the barrel's position based on gravity
     */
    public void update() {
        // Apply gravity to simulate falling
        velocityY = Math.min(velocityY + GRAVITY, MAX_FALL_SPEED);
        position = new Point(position.x, position.y + velocityY);
    }

    /**
     * Gets the bottom Y coordinate of the barrel
     */
    public double getBottom() {
        return position.y + image.getHeight() / 2.0;
    }

    /**
     * Tries to snap the barrel to a platform if it's falling above one.
     * Returns true if the barrel was successfully positioned on a platform.
     */
    public boolean trySnapToPlatform(List<Platform> platforms) {
        for (Platform p : platforms) {
            if (isHorizontallyOverlapping(p) && isCloseToTopOf(p, image.getHeight())) {
                double platformTop = p.getY() - p.getImage().getHeight() / 2.0;
                position = new Point(position.x, platformTop - image.getHeight() / 2.0);
                velocityY = 0;  // stop falling
                return true;
            }
        }
        return false;
    }      

    /**
     * Checks if this barrel has been destroyed by Mario with a hammer
     */
    public boolean isDestroyed() {
        return destroyed;
    }

    /**
     * Marks this barrel as destroyed so it won't be rendered anymore
     */
    public void destroy() {
        destroyed = true;
    }

    /**
     * Sets the position of the barrel
     */
    public void setPosition(double x, double y) {
        this.position = new Point(x, y);
        this.velocityY = 0; // Reset velocity when repositioning
    }

    /**
     * Checks if the barrel can be scored for points in the current frame
     */
    public boolean canBeScoredAgain() { 
        return !scoredThisFrame; 
    }
    
    /**
     * Marks the barrel as having been scored this frame
     */
    public void markScored() { 
        scoredThisFrame = true;
    }
    
    /**
     * Resets per-frame flags, called at the start of each frame
     */
    public void resetFrameFlags() {
        scoredThisFrame = false;
    }

    @Override
    public void render() {
        if (!destroyed) {
            super.render();
        }
    }
}
