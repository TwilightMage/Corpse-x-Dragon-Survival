package org.drakosha.corpsedragonsurvival.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DragonCorpseCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
    private final DragonCorpseData data = new DragonCorpseData();
    private final LazyOptional<DragonCorpseData> instance = LazyOptional.of(() -> this.data);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == Capabilities.DRAGON_CORPSE_CAPABILITY ? this.instance.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return data.writeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        data.readNBT(nbt);
    }
}
