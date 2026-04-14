package com.capitalcraft.capitalcraft.world.feature;

import com.capitalcraft.capitalcraft.Capitalcraft;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

public final class CapitalcraftWorldFeatures {

    public static final Feature<DefaultFeatureConfig> SKYSCRAPER = Registry.register(
            Registries.FEATURE,
            Identifier.of(Capitalcraft.MOD_ID, "skyscraper"),
            new SkyscraperFeature(DefaultFeatureConfig.CODEC)
    );

    private CapitalcraftWorldFeatures() {
    }

    public static void init() {
    }
}
