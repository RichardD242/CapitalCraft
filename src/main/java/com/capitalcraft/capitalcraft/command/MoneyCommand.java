package com.capitalcraft.capitalcraft.command;

import com.capitalcraft.capitalcraft.market.TradingLedger;
import com.capitalcraft.capitalcraft.market.TradingPortfolio;
import com.capitalcraft.capitalcraft.util.PlayerTradingData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class MoneyCommand {

    private MoneyCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("money")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                        .executes(context -> setMoney(context.getSource(), IntegerArgumentType.getInteger(context, "amount")))));
    }

    private static int setMoney(ServerCommandSource source, int amount) {
        ServerPlayerEntity player;
        try {
            player = source.getPlayerOrThrow();
        } catch (CommandSyntaxException exception) {
            source.sendError(Text.literal("This command can only be used by a player."));
            return 0;
        }

        TradingPortfolio updated = TradingLedger.getPortfolio(player).withCash(amount);
        PlayerTradingData.setPortfolio(player, updated);
        TradingLedger.syncWallet(player);
        source.sendFeedback(() -> Text.literal("Set cash to " + amount + " EUR"), true);
        return 1;
    }
}