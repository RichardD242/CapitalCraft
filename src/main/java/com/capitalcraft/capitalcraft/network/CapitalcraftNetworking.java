package com.capitalcraft.capitalcraft.network;

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
        PayloadTypeRegistry.playC2S().register(TradingTerminalPayloads.TRADING_TERMINAL_TRADE_REQUEST_ID, TradingTerminalPayloads.TRADING_TERMINAL_TRADE_REQUEST_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TradingTerminalPayloads.TRADING_TERMINAL_TRADE_REQUEST_ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> TradingLedger.handleTrade(player, payload.terminalPos(), payload.assetIndex(), payload.quantity(), payload.buying()));
        });
    }

    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(TradingTerminalPayloads.TRADING_TERMINAL_SYNC_ID, (payload, context) -> {
            MinecraftClient client = context.client();
            client.execute(() -> {
                if (client.currentScreen instanceof com.capitalcraft.capitalcraft.screen.TradingTerminalScreen tradingTerminalScreen) {
                    tradingTerminalScreen.applySnapshot(payload.portfolioData());
                }
            });
        });
    }

    public static void requestTrade(BlockPos terminalPos, int assetIndex, int quantity, boolean buying) {
        ClientPlayNetworking.send(new TradingTerminalPayloads.TradingTerminalTradeRequestPayload(terminalPos, assetIndex, quantity, buying));
    }
}