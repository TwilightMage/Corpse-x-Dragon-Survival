package org.drakosha.corpsedragonsurvival;

import de.maxhenkel.corpse.corelib.helpers.Pair;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = CorpseDragonSurvival.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CORPSE_ANIMATION_MAP = BUILDER
            .comment("""
                    Animation IDs to use for corpse poses by body types
                    Animation will be frozen on first frame
                    Each line must consist of two parts: body type list and animation list
                    Lists are delimited by semicolon""")
            .defineList("corpseAnimationMap", List.of(
                    "center;east;west;south;north:sleeping_on_side_left;sleeping_on_side_right;sleeping_on_back"
            ), o -> true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static Map<String, List<String>> corpseEmoteMap;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        corpseEmoteMap = new HashMap<>();

        var lines = CORPSE_ANIMATION_MAP.get().stream().map(s -> {
            String[] parts = s.split(":", 2);
            String[] keys = parts[0].split(";");
            String[] values = parts[1].split(";");

            return new Pair<>(keys, values);
        }).toList();

        lines.forEach(pair -> Arrays.stream(pair.getKey()).forEach(s -> corpseEmoteMap.put(s, List.of(pair.getValue()))));
    }
}
