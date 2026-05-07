package com.capitalcraft.capitalcraft.mixin;

import com.capitalcraft.capitalcraft.world.CapitalcraftBiomes;
import com.mojang.datafixers.util.Pair;
import java.util.function.Consumer;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.biome.source.util.VanillaBiomeParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VanillaBiomeParameters.class)
public class VanillaBiomeParametersMixin {

    @Inject(method = "writeOverworldBiomeParameters", at = @At("TAIL"))
    private static void capitalcraft$addSiliconValley(Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameters, CallbackInfo ci) {
        parameters.accept(Pair.of(
                MultiNoiseUtil.createNoiseHypercube(
                        MultiNoiseUtil.ParameterRange.of(-0.45F, 0.45F),
                        MultiNoiseUtil.ParameterRange.of(-0.45F, 0.45F),
                        MultiNoiseUtil.ParameterRange.of(-0.35F, 0.35F),
                        MultiNoiseUtil.ParameterRange.of(-0.35F, 0.35F),
                        MultiNoiseUtil.ParameterRange.of(-0.25F, 0.25F),
                        MultiNoiseUtil.ParameterRange.of(-0.15F, 0.15F),
                        0.0F
                ),
                CapitalcraftBiomes.SILICON_VALLEY_KEY
        ));
    }
}
