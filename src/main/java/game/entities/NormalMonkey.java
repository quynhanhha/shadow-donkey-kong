package game.entities;

import bagel.Image;
import bagel.util.Point;
import game.core.Config;
import game.core.GameObject;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a basic enemy monkey that patrols platforms.
 * Normal monkeys move back and forth along platforms and can be destroyed by Mario's hammer or bullets.
 */
public class NormalMonkey extends GameObject {
    /** Sprite for monkey facing left */
    private static final Image MONKEY_LEFT = new Image("res/normal_monkey_left.png");
    /** Sprite for monkey facing right */
    private static final Image MONKEY_RIGHT = new Image("res/normal_monkey_right.png");    

    /** List of distances for the monkey's patrol route */
    protected final List<Integer> route;
    /** Current index in the patrol route */
    protected int routeIndex = 0;
    /** Distance walked in current route segment */
    protected double walked = 0;

    /** Whether the monkey is facing right */
    protected boolean facingRight;
    /** Whether the monkey has been destroyed */
    protected boolean destroyed = false;
    /** List of platforms in the game */
    protected List<Platform> platforms;

    /** Movement speed of the monkey */
    protected static final double SPEED = 0.5;

    /**
     * Creates a new normal monkey at the specified position
     * @param x The x-coordinate of the monkey's starting position
     * @param y The y-coordinate of the monkey's starting position
     * @param direction Initial direction ("left" or "right")
     * @param routeStr Comma-separated list of distances for patrol route
     */
    public NormalMonkey(double x, double y, String direction, String routeStr) {
        super(direction.equals("right") ? "res/normal_monkey_right.png" : "res/normal_monkey_left.png", x, y);
        this.facingRight = direction.equals("right");
        this.route = Arrays.stream(routeStr.split(","))
                           .map(String::trim)
                           .map(Integer::parseInt)
                           .collect(Collectors.toList());
        updateImage();
    }

    /**
     * Sets the list of platforms for the monkey to patrol
     * @param platforms List of platforms in the game
     */
    public void setPlatforms(List<Platform> platforms) {
        this.platforms = platforms;
    }

    /**
     * Updates the monkey's position and state
     * Handles movement, platform snapping, and direction changes
     */
    public void update() {
        if (destroyed) return;
        
        // Move
        double dx = facingRight ? SPEED : -SPEED;
        position = new Point(position.x + dx, position.y);
        walked += Math.abs(dx);

        // Check if current route segment is done
        if (walked >= route.get(routeIndex)) {
            walked = 0;
            facingRight = !facingRight;
            updateImage();
            routeIndex = (routeIndex + 1) % route.size();
        }

        // Optional: screen edge bounce
        double halfWidth = image.getWidth() / 2.0;
        double screenW = Config.getAppInt("window.width");
        if ((position.x - halfWidth <= 0 && !facingRight) ||
            (position.x + halfWidth >= screenW && facingRight)) {
            // Reverse immediately
            walked = 0;
            facingRight = !facingRight;
            updateImage();
            // Don't advance routeIndex; treat as bounce
        }

        boolean standing = platforms.stream().anyMatch(p -> isHorizontallyOverlapping(p) && isCloseToTopOf(p, 5.0));

        if (!standing) {
            handleNoPlatformBelow(platforms);
        }

        snapToPlatform(platforms);
    }

    /**
     * Marks the monkey as destroyed
     */
    public void destroy() {
        destroyed = true;
    }

    /**
     * Checks if the monkey has been destroyed
     * @return true if the monkey is destroyed, false otherwise
     */
    public boolean isDestroyed() {
        return destroyed;
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
     * Checks for collision with Mario
     * @param mario The player character to check collision with
     * @return true if Mario collides with the monkey, false otherwise
     */
    public boolean intersectsMario(Mario mario) {
        return intersects(mario, 5.0, 5.0);
    }

    /**
     * Changes the monkey's direction and resets route progress
     */
    public void flipDirection() {
        walked = 0;
        routeIndex = (routeIndex + 1) % route.size();
        facingRight = !facingRight;
        updateImage();
    }
    
    /**
     * Updates the monkey's sprite based on direction
     */
    protected void updateImage() {
        image = facingRight ? MONKEY_RIGHT : MONKEY_LEFT;
    }
    
    /**
     * Handles the case when no platform is found below the monkey
     * @param platforms List of platforms to check
     */
    @Override
    protected void handleNoPlatformBelow(List<Platform> platforms) {
        // When no platform is found below, flip direction
        flipDirection();
    }
}
