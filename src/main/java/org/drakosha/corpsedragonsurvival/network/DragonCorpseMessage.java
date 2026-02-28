package org.drakosha.corpsedragonsurvival.network;

import by.dragonsurvivalteam.dragonsurvival.network.IMessage;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.drakosha.corpsedragonsurvival.DragonCorpseUtils;
import org.drakosha.corpsedragonsurvival.capabilities.Capabilities;

import java.util.function.Supplier;

public class DragonCorpseMessage implements IMessage<DragonCorpseMessage> {
    private CompoundTag nbt;
    private int corpseId;

    public DragonCorpseMessage() {
        super();
    }

    public DragonCorpseMessage(CorpseEntity corpse) {
        super();
        DragonCorpseUtils.ifDragonCorpse(corpse.getCapability(Capabilities.DRAGON_CORPSE_CAPABILITY),data -> {
            this.nbt = data.writeNBT();
        });
        corpseId = corpse.getId();
    }

    public DragonCorpseMessage(CompoundTag nbt, int corpseId) {
        super();
        this.nbt = nbt;
        this.corpseId = corpseId;
    }

    public void encode(DragonCorpseMessage message, FriendlyByteBuf buffer) {
        buffer.writeNbt(message.nbt);
        buffer.writeInt(message.corpseId);
    }

    public DragonCorpseMessage decode(FriendlyByteBuf buffer) {
        return new DragonCorpseMessage(buffer.readNbt(), buffer.readInt());
    }

    @Override
    public void handle(DragonCorpseMessage message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.enqueueWork(this::run);
        }
        context.setPacketHandled(true);
    }

    private void run()
    {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity entity = level.getEntity(corpseId);
        if (entity == null) return;

        if (!(entity instanceof CorpseEntity corpse)) return;

        corpse.getCapability(Capabilities.DRAGON_CORPSE_CAPABILITY).ifPresent(data -> {
            data.readNBT(nbt);
        });
    }
}
