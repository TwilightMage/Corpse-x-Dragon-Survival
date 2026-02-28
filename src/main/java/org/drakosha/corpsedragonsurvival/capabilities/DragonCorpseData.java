package org.drakosha.corpsedragonsurvival.capabilities;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.NBTInterface;
import net.minecraft.nbt.CompoundTag;

public class DragonCorpseData implements NBTInterface {
    public DragonStateHandler handler = new DragonStateHandler();
    public String animation = "";

    @Override
    public CompoundTag writeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("handler", handler.writeNBT());
        tag.putString("animation", animation);

        return tag;
    }

    @Override
    public void readNBT(CompoundTag tag) {
        if (tag.contains("handler")) {
            handler.readNBT(tag.getCompound("handler"));
        }

        animation = tag.getString("animation");
    }
}
