package com.capitalcraft.capitalcraft.market;

import com.capitalcraft.capitalcraft.item.CapitalcraftItems;
import com.capitalcraft.capitalcraft.network.TradingTerminalPayloads;
import com.capitalcraft.capitalcraft.network.CapitalcraftNetworking;
import com.capitalcraft.capitalcraft.util.PlayerTradingData;
import net.minecraft.item.ItemStack;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public final class TradingLedger {

    private TradingLedger() {
    }

    public static TradingPortfolio getPortfolio(ServerPlayerEntity player) {
        return PlayerTradingData.getPortfolio(player);
    }

    public static void sendSnapshot(ServerPlayerEntity player, BlockPos terminalPos) {
        TradingPortfolio portfolio = getPortfolio(player);
        ServerPlayNetworking.send(player, new TradingTerminalPayloads.TradingTerminalSyncPayload(terminalPos, portfolio.toNbt()));
        CapitalcraftNetworking.sendWalletSync(player, portfolio.cash());
    }

    public static void handleTrade(ServerPlayerEntity player, BlockPos terminalPos, int assetIndex, int quantity, boolean buying) {
        if (player.squaredDistanceTo(terminalPos.getX() + 0.5, terminalPos.getY() + 0.5, terminalPos.getZ() + 0.5) > 64.0D) {
            return;
        }

        TradingPortfolio currentPortfolio = getPortfolio(player);
        TradingPortfolio updatedPortfolio = buying ? currentPortfolio.buy(assetIndex, quantity) : currentPortfolio.sell(assetIndex, quantity);
        PlayerTradingData.setPortfolio(player, updatedPortfolio);
        sendSnapshot(player, terminalPos);
    }

    public static void handleRefresh(ServerPlayerEntity player, BlockPos terminalPos, int assetIndex) {
        if (player.squaredDistanceTo(terminalPos.getX() + 0.5, terminalPos.getY() + 0.5, terminalPos.getZ() + 0.5) > 64.0D) {
            return;
        }

        MarketSimulator.refreshPrice(assetIndex);
        sendSnapshot(player, terminalPos);
    }

    public static void handleAtmWithdraw(ServerPlayerEntity player, BlockPos atmPos, int amount) {
        if (amount != 100 && amount != 200 && amount != 500 && amount != 1000) {
            return;
        }

        if (player.squaredDistanceTo(atmPos.getX() + 0.5, atmPos.getY() + 0.5, atmPos.getZ() + 0.5) > 64.0D) {
            return;
        }

        TradingPortfolio current = getPortfolio(player);
        if (current.cash() < amount) {
            CapitalcraftNetworking.sendWalletSync(player, current.cash());
            return;
        }

        TradingPortfolio updated = new TradingPortfolio(
            current.cash() - amount,
                current.realizedPnl(),
                toArray(current, true),
                toArray(current, false)
        );
        PlayerTradingData.setPortfolio(player, updated);
        CapitalcraftNetworking.sendWalletSync(player, updated.cash());

        ItemStack payout = CapitalcraftItems.createMoneyVoucher(amount);
        if (!player.getInventory().insertStack(payout)) {
            player.dropItem(payout, false);
        }
    }
    
    public static void handlePlayerDeath(ServerPlayerEntity player) {
        TradingPortfolio portfolio = getPortfolio(player);
        TradingPortfolio penalizedPortfolio = portfolio.applyDeathPenalty();
        PlayerTradingData.setPortfolio(player, penalizedPortfolio);
        CapitalcraftNetworking.sendWalletSync(player, penalizedPortfolio.cash());
    }

    public static void syncWallet(ServerPlayerEntity player) {
        TradingPortfolio portfolio = getPortfolio(player);
        CapitalcraftNetworking.sendWalletSync(player, portfolio.cash());
    }

    private static int[] toArray(TradingPortfolio portfolio, boolean quantities) {
        int[] values = new int[TradingMarket.assetCount()];
        for (int i = 0; i < TradingMarket.assetCount(); i++) {
            values[i] = quantities ? portfolio.quantity(i) : portfolio.averageCost(i);
        }
        return values;
    }
}