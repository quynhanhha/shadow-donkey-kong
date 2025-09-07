package game.entities;

import bagel.util.Point;
import game.core.Config;
import game.core.GameObject;
import java.util.List;
import bagel.Input;
import bagel.Keys;
import bagel.Image;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
/**
 * Represents Mario, the main player character.
 */
public class Mario extends GameObject {

    // Physics constants
    private static final double GRAVITY = 0.2;
    private static final double MAX_FALL_SPEED = 10;
    private static final double JUMP_VELOCITY = -5;
    private static final double MOVE_SPEED = 3.5;
    private static final double JUMP_OVER_X_TOLERANCE = 5.0; 
    
    // Collision detection parameters
    private final double PLATFORM_BUFFER;
    private final double COLLISION_TOLERANCE;

    // Sprite images
    private static final Image SPRITE_LEFT = new Image("res/mario_left.png");
    private static final Image SPRITE_RIGHT = new Image("res/mario_right.png");
    private static final Image HAMMER_RIGHT = new Image("res/mario_hammer_right.png");
    private static final Image HAMMER_LEFT = new Image("res/mario_hammer_left.png");
    private static final Image BLASTER_RIGHT = new Image("res/mario_blaster_right.png");
    private static final Image BLASTER_LEFT = new Image("res/mario_blaster_left.png");
    
    // Physics state
    private double velocityY = 0;
    private double velocityX = 0;
    private boolean onGround = false;
    private boolean facingRight = true;
    private int climbingCooldownFrames = 0;
    
    // Game state
    private List<Platform> platforms;
    private List<Ladder> ladders = new ArrayList<>();
    private List<Barrel> barrels = new ArrayList<>();
    private boolean hasHammer = false;
    private int destroyedBarrels = 0;
    private int jumpedBarrels = 0;
    private double prevBottomY = 0;  // Initialize to prevent first-frame issues
    private Set<Barrel> jumpedThisJump = new HashSet<>(); // Track barrels jumped in current jump
    private boolean hasBlaster = false;
    private int bulletCount = 0;

    
    // Climbing state
    private boolean isClimbing = false;
    private boolean canClimb = false;
    private boolean isOnLadder = false;
    private boolean wasInAir = false;
    private static final double CLIMB_SPEED = 2.0;
    private static final double LADDER_DETECT_TOLERANCE = 15.0; 

    private boolean jumpedRecently = false;
    private static final int JUMP_RECENT_FRAMES = 40; 
    private int jumpRecentCounter = 0;
    private double jumpStartX = 0;
    private Ladder lastUsedLadder = null;
    private static final double LADDER_SCORING_EXCLUSION_ZONE = 20.0; 

    private int finalScore = 0;
    private int totalScore = 0;

    /**
     * Creates a new Mario instance at the specified position
     */
    public Mario(double x, double y, List<Platform> platforms) {
        super("res/mario_right.png", x, y);
        this.platforms = platforms;
        
        PLATFORM_BUFFER = getImage().getHeight() * 0.1; 
        COLLISION_TOLERANCE = getImage().getWidth() * 0.05; 
        
        this.prevBottomY = position.y + getImage().getHeight() / 2.0;
    }

    /**
     * Returns whether Mario has the hammer powerup
     */
    public boolean hasHammer() {
        return hasHammer;
    }

    /**
     * Gives Mario the hammer powerup
     */
    public void collectHammer() {
        hasHammer = true;
        hasBlaster = false; // hammer overrides blaster
        bulletCount = 0;
    }

    public void collectBlaster(int bullets) {
        // If already has a blaster, just add bullets
        if (hasBlaster) {
            bulletCount += bullets;
        } else {
            // First time collecting a blaster
            hasBlaster = true;
            bulletCount = bullets;
            hasHammer = false; // blaster overrides hammer
        }
    }
    
    public boolean hasBlaster() {
        return hasBlaster;
    }
    
    public boolean isOutOfBullets() {
        return bulletCount <= 0;
    }
    
