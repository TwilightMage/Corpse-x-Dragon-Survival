package org.drakosha.corpsedragonsurvival;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = CorpseDragonSurvival.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<List<? extends List<? extends List<? extends String>>>> CORPSE_ANIMATION_OVERRIDES = BUILDER
            .comment("""
                    Animation IDs to use for corpse poses by body types
                    Animation will be frozen on first frame
                    Each line must consist of two arrays: body type list and animation list
                    Example:
                    [
                        [["dragonsurvival:north", "dragonsurvival:east"], ["animation1", "animation2"]],
                        [["dragonsurvival:bee_queen"], ["animation3"]]
                    ]""")
            .defineList("corpseAnimationOverrides",
                    List.of(),
                    () -> List.of(List.of(), List.of()),
                    obj -> true
            );

    private static final List<List<List<String>>> corpseAnimationOverridesBase = List.of(
            List.of(
                    List.of("dragonsurvival:center",
                            "dragonsurvival:east",
                            "dragonsurvival:west",
                            "dragonsurvival:south",
                            "dragonsurvival:north"),
                    List.of("sleeping_on_side_left",
                            "resting_straight",
                            "resting_on_back")
            ),
            List.of(
                    List.of("dragonsurvival:aether_body"),
                    List.of("sleep",
                            "sleep_left")
            ),
            List.of(
                    List.of("dragonsurvival:bee_queen"),
                    List.of("sleep",
                            "resting_straight")
            ),
            List.of(
                    List.of("dragonsurvival:claw_monster"),
                    List.of("rocking_on_back")
            ),
            List.of(
                    List.of("dragonsurvival:griffin_general"),
                    List.of("sleep")
            )
    );

    static final ModConfigSpec SPEC = BUILDER.build();

    public static Map<String, List<String>> corpseEmoteMap;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
        corpseEmoteMap = new HashMap<>();

        applyCorpseAnimationOverrides(corpseAnimationOverridesBase);
        applyCorpseAnimationOverrides(CORPSE_ANIMATION_OVERRIDES.get());
    }

    private static void applyCorpseAnimationOverrides(List<? extends List<? extends List<? extends String>>> overrides)
    {
        for (var mapping : overrides) {
            for (var bodyType : mapping.get(0)) {
                corpseEmoteMap.put(bodyType, mapping.get(1).stream().collect(Collectors.toUnmodifiableList()));
            }
        }
    }
}
