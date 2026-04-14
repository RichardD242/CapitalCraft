package com.capitalcraft.capitalcraft.event;

import com.capitalcraft.capitalcraft.market.TradingLedger;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerEventListener {
    
    private PlayerEventListener() {
    }
    
    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity player) {
                TradingLedger.handlePlayerDeath(player);
            }
        });
    }
}