    public Bullet tryShoot() {
        if (hasBlaster && bulletCount > 0) {
            bulletCount--;
            if (bulletCount == 0) {
                hasBlaster = false;
            }
            return new Bullet(getX(), getY(), facingRight);
        }
        return null;
    }    

    @Override
    public void update(Input input) {
        double currentBottom = position.y + getImage().getHeight() / 2.0;

        wasInAir = !onGround;  
        
        handleInput(input);
        
        checkLadderCollisions(ladders, input);

        if (!isOnLadder) {
            if (isClimbing) {
                climbingCooldownFrames = 10;
            }
            isClimbing = false;
        }
        
        climb(input, ladders);

        if (climbingCooldownFrames > 0) {
            climbingCooldownFrames--;
        }
        
        if (jumpRecentCounter > 0) {
            jumpRecentCounter--;
            if (jumpRecentCounter == 0) {
                jumpedRecently = false;
            }
        }
        
        if (isClimbing) {
            jumpedRecently = false;
            jumpRecentCounter = 0;
        }
        
        if (isClimbing && isOnLadder) {
            moveVertically();
        } else {
            applyGravity();
            moveVertically();
        }
        
        moveHorizontally();      
        updateSprite();

        if (onGround && wasInAir) {
            jumpedThisJump.clear();
        }        
    }

    /**
     * Processes keyboard input for Mario's movement
     */
    private void handleInput(Input input) {
        boolean left = input.isDown(Keys.LEFT);
        boolean right = input.isDown(Keys.RIGHT);
    
        // Horizontal movement
        if (left && !right) {
            velocityX = -MOVE_SPEED;
            facingRight = false;
        } else if (right && !left) {
            velocityX = MOVE_SPEED;
            facingRight = true;
        } else {
            velocityX = 0;
        }
    
        // Jumping
        if (input.wasPressed(Keys.SPACE) && onGround) {
            velocityY = JUMP_VELOCITY;
            onGround = false;
            jumpedRecently = true;
            jumpRecentCounter = JUMP_RECENT_FRAMES;
            jumpStartX = position.x;
            jumpedThisJump.clear();
        }
    }

    /**
     * Applies gravity when Mario is in the air
     */
    private void applyGravity() {
        if (!onGround && !isClimbing) {
            velocityY = Math.min(velocityY + GRAVITY, MAX_FALL_SPEED);
        }
    }    

    /**
     * Applies vertical movement
     */
    private void moveVertically() {
        position = new Point(position.x, position.y + velocityY);
    }

    /**
     * Applies horizontal movement with screen boundary checks
     */
    private void moveHorizontally() {
        double newX = position.x + velocityX;
        double halfWidth = getImage().getWidth() / 2.0;
        double screenWidth = Config.getAppInt("window.width");
    
        newX = Math.max(halfWidth, Math.min(newX, screenWidth - halfWidth));
        
        position = new Point(newX, position.y);
    }    

    /**
     * Updates Mario's sprite based on direction and power-up state
     */
    private void updateSprite() {
        // Determine sprite based on power-ups, with blaster taking priority over hammer
        if (hasBlaster && bulletCount > 0) {
            image = facingRight ? BLASTER_RIGHT : BLASTER_LEFT;
        } else if (hasHammer) {
            image = facingRight ? HAMMER_RIGHT : HAMMER_LEFT;
        } else {
            image = facingRight ? SPRITE_RIGHT : SPRITE_LEFT;
        }
    }

    /**
     * Checks and handles collisions with platforms
     */
    public void checkPlatformCollision(List<Platform> platforms) {
        this.platforms = platforms;

        if (isClimbing) {
            return; 
        }

        onGround = false;

        for (Platform platform : platforms) {
            if (isStandingOn(platform)) {
                snapToTopOfPlatform(platform);
                break;
            }
        }

        checkFallBelowScreen();
    }
    
    /**
     * Snaps Mario to the top of a platform
     */
    private void snapToTopOfPlatform(Platform platform) {
        double platformTop = platform.getY() - platform.getImage().getHeight() / 2.0;
        double marioHalfHeight = getImage().getHeight() / 2.0;
        
        position = new Point(position.x, platformTop - marioHalfHeight);
        velocityY = 0;
        onGround = true;
        
        // Clear the set of jumped barrels when landing
        if (!jumpedThisJump.isEmpty()) {
            jumpedThisJump.clear();
        }
    }

