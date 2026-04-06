
Combat Mutation
-------------------------------------------
Forge mod for Minecraft 1.20.1 that makes mobs recover and evolve after combat.

Behavior
==============================

- Any mob that takes damage enters a recovery window.
- If the mob survives for the configured cooldown without taking more damage, it heals back to full health.
- If the damage came from a player or another mob, the survivor also mutates at the end of that recovery window.
- Each mutation permanently increases max health, movement speed, and any attack or armor attributes the mob already has.
- Mutation state is stored on the mob, so it survives save and reload.

Config
==============================

The common config file is generated as `run/config/combatmutation-common.toml`.

Default values:
- `healDelaySeconds = 10`
- `maxMutations = 5`
- `bonusHealthPerMutation = 4.0`
- `bonusAttackPerMutation = 1.5`
- `bonusArmorPerMutation = 1.0`
- `speedMultiplierPerMutation = 0.08`

Setup
==============================

This mod targets Minecraft 1.20.1 with Forge 47.4.16 and requires Java 17.

1. Install a Java 17 JDK.
2. Open the project in IntelliJ IDEA or Eclipse as a Gradle project.
3. Run `gradlew genIntellijRuns` or `gradlew genEclipseRuns`.
4. Launch the generated client configuration.

Useful commands:
- `gradlew runClient`
- `gradlew runServer`
- `gradlew build`

GitHub Actions Build
==============================

If you cannot install Java locally, upload this project to GitHub and use the `Build Mod` workflow.

1. Push the contents of this folder to a GitHub repository.
2. Open the repository on GitHub.
3. Click the `Actions` tab.
4. Run `Build Mod`, or push a commit to trigger it automatically.
5. Open the finished workflow run and download the `combat-mutation-jar` artifact.
