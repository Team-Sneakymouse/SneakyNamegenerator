# SneakyNamegenerator

A powerful, data-driven fantasy name generator plugin for Minecraft (Paper 1.21.4). Create complex, linguistically authentic names using a recursive template engine with weighted lists and interactive chat features.

## Features

*   **Recursive Template Engine**: Build names from nested syllables, components, and weighted lists.
*   **Rich Interactive UX**: 
    *   **Click-to-Copy**: Click any generated name in chat to copy it to your clipboard.
    *   **Navigation Buttons**: Use `[ < Previous ]` and `[ Next > ]` buttons to cycle through your generation history or generate more sets.
    *   **Session Management**: History is tracked per-player for the duration of their session.
*   **Modular Configuration**:
    *   **Directory Loading**: Automatically scans and merges all `.yml` files in the plugin folder.
    *   **Hidden Templates**: Hide internal helper templates (like syllables) from tab completion.
    *   **Cleanup Engine**: Regex-based post-processing to fix generative artifacts (like trailing apostrophes).
*   **CLI Testing Mode**: Test your generation logic directly from the terminal without starting a Minecraft server.

## Quick Start

1.  Drop the JAR into your `plugins` folder.
2.  Start the server. The plugin will automatically extract the default templates.
3.  Type `/namegen elven_modern` or `/namegen elven_traditional` in-game.

## Configuration

### Template Structure
Templates are defined in YAML. They support simple strings, weighted lists, or complex patterns.

```yaml
templates:
  # A hidden internal syllable component
  _syllable:
    hidden: true
    variants: ["%onset%%nucleus%%coda%"]

  # A main template visible in tab completion
  fantasy_name:
    pattern: "%_syllable%%_syllable%"
    capitalization: "(?<=^|\\s)."
    cleanup: "'+(?=\\s|$)"
```

### weighted Lists
Lists can contain simple strings or weighted entries.

```yaml
lists:
  colors:
    - "Red": 10
    - "Blue": 10
    - "Gold": 1 # Rare!
```

## ⌨️ Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/namegen <type> [amount]` | Generates name(s) of a specific type. | `sneakynamegenerator.command.namegen` |
| `/namegen next` | Generates the next set in the current session. | `sneakynamegenerator.command.namegen` |
| `/namegen prev` | Shows the previous set in the session history. | `sneakynamegenerator.command.namegen` |
| `/namegen reload` | Reloads all generator configurations. | `sneakynamegenerator.command.reload` |

## 🧪 CLI Testing
Speed up your development by testing templates from your terminal:

```bash
./gradlew run --args="src/main/resources/generators elven_modern 10"
```

---