    /**
     * Gets the bottom Y coordinate of Mario
     */
    public double getBottomY() {
        return position.y + getImage().getHeight() / 2.0;
    }

    /**
     * Awards points for jumping over barrels
     */
    public void awardJumpPoints(List<Barrel> barrels, double oldBottomY) {
        if (barrels == null || barrels.isEmpty()) return;
        
        // Skip scoring if any of these early exit conditions are met
        if (shouldSkipJumpScoring()) {
            return;
        }
        
        double currentBottom = position.y + getImage().getHeight()/2;
        double horizontalMovementSinceJump = Math.abs(position.x - jumpStartX);
        boolean hasMovedHorizontallyEnough = horizontalMovementSinceJump > 1.5;
        
        // These counters were in the original code but not currently used elsewhere
        // Keeping them for compatibility
        int barrelsInRange = 0;
        int barrelsNearMissing = 0;
        
        // Sort barrels by proximity to Mario for consistent processing
        List<Barrel> sortedBarrels = getSortedBarrelsByProximity(barrels);
        
        // Process barrels for scoring
        for (Barrel barrel : sortedBarrels) {
            // Skip barrels that are already destroyed or already jumped over
            if (barrel.isDestroyed() || jumpedThisJump.contains(barrel)) {
                continue;
            }

            // Skip barrels not on the same vertical layer as Mario
            if (!isOnSameVerticalLayer(barrel)) {
                continue;
            }
            
            // Check if barrel is near a ladder that was recently used
            boolean nearRecentLadder = isBarrelNearRecentLadder(barrel);
            
            // Calculate position relationships between Mario and barrel
            PositionRelationship rel = getPositionRelationship(barrel, currentBottom, oldBottomY);
            
            // Don't allow scoring when standing still on same level as barrel
            boolean validJumpingState = (!onGround || !rel.isOnSamePlatformAsBarrel) && 
                                      rel.hasSignificantVerticalMovement;
            
            // Count barrels that are candidates for scoring (in proper range)
            if (rel.horizontallyAligned && rel.verticallyAligned) {
                barrelsInRange++;

                boolean almostScored = rel.horizontallyAligned && 
                               (rel.wasAbove || rel.verticallyAboveEnough) && 
                               rel.movedDownward && 
                               validJumpingState && 
                               !nearRecentLadder && 
                               barrel.canBeScoredAgain();
                               
                if (almostScored) {
                    barrelsNearMissing++;
                }
            }
            
            // Determine if this jump should score points
            if ((rel.wasAbove || rel.verticallyAboveEnough) && 
                rel.movedDownward && 
                rel.horizontallyAligned && 
                validJumpingState && 
                barrel.canBeScoredAgain() && 
                !nearRecentLadder &&
                hasMovedHorizontallyEnough) {
                
                jumpedBarrels++;
                barrel.markScored();
                jumpedThisJump.add(barrel);
                break;
            }
        }
    }
    
    /**
     * Checks if jump scoring should be skipped based on various conditions
     */
    private boolean shouldSkipJumpScoring() {
        if (climbingCooldownFrames > 0) return true;
        if (isClimbing) return true;
        if (Math.abs(velocityY) < 0.1 && onGround) return true;
        
        boolean nearAnyLadder = isNearAnyLadder();
        if (nearAnyLadder && !jumpedRecently && Math.abs(velocityY) < 0.1) return true;
        
        boolean validJumpState = jumpedRecently || velocityY > 0;
        if (!validJumpState && !onGround) return true;
        
        return false;
    }
    
    /**
     * Returns barrels sorted by proximity to Mario
     */
    private List<Barrel> getSortedBarrelsByProximity(List<Barrel> barrels) {
        List<Barrel> sortedBarrels = new ArrayList<>(barrels);
        sortedBarrels.sort((b1, b2) -> {
            double d1 = Math.abs(position.x - b1.getX());
            double d2 = Math.abs(position.x - b2.getX());
            return Double.compare(d1, d2);
        });
        return sortedBarrels;
    }
    
