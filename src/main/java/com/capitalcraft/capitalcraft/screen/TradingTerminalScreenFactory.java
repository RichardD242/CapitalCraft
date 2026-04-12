package com.capitalcraft.capitalcraft.screen;

import com.capitalcraft.capitalcraft.market.TradingLedger;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class TradingTerminalScreenFactory implements ExtendedScreenHandlerFactory<BlockPos> {

    private final BlockPos terminalPos;

    public TradingTerminalScreenFactory(BlockPos terminalPos) {
        this.terminalPos = terminalPos.toImmutable();
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inventory, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            TradingLedger.sendSnapshot(serverPlayer, terminalPos);
        }

        return new TradingTerminalScreenHandler(syncId, inventory, terminalPos);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.capitalcraft.trading_terminal");
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        return terminalPos;
    }
}