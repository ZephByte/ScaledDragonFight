# Scaled Dragon Fight

[![GitHub release](https://img.shields.io/github/v/release/ZephByte/ScaledDragonFight?include_prereleases&label=release&color=blue)](https://github.com/ZephByte/ScaledDragonFight/releases/latest)
[![Fabric](https://img.shields.io/badge/mod%20loader-fabric-blue)](https://fabricmc.net/)
[![Modrinth](https://img.shields.io/modrinth/v/scaled-dragon-fight?label=modrinth&logo=modrinth&color=00AF5C)](https://modrinth.com/mod/scaled-dragon-fight)
[![License: MIT](https://img.shields.io/github/license/ZephByte/ScaledDragonFight)](LICENSE.txt)

A server-side Fabric mod that scales the Ender Dragon's health with the number of players in The End, and can hold back the first dragon spawn behind a countdown.

---

## Features

- **Health scaling:** the dragon's max health grows with every eligible player in The End when it spawns.
- **Spawn delay:** delay the very first dragon spawn in a world, so everyone has time to get to The End.
- **Countdown:** show the time left before the dragon spawns in the action bar, separately for the Overworld, the Nether, and The End.
- **Broadcasts:** announce the dragon to the whole server when it appears, including how many players it scaled with.
- **Creative players:** choose whether players in Creative mode count toward scaling.
- **Reload in game:** apply config changes with `/scaleddragonfight reload`, no restart needed.
- **Server-side only:** players don't need to install anything.

---

## Download

- **[Modrinth](https://modrinth.com/mod/scaled-dragon-fight)** (recommended)
- **[GitHub Releases](https://github.com/ZephByte/ScaledDragonFight/releases)**

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/) on your server.
2. Install [Fabric API](https://modrinth.com/mod/fabric-api).
3. Install [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin).
4. Drop the mod `.jar` for your Minecraft version into the server's `mods` folder. Jars are named `ScaledDragonFight-<mod version>+<Minecraft version>.jar`, e.g. `ScaledDragonFight-v2.0.0+26.3.jar`.
5. Start the server once to generate `config/scaleddragonfight.toml`.

Server-side only. No client installation is needed.

---

## Compatibility

| Minecraft | Fabric Loader | Fabric API | Fabric Language Kotlin |
|---|---|---|---|
| `26.3` | `0.20.1+` | `0.166.2+26.3` | `1.13.9+kotlin.2.3.10` |
| `26.2` | `0.20.1+` | `0.161.0+26.2` | `1.13.9+kotlin.2.3.10` |
| `26.1` | `0.18.4+` | `0.144.0+26.1` | `1.13.9+kotlin.2.3.10` |
| `1.21.11` | `0.18.4+` | `0.140.2+1.21.11` | `1.13.9+kotlin.2.3.10` |
| `1.21.10` | `0.17.3+` | `0.136.0+1.21.10` | `1.13.9+kotlin.2.3.10` |
| `1.21.9` | `0.17.2+` | `0.133.14+1.21.9` | `1.13.9+kotlin.2.3.10` |
| `1.21.8` | `0.17.2+` | `0.131.0+1.21.8` | `1.13.9+kotlin.2.3.10` |
| `1.21.7` | `0.16.14+` | `0.128.2+1.21.7` | `1.13.9+kotlin.2.3.10` |
| `1.21.5` | `0.16.13+` | `0.120.0+1.21.5` | `1.13.9+kotlin.2.3.10` |
| `1.21.4` | `0.18.4+` | `0.119.4+1.21.4` | `1.13.9+kotlin.2.3.10` |

Each jar also works on that version's patch releases, e.g. the 26.1 jar runs on 26.1.1 and 26.1.2.

Minecraft 1.21.x servers need Java 21 or newer, and 26.x servers need Java 25 or newer.

This mod changes how the vanilla dragon spawns, so it may not work well with mods or datapacks that add their own dragon spawning, such as True Ending.

---

## Usage

| Command | Description |
|---|---|
| `/scaleddragonfight reload` | Reloads `config/scaleddragonfight.toml` |

Requires permission level 3 or higher (server ops have level 4 by default).

### How health scales

The dragon's max health is `baseDragonHealth + additionalHealthPerPlayer × players`, counting the eligible players in The End when it spawns. By default the first player doesn't add health, so scaling starts with the second player. Turn on `scaleWithOnePlayer` to count the first player too.

With the defaults (200 base, 100 per player):

| Players in The End | Max health | With `scaleWithOnePlayer` |
|---|---|---|
| 1 | 200 | 300 |
| 2 | 300 | 400 |
| 3 | 400 | 500 |

### Config

All settings live in `config/scaleddragonfight.toml`. Each one is written to the file with its default and a description.

| Setting | Default | Description |
|---|---|---|
| `general.enableMod` | `true` | Turn the whole mod on or off |
| `general.scaleWithOnePlayer` | `false` | Count the first player toward scaling |
| `general.countCreativeModePlayers` | `false` | Count players in Creative mode toward scaling |
| `healthScaling.baseDragonHealth` | `200.0` | The dragon's base health (vanilla is 200) |
| `healthScaling.additionalHealthPerPlayer` | `100.0` | Health added for each counted player |
| `broadcastMessages.enableBroadcast` | `true` | Announce the dragon to the server when it appears |
| `spawnDelay.enableInitialSpawnDelay` | `true` | Delay the first dragon spawn in a world |
| `spawnDelay.initialSpawnDelaySeconds` | `60` | How long to delay it, in seconds |
| `countdown.enableCountdownOverworld` | `true` | Show the countdown to players in the Overworld |
| `countdown.enableCountdownNether` | `false` | Show the countdown to players in the Nether |
| `countdown.enableCountdownTheEnd` | `true` | Show the countdown to players in The End |

---

## Updating from 1.x

Version 2.0.0 replaced `scaleddragonfight.properties` with `scaleddragonfight.toml`. On the first start after updating, your old settings are imported into the new file and the old one is kept as `scaleddragonfight.properties.bak`. The first operator to join gets a message about it.

The old single countdown toggle is now one toggle per dimension. If you had the countdown turned off, it stays off everywhere. Review the new file, then run `/scaleddragonfight reload` to apply any changes.

---

## Building from Source

```bash
git clone https://github.com/ZephByte/ScaledDragonFight.git
cd ScaledDragonFight
./gradlew build
```

One jar per Minecraft version is written to `versions/<Minecraft version>/build/libs/`.

---

## License

MIT - free to use, modify, and redistribute. See [LICENSE](LICENSE.txt).

---

## Author

Made by [ZephByte](https://github.com/ZephByte).
