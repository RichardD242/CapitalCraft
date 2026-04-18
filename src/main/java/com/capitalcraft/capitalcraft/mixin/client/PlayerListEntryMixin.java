package com.capitalcraft.capitalcraft.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {

    private static final Identifier CEO_SKIN = Identifier.of("capitalcraft", "textures/2026_03_10_business-steve-23915725.png");

    @Shadow
    public abstract GameProfile getProfile();

    @Inject(method = "getSkinTextures", at = @At("HEAD"), cancellable = true)
    private void capitalcraft$applyCeoSkin(CallbackInfoReturnable<SkinTextures> cir) {
        GameProfile profile = getProfile();
        if (profile == null) {
            return;
        }

        String name = profile.getName();
        if (name == null || !name.startsWith("ceo_")) {
            return;
        }

        cir.setReturnValue(new SkinTextures(CEO_SKIN, null, null, null, SkinTextures.Model.WIDE, false));
    }
}
