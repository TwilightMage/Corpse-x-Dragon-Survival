package org.drakosha.corpsedragonsurvival.capabilities;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.drakosha.corpsedragonsurvival.CorpseDragonSurvival;

@Mod.EventBusSubscriber(modid = CorpseDragonSurvival.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Capabilities {
    public static final Capability<DragonCorpseData> DRAGON_CORPSE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<DragonStateHandler> DRAGON_CAPABILITY = by.dragonsurvivalteam.dragonsurvival.common.capability.Capabilities.DRAGON_CAPABILITY;

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (entity instanceof CorpseEntity) {
            ResourceLocation key = ResourceLocation.fromNamespaceAndPath(CorpseDragonSurvival.MODID, "dragon_corpse");
            DragonCorpseCapabilityProvider provider = new DragonCorpseCapabilityProvider();

            event.addCapability(key, provider);
        }
    }
}
