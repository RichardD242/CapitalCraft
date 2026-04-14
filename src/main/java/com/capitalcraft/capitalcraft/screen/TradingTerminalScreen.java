package com.capitalcraft.capitalcraft.screen;

import com.capitalcraft.capitalcraft.market.MarketSimulator;
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

    private static final int ROWS_PER_PAGE = 5;
    private static final int BUY_QUANTITY = 1;
    private static final int PANEL_COLOR = 0xCC12161F;
    private static final int PANEL_BORDER_COLOR = 0xFF596580;
    private static final int ACCENT_COLOR = 0xFFF2C94C;
    private static final int POSITIVE_COLOR = 0xFF73D17C;
    private static final int NEGATIVE_COLOR = 0xFFEC6B6B;
    private static final int ROW_HEIGHT = 22;
    private static final int MARKET_PANEL_WIDTH = 256;
    private static final int MARKET_PANEL_HEIGHT = 186;
    private static final int PORTFOLIO_PANEL_WIDTH = 120;
    private static final int PANEL_TOP_OFFSET = 24;
    private static final int PANEL_LEFT_MARGIN = 10;
    private static final int PANEL_GAP = 8;
    
    private static final float EUR_TO_CREDITS_RATE = 1100.0f;
    private final List<ButtonWidget> buyButtons = new ArrayList<>();
    private final List<ButtonWidget> sellButtons = new ArrayList<>();
    private final List<ButtonWidget> refreshButtons = new ArrayList<>();
    private TradingPortfolio snapshot = TradingPortfolio.createDefault();
    private int page = 0;

    public TradingTerminalScreen(TradingTerminalScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 404;
        this.backgroundHeight = 228;
        this.titleX = 12;
        this.titleY = 8;
    }

    @Override
    protected void init() {
        super.init();
        buyButtons.clear();
        sellButtons.clear();
        refreshButtons.clear();

        int marketLeft = this.x + PANEL_LEFT_MARGIN + 6;
        int top = this.y + 50;

        for (int row = 0; row < ROWS_PER_PAGE; row++) {
            int rowY = top + row * ROW_HEIGHT;
            int refreshX = marketLeft + 166;
            int buyX = marketLeft + 188;
            int sellX = marketLeft + 220;
            int visibleRow = row;

            ButtonWidget refreshButton = addDrawableChild(ButtonWidget.builder(Text.literal("R"), button -> {
                        int assetIndex = assetIndexForRow(visibleRow);
                        if (assetIndex >= 0) {
                            CapitalcraftNetworking.requestRefresh(getScreenHandler().getTerminalPos(), assetIndex);
                        }
                    })
                    .dimensions(refreshX, rowY, 18, 14)
                    .build());

            ButtonWidget buyButton = addDrawableChild(ButtonWidget.builder(Text.literal("Buy"), button -> {
                        int assetIndex = assetIndexForRow(visibleRow);
                        if (assetIndex >= 0) {
                            CapitalcraftNetworking.requestTrade(getScreenHandler().getTerminalPos(), assetIndex, BUY_QUANTITY, true);
                        }
                    })
                    .dimensions(buyX, rowY, 28, 14)
                    .build());
            ButtonWidget sellButton = addDrawableChild(ButtonWidget.builder(Text.literal("Sell"), button -> {
                        int assetIndex = assetIndexForRow(visibleRow);
                        if (assetIndex >= 0) {
                            CapitalcraftNetworking.requestTrade(getScreenHandler().getTerminalPos(), assetIndex, BUY_QUANTITY, false);
                        }
                    })
                    .dimensions(sellX, rowY, 30, 14)
                    .build());

            refreshButtons.add(refreshButton);
            buyButtons.add(buyButton);
            sellButtons.add(sellButton);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("<"), button -> {
                    page = Math.max(0, page - 1);
                })
                .dimensions(this.x + 16, this.y + this.backgroundHeight - 24, 20, 14)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal(">"), button -> {
                    int maxPage = Math.max(0, (TradingMarket.assetCount() - 1) / ROWS_PER_PAGE);
                    page = Math.min(maxPage, page + 1);
                })
                .dimensions(this.x + 42, this.y + this.backgroundHeight - 24, 20, 14)
                .build());
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
        int panelTop = top + PANEL_TOP_OFFSET;
        int marketLeft = left + PANEL_LEFT_MARGIN;
        int portfolioLeft = marketLeft + MARKET_PANEL_WIDTH + PANEL_GAP;

        context.fillGradient(left, top, right, bottom, 0xFF0E131C, 0xFF151D2A);

        context.fill(marketLeft, panelTop, marketLeft + MARKET_PANEL_WIDTH, panelTop + MARKET_PANEL_HEIGHT, PANEL_COLOR);
        context.drawBorder(marketLeft, panelTop, MARKET_PANEL_WIDTH, MARKET_PANEL_HEIGHT, PANEL_BORDER_COLOR);

        context.fill(portfolioLeft, panelTop, portfolioLeft + PORTFOLIO_PANEL_WIDTH, panelTop + MARKET_PANEL_HEIGHT, PANEL_COLOR);
        context.drawBorder(portfolioLeft, panelTop, PORTFOLIO_PANEL_WIDTH, MARKET_PANEL_HEIGHT, PANEL_BORDER_COLOR);

        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Trading Terminal"), left + (this.backgroundWidth / 2), top + 8, ACCENT_COLOR);

        drawMarketPanel(context, marketLeft + 6, panelTop + 6);
        drawPortfolioPanel(context, portfolioLeft + 6, panelTop + 6);
    }

    private void drawMarketPanel(DrawContext context, int startX, int startY) {
        context.drawTextWithShadow(textRenderer, Text.literal("Market"), startX, startY, ACCENT_COLOR);
        context.drawTextWithShadow(textRenderer, Text.literal("Sym"), startX, startY + 11, 0xFF9DB0C8);
        context.drawTextWithShadow(textRenderer, Text.literal("Price"), startX + 30, startY + 11, 0xFF9DB0C8);
        context.drawTextWithShadow(textRenderer, Text.literal("Held"), startX + 70, startY + 11, 0xFF9DB0C8);
        context.drawTextWithShadow(textRenderer, Text.literal("P/L"), startX + 102, startY + 11, 0xFF9DB0C8);
        context.drawTextWithShadow(textRenderer, Text.literal("Pos"), startX + 136, startY + 11, 0xFF9DB0C8);

        for (int row = 0; row < ROWS_PER_PAGE; row++) {
            int index = assetIndexForRow(row);
            if (index < 0) {
                continue;
            }
            int rowY = startY + 24 + row * ROW_HEIGHT;
            int price = MarketSimulator.getPrice(index);
            int heldQuantity = snapshot.quantity(index);
            int positionValue = snapshot.positionValue(index);
            int unrealized = heldQuantity * (price - snapshot.averageCost(index));

            String symbol = TradingMarket.SYMBOLS[index];
            context.drawTextWithShadow(textRenderer, symbol, startX, rowY, 0xFFF2F5FA);
            context.drawTextWithShadow(textRenderer, formatPrice(price), startX + 30, rowY, 0xFFD9E1F2);
            context.drawTextWithShadow(textRenderer, "x" + heldQuantity, startX + 72, rowY, 0xFF9DB0C8);
            context.drawTextWithShadow(textRenderer, formatSignedMoney(unrealized), startX + 102, rowY, colorForValue(unrealized));
            int textWidth = textRenderer.getWidth(formatSignedMoney(unrealized));
            context.drawTextWithShadow(textRenderer, formatPrice(positionValue), startX + 156 - textWidth, rowY, 0xFFB8D7FF);
        }

        int maxPage = Math.max(0, (TradingMarket.assetCount() - 1) / ROWS_PER_PAGE);
        context.drawTextWithShadow(textRenderer, Text.literal("Page " + (page + 1) + "/" + (maxPage + 1)), startX + 4, startY + 138, 0xFF9DB0C8);
        context.drawTextWithShadow(textRenderer, Text.literal("R=Refresh"), startX + 84, startY + 138, 0xFF9DB0C8);
    }

    private void drawPortfolioPanel(DrawContext context, int startX, int startY) {
        int valueX = startX + 54;
        int lineY = startY;

        context.drawTextWithShadow(textRenderer, Text.literal("Portfolio"), startX, lineY, ACCENT_COLOR);
        lineY += 16;

        int cashEUR = (int) (snapshot.cash() / EUR_TO_CREDITS_RATE);
        int worthEUR = (int) (snapshot.netWorth() / EUR_TO_CREDITS_RATE);

        drawPortfolioRow(context, "Cash", cashEUR + "E", startX, valueX, lineY, 0xFFDDE3EE, 0xFFF2F5FA);
        lineY += 12;
        drawPortfolioRow(context, "Cred", formatPrice(snapshot.cash()), startX, valueX, lineY, 0xFF9DB0C8, 0xFFB8D7FF);
        lineY += 14;
        drawPortfolioRow(context, "Worth", worthEUR + "E", startX, valueX, lineY, 0xFFDDE3EE, 0xFFF2F5FA);
        lineY += 14;
        drawPortfolioRow(context, "Real", formatSignedMoney(snapshot.realizedPnl()), startX, valueX, lineY, 0xFFDDE3EE, colorForValue(snapshot.realizedPnl()));
        lineY += 14;
        drawPortfolioRow(context, "Unreal", formatSignedMoney(snapshot.unrealizedPnl()), startX, valueX, lineY, 0xFFDDE3EE, colorForValue(snapshot.unrealizedPnl()));
        lineY += 14;
        drawPortfolioRow(context, "Total", formatSignedMoney(snapshot.totalPnl()), startX, valueX, lineY, 0xFFDDE3EE, colorForValue(snapshot.totalPnl()));
    }

    private void drawPortfolioRow(DrawContext context, String label, String value, int labelX, int valueX, int y, int labelColor, int valueColor) {
        context.drawTextWithShadow(textRenderer, label, labelX, y, labelColor);
        context.drawTextWithShadow(textRenderer, value, valueX, y, valueColor);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(textRenderer, this.title, this.titleX, this.titleY, ACCENT_COLOR, false);
    }

    private void updateButtonStates() {
        for (int row = 0; row < ROWS_PER_PAGE; row++) {
            int index = assetIndexForRow(row);
            boolean valid = index >= 0;
            refreshButtons.get(row).active = valid;
            if (!valid) {
                buyButtons.get(row).active = false;
                sellButtons.get(row).active = false;
                continue;
            }

            int price = MarketSimulator.getPrice(index);
            buyButtons.get(row).active = snapshot.cash() >= price;
            sellButtons.get(row).active = snapshot.quantity(index) > 0;
        }
    }

    private int assetIndexForRow(int row) {
        int index = page * ROWS_PER_PAGE + row;
        return index < TradingMarket.assetCount() ? index : -1;
    }

    public void applySnapshot(TradingPortfolio portfolio) {
        snapshot = portfolio;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static int colorForValue(int value) {
        if (value > 0) return POSITIVE_COLOR;
        if (value < 0) return NEGATIVE_COLOR;
        return 0xFFB8C2D8;
    }

    private static String formatSignedMoney(int value) {
        if (value > 0) return "+$" + value;
        if (value < 0) return "-$" + Math.abs(value);
        return "$0";
    }
    
    private static String formatPrice(int value) {
        if (value >= 1000000) {
            return "$" + (value / 1000000) + "M";
        }
        if (value >= 1000) {
            return "$" + (value / 1000) + "K";
        }
        return "$" + value;
    }
}