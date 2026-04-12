package com.capitalcraft.capitalcraft.screen;

import com.capitalcraft.capitalcraft.market.TradingAsset;
import com.capitalcraft.capitalcraft.market.TradingMarket;
import com.capitalcraft.capitalcraft.market.TradingPortfolio;
import com.capitalcraft.capitalcraft.network.CapitalcraftNetworking;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class TradingTerminalScreen extends net.minecraft.client.gui.screen.ingame.HandledScreen<TradingTerminalScreenHandler> {

    private static final int BUY_QUANTITY = 1;
    private static final int PANEL_COLOR = 0xCC12161F;
    private static final int PANEL_BORDER_COLOR = 0xFF596580;
    private static final int ACCENT_COLOR = 0xFFF2C94C;
    private static final int POSITIVE_COLOR = 0xFF73D17C;
    private static final int NEGATIVE_COLOR = 0xFFEC6B6B;

    private final List<ButtonWidget> buyButtons = new ArrayList<>();
    private final List<ButtonWidget> sellButtons = new ArrayList<>();
    private TradingPortfolio snapshot = TradingPortfolio.createDefault();

    public TradingTerminalScreen(TradingTerminalScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 320;
        this.backgroundHeight = 222;
        this.titleX = 18;
        this.titleY = 12;
        this.playerInventoryTitleX = 18;
        this.playerInventoryTitleY = 132;
    }

    @Override
    protected void init() {
        super.init();
        buyButtons.clear();
        sellButtons.clear();

        int left = this.x + 14;
        int top = this.y + 36;
        int rowWidth = 292;

        for (int index = 0; index < TradingMarket.assetCount(); index++) {
            int rowY = top + index * 22;
            int buyX = left + rowWidth - 94;
            int sellX = left + rowWidth - 46;
            int assetIndex = index;

                    ButtonWidget buyButton = addDrawableChild(ButtonWidget.builder(Text.literal("Buy 1"), button -> CapitalcraftNetworking.requestTrade(getScreenHandler().getTerminalPos(), assetIndex, BUY_QUANTITY, true))
                    .dimensions(buyX, rowY, 44, 18)
                    .build());
                    ButtonWidget sellButton = addDrawableChild(ButtonWidget.builder(Text.literal("Sell 1"), button -> CapitalcraftNetworking.requestTrade(getScreenHandler().getTerminalPos(), assetIndex, BUY_QUANTITY, false))
                    .dimensions(sellX, rowY, 44, 18)
                    .build());

            buyButtons.add(buyButton);
            sellButtons.add(sellButton);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        updateButtonStates();
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int left = this.x;
        int top = this.y;
        int right = left + this.backgroundWidth;
        int bottom = top + this.backgroundHeight;

        context.fillGradient(left, top, right, bottom, 0xFF0D1017, 0xFF171B24);
        context.fill(left + 10, top + 24, left + 212, bottom - 16, PANEL_COLOR);
        context.fill(left + 218, top + 24, right - 10, bottom - 16, PANEL_COLOR);
        context.drawBorder(left + 10, top + 24, 202, bottom - top - 40, PANEL_BORDER_COLOR);
        context.drawBorder(left + 218, top + 24, 92, bottom - top - 40, PANEL_BORDER_COLOR);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("block.capitalcraft.trading_terminal"), left + this.backgroundWidth / 2, top + 9, ACCENT_COLOR);

        drawMarketPanel(context, left + 18, top + 32);
        drawPortfolioPanel(context, left + 226, top + 32);
    }

    private void drawMarketPanel(DrawContext context, int startX, int startY) {
        context.drawTextWithShadow(textRenderer, Text.literal("Market Prices"), startX, startY, ACCENT_COLOR);

        for (int index = 0; index < TradingMarket.assetCount(); index++) {
            TradingAsset asset = TradingMarket.asset(index);
            int rowY = startY + 14 + index * 22;
            int heldQuantity = snapshot.quantity(index);
            int positionValue = snapshot.positionValue(index);
            int unrealized = heldQuantity * (asset.price() - snapshot.averageCost(index));

            context.drawTextWithShadow(textRenderer, asset.symbol() + "  " + asset.name(), startX, rowY, 0xFFF2F5FA);
            context.drawTextWithShadow(textRenderer, "$" + asset.price(), startX + 118, rowY, 0xFFD9E1F2);
            context.drawTextWithShadow(textRenderer, "Hold " + heldQuantity, startX + 154, rowY, 0xFF9DB0C8);
            context.drawTextWithShadow(textRenderer, formatSignedMoney(unrealized), startX + 210, rowY, colorForValue(unrealized));
            context.drawTextWithShadow(textRenderer, "$" + positionValue, startX + 252, rowY, 0xFFB8D7FF);
        }
    }

    private void drawPortfolioPanel(DrawContext context, int startX, int startY) {
        context.drawTextWithShadow(textRenderer, Text.literal("Portfolio"), startX, startY, ACCENT_COLOR);
        context.drawTextWithShadow(textRenderer, "Cash", startX, startY + 18, 0xFFDDE3EE);
        context.drawTextWithShadow(textRenderer, "$" + snapshot.cash(), startX + 62, startY + 18, 0xFFF2F5FA);
        context.drawTextWithShadow(textRenderer, "Value", startX, startY + 36, 0xFFDDE3EE);
        context.drawTextWithShadow(textRenderer, "$" + snapshot.netWorth(), startX + 62, startY + 36, 0xFFF2F5FA);
        context.drawTextWithShadow(textRenderer, "Realized", startX, startY + 54, 0xFFDDE3EE);
        context.drawTextWithShadow(textRenderer, formatSignedMoney(snapshot.realizedPnl()), startX + 62, startY + 54, colorForValue(snapshot.realizedPnl()));
        context.drawTextWithShadow(textRenderer, "Unrealized", startX, startY + 72, 0xFFDDE3EE);
        context.drawTextWithShadow(textRenderer, formatSignedMoney(snapshot.unrealizedPnl()), startX + 62, startY + 72, colorForValue(snapshot.unrealizedPnl()));
        context.drawTextWithShadow(textRenderer, "Total P/L", startX, startY + 90, 0xFFDDE3EE);
        context.drawTextWithShadow(textRenderer, formatSignedMoney(snapshot.totalPnl()), startX + 62, startY + 90, colorForValue(snapshot.totalPnl()));
    }

    private void updateButtonStates() {
        for (int index = 0; index < TradingMarket.assetCount(); index++) {
            TradingAsset asset = TradingMarket.asset(index);
            buyButtons.get(index).active = snapshot.cash() >= asset.price();
            sellButtons.get(index).active = snapshot.quantity(index) > 0;
        }
    }

    public void applySnapshot(TradingPortfolio portfolio) {
        snapshot = portfolio;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static int colorForValue(int value) {
        if (value > 0) {
            return POSITIVE_COLOR;
        }

        if (value < 0) {
            return NEGATIVE_COLOR;
        }

        return 0xFFB8C2D8;
    }

    private static String formatSignedMoney(int value) {
        if (value > 0) {
            return "+$" + value;
        }

        if (value < 0) {
            return "-$" + Math.abs(value);
        }

        return "$0";
    }
}