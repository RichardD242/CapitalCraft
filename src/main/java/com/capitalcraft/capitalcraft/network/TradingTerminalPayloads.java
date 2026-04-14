package com.capitalcraft.capitalcraft.network;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public final class TradingTerminalPayloads {

    public static final CustomPayload.Id<TradingTerminalSyncPayload> TRADING_TERMINAL_SYNC_ID = CustomPayload.id("trading_terminal_sync");
    public static final PacketCodec<io.netty.buffer.ByteBuf, TradingTerminalSyncPayload> TRADING_TERMINAL_SYNC_CODEC = CustomPayload.codecOf(TradingTerminalPayloads::encodeSyncPayload, TradingTerminalPayloads::decodeSyncPayload);

    public static final CustomPayload.Id<TradingTerminalTradeRequestPayload> TRADING_TERMINAL_TRADE_REQUEST_ID = CustomPayload.id("trading_terminal_trade_request");
    public static final PacketCodec<io.netty.buffer.ByteBuf, TradingTerminalTradeRequestPayload> TRADING_TERMINAL_TRADE_REQUEST_CODEC = CustomPayload.codecOf(TradingTerminalPayloads::encodeTradeRequestPayload, TradingTerminalPayloads::decodeTradeRequestPayload);
    public static final CustomPayload.Id<StockRefreshRequestPayload> STOCK_REFRESH_REQUEST_ID = CustomPayload.id("stock_refresh_request");
    public static final PacketCodec<io.netty.buffer.ByteBuf, StockRefreshRequestPayload> STOCK_REFRESH_REQUEST_CODEC = CustomPayload.codecOf(TradingTerminalPayloads::encodeStockRefreshRequestPayload, TradingTerminalPayloads::decodeStockRefreshRequestPayload);
    public static final CustomPayload.Id<AtmWithdrawRequestPayload> ATM_WITHDRAW_REQUEST_ID = CustomPayload.id("atm_withdraw_request");
    public static final PacketCodec<io.netty.buffer.ByteBuf, AtmWithdrawRequestPayload> ATM_WITHDRAW_REQUEST_CODEC = CustomPayload.codecOf(TradingTerminalPayloads::encodeAtmWithdrawRequestPayload, TradingTerminalPayloads::decodeAtmWithdrawRequestPayload);
    public static final CustomPayload.Id<WalletSyncPayload> WALLET_SYNC_ID = CustomPayload.id("wallet_sync");
    public static final PacketCodec<io.netty.buffer.ByteBuf, WalletSyncPayload> WALLET_SYNC_CODEC = CustomPayload.codecOf(TradingTerminalPayloads::encodeWalletSyncPayload, TradingTerminalPayloads::decodeWalletSyncPayload);

    private TradingTerminalPayloads() {
    }

    private static void encodeSyncPayload(TradingTerminalSyncPayload payload, io.netty.buffer.ByteBuf buffer) {
        BlockPos.PACKET_CODEC.encode(buffer, payload.terminalPos());
        NbtCompound nbt = payload.portfolioData();
        PacketCodecs.NBT_COMPOUND.encode(buffer, nbt);
    }

    private static TradingTerminalSyncPayload decodeSyncPayload(io.netty.buffer.ByteBuf buffer) {
        BlockPos terminalPos = BlockPos.PACKET_CODEC.decode(buffer);
        NbtCompound portfolioData = PacketCodecs.NBT_COMPOUND.decode(buffer);
        return new TradingTerminalSyncPayload(terminalPos, portfolioData);
    }

    private static void encodeTradeRequestPayload(TradingTerminalTradeRequestPayload payload, io.netty.buffer.ByteBuf buffer) {
        BlockPos.PACKET_CODEC.encode(buffer, payload.terminalPos());
        PacketCodecs.VAR_INT.encode(buffer, payload.assetIndex());
        PacketCodecs.VAR_INT.encode(buffer, payload.quantity());
        PacketCodecs.BOOLEAN.encode(buffer, payload.buying());
    }

    private static TradingTerminalTradeRequestPayload decodeTradeRequestPayload(io.netty.buffer.ByteBuf buffer) {
        BlockPos terminalPos = BlockPos.PACKET_CODEC.decode(buffer);
        int assetIndex = PacketCodecs.VAR_INT.decode(buffer);
        int quantity = PacketCodecs.VAR_INT.decode(buffer);
        boolean buying = PacketCodecs.BOOLEAN.decode(buffer);
        return new TradingTerminalTradeRequestPayload(terminalPos, assetIndex, quantity, buying);
    }

    private static void encodeStockRefreshRequestPayload(StockRefreshRequestPayload payload, io.netty.buffer.ByteBuf buffer) {
        BlockPos.PACKET_CODEC.encode(buffer, payload.terminalPos());
        PacketCodecs.VAR_INT.encode(buffer, payload.assetIndex());
    }

    private static StockRefreshRequestPayload decodeStockRefreshRequestPayload(io.netty.buffer.ByteBuf buffer) {
        BlockPos terminalPos = BlockPos.PACKET_CODEC.decode(buffer);
        int assetIndex = PacketCodecs.VAR_INT.decode(buffer);
        return new StockRefreshRequestPayload(terminalPos, assetIndex);
    }

    private static void encodeAtmWithdrawRequestPayload(AtmWithdrawRequestPayload payload, io.netty.buffer.ByteBuf buffer) {
        BlockPos.PACKET_CODEC.encode(buffer, payload.atmPos());
        PacketCodecs.VAR_INT.encode(buffer, payload.amount());
    }

    private static AtmWithdrawRequestPayload decodeAtmWithdrawRequestPayload(io.netty.buffer.ByteBuf buffer) {
        BlockPos atmPos = BlockPos.PACKET_CODEC.decode(buffer);
        int amount = PacketCodecs.VAR_INT.decode(buffer);
        return new AtmWithdrawRequestPayload(atmPos, amount);
    }

    private static void encodeWalletSyncPayload(WalletSyncPayload payload, io.netty.buffer.ByteBuf buffer) {
        PacketCodecs.VAR_INT.encode(buffer, payload.cash());
    }

    private static WalletSyncPayload decodeWalletSyncPayload(io.netty.buffer.ByteBuf buffer) {
        int cash = PacketCodecs.VAR_INT.decode(buffer);
        return new WalletSyncPayload(cash);
    }

    public record TradingTerminalSyncPayload(BlockPos terminalPos, NbtCompound portfolioData) implements CustomPayload {

        @Override
        public Id<? extends CustomPayload> getId() {
            return TRADING_TERMINAL_SYNC_ID;
        }
    }

    public record TradingTerminalTradeRequestPayload(BlockPos terminalPos, int assetIndex, int quantity, boolean buying) implements CustomPayload {

        @Override
        public Id<? extends CustomPayload> getId() {
            return TRADING_TERMINAL_TRADE_REQUEST_ID;
        }
    }

    public record StockRefreshRequestPayload(BlockPos terminalPos, int assetIndex) implements CustomPayload {

        @Override
        public Id<? extends CustomPayload> getId() {
            return STOCK_REFRESH_REQUEST_ID;
        }
    }

    public record AtmWithdrawRequestPayload(BlockPos atmPos, int amount) implements CustomPayload {

        @Override
        public Id<? extends CustomPayload> getId() {
            return ATM_WITHDRAW_REQUEST_ID;
        }
    }

    public record WalletSyncPayload(int cash) implements CustomPayload {

        @Override
        public Id<? extends CustomPayload> getId() {
            return WALLET_SYNC_ID;
        }
    }
}