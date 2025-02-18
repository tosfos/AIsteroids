# AIsteroids

A multi-threaded, object-oriented arcade space shooter built entirely in Java. This project demonstrates expert-level design, utilizing multiple threads, advanced collision detection, and Swing for rendering and input management. The game features a dynamically updating game loop, multi-threaded asteroid spawning, and smooth, responsive controls.

## Features

- **Procedurally Generated Sound Effects:**
  - Laser sounds for bullet firing
  - Explosion sounds when asteroids are destroyed
  - Thruster sounds during acceleration
  - All sounds are synthesized in real-time using Java's audio system

- **Scoring System:**
  - Points awarded for destroying asteroids
  - Larger asteroids worth more points
  - Score displayed during gameplay

- **Game Over System:**
  - Lives system with visual counter
  - Invulnerability period after taking damage
  - Game over screen with final score
  - Press 'N' to start a new game

- **Multi-threaded Game Loop:**  
  The game logic runs on its own dedicated thread (in `GameEngine.java`), ensuring smooth physics updates and collision detection at roughly 60 FPS.

- **Concurrent Asteroid Spawning:**  
  A separate thread periodically creates new asteroids, keeping the gameplay challenging and unpredictable.

- **Object-Oriented Design:**  
  - An abstract `GameObject` class defines common properties and behaviors.  
  - `PlayerShip`, `Asteroid`, and `Bullet` extend from `GameObject` to encapsulate their unique logic and rendering.  
  - Polymorphism is used extensively so that all game entities are handled uniformly by the game engine.

- **Collision Detection and Handling:**  
  The engine detects collisions between objects (e.g., bullets with asteroids, player ship with asteroids). Asteroids split when hit, simulating classic Asteroids game behavior.

- **Screen Wrapping:**  
  All objects, including the player ship, asteroids, and bullets, wrap around the screen edges, providing a continuous playfield.

- **Responsive Controls:**  
  Utilize the keyboard to steer the player ship:
  - **Left Arrow:** Rotate left.
  - **Right Arrow:** Rotate right.
  - **Up Arrow:** Accelerate forward.
  - **Spacebar:** Fire bullets.

- **Thread Safety:**  
  Synchronization is applied to shared resources (such as the game objects list) to ensure safe access from multiple threads.

## Project Structure

- **SoundManager.java**
  Handles real-time sound synthesis and playback using Java's audio system. Generates retro-style sound effects for all game events.

- **Main.java**  
  The entry point of the application. It creates the game window using Swing, initializes the game engine, and starts the game loop.

- **GameEngine.java**  
  Contains the main game update logic, collision detection, and object management. It also handles screen wrapping and launches a separate thread for FPS simulation and asteroid spawning.

- **GamePanel.java**  
  A custom `JPanel` responsible for rendering all game objects and handling keyboard input using the Swing Event Dispatch Thread (EDT). It leverages a Swing `Timer` to maintain roughly 60 FPS on screen.

- **GameObject.java**  
  An abstract class that defines the core properties (position, velocity, and alive status) and abstract methods (`update()`, `draw()`, `getBounds()`) for all game entities.

- **PlayerShip.java**  
  Implements the player-controlled ship. It handles rotation, acceleration, damage (via collision with asteroids), and bullet firing.

- **Asteroid.java**  
  Represents asteroids. When hit by a bullet, an asteroid will either split into smaller pieces or be destroyed if it is already small.

- **Bullet.java**  
  Defines bullets fired by the player's ship, managing their lifespan and movement.

## Requirements

- **Java Version:**  
  Java 8 or later is required.

- **IDE (Optional):**  
  This project can be compiled and run from the command line or through your favorite IDE (IntelliJ IDEA, Eclipse, NetBeans, etc.).

## Compiling and Running

1. **Compile** all the Java files using:
   ```bash
   javac *.java
   ```
2. **Run** the game by launching the `Main` class:
   ```bash
   java Main
   ```

Alternatively, you can import the project into your IDE and run the `Main` class directly.

## Usage and Controls

- **Left Arrow:** Rotate the player's ship counter-clockwise.
- **Right Arrow:** Rotate the player's ship clockwise.
- **Up Arrow:** Accelerate the player's ship in the current facing direction.
- **Space Bar:** Fire a bullet from the ship's tip.
- **N:** Start a new game (when game over)

## Technical Overview

- **Object-Oriented Principles:**  
  - *Encapsulation:* Each game entity (ship, asteroid, bullet) manages its own state and behavior.  
  - *Inheritance:* Common functionality is defined in the abstract `GameObject` class, which is then extended by other classes.  
  - *Polymorphism:* The game engine processes different game objects in a unified collection, regardless of their specific types.

- **Multi-threading:**  
  - The **GameEngine** runs the main update loop on a separate thread to keep game simulation smooth.  
  - **Asteroid Spawning:** A separate thread periodically spawns new asteroids.  
  - **Sound Playback:** Sound effects are handled on a separate thread pool.
  - **Event Dispatch Thread (EDT):** Swing handles rendering and user input, which is coordinated with the game engine threads using synchronized blocks.

## Future Enhancements

- **Enhanced Graphics:**  
  Incorporate detailed animations, explosion effects, and improved visuals.

- **Sound Effects:**  
  Add background music and sound effects for actions like firing bullets and asteroid explosions.

- **Score and High Score System:**  
  Track the player's score and add a high score leaderboard.

- **Improved Collision Detection:**  
  Consider more advanced collision detection (e.g., pixel-perfect) for more complex object shapes.

## License

This project is licensed under the MIT License. See the LICENSE file for details.

## Acknowledgments

This project was developed as an example of applying advanced Java programming techniques in game development. It showcases multi-threading, object-oriented design, and real-time rendering using Java Swing.

Enjoy the game and happy coding! 