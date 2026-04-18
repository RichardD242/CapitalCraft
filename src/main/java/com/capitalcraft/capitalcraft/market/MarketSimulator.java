package com.capitalcraft.capitalcraft.market;

import java.util.OptionalInt;
import java.util.Random;


public class MarketSimulator {
    
    private static final Random RANDOM = new Random();
    private static final int[] PRICES = new int[TradingMarket.assetCount()];
    
    static {
        for (int i = 0; i < TradingMarket.assetCount(); i++) {
            PRICES[i] = TradingMarket.ASSET_BASE_PRICES[i];
        }
    }
    
    private MarketSimulator() {
    }
    
    public static void tick(long serverTime) {
    }

    public static int refreshPrice(int assetIndex) {
        if (assetIndex < 0 || assetIndex >= TradingMarket.assetCount()) {
            return 0;
        }

        String symbol = TradingMarket.SYMBOLS[assetIndex];
        OptionalInt livePrice = EulerpoolPriceFetcher.fetchUsdScaled(symbol);
        if (livePrice.isPresent()) {
            PRICES[assetIndex] = clamp(assetIndex, livePrice.getAsInt());
            return PRICES[assetIndex];
        }

        int current = PRICES[assetIndex];
        float randomChange = (RANDOM.nextFloat() - 0.5f) * 0.06f;
        int simulated = (int) (current * (1.0f + randomChange));
        PRICES[assetIndex] = clamp(assetIndex, simulated);
        return PRICES[assetIndex];
    }
    
    public static int getPrice(int assetIndex) {
        if (assetIndex < 0 || assetIndex >= TradingMarket.assetCount()) {
            return 0;
        }
        return PRICES[assetIndex];
    }

    public static int getPriceInCurrencyUnits(int assetIndex) {
        return getPrice(assetIndex) / 100;
    }
    
    public static int[] getAllPrices() {
        return PRICES.clone();
    }
    
    public static void reset() {
        for (int i = 0; i < TradingMarket.assetCount(); i++) {
            PRICES[i] = TradingMarket.ASSET_BASE_PRICES[i];
        }
    }

    private static int clamp(int assetIndex, int price) {
        int min = Math.max(100, TradingMarket.ASSET_BASE_PRICES[assetIndex] / 10);
        int max = TradingMarket.ASSET_BASE_PRICES[assetIndex] * 5;
        return Math.max(min, Math.min(max, price));
    }
}
