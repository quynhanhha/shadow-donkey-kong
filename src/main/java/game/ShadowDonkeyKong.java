package game;

import bagel.*;
import bagel.util.Point;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;
import game.core.Config;
import game.core.GameObject;
import game.IOUtils;
import game.entities.Mario;
import game.entities.Platform;
import game.entities.DonkeyKong;
import java.util.ArrayList;
import java.util.List;
import game.entities.Barrel;
import game.entities.Hammer;
import game.entities.Ladder;
import game.entities.Bullet;
import game.entities.Blaster;
import game.entities.NormalMonkey;
import game.entities.IntelligentMonkey;
import game.entities.Banana;

/**
 * The main class for the Shadow Donkey Kong game.
 * This class extends {@code AbstractGame} and is responsible for managing game initialization,
 * updates, rendering, and handling user input.
 */
public class ShadowDonkeyKong extends AbstractGame {
    // Constants for UI layout
    private static final int SCORE_DISPLAY_OFFSET_Y = 30;
    private static final int GAME_OVER_SCORE_OFFSET_Y = 60;
    private static final int CONTINUE_PROMPT_OFFSET_Y = 100;
    private static final int SECONDS_PER_MINUTE = 60;
    
    // Constants for game messages
    private static final String GAME_OVER_MESSAGE = "GAME OVER, YOU LOST!";
    private static final String WIN_MESSAGE = "CONGRATULATIONS, YOU WON!";
    private static final String SCORE_FORMAT = "SCORE %d";
    private static final String TIME_LEFT_FORMAT = "TIME LEFT %d";
    private static final String FINAL_SCORE_FORMAT = "YOUR FINAL SCORE %d";
    private static final String CONTINUE_PROMPT = "PRESS SPACE TO CONTINUE...";
    
    // Game state enum
    private enum GameState { TITLE, PLAYING, GAME_OVER, WIN }
    
    // Game properties
    private final Properties GAME_PROPS;
    private final Properties MESSAGE_PROPS;
    private Image background;
    private GameState currentState = GameState.TITLE;
    private int maxFrames;
    private int currentFrame = 0;
    private int currentLevel = 1;
    private int totalScore = 0;

    // Game entities
    private Mario mario;
    private DonkeyKong donkey;
    private Hammer hammer;
    private final List<Platform> platforms = new ArrayList<>();
    private final List<Barrel> barrels = new ArrayList<>();
    private final List<Ladder> ladders = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Blaster> blasters = new ArrayList<>();
    private final List<NormalMonkey> normalMonkeys = new ArrayList<>();
    private final List<IntelligentMonkey> intelligentMonkeys = new ArrayList<>();
    private final List<Banana> bananas = new ArrayList<>();

    /**
     * Initializes the game with the provided properties
     */
    public ShadowDonkeyKong(Properties gameProps, Properties messageProps) {
        super(Integer.parseInt(gameProps.getProperty("window.width")),
              Integer.parseInt(gameProps.getProperty("window.height")),
              messageProps.getProperty("home.title"));

        this.GAME_PROPS = gameProps;
        this.MESSAGE_PROPS = messageProps;
        this.background = new Image(GAME_PROPS.getProperty("backgroundImage"));
        
        initializeGame();
    }
    
    /**
     * Initializes or resets all game entities to their starting state
     */
    private void initializeGame() {
        // Load basic entities for all levels
        loadPlatforms();
        loadLadders();
        connectLadders();
        loadBarrels();
        loadHammer();

        // Load level 2 specific entities
        if (currentLevel == 2) {
            bullets.clear();
            bananas.clear();
            loadBlasters();
            loadNormalMonkeys();
            loadIntelligentMonkeys();
        }        
        
        // Load player and enemy
        initializePlayer();
        initializeDonkey();

        // Reset game timer
        this.maxFrames = Config.getAppInt("gamePlay.maxFrames"); 
        this.currentFrame = 0;
    }
    
    /**
     * Initialize player character
     */
    private void initializePlayer() {
        String[] marioCoords = Config.getApp("mario.level" + currentLevel).split(",");
        mario = new Mario(
            Double.parseDouble(marioCoords[0]),
            Double.parseDouble(marioCoords[1]),
            platforms
        );
        mario.addToScore(totalScore);
    }
    
