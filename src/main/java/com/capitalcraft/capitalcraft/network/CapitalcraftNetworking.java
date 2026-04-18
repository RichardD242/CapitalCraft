package com.capitalcraft.capitalcraft.network;

import com.capitalcraft.capitalcraft.market.TradingPortfolio;
import com.capitalcraft.capitalcraft.market.TradingLedger;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public final class CapitalcraftNetworking {

    private CapitalcraftNetworking() {
    }

    public static void init() {
        PayloadTypeRegistry.playS2C().register(TradingTerminalPayloads.TRADING_TERMINAL_SYNC_ID, TradingTerminalPayloads.TRADING_TERMINAL_SYNC_CODEC);
        PayloadTypeRegistry.playS2C().register(TradingTerminalPayloads.WALLET_SYNC_ID, TradingTerminalPayloads.WALLET_SYNC_CODEC);
        PayloadTypeRegistry.playC2S().register(TradingTerminalPayloads.TRADING_TERMINAL_TRADE_REQUEST_ID, TradingTerminalPayloads.TRADING_TERMINAL_TRADE_REQUEST_CODEC);
        PayloadTypeRegistry.playC2S().register(TradingTerminalPayloads.STOCK_REFRESH_REQUEST_ID, TradingTerminalPayloads.STOCK_REFRESH_REQUEST_CODEC);
        PayloadTypeRegistry.playC2S().register(TradingTerminalPayloads.ATM_WITHDRAW_REQUEST_ID, TradingTerminalPayloads.ATM_WITHDRAW_REQUEST_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TradingTerminalPayloads.TRADING_TERMINAL_TRADE_REQUEST_ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> TradingLedger.handleTrade(player, payload.terminalPos(), payload.assetIndex(), payload.quantity(), payload.buying()));
        });

        ServerPlayNetworking.registerGlobalReceiver(TradingTerminalPayloads.STOCK_REFRESH_REQUEST_ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> TradingLedger.handleRefresh(player, payload.terminalPos(), payload.assetIndex()));
        });

        ServerPlayNetworking.registerGlobalReceiver(TradingTerminalPayloads.ATM_WITHDRAW_REQUEST_ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> TradingLedger.handleAtmWithdraw(player, payload.atmPos(), payload.amount()));
        });
    }

    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(TradingTerminalPayloads.TRADING_TERMINAL_SYNC_ID, (payload, context) -> {
            MinecraftClient client = context.client();
            client.execute(() -> {
                if (client.currentScreen instanceof com.capitalcraft.capitalcraft.screen.TradingTerminalScreen tradingTerminalScreen) {
                    tradingTerminalScreen.applySnapshot(TradingPortfolio.fromNbt(payload.portfolioData()));
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(TradingTerminalPayloads.WALLET_SYNC_ID, (payload, context) -> {
            context.client().execute(() -> {
                com.capitalcraft.capitalcraft.client.ClientWalletState.setCash(payload.cash());
                if (context.client().currentScreen instanceof com.capitalcraft.capitalcraft.screen.TradingTerminalScreen tradingTerminalScreen) {
                    tradingTerminalScreen.applyWalletUpdate(payload.cash());
                }
            });
        });
    }

    public static void requestTrade(BlockPos terminalPos, int assetIndex, int quantity, boolean buying) {
        ClientPlayNetworking.send(new TradingTerminalPayloads.TradingTerminalTradeRequestPayload(terminalPos, assetIndex, quantity, buying));
    }

    public static void requestRefresh(BlockPos terminalPos, int assetIndex) {
        ClientPlayNetworking.send(new TradingTerminalPayloads.StockRefreshRequestPayload(terminalPos, assetIndex));
    }

    public static void requestAtmWithdraw(BlockPos atmPos, int amount) {
        ClientPlayNetworking.send(new TradingTerminalPayloads.AtmWithdrawRequestPayload(atmPos, amount));
    }

    public static void sendWalletSync(ServerPlayerEntity player, int cash) {
        ServerPlayNetworking.send(player, new TradingTerminalPayloads.WalletSyncPayload(cash));
    }
}