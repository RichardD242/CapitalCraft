package com.capitalcraft.capitalcraft.command;

import com.capitalcraft.capitalcraft.block.safeblockentity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public final class PinCommand {

    private static final int PIN_LENGTH = 4;

    private PinCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("pin")
                .then(CommandManager.literal("create")
                        .then(CommandManager.argument("pin", StringArgumentType.word())
                    .executes(context -> createPin(context.getSource(), StringArgumentType.getString(context, "pin")))))
            .then(CommandManager.literal("delete")
                .executes(context -> deletePin(context.getSource())))
            .then(CommandManager.argument("pin", StringArgumentType.word())
                .executes(context -> unlockPin(context.getSource(), StringArgumentType.getString(context, "pin")))));
    }

    private static int createPin(ServerCommandSource source, String pin) throws CommandSyntaxException {
        if (!pin.matches("\\d{" + PIN_LENGTH + "}")) {
            source.sendError(Text.literal("PIN must be exactly 4 digits."));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayerOrThrow();
        HitResult hitResult = player.raycast(5.0D, 1.0F, false);
        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            source.sendError(Text.literal("Look at a safe to set its PIN."));
            return 0;
        }

        BlockPos pos = blockHitResult.getBlockPos();
        BlockEntity blockEntity = source.getWorld().getBlockEntity(pos);
        if (!(blockEntity instanceof safeblockentity safe)) {
            source.sendError(Text.literal("The targeted block is not a safe."));
            return 0;
        }

        safe.setPin(pin);
        safe.setLocked(true);
        if (source.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            serverWorld.getChunkManager().markForUpdate(pos);
        }
        source.sendFeedback(() -> Text.literal("Created PIN " + pin + " for the safe."), true);
        return 1;
    }

    private static int unlockPin(ServerCommandSource source, String pin) throws CommandSyntaxException {
        if (!pin.matches("\\d{" + PIN_LENGTH + "}")) {
            source.sendError(Text.literal("PIN must be exactly 4 digits."));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayerOrThrow();
        HitResult hitResult = player.raycast(5.0D, 1.0F, false);
        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            source.sendError(Text.literal("Look at a safe to unlock it."));
            return 0;
        }

        BlockPos pos = blockHitResult.getBlockPos();
        BlockEntity blockEntity = source.getWorld().getBlockEntity(pos);
        if (!(blockEntity instanceof safeblockentity safe)) {
            source.sendError(Text.literal("The targeted block is not a safe."));
            return 0;
        }

        if (!safe.hasPin()) {
            source.sendError(Text.literal("That safe has no PIN set."));
            return 0;
        }

        if (!safe.checkPin(pin)) {
            source.sendError(Text.literal("Wrong PIN."));
            return 0;
        }

        safe.setLocked(false);
        if (source.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            serverWorld.getChunkManager().markForUpdate(pos);
        }
        source.sendFeedback(() -> Text.literal("Safe unlocked."), true);
        return 1;
    }

    private static int deletePin(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        HitResult hitResult = player.raycast(5.0D, 1.0F, false);
        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            source.sendError(Text.literal("Look at a safe to delete its PIN."));
            return 0;
        }

        BlockPos pos = blockHitResult.getBlockPos();
        BlockEntity blockEntity = source.getWorld().getBlockEntity(pos);
        if (!(blockEntity instanceof safeblockentity safe)) {
            source.sendError(Text.literal("The targeted block is not a safe."));
            return 0;
        }

        if (!safe.hasPin()) {
            source.sendError(Text.literal("That safe doesn't have a PIN set."));
            return 0;
        }

        safe.setPin("");
        safe.setLocked(false);
        if (source.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            serverWorld.getChunkManager().markForUpdate(pos);
        }
        source.sendFeedback(() -> Text.literal("Safe PIN deleted."), true);
        return 1;
    }
}