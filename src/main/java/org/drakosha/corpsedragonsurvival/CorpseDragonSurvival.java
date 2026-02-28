package org.drakosha.corpsedragonsurvival;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import com.mojang.logging.LogUtils;
import de.maxhenkel.corpse.corelib.CachedMap;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import org.drakosha.corpsedragonsurvival.capabilities.Capabilities;
import org.drakosha.corpsedragonsurvival.capabilities.DragonCorpseData;
import org.drakosha.corpsedragonsurvival.network.DragonCorpseMessage;
import org.drakosha.corpsedragonsurvival.network.Network;
import org.slf4j.Logger;
import net.minecraft.server.level.ServerPlayer;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CorpseDragonSurvival.MODID)
public class CorpseDragonSurvival {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "corpsedragonsurvival";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // corpse -> corpse state
    public static final CachedMap<CorpseEntity, DragonCorpseState> CORPSE_STATES = new CachedMap(10000L);

    public CorpseDragonSurvival() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        Network.register();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public class ModEvents {
        @SubscribeEvent
        public static void onEntityJoinLevelEvent(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof CorpseEntity corpse)
            {
                if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel serverLevel) {
                    corpse.getCorpseUUID().ifPresent(uuid -> {
                        ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);

                        if (player != null) {
                            LazyOptional<DragonStateHandler> dragonCap = player.getCapability(Capabilities.DRAGON_CAPABILITY);

                            DragonCorpseUtils.ifDragon(dragonCap, (playerHandler) -> {
                                LazyOptional<DragonCorpseData> dragonCorpseCap = corpse.getCapability(Capabilities.DRAGON_CORPSE_CAPABILITY);
                                dragonCorpseCap.ifPresent(corpseData -> {
                                    corpseData.handler = new DragonStateHandler();
                                    corpseData.handler.readNBT(playerHandler.writeNBT());
                                    corpseData.animation = DragonCorpseUtils.pickDeadAnimation(playerHandler);

                                    CorpseDragonSurvival.LOGGER.debug("Saved dragon data for corpse {}", player.getGameProfile().getName());
                                });
                            });
                        } else {
                            CorpseDragonSurvival.LOGGER.error("Failed to find corpse owner with UUID: {}", uuid);
                        }
                    });
                }
            }
        }

        @SubscribeEvent
        public static void onStartTracking(PlayerEvent.StartTracking event) {
            if (event.getTarget() instanceof CorpseEntity corpse) {
                corpse.getCapability(Capabilities.DRAGON_CORPSE_CAPABILITY).ifPresent(data -> {
                    if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                        Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new DragonCorpseMessage(data.writeNBT(), corpse.getId()));
                    }
                });
            }
        }
    }
}