    /**
     * Initialize Donkey Kong
     */
    private void initializeDonkey() {
        String[] donkeyCoords = Config.getApp("donkey.level" + currentLevel).split(",");
        donkey = new DonkeyKong(
            Double.parseDouble(donkeyCoords[0]),
            Double.parseDouble(donkeyCoords[1])
        );
        donkey.snapToPlatform(platforms);
    }
    
    /**
     * Helper method to parse coordinates from a string in the format "x,y"
     * 
     * @param coordString The string containing coordinates
     * @return Array of doubles [x, y]
     */
    private double[] parseCoordinates(String coordString) {
        String[] parts = coordString.split(",");
        return new double[] {
            Double.parseDouble(parts[0].trim()),
            Double.parseDouble(parts[1].trim())
        };
    }
    
    /**
     * Loads platforms from configuration
     */
    private void loadPlatforms() {
        platforms.clear();
        String raw = Config.getApp("platforms.level" + currentLevel);
        
        if (raw.isEmpty()) {
            System.err.println("No platform data found!");
            return;
        }

        for (String pair : raw.split(";")) {
            double[] coords = parseCoordinates(pair);
            platforms.add(new Platform(coords[0], coords[1]));
        }
    }

    /**
     * Loads barrels from configuration
     */
    private void loadBarrels() {
        barrels.clear();
        int barrelCount = Config.getAppInt("barrel.level" + currentLevel + ".count");
        
        for (int i = 1; i <= barrelCount; i++) {
            String coordString = Config.getApp("barrel.level" + currentLevel + "." + i);
            double[] coords = parseCoordinates(coordString);
            barrels.add(new Barrel(coords[0], coords[1]));
        }
    }
    
    /**
     * Loads the hammer from configuration
     */
    private void loadHammer() {
        String coordString = Config.getApp("hammer.level" + currentLevel + ".1");
        double[] coords = parseCoordinates(coordString);
        hammer = new Hammer(coords[0], coords[1]);
    }  

    private void loadBlasters() {
        blasters.clear();
        int count = Config.getAppInt("blaster.level" + currentLevel + ".count");
        for (int i = 1; i <= count; i++) {
            String coordString = Config.getApp("blaster.level" + currentLevel + "." + i);
            double[] coords = parseCoordinates(coordString);
            blasters.add(new Blaster(coords[0], coords[1]));
        }
    }   
    
    /**
     * Helper method to parse monkey data from a configuration string
     * 
     * @param value The config value in format "x,y;direction;route"
     * @return Array containing [x, y, direction, route]
     */
    private Object[] parseMonkeyData(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        String[] parts = value.split(";");
        if (parts.length != 3) {
            return null;
        }
        
        double[] coords = parseCoordinates(parts[0]);
        String direction = parts[1].trim();
        String route = parts[2].trim();
        
        return new Object[] { coords[0], coords[1], direction, route };
    }
    
    private void loadNormalMonkeys() {
        normalMonkeys.clear();
    
        String countKey = "normalMonkey.level" + currentLevel + ".count";
        String rawCount = Config.getApp(countKey);
        if (rawCount == null || rawCount.trim().isEmpty()) return;
    
        int count = Integer.parseInt(rawCount.trim());
     
        for (int i = 1; i <= count; i++) {
            String key = "normalMonkey.level" + currentLevel + "." + i;
            String value = Config.getApp(key);
            
            Object[] monkeyData = parseMonkeyData(value);
            if (monkeyData == null) continue;
            
            double x = (double) monkeyData[0];
            double y = (double) monkeyData[1];
            String direction = (String) monkeyData[2];
            String route = (String) monkeyData[3];
    
            NormalMonkey monkey = new NormalMonkey(x, y, direction, route);
            monkey.setPlatforms(platforms);
            normalMonkeys.add(monkey);
        }
    } 
    
    private void loadIntelligentMonkeys() {
        intelligentMonkeys.clear();
    
        String keyBase = "intelligentMonkey.level" + currentLevel;
        int count = Config.getAppInt(keyBase + ".count");
    
        for (int i = 1; i <= count; i++) {
            String key = keyBase + "." + i;
            String value = Config.getApp(key);
            
            Object[] monkeyData = parseMonkeyData(value);
            if (monkeyData == null) continue;
            
            double x = (double) monkeyData[0];
            double y = (double) monkeyData[1];
            String direction = (String) monkeyData[2];
            String route = (String) monkeyData[3];
    
            IntelligentMonkey monkey = new IntelligentMonkey(x, y, direction, route);
            monkey.setPlatforms(platforms);
            intelligentMonkeys.add(monkey);
        }
    }
    
