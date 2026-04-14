package com.capitalcraft.capitalcraft.block;

import com.capitalcraft.capitalcraft.market.TradingLedger;
import com.capitalcraft.capitalcraft.screen.AtmScreenFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AtmBlock extends Block {

    public AtmBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        if (player instanceof ServerPlayerEntity serverPlayer && state.isOf(this)) {
            serverPlayer.openHandledScreen(new AtmScreenFactory(pos));
            TradingLedger.syncWallet(serverPlayer);
            return ActionResult.CONSUME;
        }

        return ActionResult.PASS;
    }
}
