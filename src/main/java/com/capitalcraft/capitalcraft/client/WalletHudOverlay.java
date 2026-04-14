package com.capitalcraft.capitalcraft.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class WalletHudOverlay {

    private static final float EUR_TO_CREDITS_RATE = 1100.0f;
    private static final float EUR_TO_USD_RATE = 1.10f;

    private WalletHudOverlay() {
    }

    @SuppressWarnings("deprecation")
    public static void register() {
        HudRenderCallback.EVENT.register(WalletHudOverlay::render);
    }

    private static void render(DrawContext context, net.minecraft.client.render.RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }

        int cashCredits = ClientWalletState.getCash();
        int cashEur = Math.round(cashCredits / EUR_TO_CREDITS_RATE);
        int cashUsd = Math.round(cashEur * EUR_TO_USD_RATE);

        int width = client.getWindow().getScaledWidth();
        int x = width - 140;
        int y = 12;

        context.fill(x - 6, y - 6, x + 126, y + 34, 0x88000000);
        context.drawTextWithShadow(client.textRenderer, Text.literal("Wallet"), x, y, 0xFFF2C94C);
        context.drawTextWithShadow(client.textRenderer, Text.literal("EUR: " + cashEur), x, y + 12, 0xFFFFFFFF);
        context.drawTextWithShadow(client.textRenderer, Text.literal("USD: " + cashUsd), x, y + 22, 0xFFB9C8E8);
    }
}
