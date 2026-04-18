package com.capitalcraft.capitalcraft.market;

public final class TradingMarket {

    public static final int[] ASSET_BASE_PRICES = {
        22000,
        18000,
        17000,
        59000,
        45000
    };

    public static final String[] SYMBOLS = {
        "META", "AAPL", "GOOGL", "NVDA", "MSFT"
    };

    public static final String[] NAMES = {
        "Meta", "Apple", "Alphabet", "NVIDIA", "Microsoft"
    };

    public static final String[] DESCRIPTIONS = {
        "Social media and advertising",
        "Consumer hardware and services",
        "Search and cloud platforms",
        "AI chips and datacenter hardware",
        "Enterprise cloud and software"
    };

    private TradingMarket() {
    }

    public static int assetCount() {
        return SYMBOLS.length;
    }

    public static TradingAsset asset(int index) {
        if (index < 0 || index >= assetCount()) {
            return null;
        }
        int price = MarketSimulator.getPrice(index);
        return new TradingAsset(SYMBOLS[index], NAMES[index], price, DESCRIPTIONS[index]);
    }

    public static int getBasePrice(int assetIndex) {
        if (assetIndex < 0 || assetIndex >= ASSET_BASE_PRICES.length) {
            return 0;
        }
        return ASSET_BASE_PRICES[assetIndex];
    }
}