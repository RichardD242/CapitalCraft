package com.capitalcraft.capitalcraft.market;

import com.capitalcraft.capitalcraft.network.TradingTerminalPayloads;
import com.capitalcraft.capitalcraft.screen.TradingTerminalScreenHandler;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public final class TradingLedger {

    private static final Map<UUID, TradingPortfolio> PORTFOLIOS = new ConcurrentHashMap<>();

    private TradingLedger() {
    }

    public static TradingPortfolio getPortfolio(ServerPlayerEntity player) {
        return PORTFOLIOS.computeIfAbsent(player.getUuid(), ignored -> TradingPortfolio.createDefault());
    }

    public static void sendSnapshot(ServerPlayerEntity player, BlockPos terminalPos) {
        TradingPortfolio portfolio = getPortfolio(player);
        ServerPlayNetworking.send(player, new TradingTerminalPayloads.TradingTerminalSyncPayload(terminalPos, portfolio.toNbt()));
    }

    public static void handleTrade(ServerPlayerEntity player, BlockPos terminalPos, int assetIndex, int quantity, boolean buying) {
        if (player.squaredDistanceTo(terminalPos.getX() + 0.5, terminalPos.getY() + 0.5, terminalPos.getZ() + 0.5) > 64.0D) {
            return;
        }

        TradingPortfolio currentPortfolio = getPortfolio(player);
        TradingPortfolio updatedPortfolio = buying ? currentPortfolio.buy(assetIndex, quantity) : currentPortfolio.sell(assetIndex, quantity);
        PORTFOLIOS.put(player.getUuid(), updatedPortfolio);
        sendSnapshot(player, terminalPos);
    }
}