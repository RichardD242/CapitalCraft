package com.capitalcraft.capitalcraft.screen;

import com.capitalcraft.capitalcraft.client.ClientWalletState;
import com.capitalcraft.capitalcraft.network.CapitalcraftNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class AtmScreen extends net.minecraft.client.gui.screen.ingame.HandledScreen<AtmScreenHandler> {

    public AtmScreen(AtmScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
        this.titleX = 10;
        this.titleY = 10;
    }

    @Override
    protected void init() {
        super.init();

        int left = this.x + 13;
        int top = this.y + 56;

        addDrawableChild(ButtonWidget.builder(Text.literal("Withdraw 100"), b -> CapitalcraftNetworking.requestAtmWithdraw(getScreenHandler().getAtmPos(), 100))
            .dimensions(left, top, 72, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Withdraw 200"), b -> CapitalcraftNetworking.requestAtmWithdraw(getScreenHandler().getAtmPos(), 200))
            .dimensions(left + 78, top, 72, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Withdraw 500"), b -> CapitalcraftNetworking.requestAtmWithdraw(getScreenHandler().getAtmPos(), 500))
            .dimensions(left, top + 24, 72, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Withdraw 1000"), b -> CapitalcraftNetworking.requestAtmWithdraw(getScreenHandler().getAtmPos(), 1000))
            .dimensions(left + 78, top + 24, 72, 20)
                .build());
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int left = this.x;
        int top = this.y;
        int right = left + this.backgroundWidth;
        int bottom = top + this.backgroundHeight;

        context.fillGradient(left, top, right, bottom, 0xFF111620, 0xFF1A2331);
        context.drawBorder(left, top, this.backgroundWidth, this.backgroundHeight, 0xFF51627A);

        int eur = ClientWalletState.getCash();

        context.drawTextWithShadow(textRenderer, Text.literal("ATM"), left + 10, top + 10, 0xFFF2C94C);
        context.drawTextWithShadow(textRenderer, Text.literal("Balance: " + eur + " EUR"), left + 10, top + 25, 0xFFE6EEF9);
        context.drawTextWithShadow(textRenderer, Text.literal("Start Balance: 1000 EUR"), left + 10, top + 37, 0xFFB9C8E8);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(textRenderer, this.title, this.titleX, this.titleY, 0xFFF2C94C, false);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
