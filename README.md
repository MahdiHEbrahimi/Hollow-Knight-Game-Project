# 🎮 Hollow Knight – libGDX Project

> A 2D action‑platformer demo developed as the graphic project for the **Advanced Programming** course at **Sharif University of Technology** – Spring 2026.

---

## 📖 About

This project is a fan‑inspired implementation of a **Hollow Knight**‑style game using the **libGDX** framework. It demonstrates core concepts of object‑oriented design, game loops, collision detection, entity management, and asset pipeline integration – all in a clean, modular architecture.

The game features:
- A **player character** with movement, jumping, and attacking mechanics
- **Tile‑based maps** loaded from Tiled `.tmx` files
- **Enemy AI** with basic patrol and chase behaviours
- **Camera follow** and smooth parallax backgrounds
- **Particle effects** and UI overlays
- **Asset management** using libGDX’s `AssetManager`

This is not a full recreation of the original game, but a technical showcase of what can be built with libGDX in a semester‑long project.

---

## 🛠️ Technologies

| Layer | Technology |
|-------|------------|
| **Language** | Java 25 |
| **Framework** | libGDX 1.13.0 |
| **Build Tool** | Gradle 9.5.1 (with Wrapper) |
| **Desktop Launcher** | LWJGL3 |
| **Mapping** | Tiled Map Editor |
| **Version Control** | Git + GitHub |

---

## 🚀 How to Build & Run

### Prerequisites
- Java 25 (or later)
- Gradle 9.5.1 (or use the included wrapper)

### Build the executable JAR
```bash
# On Linux / macOS
./gradlew lwjgl3:dist

# On Windows
gradlew.bat lwjgl3:dist
```

The runnable `.jar` file will be generated at:
```
lwjgl3/build/libs/HollowKnight-1.0.0.jar
```

### Run the game
```bash
java -jar lwjgl3/build/libs/HollowKnight-1.0.0.jar
```

Or simply double‑click the JAR file on your desktop.

---

## 📁 Project Structure

```
.
├── core/               # Main game logic (entities, maps, UI, physics)
├── lwjgl3/             # Desktop launcher and platform‑specific code
├── assets/             # All images, fonts, sounds, and Tiled maps
├── build.gradle        # Root build script
└── settings.gradle     # Module definitions
```

---

## 🎯 Features Implemented

- [x] Player movement (walk, jump, dash)
- [x] Attack combo system
- [x] Enemy AI (patrol, chase, attack)
- [x] Level loading from `.tmx` files
- [x] Collision detection with tiles and entities
- [x] Health & damage system
- [x] Simple particle effects
- [x] Menu screen and pause functionality
- [x] Asset pooling for performance

---

## 👨‍🏫 Course Context

This project was submitted as the **graphic assignment** for the *Advanced Programming* course (Spring 2026) at **Sharif University of Technology**.  
It was developed in a solely over 6 weeks, with emphasis on:

- Design patterns (Observer, State, Factory, Singleton)
- Clean code and SOLID principles
- Unit testing (JUnit)
- Version control with Git
- Build automation with Gradle

---

## 🙏 Acknowledgements

- **libGDX community** – for the excellent framework and documentation
- **Team Cherry** – for creating the original *Hollow Knight* that inspired this work

---

## 📜 License

This project is for **educational purposes only**. All assets (sprites, sounds, maps) are either original or used under fair use for non‑commercial demonstration.

---

## 📬 Contact

Have questions or suggestions? Feel free to open an issue or reach out via [GitHub](https://github.com/your-username/HollowKnight).

---

> *Built with ❤️ and a lot of coffee at Sharif University of Technology.*
