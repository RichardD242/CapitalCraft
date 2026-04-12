package com.capitalcraft.capitalcraft.market;

import net.minecraft.nbt.NbtCompound;

public final class TradingPortfolio {

    private static final String CASH_KEY = "cash";
    private static final String REALIZED_PNL_KEY = "realizedPnl";
    private static final String QUANTITIES_KEY = "quantities";
    private static final String AVERAGE_COSTS_KEY = "averageCosts";
    private static final int STARTING_CASH = 10_000;

    private final int cash;
    private final int realizedPnl;
    private final int[] quantities;
    private final int[] averageCosts;

    public TradingPortfolio(int cash, int realizedPnl, int[] quantities, int[] averageCosts) {
        this.cash = cash;
        this.realizedPnl = realizedPnl;
        this.quantities = quantities.clone();
        this.averageCosts = averageCosts.clone();
    }

    public static TradingPortfolio createDefault() {
        return new TradingPortfolio(STARTING_CASH, 0, new int[TradingMarket.assetCount()], new int[TradingMarket.assetCount()]);
    }

    public static TradingPortfolio fromNbt(NbtCompound nbt) {
        int[] quantities = normalizeArray(nbt.getIntArray(QUANTITIES_KEY).orElse(new int[0]));
        int[] averageCosts = normalizeArray(nbt.getIntArray(AVERAGE_COSTS_KEY).orElse(new int[0]));
        int cash = nbt.getInt(CASH_KEY).orElse(STARTING_CASH);
        int realizedPnl = nbt.getInt(REALIZED_PNL_KEY).orElse(0);
        return new TradingPortfolio(cash, realizedPnl, quantities, averageCosts);
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt(CASH_KEY, cash);
        nbt.putInt(REALIZED_PNL_KEY, realizedPnl);
        nbt.putIntArray(QUANTITIES_KEY, quantities);
        nbt.putIntArray(AVERAGE_COSTS_KEY, averageCosts);
        return nbt;
    }

    public int cash() {
        return cash;
    }

    public int realizedPnl() {
        return realizedPnl;
    }

    public int quantity(int index) {
        return quantities[index];
    }

    public int averageCost(int index) {
        return averageCosts[index];
    }

    public int unrealizedPnl() {
        int total = 0;
        for (int index = 0; index < TradingMarket.assetCount(); index++) {
            total += quantity(index) * (TradingMarket.asset(index).price() - averageCost(index));
        }

        return total;
    }

    public int totalPnl() {
        return realizedPnl + unrealizedPnl();
    }

    public int netWorth() {
        int total = cash;

        for (int index = 0; index < TradingMarket.assetCount(); index++) {
            total += quantity(index) * TradingMarket.asset(index).price();
        }

        return total;
    }

    public int positionValue(int index) {
        return quantity(index) * TradingMarket.asset(index).price();
    }

    public TradingPortfolio buy(int assetIndex, int requestedQuantity) {
        if (assetIndex < 0 || assetIndex >= TradingMarket.assetCount() || requestedQuantity <= 0) {
            return this;
        }

        int price = TradingMarket.asset(assetIndex).price();
        int affordableQuantity = cash / price;
        int quantityToBuy = Math.min(requestedQuantity, affordableQuantity);

        if (quantityToBuy <= 0) {
            return this;
        }

        int[] nextQuantities = quantities.clone();
        int[] nextAverageCosts = averageCosts.clone();
        int currentQuantity = nextQuantities[assetIndex];
        int newQuantity = currentQuantity + quantityToBuy;
        long weightedCost = (long) currentQuantity * nextAverageCosts[assetIndex] + (long) quantityToBuy * price;

        nextQuantities[assetIndex] = newQuantity;
        nextAverageCosts[assetIndex] = (int) (weightedCost / newQuantity);

        return new TradingPortfolio(cash - quantityToBuy * price, realizedPnl, nextQuantities, nextAverageCosts);
    }

    public TradingPortfolio sell(int assetIndex, int requestedQuantity) {
        if (assetIndex < 0 || assetIndex >= TradingMarket.assetCount() || requestedQuantity <= 0) {
            return this;
        }

        int heldQuantity = quantities[assetIndex];
        int quantityToSell = Math.min(requestedQuantity, heldQuantity);

        if (quantityToSell <= 0) {
            return this;
        }

        int price = TradingMarket.asset(assetIndex).price();
        int basis = averageCosts[assetIndex];
        int[] nextQuantities = quantities.clone();
        int[] nextAverageCosts = averageCosts.clone();

        nextQuantities[assetIndex] = heldQuantity - quantityToSell;
        if (nextQuantities[assetIndex] == 0) {
            nextAverageCosts[assetIndex] = 0;
        }

        int nextRealizedPnl = realizedPnl + quantityToSell * (price - basis);
        int nextCash = cash + quantityToSell * price;

        return new TradingPortfolio(nextCash, nextRealizedPnl, nextQuantities, nextAverageCosts);
    }

    private static int[] normalizeArray(int[] values) {
        int[] normalized = new int[TradingMarket.assetCount()];
        System.arraycopy(values, 0, normalized, 0, Math.min(values.length, normalized.length));
        return normalized;
    }
}