    /**
     * Loads ladders from configuration
     */
    private void loadLadders() {
        ladders.clear();
        int ladderCount = Config.getAppInt("ladder.level" + currentLevel + ".count");
    
        for (int i = 1; i <= ladderCount; i++) {
            String coordString = Config.getApp("ladder.level" + currentLevel + "." + i);
            double[] coords = parseCoordinates(coordString);
            ladders.add(new Ladder(coords[0], coords[1]));
        }
    }    

    /**
     * Connects ladders to the appropriate platforms
     */
    private void connectLadders() {
        for (Ladder ladder : ladders) {
            ladder.connectToPlatformAbove(platforms);
        }
    }

    @Override
    protected void update(Input input) {
        if (input.wasPressed(Keys.ESCAPE)) {
            Window.close();
        }

        background.draw(Window.getWidth() / 2.0, Window.getHeight() / 2.0);

        switch (currentState) {
            case TITLE:
                updateTitleScreen(input);
                break;
            case GAME_OVER:
                updateGameOverScreen(input);
                break;
            case WIN:
                updateWinScreen(input);
                break;
            case PLAYING:
                updateGamePlay(input);
                break;
        }
    }
    
    /**
     * Updates and renders the title screen
     */
    private void updateTitleScreen(Input input) {
        drawCenteredText(Config.getMsg("home.title"), "home.title.fontSize", Config.getAppInt("home.title.y"));
        drawCenteredText(Config.getMsg("home.prompt"), "home.prompt.fontSize", Config.getAppInt("home.prompt.y"));
    
        if (input.wasPressed(Keys.ENTER)) {
            currentLevel = 1;
            initializeGame();
            currentState = GameState.PLAYING;
        } else if (input.wasPressed(Keys.NUM_2)) {
            currentLevel = 2;
            initializeGame();
            currentState = GameState.PLAYING;
        }        
    }
    
    /**
     * Helper method to prepare game end screen (win or game over)
     * 
     * @param message The win/lose message to display
     * @return The final score to display
     */
    private int prepareGameEndScreen(String message) {
        Font font = new Font(Config.getApp("font"), Config.getAppInt("gameEnd.status.fontSize"));
        drawCenteredString(font, message, Config.getAppInt("gameEnd.status.y"));
    
        font = new Font(Config.getApp("font"), Config.getAppInt("gameEnd.scores.fontSize"));
        
        int finalScore = 0;
        if (currentState == GameState.WIN) {
            int timeLeft = (maxFrames - currentFrame) / SECONDS_PER_MINUTE;
            int timeBonus = timeLeft * 3;
            finalScore = mario.getFinalScore() + timeBonus;
        }
        
        drawCenteredString(font, String.format(FINAL_SCORE_FORMAT, finalScore),
                Config.getAppInt("gameEnd.status.y") + GAME_OVER_SCORE_OFFSET_Y);
        
        return finalScore;
    }
    
    /**
     * Updates and renders the game over screen
     */
    private void updateGameOverScreen(Input input) {
        prepareGameEndScreen(GAME_OVER_MESSAGE);
        
        Font font = new Font(Config.getApp("font"), Config.getAppInt("gameEnd.scores.fontSize"));
        drawCenteredString(font, CONTINUE_PROMPT, Window.getHeight() - CONTINUE_PROMPT_OFFSET_Y);
        
        if (input.wasPressed(Keys.SPACE)) {
            restartGame();
        }
    }

    /**
     * Updates and renders the win screen
     */
    private void updateWinScreen(Input input) {
        int finalScore = prepareGameEndScreen(WIN_MESSAGE);
        
        // For Level 1, automatically start Level 2
        if (currentLevel == 1) {
            totalScore = finalScore;
            currentLevel = 2;
            initializeGame();
            currentState = GameState.PLAYING;
        } else {
            // For Level 2, show prompt and wait for SPACE key
            Font font = new Font(Config.getApp("font"), Config.getAppInt("gameEnd.scores.fontSize"));
            drawCenteredString(font, CONTINUE_PROMPT, Window.getHeight() - CONTINUE_PROMPT_OFFSET_Y);
            
            if (input.wasPressed(Keys.SPACE)) {
                totalScore = finalScore;
                currentLevel = 1;
                initializeGame();
                currentState = GameState.TITLE;
            }
        }
    }    
    
