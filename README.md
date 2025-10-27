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
enable = true                       # Enable or disable broadcast messages
onSpawn = true                      # Send message when a scaled dragon spawns
onDeath = false                     # Send message when the dragon is defeated
showHealthInMessage = true          # Include dragon’s health value in broadcast

# ─────────────────────────────
# Initial Spawn Delay
# ─────────────────────────────
[initialSpawnDelay]
enable = true                       # Delay the very first dragon spawn in The End
delaySeconds = 60                   # Time (in seconds) before first dragon spawns
showCountdown = true                # Show XP bar countdown during the delay

# ─────────────────────────────
# Miscellaneous
# ─────────────────────────────
[misc]
logDebugInfo = false                # Enable extra debug logging in console

```

---

## 💻 Commands

| Command                       | Description                         |
|------------------------------|-------------------------------------|
| `/scaleddragonfight reload`  | Reloads the mod’s configuration     |

Requires permission level 2 (OP status).

---

## ❓ FAQ

**1. Why did my configuration reset after updating to v2.0.0?**

Version 2.0.0 switched the config file from `.properties` to the new `.toml` format. Your old settings were automatically backed up to `config/scaleddragonfight.properties.bak`.

Please copy your settings to the new `scaleddragonfight.toml` file. You can then run `/scaleddragonfight reload` to apply changes without a server restart. For more details, see the v2.0.0 Changelog.

---

## 🔌 Compatibility

- Minecraft: `1.21.5, 1.21.7 - 1.21.9`
- Fabric Loader: `0.14+`
- Fabric API required
- **Server-side only** — no client-side installation needed

---

## 📦 Installation

1. Install [Fabric Loader](https://fabricmc.net/) on your server.
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) on the server.
3. Drop the `scaleddragonfight-x.x.x.jar` into the server’s `mods` folder.
4. Start the server once to generate the config file.
5. Modify the config if desired and reload with `/scaleddragonfight reload`.

---

## 📜 License

MIT License — free to use, modify, and distribute.

---

## 👤 Author

Developed by [ZephByte](https://github.com/zephbyte)