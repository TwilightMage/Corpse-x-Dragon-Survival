package org.drakosha.corpsedragonsurvival.capabilities;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.drakosha.corpsedragonsurvival.CorpseDragonSurvival;

import java.util.function.Supplier;

public class Capabilities {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, CorpseDragonSurvival.MODID);

    public static final Supplier<AttachmentType<DragonCorpseData>> DRAGON_CORPSE_DATA =
            ATTACHMENT_TYPES.register("dragon_corpse", () -> AttachmentType.serializable(DragonCorpseData::new)
                    .build());

    public static final EntityCapability<DragonStateHandler, Void> DRAGON_CAPABILITY = by.dragonsurvivalteam.dragonsurvival.common.capability.Capabilities.DRAGON_CAPABILITY;

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
