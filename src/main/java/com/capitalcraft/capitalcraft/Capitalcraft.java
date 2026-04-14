package com.capitalcraft.capitalcraft;

import com.capitalcraft.capitalcraft.block.CapitalcraftBlocks;
import com.capitalcraft.capitalcraft.command.CeoCommand;
import com.capitalcraft.capitalcraft.item.CapitalcraftItems;
import com.capitalcraft.capitalcraft.event.PlayerEventListener;
import com.capitalcraft.capitalcraft.market.MarketSimulator;
import com.capitalcraft.capitalcraft.market.TradingLedger;
import com.capitalcraft.capitalcraft.network.CapitalcraftNetworking;
import com.capitalcraft.capitalcraft.screen.CapitalcraftScreens;
import com.capitalcraft.capitalcraft.world.CapitalcraftBiomes;
import com.capitalcraft.capitalcraft.world.feature.CapitalcraftWorldFeatures;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class Capitalcraft implements ModInitializer {

    public static final String MOD_ID = "capitalcraft";

    @Override
    public void onInitialize() {
        CapitalcraftBlocks.init();
        CapitalcraftItems.init();
        CapitalcraftWorldFeatures.init();
        CapitalcraftScreens.init();
        CapitalcraftNetworking.init();
        CapitalcraftBiomes.init();
        CeoCommand.register();
        PlayerEventListener.register();
        
        MarketSimulator.reset();
        
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            MarketSimulator.tick(server.getTicks());
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            TradingLedger.syncWallet(handler.player);
        });
    }
}