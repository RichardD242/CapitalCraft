package com.capitalcraft.capitalcraft.util;

import com.capitalcraft.capitalcraft.market.TradingMarket;
import com.capitalcraft.capitalcraft.market.TradingPortfolio;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;

public final class PlayerTradingData {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "capitalcraft_portfolios.json";

    private static final Map<UUID, TradingPortfolio> CACHE = new ConcurrentHashMap<>();
    private static Path loadedFrom;

    private PlayerTradingData() {
    }

    public static TradingPortfolio getPortfolio(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return TradingPortfolio.createDefault();
        }

        ensureLoaded(serverPlayer.getServer());
        return CACHE.computeIfAbsent(serverPlayer.getUuid(), ignored -> TradingPortfolio.createDefault());
    }

    public static void setPortfolio(ServerPlayerEntity player, TradingPortfolio portfolio) {
        ensureLoaded(player.getServer());
        CACHE.put(player.getUuid(), portfolio);
        save(player.getServer());
    }

    private static synchronized void ensureLoaded(MinecraftServer server) {
        Path dataFile = getDataFile(server);
        if (dataFile.equals(loadedFrom)) {
            return;
        }

        CACHE.clear();
        loadedFrom = dataFile;

        if (!Files.exists(dataFile)) {
            return;
        }

        try {
            String raw = Files.readString(dataFile, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                UUID uuid = UUID.fromString(entry.getKey());
                TradingPortfolio portfolio = fromJson(entry.getValue().getAsJsonObject());
                CACHE.put(uuid, portfolio);
            }
        } catch (Exception ignored) {
            CACHE.clear();
        }
    }

    private static synchronized void save(MinecraftServer server) {
        Path dataFile = getDataFile(server);
        JsonObject root = new JsonObject();

        for (Map.Entry<UUID, TradingPortfolio> entry : CACHE.entrySet()) {
            root.add(entry.getKey().toString(), toJson(entry.getValue()));
        }

        try {
            Files.createDirectories(dataFile.getParent());
            Files.writeString(dataFile, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private static Path getDataFile(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT).resolve("data").resolve(FILE_NAME);
    }

    private static JsonObject toJson(TradingPortfolio portfolio) {
        JsonObject object = new JsonObject();
        object.addProperty("cash", portfolio.cash());
        object.addProperty("realizedPnl", portfolio.realizedPnl());

        JsonArray quantities = new JsonArray();
        JsonArray averageCosts = new JsonArray();
        for (int index = 0; index < TradingMarket.assetCount(); index++) {
            quantities.add(portfolio.quantity(index));
            averageCosts.add(portfolio.averageCost(index));
        }

        object.add("quantities", quantities);
        object.add("averageCosts", averageCosts);
        return object;
    }

    private static TradingPortfolio fromJson(JsonObject object) {
        int assetCount = TradingMarket.assetCount();
        int[] quantities = new int[assetCount];
        int[] averageCosts = new int[assetCount];

        if (object.has("quantities")) {
            JsonArray quantityArray = object.getAsJsonArray("quantities");
            for (int index = 0; index < Math.min(assetCount, quantityArray.size()); index++) {
                quantities[index] = quantityArray.get(index).getAsInt();
            }
        }

        if (object.has("averageCosts")) {
            JsonArray costArray = object.getAsJsonArray("averageCosts");
            for (int index = 0; index < Math.min(assetCount, costArray.size()); index++) {
                averageCosts[index] = costArray.get(index).getAsInt();
            }
        }

        int cash = object.has("cash") ? object.get("cash").getAsInt() : TradingPortfolio.createDefault().cash();
        int realizedPnl = object.has("realizedPnl") ? object.get("realizedPnl").getAsInt() : 0;
        return new TradingPortfolio(cash, realizedPnl, quantities, averageCosts);
    }
}

