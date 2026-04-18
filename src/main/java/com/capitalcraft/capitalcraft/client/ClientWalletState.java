package com.capitalcraft.capitalcraft.client;

public final class ClientWalletState {

    private static final int DEFAULT_CASH = 1_000;
    private static int cash = DEFAULT_CASH;

    private ClientWalletState() {
    }

    public static int getCash() {
        return cash;
    }

    public static void setCash(int value) {
        cash = Math.max(0, value);
    }
}
