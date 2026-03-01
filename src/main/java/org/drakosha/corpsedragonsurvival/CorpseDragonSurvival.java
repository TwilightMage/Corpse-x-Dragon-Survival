package org.drakosha.corpsedragonsurvival;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import com.mojang.logging.LogUtils;
import de.maxhenkel.corpse.corelib.CachedMap;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.drakosha.corpsedragonsurvival.capabilities.Capabilities; // You will need to update this class
import org.drakosha.corpsedragonsurvival.capabilities.DragonCorpseData;
import org.drakosha.corpsedragonsurvival.network.DragonCorpseMessage;
import org.slf4j.Logger;

import java.util.Objects;

@Mod(CorpseDragonSurvival.MODID)
public class CorpseDragonSurvival {

    public static final String MODID = "corpsedragonsurvival";
    public static final Logger LOGGER = LogUtils.getLogger();

    // corpse -> corpse state
    public static final CachedMap<CorpseEntity, DragonCorpseState> CORPSE_STATES = new CachedMap(10000L);

    public CorpseDragonSurvival(IEventBus eventBus, ModContainer modContainer) {
        Capabilities.register(eventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    @EventBusSubscriber(modid = CorpseDragonSurvival.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static class ModEvents {
        @SubscribeEvent
        public static void onEntityJoinLevelEvent(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof CorpseEntity corpse)
            {
                if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel serverLevel) {
                    corpse.getCorpseUUID().ifPresent(uuid -> {
                        ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);

                        if (player != null) {
                            DragonStateHandler playerHandler = player.getCapability(Capabilities.DRAGON_CAPABILITY);

                            if (DragonCorpseUtils.isDragon(playerHandler)) {
                                HolderLookup.Provider provider = serverLevel.registryAccess();

                                DragonCorpseData corpseData = new DragonCorpseData();
                                corpseData.handler.deserializeNBT(provider, playerHandler.serializeNBT(provider));
                                corpseData.animation = DragonCorpseUtils.pickDeadAnimation(playerHandler);
                                corpseData.scale = corpseData.handler.calculateScale(Objects.requireNonNull(player.getAttribute(Attributes.SCALE)), corpseData.handler.getGrowth());

                                corpseData.mainHand = corpse.getEquipment().get(EquipmentSlot.MAINHAND.ordinal());
                                corpseData.offHand = corpse.getEquipment().get(EquipmentSlot.OFFHAND.ordinal());
                                corpseData.feet = corpse.getEquipment().get(EquipmentSlot.FEET.ordinal());
                                corpseData.legs = corpse.getEquipment().get(EquipmentSlot.LEGS.ordinal());
                                corpseData.chest = corpse.getEquipment().get(EquipmentSlot.CHEST.ordinal());
                                corpseData.head = corpse.getEquipment().get(EquipmentSlot.HEAD.ordinal());

                                corpse.setData(Capabilities.DRAGON_CORPSE_DATA, corpseData);

                                CorpseDragonSurvival.LOGGER.debug("Saved dragon data for corpse {}", player.getGameProfile().getName());
                            }
                        } else {
                            CorpseDragonSurvival.LOGGER.error("Failed to find corpse owner with UUID: {}", uuid);
                        }
                    });
                }
            }
        }

        @SubscribeEvent
        public static void onStartTracking(PlayerEvent.StartTracking event) {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                if (event.getTarget() instanceof CorpseEntity corpse) {
                    DragonCorpseData corpseData = corpse.getData(Capabilities.DRAGON_CORPSE_DATA);

                    if (DragonCorpseUtils.isDragonCorpse(corpseData)) {
                        HolderLookup.Provider provider = serverPlayer.level().registryAccess();
                        PacketDistributor.sendToPlayer(serverPlayer, new DragonCorpseMessage(corpseData.serializeNBT(provider), corpse.getId()));
                    }
                }
            }
        }
    }
}