    /**
     * Checks if a barrel is on the same vertical layer as Mario
     */
    private boolean isOnSameVerticalLayer(Barrel barrel) {
        double marioCenterY = position.y;
        double barrelCenterY = barrel.getY();
        return Math.abs(marioCenterY - barrelCenterY) <= 70;
    }
    
    /**
     * Checks if a barrel is near a ladder that was recently used
     */
    private boolean isBarrelNearRecentLadder(Barrel barrel) {
        if (lastUsedLadder != null) {
            double ladderX = lastUsedLadder.getX();
            double barrelX = barrel.getX();
            double horizontalDistance = Math.abs(ladderX - barrelX);
            
            return horizontalDistance < LADDER_SCORING_EXCLUSION_ZONE;
        }
        return false;
    }
    
    /**
     * Class to store position relationships between Mario and a barrel
     */
    private class PositionRelationship {
        boolean isOnSamePlatformAsBarrel;
        boolean hasSignificantVerticalMovement;
        boolean wasAbove;
        boolean movedDownward;
        boolean horizontallyAligned;
        boolean verticallyAligned;
        boolean verticallyAboveEnough;
    }
    
    /**
     * Calculates position relationships between Mario and a barrel
     */
    private PositionRelationship getPositionRelationship(Barrel barrel, double currentBottom, double oldBottomY) {
        PositionRelationship rel = new PositionRelationship();
        
        double barrelTop = barrel.getY() - barrel.getImage().getHeight()/2;
        double barrelBottom = barrel.getY() + barrel.getImage().getHeight()/2;
        
        rel.isOnSamePlatformAsBarrel = Math.abs(currentBottom - barrelBottom) < 40;
        rel.hasSignificantVerticalMovement = Math.abs(currentBottom - oldBottomY) >= 0.5;
        rel.wasAbove = oldBottomY < barrelTop + 8;
        rel.movedDownward = currentBottom >= oldBottomY - 2;
        rel.horizontallyAligned = Math.abs(position.x - barrel.getX()) <= 25;
        rel.verticallyAligned = Math.abs(currentBottom - barrelTop) <= 20;
        rel.verticallyAboveEnough = currentBottom < barrelBottom - 3;
        
        return rel;
    }
    
    /**
     * Handles the case where Mario falls below the screen
     */
    private void checkFallBelowScreen() {
        double screenBottom = Config.getAppInt("window.height");
        
        if (position.y > screenBottom + 200) {
            Platform lowest = platforms.stream()
                .max((p1, p2) -> Double.compare(p1.getY(), p2.getY()))
                .orElse(platforms.get(0));
            
            position = new Point(lowest.getX(), lowest.getY() - getImage().getHeight() / 2.0);
            velocityY = 0;
            onGround = true;
        }
    }

    /**
     * Determines if Mario is standing on a platform
     */
    private boolean isStandingOn(Platform platform) {
        double marioBottom = position.y + getImage().getHeight() / 2.0;
        double platformTop = platform.getY() - platform.getImage().getHeight() / 2.0;
        
        // Calculate horizontal bounds
        double marioLeft = position.x - getImage().getWidth() / 2.0;
        double marioRight = position.x + getImage().getWidth() / 2.0;
        double platformLeft = platform.getX() - platform.getImage().getWidth() / 2.0;
        double platformRight = platform.getX() + platform.getImage().getWidth() / 2.0;
    
        // Check alignment conditions
        boolean horizontallyAligned = marioRight > platformLeft && marioLeft < platformRight;
        boolean verticallyAligned = Math.abs(marioBottom - platformTop) <= PLATFORM_BUFFER;
        boolean movingDownward = velocityY >= 0;
    
        return movingDownward && horizontallyAligned && verticallyAligned;
    }

    /**
     * Checks for collisions with barrels and handles appropriate actions
     */
    public void checkBarrelCollisions(List<Barrel> barrels) {
        this.barrels = barrels;
        
        for (Barrel barrel : barrels) {
            if (intersects(barrel)) {
                if (hasHammer && !barrel.isDestroyed()) {
                    barrel.destroy();
                    destroyedBarrels++;
                } else if (!hasHammer) {
                }
            }
        }
    }
    
