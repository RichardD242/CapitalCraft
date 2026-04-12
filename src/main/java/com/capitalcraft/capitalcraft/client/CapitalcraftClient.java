package com.capitalcraft.capitalcraft.client;

import com.capitalcraft.capitalcraft.network.CapitalcraftNetworking;
import com.capitalcraft.capitalcraft.screen.CapitalcraftScreens;
import net.fabricmc.api.ClientModInitializer;

public class CapitalcraftClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CapitalcraftScreens.registerClientScreens();
        CapitalcraftNetworking.initClient();
    }
}
