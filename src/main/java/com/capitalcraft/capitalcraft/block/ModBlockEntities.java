package com.capitalcraft.capitalcraft.block;

import com.capitalcraft.capitalcraft.Capitalcraft;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlockEntities {
    public static final BlockEntityType<safeblockentity> SAFE_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(Capitalcraft.MOD_ID, "safe"),
            FabricBlockEntityTypeBuilder.create(safeblockentity::new, CapitalcraftBlocks.SAFE).build()
    );

    private ModBlockEntities() {
    }

    public static void init() {
    }
}