    /**
     * Helper method to check if a bullet hits a game entity and award points if needed
     * 
     * @param bullet The bullet to check
     * @param entity The entity to check for collision
     * @param tolerance The collision tolerance
     * @return true if the bullet hit and should be removed
     */
    private boolean checkBulletHit(Bullet bullet, GameObject entity, double tolerance) {
        if (entity.intersects(bullet, tolerance, tolerance)) {
            if (entity instanceof NormalMonkey) {
                ((NormalMonkey) entity).destroy();
                mario.addToScore(100);
                return true;
            } else if (entity instanceof IntelligentMonkey) {
                ((IntelligentMonkey) entity).destroy();
                mario.addToScore(100);
                return true;
            } else if (entity instanceof DonkeyKong) {
                ((DonkeyKong) entity).reduceHealth();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Updates and renders the main gameplay
     */
    private void updateGamePlay(Input input) {
        currentFrame++;
        
        if (currentFrame >= maxFrames) {
            currentState = GameState.GAME_OVER;
            return;
        }

        barrels.forEach(Barrel::resetFrameFlags);

        mario.checkLadderCollisions(ladders, input);

        double oldBottomY = mario.getBottomY();

        mario.update(input);

        mario.awardJumpPoints(barrels, oldBottomY);

        mario.checkBarrelCollisions(barrels);

        if (!mario.isClimbing()) {
            mario.checkPlatformCollision(platforms);
        }
        
        if (currentLevel == 2) {
            for (Blaster blaster : blasters) {
                blaster.update(mario);
            }
        
            if (input.wasPressed(Keys.S)) {
                Bullet bullet = mario.tryShoot();
                if (bullet != null) {
                    bullets.add(bullet);
                }
            }
            
            // Update bullet positions and remove bullets that should despawn
            for (Bullet b : new ArrayList<>(bullets)) {
                if (b.updateAndShouldDespawn()) {
                    bullets.remove(b);
                    continue;
                }
                
                // Check for bullet hits on monkeys
                boolean bulletHit = false;
                
                // Check normal monkeys
                for (NormalMonkey m : normalMonkeys) {
                    if (!m.isDestroyed() && checkBulletHit(b, m, 5.0)) {
                        bullets.remove(b);
                        bulletHit = true;
                        break;
                    }
                }
                
                if (bulletHit) continue;
                
                // Check intelligent monkeys
                for (IntelligentMonkey m : intelligentMonkeys) {
                    if (!m.isDestroyed() && checkBulletHit(b, m, 5.0)) {
                        bullets.remove(b);
                        bulletHit = true;
                        break;
                    }
                }
                
                if (bulletHit) continue;
                
                // Check if bullet hit Donkey Kong
                if (!donkey.isDead() && checkBulletHit(b, donkey, 5.0)) {
                    bullets.remove(b);
                    
                    if (donkey.isDead()) {
                        currentState = GameState.WIN;
                    }
                }
            }
            
            // Update normal monkeys
            for (NormalMonkey m : normalMonkeys) {
                m.update();
            
                // Handle collision with Mario
                if (!m.isDestroyed() && m.intersectsMario(mario)) {
                    if (mario.hasHammer()) {
                        // Destroy monkey with hammer
                        m.destroy();
                        mario.addToScore(100);
                    } else {
                        // Kill Mario if not holding hammer
                        currentState = GameState.GAME_OVER;
                    }
                }
            } 

            // Update intelligent monkeys
            for (IntelligentMonkey monkey : intelligentMonkeys) {
                monkey.update();
            
                if (!monkey.isDestroyed() && monkey.shouldFireBanana()) {
                    Point spawn = monkey.getBananaSpawnPoint();
                    bananas.add(new Banana(spawn.x, spawn.y, monkey.isFacingRight()));
                }
            
                // Mario collision
                if (!monkey.isDestroyed() && monkey.intersectsMario(mario)) {
                    if (mario.hasHammer()) {
                        // Destroy monkey with hammer
                        monkey.destroy();
                        mario.addToScore(100);
                    } else {
                        // Kill Mario if not holding hammer
                        currentState = GameState.GAME_OVER;
                    }
                }
            }
            
            // Update bananas
            for (Banana banana : new ArrayList<>(bananas)) {
                if (banana.intersects(mario, 5.0, 5.0)) {
                    currentState = GameState.GAME_OVER;
                }
            
                if (banana.updateAndShouldDespawn()) {
                    bananas.remove(banana);
                }
            }           
        }        

        if (mario.intersects(donkey) && mario.hasHammer()) {
            currentState = GameState.WIN;
            return;
        }
        
        hammer.update(mario);
        updateBarrels();

        if (mario.intersects(donkey) && !mario.hasHammer()) {
            currentState = GameState.GAME_OVER;
        }
        
        renderGameEntities();
        renderHUD();
    }
    
    /**
     * Updates all barrels and checks for collisions
     */
    private void updateBarrels() {
        for (Barrel barrel : barrels) {
            if (!barrel.isDestroyed()) {
                barrel.update();
                barrel.trySnapToPlatform(platforms);
                
                if (mario.intersects(barrel) && !mario.hasHammer()) {
                    currentState = GameState.GAME_OVER;
                }
            }
        }
    }
    
    /**
     * Renders all game entities
     */
    private void renderGameEntities() {
        // Always render these entities
        platforms.forEach(Platform::render);
        ladders.forEach(Ladder::render);
        hammer.render();
        barrels.forEach(Barrel::render);
        donkey.render();

        // Render level 2 specific entities
        if (currentLevel == 2) {
            blasters.forEach(Blaster::render);
            bullets.forEach(Bullet::render);
            normalMonkeys.forEach(NormalMonkey::render);
            intelligentMonkeys.forEach(IntelligentMonkey::render);
            bananas.forEach(Banana::render);
        }

        // Always render Mario last (so he appears on top)
        mario.render();
    }

    /**
     * Renders the heads-up display with score and time
     */
    private void renderHUD() {
        int fontSize = Config.getAppInt("gamePlay.score.fontSize");
        int x = Config.getAppInt("gamePlay.score.x");
        int y = Config.getAppInt("gamePlay.score.y");
    
        Font font = new Font(Config.getApp("font"), fontSize);
    
        int score = mario.getFinalScore();
        int timeLeft = (maxFrames - currentFrame) / SECONDS_PER_MINUTE;
    
        font.drawString(String.format(SCORE_FORMAT, score), x, y);
        font.drawString(String.format(TIME_LEFT_FORMAT, timeLeft), x, y + SCORE_DISPLAY_OFFSET_Y);

        if (currentLevel == 2) {
            // Get coordinates for the Donkey health display
            String[] coords = Config.getApp("gamePlay.donkeyhealth.coords").split(",");
            int healthX = Integer.parseInt(coords[0].trim());
            int healthY = Integer.parseInt(coords[1].trim());
            
            // Display Donkey health
            font.drawString("Donkey Health " + donkey.getHealth(), healthX, healthY);
            
            // Display bullet count 30 pixels below health
            font.drawString("Bullet " + mario.getBulletCount(), healthX, healthY + 30);
        } 
    }    
    
    /**
     * Restarts the game after game over
     */
    private void restartGame() {
        totalScore = 0; // Reset score on game over
        initializeGame();
        currentState = GameState.TITLE;
    }
    
    /**
     * Draws text centered horizontally at the given y position
     */
    private void drawCenteredText(String text, String fontSizeKey, int y) {
        int fontSize = Config.getAppInt(fontSizeKey);
        Font font = new Font(Config.getApp("font"), fontSize);
        drawCenteredString(font, text, y);
    }
    
    /**
     * Draws a string centered horizontally at the given y position
     */
    private void drawCenteredString(Font font, String text, int y) {
        double x = (Window.getWidth() - font.getWidth(text)) / 2.0;
        font.drawString(text, x, y);
    }

    public static void main(String[] args) {
        Properties gameProps = IOUtils.readPropertiesFile("res/app.properties");
        Properties messageProps = IOUtils.readPropertiesFile("res/message.properties");
        new ShadowDonkeyKong(gameProps, messageProps).run();
    }
} 