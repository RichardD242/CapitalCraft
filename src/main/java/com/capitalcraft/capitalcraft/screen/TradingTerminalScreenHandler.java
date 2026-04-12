package com.capitalcraft.capitalcraft.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;

public class TradingTerminalScreenHandler extends ScreenHandler {

    private final BlockPos terminalPos;

    public TradingTerminalScreenHandler(int syncId, PlayerInventory inventory, BlockPos terminalPos) {
        super(CapitalcraftScreens.TRADING_TERMINAL_SCREEN_HANDLER, syncId);
        this.terminalPos = terminalPos.toImmutable();
    }

    public BlockPos getTerminalPos() {
        return terminalPos;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return player.squaredDistanceTo(terminalPos.getX() + 0.5D, terminalPos.getY() + 0.5D, terminalPos.getZ() + 0.5D) <= 64.0D;
    }
}