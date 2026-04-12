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
}