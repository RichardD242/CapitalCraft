package com.capitalcraft.capitalcraft.market;

public final class TradingMarket {

    public static final int[] ASSET_BASE_PRICES = {
        59000,  // NVDA
        22000,  // META
        45000,  // MSFT
        18000,  // AAPL
        17000,  // GOOGL
        36000,  // AMZN
        70000,  // BRK.B
        19000,  // AVGO
        28000,  // TSLA
        18000,  // JPM
        12000,  // WMT
        10500,  // V
        8800,   // MA
        10000,  // XOM
        43000,  // ORCL
        51000,  // COST
        6000,   // UNH
        26000,  // HD
        62000,  // NFLX
        28000,  // AMD
        33000,  // CRM
        12000,  // KO
        11000,  // PEP
        19000,  // ADBE
        17500   // CSCO
    };

    public static final String[] SYMBOLS = {
        "NVDA", "META", "MSFT", "AAPL", "GOOGL",
        "AMZN", "BRK.B", "AVGO", "TSLA", "JPM",
        "WMT", "V", "MA", "XOM", "ORCL",
        "COST", "UNH", "HD", "NFLX", "AMD",
        "CRM", "KO", "PEP", "ADBE", "CSCO"
    };

    public static final String[] NAMES = {
        "NVIDIA", "Meta", "Microsoft", "Apple", "Alphabet",
        "Amazon", "Berkshire Hathaway", "Broadcom", "Tesla", "JPMorgan",
        "Walmart", "Visa", "Mastercard", "Exxon Mobil", "Oracle",
        "Costco", "UnitedHealth", "Home Depot", "Netflix", "AMD",
        "Salesforce", "Coca-Cola", "PepsiCo", "Adobe", "Cisco"
    };

    public static final String[] DESCRIPTIONS = {
        "AI chips and datacenter hardware",
        "Social media and advertising",
        "Enterprise cloud and software",
        "Consumer hardware and services",
        "Search and cloud platforms",
        "E-commerce and cloud",
        "Diversified holding company",
        "Semiconductor and infrastructure",
        "EV and energy",
        "Banking and financial services",
        "Retail and e-commerce",
        "Payments network",
        "Global card payments",
        "Energy and oil",
        "Enterprise databases and cloud",
        "Retail warehouse",
        "Healthcare and insurance",
        "Home improvement retail",
        "Streaming entertainment",
        "Semiconductor design",
        "Enterprise CRM",
        "Beverages",
        "Beverages and snacks",
        "Creative software",
        "Networking infrastructure"
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