    /**
     * Public collision detection method
     */
    public boolean intersects(GameObject other) {
        return intersects(other, 2.0, 2.0); 
    }
    
    /**
     * Gets the number of barrels destroyed with hammer
     */
    public int getDestroyedBarrelsCount() {
        return destroyedBarrels;
    }
    
    /**
     * Gets the number of barrels jumped over
     */
    public int getJumpedBarrelsCount() {
        return jumpedBarrels;
    }

    /**
     * Calculates and returns the final score
     * 100 points per destroyed barrel, 30 points per jumped barrel, plus additional points
     */
    public int getFinalScore() {
        return destroyedBarrels * 100 + jumpedBarrels * 30 + finalScore;
    }

    /**
     * Gets center X position of Mario
     */
    public double getCenter() {
        return position.x;
    }    

    /**
     * Sets whether Mario can climb ladders
     */
    public void setCanClimb(boolean value) {
        this.canClimb = value;
        if (!value) {
            isClimbing = false;
        }
    }

    /**
     * Checks if Mario is near ladders to enable climbing
     * Uses precise collision detection with horizontal alignment
     */
    public void checkLadderCollisions(List<Ladder> ladders, Input input) {
        this.ladders = ladders;
        boolean canClimb = false;
        isOnLadder = false;
        
        Ladder bestLadder = findBestLadder(input);
        
        if (bestLadder != null) {
            canClimb = true;
            isOnLadder = intersects(bestLadder);
        }
        
        setCanClimb(canClimb);
    }
    
    /**
     * Find the best ladder for Mario to interact with based on position and input
     */
    private Ladder findBestLadder(Input input) {
        Ladder bestLadder = null;
        double bestScore = Double.MAX_VALUE;
        
        for (Ladder ladder : ladders) {
            double score = getLadderScore(ladder, input);
            
            if (score < bestScore) {
                bestScore = score;
                bestLadder = ladder;
            }
        }
        
        return (bestScore < 100) ? bestLadder : null;
    }
    
    /**
     * Calculate a score for a ladder based on Mario's position and input
     * Lower scores mean better matches
     */
    private double getLadderScore(Ladder ladder, Input input) {
        double marioBottom = position.y + getImage().getHeight() / 2.0;
        double ladderTop = ladder.getY() - ladder.getImage().getHeight() / 2.0;
        double ladderBottom = ladder.getY() + ladder.getImage().getHeight() / 2.0;
        
        // Check horizontal alignment first - if not aligned, very high score
        boolean horizontallyAligned = getCenter() >= ladder.getLeft() && 
                                     getCenter() <= ladder.getRight();
        if (!horizontallyAligned) {
            return Double.MAX_VALUE;
        }
        
        boolean atBottomOfLadder = Math.abs(marioBottom - ladderBottom) < LADDER_DETECT_TOLERANCE;
        
        boolean atTopOfLadder = Math.abs(marioBottom - ladderTop) < LADDER_DETECT_TOLERANCE && onGround;
        
        boolean onLadder = intersects(ladder);
        
        // Calculate score based on position and input
        if (input.isDown(Keys.UP)) {
            if (atBottomOfLadder) return 10;
            if (onLadder) return 20;
            return 100;
        } 
        else if (input.isDown(Keys.DOWN)) {
            if (atTopOfLadder) return 10;
            if (onLadder) return 20;
            return 100;
        }
        else {
            if (onLadder) return 10;
            return 100;
        }
    }

