package org.drakosha.corpsedragonsurvival;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.skins.DragonSkins;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.drakosha.corpsedragonsurvival.capabilities.DragonCorpseData;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public final class DragonCorpseUtils {
    public static boolean isDragon(DragonStateHandler handler)
    {
        return handler != null && handler.isDragon();
    }

    public static boolean isDragonCorpse(DragonCorpseData data)
    {
        return data != null && data.handler.isDragon();
    }

    public static ResourceLocation getPlayerSkin(String playerName, UUID playerUUID, DragonStateHandler handler) {
        if(ClientDragonRenderer.renderOtherPlayerSkins || Objects.equals(playerUUID, getLocalPlayerUUID())){
            return DragonSkins.getPlayerSkin(playerName, handler.stageKey());
        }

        return null;
    }

    private static UUID getLocalPlayerUUID() {
        var player = Minecraft.getInstance().player;
        if (player == null) return null;
        return player.getGameProfile().getId();
    }

    public static String pickDeadAnimation(DragonStateHandler handler)
    {
        String searchType = handler.bodyId().toString();

        var optionList = Config.corpseEmoteMap.getOrDefault(searchType, null);
        if (optionList == null || optionList.isEmpty())
            return "";

        Random random = new Random(System.currentTimeMillis());
        return optionList.get(random.nextInt(optionList.size()));
    }
}
