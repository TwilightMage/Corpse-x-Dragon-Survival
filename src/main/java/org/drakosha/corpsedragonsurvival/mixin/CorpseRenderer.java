package org.drakosha.corpsedragonsurvival.mixin;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonArmorRenderLayer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
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
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.core.object.PlayState;

@Mixin(targets = "de.maxhenkel.corpse.entities.CorpseRenderer")
public class CorpseRenderer {
    @Unique
    private static final Minecraft MC = Minecraft.getInstance();

    @Inject(method = "render(Lde/maxhenkel/corpse/entities/CorpseEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void onRender(CorpseEntity corpse, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLightIn, CallbackInfo ci) {
        LazyOptional<DragonCorpseData> cap = corpse.getCapability(Capabilities.DRAGON_CORPSE_CAPABILITY);

        DragonCorpseUtils.ifDragonCorpse(cap, (data) -> {
            ci.cancel();

            if (ClientDragonRender.dragonArmor == null) {
                ClientDragonRender.dragonArmor = (DragonEntity)((EntityType)DSEntities.DRAGON_ARMOR.get()).create(corpse.level());

                assert ClientDragonRender.dragonArmor != null;

                ClientDragonRender.dragonArmor.playerId = Minecraft.getInstance().player.getId();
            }

            matrixStack.pushPose();

            try {
                DragonCorpseState corpseState = CorpseDragonSurvival.CORPSE_STATES.get(corpse, () -> {
                    DragonCorpseState newCorpseState = new DragonCorpseState();
                    int fakeId = 4269 + corpse.getId();

                    newCorpseState.fakePlayer = FakeClientPlayerUtils.getFakePlayer(fakeId, data.handler);

                    newCorpseState.fakeDragon = new DragonEntity(DSEntities.DRAGON.get(), newCorpseState.fakePlayer.level()) {
                        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
                            AnimationController<DragonEntity> controller = new AnimationController(this, "fake_player_controller", 2, (state) -> {
                                if (!data.animation.isEmpty()) {
                                    return state.setAndContinue(RawAnimation.begin().thenPlayAndHold(data.animation));
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

                    for(EquipmentSlot type : EquipmentSlot.values()) {
                        newCorpseState.fakePlayer.setItemSlot(type, corpse.getEquipment().get(type.ordinal()));
                    }

                    data.handler.setMovementData(0, 0, 0, Vec3.ZERO);

                    newCorpseState.texture = DragonCorpseUtils.getPlayerSkin(corpse.getCorpseName(), corpse.getCorpseUUID().orElse(null), data.handler);

                    return newCorpseState;
                });

                //// This is the best way to sync items I can think of
                //// Items in death are valid only after we open container for the first time after entering world
                //for(EquipmentSlot type : EquipmentSlot.values()) {
                //    corpseState.fakePlayer.setItemSlot(type, corpse.getDeath().getEquipment().get(type.ordinal()));
                //}

                double size = data.handler.getSize();

                // FIXME :: This is some arbitrary scaling that was created back when the maximum size was hard capped at 40. Touching it will cause the render to desync from the hitbox.
                float scale = (float) Math.max(size / 40.0D, 0.4D);
                matrixStack.scale(scale, scale, scale);
                matrixStack.mulPose(Axis.YN.rotationDegrees(entityYaw));

                DragonRenderer dragonRenderer = (DragonRenderer) MC.getEntityRenderDispatcher().getRenderer(corpseState.fakeDragon);
                //dragonRenderer.setShadowRadius((float) ((3.0F * size + 62.0F) / 260.0F));
                ClientDragonRender.dragonModel.setCurrentTexture(corpseState.texture);

                dragonRenderer.render(corpseState.fakeDragon, entityYaw, 0, matrixStack, buffer, packedLightIn);

                if (!ClientDragonRender.armorRenderLayer) {
                    ItemStack helmet = corpseState.fakePlayer.getItemBySlot(EquipmentSlot.HEAD);
                    ItemStack chestPlate = corpseState.fakePlayer.getItemBySlot(EquipmentSlot.CHEST);
                    ItemStack legs = corpseState.fakePlayer.getItemBySlot(EquipmentSlot.LEGS);
                    ItemStack boots = corpseState.fakePlayer.getItemBySlot(EquipmentSlot.FEET);

                    ResourceLocation helmetTexture = ResourceLocation.fromNamespaceAndPath(DragonSurvivalMod.MODID, DragonArmorRenderLayer.constructArmorTexture(corpseState.fakePlayer, EquipmentSlot.HEAD));
                    ResourceLocation chestPlateTexture = ResourceLocation.fromNamespaceAndPath(DragonSurvivalMod.MODID, DragonArmorRenderLayer.constructArmorTexture(corpseState.fakePlayer, EquipmentSlot.CHEST));
                    ResourceLocation legsTexture = ResourceLocation.fromNamespaceAndPath(DragonSurvivalMod.MODID, DragonArmorRenderLayer.constructArmorTexture(corpseState.fakePlayer, EquipmentSlot.LEGS));
                    ResourceLocation bootsTexture = ResourceLocation.fromNamespaceAndPath(DragonSurvivalMod.MODID, DragonArmorRenderLayer.constructArmorTexture(corpseState.fakePlayer, EquipmentSlot.FEET));

                    renderArmorPiece(helmet, matrixStack, buffer, entityYaw, packedLightIn, corpseState.fakeDragon, 0, helmetTexture);
                    renderArmorPiece(chestPlate, matrixStack, buffer, entityYaw, packedLightIn, corpseState.fakeDragon, 0, chestPlateTexture);
                    renderArmorPiece(legs, matrixStack, buffer, entityYaw, packedLightIn, corpseState.fakeDragon, 0, legsTexture);
                    renderArmorPiece(boots, matrixStack, buffer, entityYaw, packedLightIn, corpseState.fakeDragon, 0, bootsTexture);
                }
            } catch (Throwable throwable) {
                CorpseDragonSurvival.LOGGER.error("A problem occurred while rendering a dragon corpse", throwable);
            } finally {
                matrixStack.popPose();
            }

            ClientDragonRender.dragonModel.setCurrentTexture(null);
        });
    }

    // Duplicate of private method
    private static void renderArmorPiece(ItemStack stack, PoseStack matrixStackIn, MultiBufferSource bufferIn, float yaw, int packedLightIn, DragonEntity entitylivingbaseIn, float partialTicks, ResourceLocation helmetTexture) {
        Color armorColor = Color.ofRGB(1.0F, 1.0F, 1.0F);
        if (stack != null && !stack.isEmpty()) {
            Item var10 = stack.getItem();
            if (var10 instanceof DyeableArmorItem) {
                DyeableArmorItem dyeableArmorItem = (DyeableArmorItem)var10;
                int colorCode = dyeableArmorItem.getColor(stack);
                armorColor = Color.ofOpaque(colorCode);
            }

            if (!stack.isEmpty()) {
                EntityRenderer<? super DragonEntity> dragonArmorRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(ClientDragonRender.dragonArmor);
                ClientDragonRender.dragonArmor.copyPosition(entitylivingbaseIn);
                ClientDragonRender.dragonArmorModel.setArmorTexture(helmetTexture);
                Color preColor = ((DragonRenderer)dragonArmorRenderer).renderColor;
                ((DragonRenderer)dragonArmorRenderer).shouldRenderLayers = false;
                ((DragonRenderer)dragonArmorRenderer).renderColor = armorColor;
                dragonArmorRenderer.render(entitylivingbaseIn, yaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
                ((DragonRenderer)dragonArmorRenderer).renderColor = preColor;
                ((DragonRenderer)dragonArmorRenderer).shouldRenderLayers = true;
            }
        }
    }
}
