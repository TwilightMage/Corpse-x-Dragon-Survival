package org.drakosha.corpsedragonsurvival.network;

import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.drakosha.corpsedragonsurvival.CorpseDragonSurvival;
import org.drakosha.corpsedragonsurvival.capabilities.Capabilities;
import org.drakosha.corpsedragonsurvival.capabilities.DragonCorpseData;
import org.jetbrains.annotations.NotNull;

public record DragonCorpseMessage(CompoundTag nbt, int corpseId) implements CustomPacketPayload {

    public static final Type<DragonCorpseMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CorpseDragonSurvival.MODID, "dragon_corpse"));

    public static final StreamCodec<FriendlyByteBuf, DragonCorpseMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, DragonCorpseMessage::nbt,
            ByteBufCodecs.VAR_INT, DragonCorpseMessage::corpseId,
            DragonCorpseMessage::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(final DragonCorpseMessage payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();
            Entity entity = level.getEntity(payload.corpseId());

            if (entity instanceof CorpseEntity corpse) {
                HolderLookup.Provider provider = level.registryAccess();

                DragonCorpseData data = new DragonCorpseData();
                data.deserializeNBT(provider, payload.nbt());

                data.handler.isGrowthStopped = true;

                corpse.setData(Capabilities.DRAGON_CORPSE_DATA, data);
            }
        });
    }
}
