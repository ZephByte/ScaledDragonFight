# Changelog

## Scaled Dragon Fight - v2.0.0

New config format and one release for Minecraft 26.1, 26.2, and 26.3.

### Changes
- **New TOML config** - settings now live in `config/scaleddragonfight.toml`, grouped into sections with the default value and a description above each setting
- **Automatic migration** - an existing `scaleddragonfight.properties` is imported into the new file on first start and kept as `scaleddragonfight.properties.bak`. The first operator to join is told what happened
- **Countdown per dimension** - the XP bar countdown can be turned on or off separately for the Overworld, the Nether, and The End, replacing the single countdown toggle. A disabled countdown in an old config stays off everywhere
- **Patch versions** - each jar now loads on every patch of its Minecraft version, e.g. the 26.1 jar works on 26.1.1 and 26.1.2

### Requirements
| Minecraft | Fabric Loader | Fabric API | Fabric Language Kotlin |
|---|---|---|---|
| 26.3 | 0.20.1+ | 0.166.2+26.3 | 1.13.9+kotlin.2.3.10 |
| 26.2 | 0.20.1+ | 0.161.0+26.2 | 1.13.9+kotlin.2.3.10 |
| 26.1 - 26.1.2 | 0.18.4+ | 0.144.0+26.1 | 1.13.9+kotlin.2.3.10 |
