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
    *   **Cleanup Engine**: Regex-based post-processing (`cleanup`) to fix generative artifacts (like trailing apostrophes).
    *   **Capitalization Rules**: Regex-based capitalization (`capitalization`) to control casing (e.g. capitalize each word, keep “of” lowercase).
    *   **Max Length Enforcement**: Per-template `maxLength` with retry logic so outputs stay within limits.
    *   **Alliteration Support**: Bind/reuse picks inside templates to encourage shared onsets/sounds (useful for halfling-style names).
*   **CLI Testing Mode**: Test your generation logic directly from the terminal without starting a Minecraft server.

## Quick Start

1.  Drop the JAR into your `plugins` folder.
2.  Start the server. The plugin will automatically extract the default templates.
3.  Type `/namegen elf` (or another visible type) in-game.

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
    maxLength: 32
```

### Weighted Lists
Lists can contain simple strings or weighted entries.

```yaml
lists:
  colors:
    - "Red": 10
    - "Blue": 10
    - "Gold": 1 # Rare!
```

### Template fields
- **`pattern` / `variants`**: choose a single string (`pattern`) or one/more weighted strings (`variants`). `pattern` is shorthand for a single variant.
- **`hidden`**: if `true`, the template will not appear in `/namegen` tab-complete.
- **`cleanup`**: regex applied after generation to remove unwanted artifacts.
- **`capitalization`**: regex; every match is uppercased (useful for title-casing).
- **`maxLength`**: maximum output length (default `32`). If exceeded, generation retries (up to 10 attempts).

Note: `cleanup` and `capitalization` apply to the *template being expanded*, including when it is expanded as a nested `%token%`.

### Bind/reuse (alliteration) syntax
The expander supports binding a resolved token to a variable and reusing it later in the same generation.

- **Bind**: `%=var:token%` resolves `token`, stores the result in `var`, and returns it.
- **Bind (silent)**: `%=var!:token%` resolves and stores, but returns an empty string (useful when you only want reuse).
- **Reuse**: `%=var%` returns the previously bound value.

Example (shared onset for alliteration-like coupling):

```yaml
templates:
  first_bound:
    hidden: true
    variants: ["%=onset%%vowel%%coda%"]

  last_bound:
    hidden: true
    variants: ["%=onset%%food%%suffix%"]

  fullname:
    pattern: "%=onset!:onset_list%%first_bound% %last_bound%"
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
./gradlew run --args="src/main/resources/generators elf 10"
```

### CLI debug mode
Add `debug` (or `--debug`) to print which `pattern`/`variant` was picked for each generated name. The output is formatted into fixed columns for easy scanning.

```bash
./gradlew run --args="src/main/resources/generators dwarf_masc 10 debug"
```

### Print all visible templates (debug)
This runs the CLI once per visible template key and prints a 1-name debug row for each:

```bash
for t in dwarf_masc dwarf_femme elf goblin_masc goblin_femme kobold_masc kobold_femme wildborne_masc wildborne_femme fae believer_masc believer_femme giant_masc giant_femme gnome_masc gnome_femme; do
  ./gradlew -q run --args="src/main/resources/generators $t 1 debug"
done
```

---
