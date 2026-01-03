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