package com.capitalcraft.capitalcraft.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class AtmScreenFactory implements ExtendedScreenHandlerFactory<BlockPos> {

    private final BlockPos atmPos;

    public AtmScreenFactory(BlockPos atmPos) {
        this.atmPos = atmPos.toImmutable();
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
        return new AtmScreenHandler(syncId, inventory, atmPos);
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("ATM");
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        return atmPos;
    }
}
