package org.drakosha.corpsedragonsurvival.mixin;

import by.dragonsurvivalteam.dragonsurvival.client.DragonSurvivalClient;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import org.drakosha.corpsedragonsurvival.CorpseDragonSurvival;
import org.drakosha.corpsedragonsurvival.DragonCorpseState;
import org.drakosha.corpsedragonsurvival.DragonCorpseUtils;
import org.drakosha.corpsedragonsurvival.capabilities.Capabilities;
import org.drakosha.corpsedragonsurvival.capabilities.DragonCorpseData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

@Mixin(targets = "de.maxhenkel.corpse.entities.CorpseRenderer")
public class CorpseRenderer {
    @Unique
    private static final Minecraft MC = Minecraft.getInstance();

    @Inject(method = "render(Lde/maxhenkel/corpse/entities/CorpseEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void onRender(CorpseEntity corpse, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLightIn, CallbackInfo ci) {
        if (corpse.hasData(Capabilities.DRAGON_CORPSE_DATA)) {
            ci.cancel();

            matrixStack.pushPose();

            try {
                DragonCorpseData corpseData = corpse.getData(Capabilities.DRAGON_CORPSE_DATA);

                DragonCorpseState corpseState = CorpseDragonSurvival.CORPSE_STATES.get(corpse, () -> {
                    DragonCorpseState newCorpseState = new DragonCorpseState();
                    int fakeId = FakeClientPlayerUtils.getNextIndex();

                    newCorpseState.fakePlayer = FakeClientPlayerUtils.getFakePlayer(fakeId, corpseData.handler);
                    newCorpseState.fakeDragon = new DragonEntity(DSEntities.DRAGON.get(), newCorpseState.fakePlayer.level()) {
                        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
                            AnimationController<DragonEntity> controller = new AnimationController(this, "fake_player_controller", 2, (state) -> {
                                if (!corpseData.animation.isEmpty()) {
                                    return state.setAndContinue(RawAnimation.begin().thenPlayAndHold(corpseData.animation));
                                } else {
                                    return PlayState.STOP;
                                }
                            });
                            controller.setAnimationSpeed(0);
                            controller.transitionLength(0);
                            newCorpseState.fakePlayer.animationController = controller;
                            controllers.add(controller);
                        }

                        public Player getPlayer() {
                            return newCorpseState.fakePlayer;
                        }
                    };

                    //for(EquipmentSlot type : EquipmentSlot.values()) {
                    //    newCorpseState.fakePlayer.setItemSlot(type, corpse.getEquipment().get(type.ordinal()));
                    //}
                    newCorpseState.fakePlayer.setItemSlot(EquipmentSlot.MAINHAND, corpseData.mainHand);
                    newCorpseState.fakePlayer.setItemSlot(EquipmentSlot.OFFHAND, corpseData.offHand);
                    newCorpseState.fakePlayer.setItemSlot(EquipmentSlot.FEET, corpseData.feet);
                    newCorpseState.fakePlayer.setItemSlot(EquipmentSlot.LEGS, corpseData.legs);
                    newCorpseState.fakePlayer.setItemSlot(EquipmentSlot.CHEST, corpseData.chest);
                    newCorpseState.fakePlayer.setItemSlot(EquipmentSlot.HEAD, corpseData.head);

                    newCorpseState.fakePlayer.useVisualScale = true;
                    newCorpseState.fakePlayer.scale = corpseData.scale;

                    corpseData.handler.recompileCurrentSkin();

                    newCorpseState.texture = DragonCorpseUtils.getPlayerSkin(corpse.getCorpseName(), corpse.getCorpseUUID().orElse(null), corpseData.handler);

                    return newCorpseState;
                });

                //// This is the best way to sync items I can think of
                //// Items in death are valid only after we open container for the first time after entering world
                //for(EquipmentSlot type : EquipmentSlot.values()) {
                //    corpseState.fakePlayer.setItemSlot(type, corpse.getDeath().getEquipment().get(type.ordinal()));
                //}

                matrixStack.mulPose(Axis.YN.rotationDegrees(entityYaw));

                if (corpseData.handler.body().value().isDefault()) {
                    DragonSurvivalClient.DRAGON_MODEL.setOverrideTexture(corpseState.texture);
                }

                DragonSurvivalClient.DRAGON_RENDERER.render(corpseState.fakeDragon, entityYaw, 0, matrixStack, buffer, packedLightIn);

                DragonSurvivalClient.DRAGON_MODEL.setOverrideTexture(null);
            } catch (Throwable throwable) {
                CorpseDragonSurvival.LOGGER.error("A problem occurred while rendering a dragon corpse", throwable);
            } finally {
                matrixStack.popPose();
            }
        }
    }
}
