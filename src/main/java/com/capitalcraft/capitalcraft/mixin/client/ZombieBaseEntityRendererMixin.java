package com.capitalcraft.capitalcraft.mixin.client;

import com.capitalcraft.capitalcraft.client.CeoZombieRenderStateAccessor;
import net.minecraft.client.render.entity.ZombieBaseEntityRenderer;
import net.minecraft.client.render.entity.state.ZombieEntityRenderState;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieBaseEntityRenderer.class)
public class ZombieBaseEntityRendererMixin {

    private static final Identifier APPLE_TEXTURE = Identifier.of("capitalcraft", "textures/entity/ceo/apple_skin.png");
    private static final Identifier ALPAHBET_TEXTURE = Identifier.of("capitalcraft", "textures/entity/ceo/alpahbet_skin.png");
    private static final Identifier MICROSOFT_TEXTURE = Identifier.of("capitalcraft", "textures/entity/ceo/microsoft_skin.png");
    private static final Identifier MARK_TEXTURE = Identifier.of("capitalcraft", "textures/entity/ceo/mark_skin.png");
    private static final Identifier JENSEN_TEXTURE = Identifier.of("capitalcraft", "textures/entity/ceo/jensen_skin.png");

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/mob/ZombieEntity;Lnet/minecraft/client/render/entity/state/ZombieEntityRenderState;F)V", at = @At("TAIL"))
    private void capitalcraft$captureBrand(ZombieEntity zombieEntity, ZombieEntityRenderState zombieRenderState, float tickDelta, CallbackInfo ci) {
        CeoZombieRenderStateAccessor accessor = (CeoZombieRenderStateAccessor) zombieRenderState;
        accessor.capitalcraft$setBrand(extractBrand(zombieEntity));
    }

    @Inject(method = "getTexture(Lnet/minecraft/client/render/entity/state/ZombieEntityRenderState;)Lnet/minecraft/util/Identifier;", at = @At("HEAD"), cancellable = true)
    private void capitalcraft$overrideCeoTexture(ZombieEntityRenderState state, CallbackInfoReturnable<Identifier> cir) {
        String brand = ((CeoZombieRenderStateAccessor) state).capitalcraft$getBrand();
        switch (brand) {
            case "apple" -> cir.setReturnValue(APPLE_TEXTURE);
            case "alpahbet" -> cir.setReturnValue(ALPAHBET_TEXTURE);
            case "microsoft" -> cir.setReturnValue(MICROSOFT_TEXTURE);
            case "mark" -> cir.setReturnValue(MARK_TEXTURE);
            case "jensen" -> cir.setReturnValue(JENSEN_TEXTURE);
            default -> {
            }
        }
    }

    private static String extractBrand(ZombieEntity zombieEntity) {
        for (String tag : zombieEntity.getCommandTags()) {
            if (tag.startsWith("capitalcraft_ceo_brand_")) {
                return tag.substring("capitalcraft_ceo_brand_".length());
            }
        }
        return "";
    }
}
