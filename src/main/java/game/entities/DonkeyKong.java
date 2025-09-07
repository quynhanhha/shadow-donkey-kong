package game.entities;

import game.core.Config;
import game.core.GameObject;

/**
 * Represents Donkey Kong, the main antagonist of the game.
 * Donkey Kong can be damaged by bullets and defeated with a hammer.
 */
public class DonkeyKong extends GameObject {
    /**
     * Creates a new Donkey Kong instance at the specified position
     * @param x The x-coordinate of Donkey Kong's position
     * @param y The y-coordinate of Donkey Kong's position
     */
    public DonkeyKong(double x, double y) {
        super("res/donkey_kong.png", x, y);
    }

    /**
     * Creates a new Donkey Kong instance at the default position from config
     */
    public DonkeyKong() {
        super("res/donkey_kong.png",
              Config.getAppInt("donkey.x"),
              Config.getAppInt("donkey.y"));
    }

    private int health = 5;

    /**
     * Gets Donkey Kong's current health
     * @return The current health value
     */
    public int getHealth() {
        return health;
    }

    /**
     * Reduces Donkey Kong's health by 1 when hit by a bullet
     */
    public void reduceHealth() {
        health = Math.max(0, health - 1);
    }

    /**
     * Checks if Donkey Kong has been defeated
     * @return true if Donkey Kong's health is 0, false otherwise
     */
    public boolean isDead() {
        return health == 0;
    }
}
