package game.entities;

import bagel.util.Point;
import game.core.GameObject;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Represents a ladder that Mario can climb between platforms
 */
public class Ladder extends GameObject {
    // Relative ladder height factor for determining appropriate platform connections
    private static final double PLATFORM_GAP_FACTOR = 1.5;
    
    /**
     * Creates a new ladder at the specified position
     */
    public Ladder(double x, double y) {
        super("res/ladder.png", x, y);
    }

    /**
     * Connects this ladder to the most appropriate platform above it
     * Uses a smart algorithm to detect closely-spaced platforms and choose
     * the optimal connection point
     * 
     * @param platforms List of platforms in the game
     */
    public void connectToPlatformAbove(List<Platform> platforms) {
        // Get platforms above this ladder that are horizontally aligned
        List<Platform> platformsAbove = platforms.stream()
            .filter(this::isHorizontallyAligned)
            .filter(p -> isPlatformAbove(p))
            .filter(p -> getDistanceToTop(p) <= 100.0)
            .sorted(Comparator.comparingDouble(p -> getDistanceToTop(p)))
            .toList();
        
        if (platformsAbove.isEmpty()) {
            return; // No suitable platforms found
        }
        
        // Get the two closest platforms (if available)
        Platform nearest = platformsAbove.get(0);
        Platform secondNearest = platformsAbove.size() > 1 ? platformsAbove.get(1) : null;
        
        // Choose the best platform to connect to
        Platform targetPlatform = chooseBestPlatform(nearest, secondNearest);
        
        // Connect the ladder to the chosen platform
        double platformTop = getTopOfPlatform(targetPlatform);
        position = new Point(position.x, platformTop + getImage().getHeight() / 2.0);
    }
    
    /**
     * Chooses the optimal platform to connect to based on spacing
     */
    private Platform chooseBestPlatform(Platform nearest, Platform secondNearest) {
        // If there's no second option, use the nearest
        if (secondNearest == null) {
            return nearest;
        }
        
        // Check if the gap between platforms is small enough to prefer the second one
        double nearestTop = getTopOfPlatform(nearest);
        double secondTop = getTopOfPlatform(secondNearest);
        double ladderHeight = getImage().getHeight();
        
        // Use second nearest when platforms are close together
        if (Math.abs(secondTop - nearestTop) <= PLATFORM_GAP_FACTOR * ladderHeight) {
            return secondNearest;
        }
        
        return nearest;
    }
    
    /**
     * Gets the top Y coordinate of a platform
     */
    private double getTopOfPlatform(Platform platform) {
        return platform.getY() - platform.getImage().getHeight() / 2.0;
    }
    
    /**
     * Checks if this ladder is horizontally aligned with a platform
     */
    private boolean isHorizontallyAligned(Platform platform) {
        return Math.abs(platform.getX() - position.x) < platform.getImage().getWidth() / 2.0;
    }
    
    /**
     * Checks if a platform is above this ladder
     */
    private boolean isPlatformAbove(Platform platform) {
        return getTopOfPlatform(platform) < position.y;
    }
    
    /**
     * Calculates the distance from this ladder to the top of a platform
     */
    private double getDistanceToTop(Platform platform) {
        return position.y - getTopOfPlatform(platform);
    }

    public double getLeft() {
        return position.x - getImage().getWidth() / 2.0;
    }
    
    public double getRight() {
        return position.x + getImage().getWidth() / 2.0;
    }    
}


