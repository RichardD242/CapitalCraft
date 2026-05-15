package com.capitalcraft.capitalcraft.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;




public class safeblock extends Block implements BlockEntityProvider {

    public safeblock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new safeblockentity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        if (player instanceof ServerPlayerEntity serverPlayer) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof safeblockentity safe) {
                if (!safe.hasPin()) {
                    serverPlayer.sendMessage(net.minecraft.text.Text.literal("Chest safe doesn't have a pin."), false);
                    return ActionResult.CONSUME;
                }
                if (safe.isLocked()) {
                    serverPlayer.sendMessage(net.minecraft.text.Text.literal("Safe is locked. Use /pin create xxxx, then /pin xxxx while looking at it."), false);
                    return ActionResult.CONSUME;
                }
                serverPlayer.openHandledScreen(safe);
                return ActionResult.CONSUME;
            }
        }

        return ActionResult.PASS;
    }

}
