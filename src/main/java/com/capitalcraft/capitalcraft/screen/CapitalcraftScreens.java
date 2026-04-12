package com.capitalcraft.capitalcraft.screen;

import com.capitalcraft.capitalcraft.Capitalcraft;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public final class CapitalcraftScreens {

    private static final Identifier TRADING_TERMINAL_ID = Identifier.of(Capitalcraft.MOD_ID, "trading_terminal");
    public static final ScreenHandlerType<TradingTerminalScreenHandler> TRADING_TERMINAL_SCREEN_HANDLER = Registry.register(
            Registries.SCREEN_HANDLER,
            TRADING_TERMINAL_ID,
            new ExtendedScreenHandlerType<>(TradingTerminalScreenHandler::new, net.minecraft.util.math.BlockPos.PACKET_CODEC)
    );

    private CapitalcraftScreens() {
    }

    public static void init() {
    }

    public static void registerClientScreens() {
        net.minecraft.client.gui.screen.ingame.HandledScreens.register(TRADING_TERMINAL_SCREEN_HANDLER, TradingTerminalScreen::new);
    }
}