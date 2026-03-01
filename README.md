This mod is a compatibility addon for [Corpse](https://www.curseforge.com/minecraft/mc-mods/corpse) and [Dragon Survival](https://www.curseforge.com/minecraft/mc-mods/dragons-survival).

It must present on both client and server.

## Known issues:

*   Dragon corpses don't turn skeletons over time - in progress
*   If you die as human and then turn dragon, your human corpse will be invisible (works fine in opposite, i.e. die as dragon and turn human - dragon corpse still visible) - fixed in 1.21.1, will not be fixed in 1.20.1
*   Items are not put back into claw slots - planned soon

## Configuration

### Server

*   `corpseAnimationOverrides` - Here you can define poses for corpses. Each entry is a list that consist of two parts: dragon body types (central, eastern, etc.) and animations that will be used to take pose for any of these body types. Only first animation frame is used as a pose.  
    Example:  
    ```
    [
        [["dragonsurvival:north", "dragonsurvival:east"], ["animation1", "animation2"]],
        [["dragonsurvival:bee_queen"], ["animation3"]]
    ]
    ```
    take pose from `animation1` or `animation2` for either body `dragonsurvival:north` or `dragonsurvival:east`. `dragonsurvival:bee_queen` will use only `animation3`.

## Special thanks to
- Gluttony, from DS server, for pointing me out into important places in DS code
- Black Aures, creator of DS, for help with dragon skeletons