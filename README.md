# Scaled Dragon Fight 🌠

A lightweight **server-side Fabric mod** that dynamically scales the **Ender Dragon's** health based on the number of players in The End — now with support for **delayed initial spawns** and XP bar countdowns.

No installation is needed on the client — just drop it into your server's `mods` folder and configure it to your liking.

---

## 🔧 Features

- 📈 **Dynamic Health Scaling:** The Ender Dragon's max health increases with each eligible player in The End.
- ⏱️ **Initial Spawn Delay:** Optionally delay the very first dragon spawn and show a countdown on players’ XP bars.
- ⚙️ **Server-Side Only:** No client installation required — perfect for modded or vanilla-compatible SMPs.
- 🔧 **Configurable:** Customize how scaling and spawn delays work via a simple `.properties` config file.
- 📢 **Broadcast Messages:** Optional announcements when a scaled dragon appears.
- 🛠️ **In-Game Reload:** `/scaleddragonfight reload` command to reload configuration without restarting the server.

---

## 🗂️ Configuration

After first launch, a config file will be generated at:

```
/config/scaleddragonfight.properties
```

You can configure:

```
enableMod=true                          # Enable or disable the mod
scaleWithOnePlayer=false                # Whether scaling starts with the first player
countCreativeModePlayers=false          # Include creative players in scaling
baseDragonHealth=200.0                  # Base health of the Ender Dragon
additionalHealthPerPlayer=100.0         # Health added per eligible player
enableBroadcast=true                    # Enable broadcast when dragon spawns

# Initial Spawn Delay
enableInitialSpawnDelay=true            # Delay the first dragon spawn in The End
initialSpawnDelaySeconds=60             # Time (in seconds) before first dragon spawns
showSpawnDelayCountdown=true            # Show countdown above XP bar during delay
```

---

## 💻 Commands

| Command                       | Description                         |
|------------------------------|-------------------------------------|
| `/scaleddragonfight reload`  | Reloads the mod’s configuration     |

Requires permission level 2 (OP status).

---

## 🔌 Compatibility

- Minecraft: `1.21.5, 1.21.7 - 1.21.11`
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
