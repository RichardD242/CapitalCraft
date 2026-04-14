package com.capitalcraft.capitalcraft.block;
import com.capitalcraft.capitalcraft.market.TradingLedger;
import com.capitalcraft.capitalcraft.screen.TradingTerminalScreenFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TradingTerminalBlock extends Block {

    public TradingTerminalBlock(Settings settings) {
        super(settings);
    }
    
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        if (player instanceof ServerPlayerEntity serverPlayer && state.isOf(this)) {
            serverPlayer.openHandledScreen(new TradingTerminalScreenFactory(pos));
            TradingLedger.sendSnapshot(serverPlayer, pos);
            return ActionResult.CONSUME;
        }

        return ActionResult.PASS;
    }
}