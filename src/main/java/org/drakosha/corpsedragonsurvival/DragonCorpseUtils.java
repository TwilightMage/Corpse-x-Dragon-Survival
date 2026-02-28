package org.drakosha.corpsedragonsurvival;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.client.skins.DragonSkins;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import org.drakosha.corpsedragonsurvival.capabilities.DragonCorpseData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

public final class DragonCorpseUtils {
    public static void ifDragon(@NotNull LazyOptional<DragonStateHandler> optional, @NotNull Consumer<DragonStateHandler> handler)
    {
        if (optional.isPresent() && optional.resolve().isPresent() && optional.resolve().get().isDragon())
        {
            handler.accept(optional.resolve().get());
        }
    }

    public static void ifDragonCorpse(@NotNull LazyOptional<DragonCorpseData> optional, @NotNull Consumer<DragonCorpseData> handler)
    {
        if (optional.isPresent() && optional.resolve().isPresent() && optional.resolve().get().handler.isDragon())
        {
            handler.accept(optional.resolve().get());
        }
    }

    public static ResourceLocation getPlayerSkin(String playerName, UUID playerUUID, DragonStateHandler handler) {

        boolean renderStage = renderStage(handler);

        if((ClientDragonRender.renderOtherPlayerSkins || Objects.equals(playerUUID, getLocalPlayerUUID())) && renderStage){
            return DragonSkins.getPlayerSkin(playerName, handler.getLevel());
        }

        return null;
    }

    public static boolean renderStage(DragonStateHandler handler) {
        return switch(handler.getLevel()){
            case NEWBORN -> handler.getSkinData().renderNewborn;
            case YOUNG -> handler.getSkinData().renderYoung;
            case ADULT -> handler.getSkinData().renderAdult;
        };
    }

    private static UUID getLocalPlayerUUID() {
        var player = Minecraft.getInstance().player;
        if (player == null) return null;
        return player.getGameProfile().getId();
    }

    public static String pickDeadAnimation(DragonStateHandler handler)
    {
        String searchType = handler.getBody().getBodyName();

        var optionList = Config.corpseEmoteMap.getOrDefault(searchType, null);
        if (optionList == null || optionList.isEmpty())
            return "";

        Random random = new Random(System.currentTimeMillis());
        return optionList.get(random.nextInt(optionList.size()));
    }
}
