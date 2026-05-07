package com.capitalcraft.capitalcraft.world;

import com.capitalcraft.capitalcraft.Capitalcraft;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

public final class CapitalcraftBiomes {

    public static final RegistryKey<Biome> SILICON_VALLEY_KEY = RegistryKey.of(
            RegistryKeys.BIOME,
            Identifier.of(Capitalcraft.MOD_ID, "silicon_valley")
    );

    private CapitalcraftBiomes() {
    }

}
