package game.entities;

import game.core.GameObject;

/**
 * A platform that other entities can land on.
 */
public class Platform extends GameObject {
    /**
     * Creates a new platform at the specified position
     *
     * @param x X-coordinate of the platform's center
     * @param y Y-coordinate of the platform's center
     */
    public Platform(double x, double y) {
        super("res/platform.png", x, y); 
    }

    /**
     * Gets the height of the platform image
     */
    public double getHeight() {
        return image.getHeight();
    }
    
    /**
     * Gets the width of the platform image
     */
    public double getWidth() {
        return image.getWidth();
    }
    
    /**
     * Gets the top Y-coordinate of the platform
     */
    public double getTopY() {
        return position.y - image.getHeight() / 2.0;
    }
    
    /**
     * Checks if a point is above this platform
     */
    public boolean isPointAbove(double x, double y) {
        double left = position.x - image.getWidth() / 2.0;
        double right = position.x + image.getWidth() / 2.0;
        
        return x >= left && x <= right && y < getTopY();
    }
}
