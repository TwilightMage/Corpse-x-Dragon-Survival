package org.drakosha.corpsedragonsurvival.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.drakosha.corpsedragonsurvival.CorpseDragonSurvival;

public class Network {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(CorpseDragonSurvival.MODID, "dragon_capability"))
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    private static int packetId = 0;

    public static void register() {
        INSTANCE.registerMessage(packetId++,
                DragonCorpseMessage.class,
                (msg, buf) -> msg.encode(msg, buf),
                (buf) -> {
                    DragonCorpseMessage msg = new DragonCorpseMessage();
                    return msg.decode(buf);
                },
                (msg, ctx) -> msg.handle(msg, ctx)
        );
    }
}
