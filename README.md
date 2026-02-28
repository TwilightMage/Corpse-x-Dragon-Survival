# Corpse x Dragon Survival
This mod is a compatibility addon for [Corpse](https://www.curseforge.com/minecraft/mc-mods/corpse) and [Dragon Survival](https://www.curseforge.com/minecraft/mc-mods/dragons-survival).

It must present on both client and server.

## Known issues:
- Dragon corpses don't turn skeletons over time - I need a texture at least and, also, good addition would be to filter which parts to keep on skeletons and which to remove (horns - keep, most other stuff - remove)
- If you die as human and then turn dragon, your human corpse will be invisible (works fine in opposite, i.e. die as dragon and turn human - dragon corpse still visible)

## Configuration
### Server
- `corpseAnimationMap` - Here you can define poses for corpses. Each entry is a string that consist of two parts: dragon body types (central, eastern, etc.) and animations that will be used to take pose for any of these body types. Only first animation frame is used as a pose.<br>Value example:<br>`type1;type2:anim1;anim2` - take pose from `anim1` and `anim2` for either body `type1` or `type2`.<br>You can define specific dead poses for custom models, just make sure to provide valid body type name and valid animation name.