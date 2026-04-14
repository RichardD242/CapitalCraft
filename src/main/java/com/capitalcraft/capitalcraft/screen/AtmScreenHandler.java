package com.capitalcraft.capitalcraft.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;

public class AtmScreenHandler extends ScreenHandler {

    private final BlockPos atmPos;

    public AtmScreenHandler(int syncId, PlayerInventory inventory, BlockPos atmPos) {
        super(CapitalcraftScreens.ATM_SCREEN_HANDLER, syncId);
        this.atmPos = atmPos.toImmutable();
    }

    public BlockPos getAtmPos() {
        return atmPos;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return player.squaredDistanceTo(atmPos.getX() + 0.5D, atmPos.getY() + 0.5D, atmPos.getZ() + 0.5D) <= 64.0D;
    }
}