    public void climb(Input input, List<Ladder> ladders) {
        boolean wasClimbing = isClimbing;
        
        if (!canClimb) {
            isClimbing = false;
            return;
        }
        
        Ladder bestLadder = findBestLadder(input);
        if (bestLadder == null) {
            isClimbing = false;
            return;
        }
        
        if (isClimbing && bestLadder != null) {
            lastUsedLadder = bestLadder;
        }
        
        if (!isClimbing && wasClimbing) {
            climbingCooldownFrames = 10;
        }
        
        double marioTop = position.y - getImage().getHeight() / 2.0;
        double marioBottom = position.y + getImage().getHeight() / 2.0;
        double ladderTop = bestLadder.getY() - bestLadder.getImage().getHeight() / 2.0;
        double ladderBottom = bestLadder.getY() + bestLadder.getImage().getHeight() / 2.0;
        
        boolean atBottomOfLadder = Math.abs(marioBottom - ladderBottom) < LADDER_DETECT_TOLERANCE;
        boolean atTopOfLadder = Math.abs(marioBottom - ladderTop) < LADDER_DETECT_TOLERANCE && onGround;
        boolean onLadder = intersects(bestLadder);
        
        if (input.isDown(Keys.DOWN) && marioBottom > ladderBottom - 1.0) {
            isClimbing = false;
            velocityY = 0;
            return;
        }
        
        // Handle climbing up
        if (input.isDown(Keys.UP) && (atBottomOfLadder || onLadder)) {
            isClimbing = true;
            velocityY = -CLIMB_SPEED;
            position = new Point(bestLadder.getX(), position.y);
            return;
        }
        
        // Handle climbing down
        if (input.isDown(Keys.DOWN)) {
            if (atTopOfLadder) {
                isClimbing = true;
                onGround = false;  
                velocityY = CLIMB_SPEED;
                position = new Point(bestLadder.getX(), ladderTop + 1.0);
                return;
            }
            else if (onLadder) {
                isClimbing = true;
                velocityY = CLIMB_SPEED;
                position = new Point(bestLadder.getX(), position.y);
                return;
            }
        }
        
        if (isClimbing && (input.isDown(Keys.LEFT) || input.isDown(Keys.RIGHT))) {
            isClimbing = false;
            return;
        }

        if (onLadder && isClimbing && !input.isDown(Keys.UP) && !input.isDown(Keys.DOWN)) {
            velocityY = 0;
            position = new Point(bestLadder.getX(), position.y);
            return;
        }
        
        if (isClimbing && velocityY > 0) {
            for (Platform platform : platforms) {
                if (isStandingOn(platform)) {
                    snapToTopOfPlatform(platform);
                    isClimbing = false;
                    return;
                }
            }
        }
        
        if (isClimbing) {
            boolean stillOnLadder = false;
            for (Ladder ladder : ladders) {
                boolean aligned = getCenter() >= ladder.getLeft() && getCenter() <= ladder.getRight();
                boolean touching = intersects(ladder);
                if (aligned && touching) {
                    stillOnLadder = true;
                    break;
                }
            }
            
            if (!stillOnLadder) {
                isClimbing = false;
            
                for (Platform platform : platforms) {
                    if (isStandingOn(platform)) {
                        snapToTopOfPlatform(platform);
                        break;
                    }
                }
            
                if (!jumpedThisJump.isEmpty()) {
                    jumpedThisJump.clear();
                }
            }            
        }
    }

    /**
     * Returns whether Mario is currently climbing a ladder
     */
    public boolean isClimbing() {
        return isClimbing;
    }

    public int getBulletCount() {
        return bulletCount;
    }
    
    public void addToScore(int additional) {
        finalScore += additional;
    }    

    public void setScore(int newScore) {
        this.finalScore = newScore;
    }    

    /**
     * Checks if Mario is near any ladder in the game
     * Now checks both horizontal proximity and vertical alignment
     */
    private boolean isNearAnyLadder() {
        for (Ladder ladder : ladders) {
            double horizontalDistance = Math.abs(position.x - ladder.getX());
            
            if (horizontalDistance < LADDER_SCORING_EXCLUSION_ZONE) {
                double marioBottom = position.y + getImage().getHeight() / 2.0;
                double ladderTop = ladder.getY() - ladder.getImage().getHeight() / 2.0;
                double ladderBottom = ladder.getY() + ladder.getImage().getHeight() / 2.0;
                
                if (marioBottom >= ladderTop - 10 && marioBottom <= ladderBottom + 10) {
                    return true;
                }
            }
        }
        return false;
    }
}