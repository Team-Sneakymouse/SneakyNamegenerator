# Agent instructions

Follow `../EmptyPlugin/AGENTS.md` for the shared rules.

## Repo-specific rules

- Races are data: edit `generators/<id>/components.yml` and `templates.yml` under `src/main/resources`. To ship a new id, also add an `ensureGeneratorPack` call in `SneakyNamegenerator.kt` `reloadGenerator`. Existing server files are never overwritten, so apply the same edit in `~/dev-main/SneakyNamegenerator/generators/`.
- `generator/` (Model, NameGenerator, ConfigLoader) must not import `org.bukkit`; `Main.kt` runs the same code as a CLI without a server. There are no tests yet; add JUnit tests for new template features (cleanup, capitalization, maxLength, bind picks).
- Helper templates use `hidden: true` and a leading underscore so they stay out of tab completion.
- `reloadGenerator` parses into a new registry and swaps it in only on success. Keep that, and log YAML errors with the file name.
- `SessionManager` keeps per-player history in memory, uncapped, and drops it on quit. Don't make it grow further without a cap.
