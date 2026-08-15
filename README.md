# Tetris Dungeon

Tetris Dungeon is a game that combines classic Tetris puzzle gameplay with RPG Dungeon mechanics, built entirely using Java Swing.

## Tech Stack

- **Language:** Java
- **UI Library:** Java Swing & AWT (Utilizing `Graphics2D` for custom UI rendering)
- **Core Architecture:** 
  - `Tetris.java` (Main Frame): Manages the game flow, coordinates the CardLayout (Menu, Gameplay, Event Screen), and handles the UI for score and boss HP.
  - `Board.java` (Panel): The core engine handling falling block logic, collision detection, line clearing, and board rendering.
  - `Shape.java`: Defines the structure and rotation matrices of the Tetrominoes.
  - `RoundedPanel.java`: A custom component that supports modern rounded-corner UI designs.

## Installation & Execution

1. Open a terminal and navigate to the project directory:
   ```bash
   cd /home/kennysk/java
   ```
2. Run the packaged game directly:
   ```bash
   java -jar TetrisDungeon.jar
   ```

*(Alternatively, to compile and run from source):*
```bash
javac -d bin src/com/tetris/*.java
java -cp bin com.tetris.Tetris
```

## Game Mechanics & Business Logic

### 1. Core Gameplay (Tetris)
- Automated falling block mechanics with progressively increasing speed across stages.
- Supported actions: Move left/right, rotate piece, soft drop, and hard drop.
- **Lock Delay (Grace Period):** When a block touches the bottom, it does not lock immediately. Players are given a short grace period where they can still slide left, slide right, or rotate the block to fit it into tight spaces. Moving or rotating the block successfully resets this timer.
- Next Block Preview panel.
- Ghost Piece (Shadow) and grid lines for precise block placement.

### 2. Dungeon System (Stages & Combat)
- **Stage Progression:** The game is divided into continuous stages.
- **Monsters & Bosses:** 
  - Normal stages (1, 2, 3, 4, 6...): Face off against normal Monsters with low health pools.
  - Special stages (multiples of 5 like 5, 10, 15, 20...): Encounter Bosses with significantly higher health pools.
- **Difficulty Scaling:** Speed and enemy attack frequency scale up only after a Boss is defeated (Boss Tier), creating consistent difficulty blocks instead of increasing linearly every stage.
- **Dealing Damage:** Every time the player clears lines, the current enemy takes proportional damage.
- **Enemy Attacks:** Enemies possess an Attack Timer. When this timer fills up, they attack by pushing a "Garbage line" to the bottom of the player's board. The attack speed increases as the stages progress.

### 3. Boss Skills (Buffs & Debuffs)
During a Boss fight (Special stages), the Boss will periodically cast random skills to disrupt the player. The frequency of skill usage and the number of available skills in their arsenal increase with the game's difficulty.
- **Blind (Hide Next):** Hides the next block from the preview panel.
- **Haste (Boss Fast Attack):** Significantly boosts the boss's attack timer, causing an almost immediate garbage line attack.
- **No Shadow:** Disables the Ghost Piece, making it harder to aim block drops.
- **No Rotate:** Locks the player's ability to rotate the current and upcoming blocks for a set number of drops.
- **Inverse Controls:** Reverses the player's left and right movement controls.
- **Boss Heal:** The Boss recovers a portion of their missing HP.

### 4. Event System
- Upon defeating an enemy, the game pauses (preserving the current board state) and transitions to the Event Screen.
- **Random Events:** Between normal stages, random events can occur (e.g., Finding a treasure that grants +500 points).
- **Boss Preparation (Heal Event):** Upon completing the stage immediately preceding a Boss (e.g., finishing Stage 4 or Stage 9), a guaranteed "Goddess Blessing" event triggers. This automatically removes the bottom 5 lines from the board, clearing out garbage to prepare the player for the Boss fight.
