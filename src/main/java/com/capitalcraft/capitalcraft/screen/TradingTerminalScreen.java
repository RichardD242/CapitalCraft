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
    private static final int CHART_HISTORY_POINTS = 32;
    private static final int PANEL_INNER_PADDING = 6;
    private static final int BUTTON_GAP = 2;
    private static final int REFRESH_BUTTON_WIDTH = 16;
    private static final int CHART_BUTTON_WIDTH = 16;
    private static final int BUY_BUTTON_WIDTH = 28;
    private static final int SELL_BUTTON_WIDTH = 30;
    private static final int BUTTON_HEIGHT = 14;
    
    private final List<ButtonWidget> buyButtons = new ArrayList<>();
    private final List<ButtonWidget> sellButtons = new ArrayList<>();
    private final List<ButtonWidget> refreshButtons = new ArrayList<>();
    private final List<ButtonWidget> chartButtons = new ArrayList<>();
    private TradingPortfolio snapshot = TradingPortfolio.createDefault();
    private final int[][] priceHistory = new int[TradingMarket.assetCount()][CHART_HISTORY_POINTS];
    private final int[] priceHistorySizes = new int[TradingMarket.assetCount()];
    private int selectedChartAsset = 0;

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
        chartButtons.clear();

        int marketPanelLeft = this.x + PANEL_LEFT_MARGIN;
        int marketPanelRight = marketPanelLeft + MARKET_PANEL_WIDTH;
        int top = this.y + 50;
        int controlsWidth = REFRESH_BUTTON_WIDTH + BUTTON_GAP + CHART_BUTTON_WIDTH + BUTTON_GAP + BUY_BUTTON_WIDTH + BUTTON_GAP + SELL_BUTTON_WIDTH;
        int controlsLeft = marketPanelRight - PANEL_INNER_PADDING - controlsWidth;

        for (int row = 0; row < ROWS_PER_PAGE; row++) {
            int rowY = top + row * ROW_HEIGHT;
            int refreshX = controlsLeft;
            int chartX = refreshX + REFRESH_BUTTON_WIDTH + BUTTON_GAP;
            int buyX = chartX + CHART_BUTTON_WIDTH + BUTTON_GAP;
            int sellX = buyX + BUY_BUTTON_WIDTH + BUTTON_GAP;
            int visibleRow = row;

            ButtonWidget refreshButton = addDrawableChild(ButtonWidget.builder(Text.literal("R"), button -> {
                        int assetIndex = assetIndexForRow(visibleRow);
                        if (assetIndex >= 0) {
                            CapitalcraftNetworking.requestRefresh(getScreenHandler().getTerminalPos(), assetIndex);
                        }
                    })
                    .dimensions(refreshX, rowY, REFRESH_BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());

            ButtonWidget buyButton = addDrawableChild(ButtonWidget.builder(Text.literal("Buy"), button -> {
                        int assetIndex = assetIndexForRow(visibleRow);
                        if (assetIndex >= 0) {
                            CapitalcraftNetworking.requestTrade(getScreenHandler().getTerminalPos(), assetIndex, BUY_QUANTITY, true);
                        }
                    })
                    .dimensions(buyX, rowY, BUY_BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());

            ButtonWidget chartButton = addDrawableChild(ButtonWidget.builder(Text.literal("C"), button -> {
                        int assetIndex = assetIndexForRow(visibleRow);
                        if (assetIndex >= 0) {
                            selectedChartAsset = assetIndex;
                            seedHistoryIfEmpty(assetIndex);
                        }
                    })
                    .dimensions(chartX, rowY, CHART_BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());

            ButtonWidget sellButton = addDrawableChild(ButtonWidget.builder(Text.literal("Sell"), button -> {
                        int assetIndex = assetIndexForRow(visibleRow);
                        if (assetIndex >= 0) {
                            CapitalcraftNetworking.requestTrade(getScreenHandler().getTerminalPos(), assetIndex, BUY_QUANTITY, false);
                        }
                    })
                    .dimensions(sellX, rowY, SELL_BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());

            refreshButtons.add(refreshButton);
            buyButtons.add(buyButton);
            sellButtons.add(sellButton);
            chartButtons.add(chartButton);
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
        int panelTop = top + PANEL_TOP_OFFSET;
        int marketLeft = left + PANEL_LEFT_MARGIN;
        int portfolioLeft = marketLeft + MARKET_PANEL_WIDTH + PANEL_GAP;

        context.fillGradient(left, top, right, bottom, 0xFF0E131C, 0xFF151D2A);

        context.fill(marketLeft, panelTop, marketLeft + MARKET_PANEL_WIDTH, panelTop + MARKET_PANEL_HEIGHT, PANEL_COLOR);
        context.drawBorder(marketLeft, panelTop, MARKET_PANEL_WIDTH, MARKET_PANEL_HEIGHT, PANEL_BORDER_COLOR);

        context.fill(portfolioLeft, panelTop, portfolioLeft + PORTFOLIO_PANEL_WIDTH, panelTop + MARKET_PANEL_HEIGHT, PANEL_COLOR);
        context.drawBorder(portfolioLeft, panelTop, PORTFOLIO_PANEL_WIDTH, MARKET_PANEL_HEIGHT, PANEL_BORDER_COLOR);

        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Trading Terminal"), left + (this.backgroundWidth / 2), top + 8, ACCENT_COLOR);

        context.enableScissor(marketLeft + 1, panelTop + 1, marketLeft + MARKET_PANEL_WIDTH - 1, panelTop + MARKET_PANEL_HEIGHT - 1);
        drawMarketPanel(context, marketLeft + PANEL_INNER_PADDING, panelTop + PANEL_INNER_PADDING);
        context.disableScissor();

        context.enableScissor(portfolioLeft + 1, panelTop + 1, portfolioLeft + PORTFOLIO_PANEL_WIDTH - 1, panelTop + MARKET_PANEL_HEIGHT - 1);
        drawPortfolioPanel(context, portfolioLeft + PANEL_INNER_PADDING, panelTop + PANEL_INNER_PADDING);
        context.disableScissor();
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
            int price = MarketSimulator.getPriceInCurrencyUnits(index);
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

        context.drawTextWithShadow(textRenderer, Text.literal("R=Refresh C=Chart"), startX + 4, startY + 138, 0xFF9DB0C8);
    }

    private void drawPortfolioPanel(DrawContext context, int startX, int startY) {
        int valueX = startX + 54;
        int lineY = startY;

        context.drawTextWithShadow(textRenderer, Text.literal("Portfolio"), startX, lineY, ACCENT_COLOR);
        lineY += 16;

        int cashEUR = snapshot.cash();
        int worthEUR = snapshot.netWorth();

        drawPortfolioRow(context, "Cash", cashEUR + " EUR", startX, valueX, lineY, 0xFFDDE3EE, 0xFFF2F5FA);
        lineY += 12;
        drawPortfolioRow(context, "Cred", formatPrice(snapshot.cash()), startX, valueX, lineY, 0xFF9DB0C8, 0xFFB8D7FF);
        lineY += 14;
        drawPortfolioRow(context, "Worth", worthEUR + " EUR", startX, valueX, lineY, 0xFFDDE3EE, 0xFFF2F5FA);
        lineY += 14;
        drawPortfolioRow(context, "Real", formatSignedMoney(snapshot.realizedPnl()), startX, valueX, lineY, 0xFFDDE3EE, colorForValue(snapshot.realizedPnl()));
        lineY += 14;
        drawPortfolioRow(context, "Unreal", formatSignedMoney(snapshot.unrealizedPnl()), startX, valueX, lineY, 0xFFDDE3EE, colorForValue(snapshot.unrealizedPnl()));
        lineY += 14;
        drawPortfolioRow(context, "Total", formatSignedMoney(snapshot.totalPnl()), startX, valueX, lineY, 0xFFDDE3EE, colorForValue(snapshot.totalPnl()));

        drawCandleChart(context, startX, startY + 95, PORTFOLIO_PANEL_WIDTH - 12, 84);
    }

    private void drawCandleChart(DrawContext context, int x, int y, int width, int height) {
        if (selectedChartAsset < 0 || selectedChartAsset >= TradingMarket.assetCount()) {
            selectedChartAsset = 0;
        }

        seedHistoryIfEmpty(selectedChartAsset);
        int historySize = priceHistorySizes[selectedChartAsset];
        if (historySize <= 0) {
            return;
        }

        int[] values = priceHistory[selectedChartAsset];
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < historySize; i++) {
            min = Math.min(min, values[i]);
            max = Math.max(max, values[i]);
        }
        if (min == max) {
            max = min + 1;
        }

        context.fill(x, y, x + width, y + height, 0x990B0F16);
        context.drawBorder(x, y, width, height, 0xFF4B5A73);
        context.drawTextWithShadow(textRenderer, TradingMarket.SYMBOLS[selectedChartAsset] + " Candles", x + 4, y + 4, 0xFFE7EEFF);

        int chartTop = y + 16;
        int chartBottom = y + height - 4;
        int drawableHeight = Math.max(8, chartBottom - chartTop);
        int candles = Math.max(1, Math.min(8, historySize / 4));
        int candleWidth = Math.max(3, (width - 10) / Math.max(1, candles));
        int step = Math.max(1, historySize / candles);

        for (int i = 0; i < candles; i++) {
            int start = i * step;
            int end = Math.min(historySize - 1, start + step - 1);
            if (start >= historySize) {
                break;
            }

            int open = values[start];
            int close = values[end];
            int low = open;
            int high = open;
            for (int j = start; j <= end; j++) {
                low = Math.min(low, values[j]);
                high = Math.max(high, values[j]);
            }

            int centerX = x + 6 + i * candleWidth + candleWidth / 2;
            int wickTop = chartTop + mapToChartY(high, min, max, drawableHeight);
            int wickBottom = chartTop + mapToChartY(low, min, max, drawableHeight);
            int openY = chartTop + mapToChartY(open, min, max, drawableHeight);
            int closeY = chartTop + mapToChartY(close, min, max, drawableHeight);

            int candleColor = close >= open ? POSITIVE_COLOR : NEGATIVE_COLOR;
            context.fill(centerX, wickTop, centerX + 1, wickBottom + 1, 0xFFD6DEE9);

            int bodyTop = Math.min(openY, closeY);
            int bodyBottom = Math.max(openY, closeY);
            if (bodyTop == bodyBottom) {
                bodyBottom += 1;
            }

            int bodyLeft = Math.max(x + 4, centerX - 2);
            int bodyRight = Math.min(x + width - 4, centerX + 3);
            context.fill(bodyLeft, bodyTop, bodyRight, bodyBottom + 1, candleColor);
        }
    }

    private int mapToChartY(int value, int min, int max, int height) {
        double normalized = (double) (value - min) / (double) (max - min);
        return height - (int) Math.round(normalized * height);
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
            chartButtons.get(row).active = valid;
            if (!valid) {
                buyButtons.get(row).active = false;
                sellButtons.get(row).active = false;
                continue;
            }

            int price = MarketSimulator.getPriceInCurrencyUnits(index);
            buyButtons.get(row).active = snapshot.cash() >= price;
            sellButtons.get(row).active = snapshot.quantity(index) > 0;
        }
    }

    private int assetIndexForRow(int row) {
        return row < TradingMarket.assetCount() ? row : -1;
    }

    public void applySnapshot(TradingPortfolio portfolio) {
        snapshot = portfolio;
        appendPriceHistory();
    }

    private void appendPriceHistory() {
        for (int assetIndex = 0; assetIndex < TradingMarket.assetCount(); assetIndex++) {
            int price = MarketSimulator.getPriceInCurrencyUnits(assetIndex);
            int size = priceHistorySizes[assetIndex];
            if (size < CHART_HISTORY_POINTS) {
                priceHistory[assetIndex][size] = price;
                priceHistorySizes[assetIndex] = size + 1;
            } else {
                System.arraycopy(priceHistory[assetIndex], 1, priceHistory[assetIndex], 0, CHART_HISTORY_POINTS - 1);
                priceHistory[assetIndex][CHART_HISTORY_POINTS - 1] = price;
            }
        }
    }

    private void seedHistoryIfEmpty(int assetIndex) {
        if (assetIndex < 0 || assetIndex >= TradingMarket.assetCount() || priceHistorySizes[assetIndex] > 0) {
            return;
        }

        int price = MarketSimulator.getPriceInCurrencyUnits(assetIndex);
        for (int i = 0; i < 8; i++) {
            priceHistory[assetIndex][i] = price;
        }
        priceHistorySizes[assetIndex] = 8;
    }

    public void applyWalletUpdate(int cash) {
        snapshot = snapshot.withCash(cash);
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