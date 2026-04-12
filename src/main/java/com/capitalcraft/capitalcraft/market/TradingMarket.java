package com.capitalcraft.capitalcraft.market;

public final class TradingMarket {

    public static final TradingAsset[] ASSETS = new TradingAsset[] {
            new TradingAsset("AUR", "Aurora Mining", 120, "Industrial metals fund"),
            new TradingAsset("BTA", "Blue Tide Analytics", 85, "Data platform test asset"),
            new TradingAsset("CFX", "Capital Fiber Exchange", 240, "Mock logistics index"),
            new TradingAsset("NXT", "Nextwave Robotics", 40, "Speculative growth asset")
    };

    private TradingMarket() {
    }

    public static int assetCount() {
        return ASSETS.length;
    }

    public static TradingAsset asset(int index) {
        return ASSETS[index];
    }
}