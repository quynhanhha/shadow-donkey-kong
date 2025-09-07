Shadow Donkey Kong

A small Java/Bagel platformer tribute in the Donkey Kong spirit.
Run, climb, jump, grab a hammer or a blaster, and clear two handcrafted levels under a time limit.

Demo (at a glance)

Core loop: navigate platforms → dodge barrels/monkeys → use tools → reach the goal within time.

Two levels: start with Enter (Level 1) or 2 (Level 2) from the title screen.

Config-driven: positions, timers, and UI messaging live in res/*.properties.

Tech Stack

Java 17
Maven (build + run)
Bagel 1.9.3 (graphics/game loop)
LWJGL (native bindings pulled by Maven)
Assets/Config: res/ (PNG sprites, app.properties, message.properties)

Getting Started

Prerequisites

Java 17+
Maven 3.8+

Build & Run

# from DonkeyKong_Skeleton/
mvn clean package

# run via exec plugin
mvn -q -DskipTests exec:java

If you prefer jar execution (and your system resolves natives correctly):

mvn -q -DskipTests package
java -jar target/DonkeyKong-1.0-SNAPSHOT.jar

Note: LWJGL natives are declared in pom.xml. If you see native-loading errors, ensure you're on a supported OS/arch and running with Java 17.

Controls

Global
ESC — quit game

Title screen
ENTER — start Level 1
2 — start Level 2

Gameplay
← / → — move
SPACE — jump
↑ / ↓ — climb ladders (when aligned)
S — shoot (requires blaster + bullets)

Hammers are auto-used when picked up (timed)

Game Over / Win
SPACE — continue

Gameplay Elements

Platforms & Ladders
Movement and climb behaviour use simple physics + collision checks.

Barrels
Rolling hazards; jump over to avoid or destroy with tools.

Donkey Kong
Level boss entity; defeat/clear requirements to win.

Hammer (res/hammer.png)
Temporary melee power; destroys hazards while active.

Blaster (res/blaster.png)
Grants a bullet stack; press S to fire in facing direction.

Scoring & Timer
Time-bounded runs; score displayed on end screen.

Configuration

All tunables are in res/app.properties and res/message.properties.

Window & Timing
window.width, window.height
gamePlay.maxFrames

UI Typography/Layout
home.title.fontSize, home.prompt.fontSize
gameEnd.* font sizes and positions

Entity Placement (per level)
platform.levelX.*
ladder.levelX.*
barrel.levelX.*
hammer.levelX.*
blaster.levelX.*
normalMonkey.levelX.*, intelligentMonkey.levelX.*

Messages (res/message.properties)
home.title, home.prompt
gameEnd.lost, gameEnd.won, gameEnd.continue, gameEnd.score

Tip: tweak positions and counts (e.g., platform.level2.count, platform.level2.1=...) to iterate on layout without recompiling logic.

Project Structure

DonkeyKong_Skeleton/
├─ pom.xml                      # Maven config (Java 17, exec plugin, Bagel/LWJGL deps)
├─ src/main/java/game/
│  ├─ ShadowDonkeyKong.java     # Main class + game states (TITLE/PLAYING/WIN/GAME_OVER)
│  ├─ IOUtils.java
│  ├─ core/
│  │  ├─ Config.java            # Loads app/message properties; central config access
│  │  └─ GameObject.java        # Base sprite entity (position, image, intersects)
│  └─ entities/
│     ├─ Mario.java             # Player (movement, jump, climb, tools, shooting)
│     ├─ DonkeyKong.java        # Boss
│     ├─ Platform.java, Ladder.java
│     ├─ Barrel.java
│     ├─ Hammer.java
│     ├─ Blaster.java, Bullet.java
│     ├─ NormalMonkey.java, IntelligentMonkey.java
└─ res/
   ├─ *.png, FSO8BITR.TTF
   ├─ app.properties
   └─ message.properties

Known Trade-offs / Future Work

OOP refactor:
Extract clearer component systems (physics, input, collision, animation); reduce ShadowDonkeyKong god-object responsibilities.

Collision & ladders:
Improve ladder alignment heuristics; add coyote time/buffer for jumps and ledge-grabs.

State & UI:
Centralize UI screens; add pause/options menu; separate HUD rendering.

Content pipeline:
Move level data to JSON/TOML; write a tiny level loader to replace .properties grids.

SFX/Music:
Add sound cues for jump, pickup, shoot, win/lose.

Testing:
Headless logic tests for collision, timers, and entity lifecycles.