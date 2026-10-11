# Scaled Dragon Fight 🌠

A lightweight **server-side Fabric mod** that dynamically scales the **Ender Dragon's** health based on the number of players in The End — now with support for **delayed initial spawns** and XP bar countdowns.

No installation is needed on the client — just drop it into your server's `mods` folder and configure it to your liking.

---

## 🔧 Features

- 📈 **Dynamic Health Scaling:** The Ender Dragon's max health increases with each eligible player in The End.
- ⏱️ **Initial Spawn Delay:** Optionally delay the very first dragon spawn and show a countdown on players’ XP bars.
- ⚙️ **Server-Side Only:** No client installation required — perfect for modded or vanilla-compatible SMPs.
- 🔧 **Configurable:** Customize how scaling and spawn delays work via a structured `.toml` config file.
- 📢 **Broadcast Messages:** Optional announcements when a scaled dragon appears.
- 🛠️ **In-Game Reload:** `/scaleddragonfight reload` command to reload configuration without restarting the server.

---

## 🗂️ Configuration

After first launch, a config file will be generated at:

```
/config/scaleddragonfight.toml
```

You can configure:

```
# ─────────────────────────────
# General Settings
# ─────────────────────────────
[general]
enableMod = true                    # Master enable/disable switch for the mod
scaleWithOnePlayer = false          # Whether scaling should start with the first player
countCreativeModePlayers = false    # Include players in Creative mode when scaling

# ─────────────────────────────
# Dragon Health Scaling
# ─────────────────────────────
[healthScaling]
baseDragonHealth = 200.0            # Base health of the Ender Dragon (vanilla default = 200)
additionalHealthPerPlayer = 100.0   # Extra health added per eligible player

# ─────────────────────────────
# Broadcast Messages
# ─────────────────────────────
[broadcastMessages]
enableBroadcast = true              # Enable or disable broadcast messages

# ─────────────────────────────
# Initial Spawn Delay
# ─────────────────────────────
[spawnDelay]
enableInitialSpawnDelay = true      # Delay the very first dragon spawn in The End
initialSpawnDelaySeconds = 60       # Time (in seconds) before first dragon spawns

# ─────────────────────────────
# XP Bar Countdown
# ─────────────────────────────
[countdown]
enableCountdownOverworld = true     # Show the countdown to players in the Overworld
enableCountdownTheEnd = true        # Show the countdown to players in The End
enableCountdownNether = false       # Show the countdown to players in the Nether
```

---

## 💻 Commands

| Command                       | Description                         |
|------------------------------|-------------------------------------|
| `/scaleddragonfight reload`  | Reloads the mod’s configuration     |

Requires permission level 2 (OP status).

---

## ❓ FAQ

**1. What happened to my configuration after updating to v2.0.0?**

Version 2.0.0 switched the config file from `.properties` to the new `.toml` format. Your old settings are automatically imported into `config/scaleddragonfight.toml`, and the original file is backed up to `config/scaleddragonfight.properties.bak`.

Please review the new file, since the single countdown option was split into one toggle per dimension. You can then run `/scaleddragonfight reload` to apply changes without a server restart. For more details, see the v2.0.0 Changelog.

---

## 🔌 Compatibility

- Minecraft: `1.21.4 - 1.21.5, 1.21.7 - 1.21.11`
- Fabric Loader: Latest stable version for your Minecraft version
- Fabric API required
- **Server-side only** — no client-side installation needed


> **⚠️ Mod/Datapack Interactions:** This mod modifies vanilla Ender Dragon spawning logic. It may exhibit unintended behavior or conflicts with other mods/datapacks that introduce custom dragon spawning mechanics (e.g., True Ending). While I aim to improve compatibility in the future, full support for third-party dragon overhauls is not guaranteed.
---

## 📦 Installation

1. Install [Fabric Loader](https://fabricmc.net/) on your server.
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) on the server.
3. Place the `scaleddragonfight-vX.X.X_X.X.X.jar` into your server's `mods` folder. 
   *(Note: The first version number is the mod version, and the second is the Minecraft version, e.g., `v1.1.0_1.21.1`)*
4. Start the server once to generate the config file.
5. Modify the config if desired and reload with `/scaleddragonfight reload`.

---

## 📜 License

MIT License — free to use, modify, and distribute.

---

## 👤 Author

Developed by [ZephByte](https://github.com/zephbyte)
