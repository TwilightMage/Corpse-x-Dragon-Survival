package org.drakosha.corpsedragonsurvival.capabilities;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public class DragonCorpseData implements INBTSerializable<CompoundTag> {
    public DragonStateHandler handler = new DragonStateHandler();
    public String animation = "";
    public double scale = 1;

    // Should be used until Corpse fixes equipment field on corpse entity
    public ItemStack mainHand = ItemStack.EMPTY;
    public ItemStack offHand = ItemStack.EMPTY;
    public ItemStack feet = ItemStack.EMPTY;
    public ItemStack legs = ItemStack.EMPTY;
    public ItemStack chest = ItemStack.EMPTY;
    public ItemStack head = ItemStack.EMPTY;

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("handler", handler.serializeNBT(provider));
        tag.putString("animation", animation);
        tag.putDouble("scale", scale);

        if (!mainHand.isEmpty()) tag.put("mainHand", mainHand.save(provider));
        if (!offHand.isEmpty()) tag.put("offHand", offHand.save(provider));
        if (!feet.isEmpty()) tag.put("feet", feet.save(provider));
        if (!legs.isEmpty()) tag.put("legs", legs.save(provider));
        if (!chest.isEmpty()) tag.put("chest", chest.save(provider));
        if (!head.isEmpty()) tag.put("head", head.save(provider));

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag tag) {
        if (tag.contains("handler")) {
            handler.deserializeNBT(provider, tag.getCompound("handler"));
        }

        animation = tag.getString("animation");

        if (tag.contains("scale")) {
            scale = tag.getDouble("scale");
        }

        if (tag.contains("mainHand")) mainHand = ItemStack.parse(provider, tag.getCompound("mainHand")).orElse(ItemStack.EMPTY);
        if (tag.contains("offHand")) offHand = ItemStack.parse(provider, tag.getCompound("offHand")).orElse(ItemStack.EMPTY);
        if (tag.contains("feet")) feet = ItemStack.parse(provider, tag.getCompound("feet")).orElse(ItemStack.EMPTY);
        if (tag.contains("legs")) legs = ItemStack.parse(provider, tag.getCompound("legs")).orElse(ItemStack.EMPTY);
        if (tag.contains("chest")) chest = ItemStack.parse(provider, tag.getCompound("chest")).orElse(ItemStack.EMPTY);
        if (tag.contains("head")) head = ItemStack.parse(provider, tag.getCompound("head")).orElse(ItemStack.EMPTY);
    }
}
