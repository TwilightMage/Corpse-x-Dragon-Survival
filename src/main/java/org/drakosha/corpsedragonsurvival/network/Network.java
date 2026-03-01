package org.drakosha.corpsedragonsurvival.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.drakosha.corpsedragonsurvival.CorpseDragonSurvival;

@EventBusSubscriber(modid = CorpseDragonSurvival.MODID, bus = EventBusSubscriber.Bus.GAME)
public class Network {
    @SubscribeEvent
    public static void registerPayloadHandlers(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(CorpseDragonSurvival.MODID)
                .versioned("1.0.0");

        registrar.playToClient(
                DragonCorpseMessage.TYPE,
                DragonCorpseMessage.STREAM_CODEC,
                DragonCorpseMessage::handleClient
        );
    }
}
