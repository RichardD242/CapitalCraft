package com.capitalcraft.capitalcraft;

import com.capitalcraft.capitalcraft.block.CapitalcraftBlocks;
import com.capitalcraft.capitalcraft.network.CapitalcraftNetworking;
import com.capitalcraft.capitalcraft.screen.CapitalcraftScreens;
import net.fabricmc.api.ModInitializer;

public class Capitalcraft implements ModInitializer {

    public static final String MOD_ID = "capitalcraft";

    @Override
    public void onInitialize() {
        CapitalcraftBlocks.init();
        CapitalcraftScreens.init();
        CapitalcraftNetworking.init();
    }
}