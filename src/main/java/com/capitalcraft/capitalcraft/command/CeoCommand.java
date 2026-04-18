package com.capitalcraft.capitalcraft.command;

import com.capitalcraft.capitalcraft.world.CapitalcraftBiomes;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.GameMode;
import net.minecraft.world.biome.Biome;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class CeoCommand {

    private static final String CEO_TAG = "capitalcraft_ceo";
    private static final String FIGURE_TAG = "capitalcraft_figure";
    private static final String CEO_BRAND_TAG_PREFIX = "capitalcraft_ceo_brand_";
    private static final int NATURAL_SPAWN_INTERVAL_TICKS = 200;
    private static final int NATURAL_CEO_CAP = 6;
    private static final double SPAWN_MIN_DISTANCE = 12.0D;
    private static final double SPAWN_MAX_DISTANCE = 28.0D;
    private static final int TABLIST_REMOVE_DELAY_TICKS = 30;
    private static final int CEO_REPATH_MIN_TICKS = 120;
    private static final int CEO_REPATH_MAX_TICKS = 200;
    private static final double CEO_STEP_DISTANCE = 0.08D;
    private static final double CEO_TARGET_REACHED_DISTANCE = 0.5D;
    private static final double CEO_LOOK_RADIUS = 8.0D;
    private static final String GLOBAL_CEO_SKIN_RESOURCE = "/assets/capitalcraft/textures/2026_03_10_business-steve-23915725.png";

    private static final String[] BRANDS = {
            "apple",
            "alpahbet",
            "microsoft",
            "mark",
            "jensen"
    };

    private static final Map<String, SkinTextureData> BRAND_SKINS = loadBrandSkins();
    private static final Map<UUID, Integer> TABLIST_REMOVAL_TICKS = new HashMap<>();
    private static final Map<UUID, Vec3d> CEO_MOVE_TARGETS = new HashMap<>();
    private static final Map<UUID, Integer> CEO_REPATH_TICKS = new HashMap<>();

    private CeoCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() || hand != Hand.MAIN_HAND || !(entity instanceof ServerPlayerEntity ceoNpc)) {
                return ActionResult.PASS;
            }

            if (!ceoNpc.getCommandTags().contains(CEO_TAG)) {
                return ActionResult.PASS;
            }

            String brand = getBrandFromTags(ceoNpc);
            player.sendMessage(Text.literal(dialogueForBrand(brand)), false);
            return ActionResult.SUCCESS;
        });

        ServerTickEvents.END_WORLD_TICK.register(CeoCommand::tickNaturalSpawns);
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("spawnceo")
            .requires(source -> true)
                .executes(context -> spawnRandomCeo(context.getSource())));

        dispatcher.register(CommandManager.literal("figure")
            .requires(source -> true)
            .then(CommandManager.literal("apple").executes(context -> spawnFigureCeo(context.getSource(), "apple")))
            .then(CommandManager.literal("alphabet").executes(context -> spawnFigureCeo(context.getSource(), "alphabet")))
            .then(CommandManager.literal("microsoft").executes(context -> spawnFigureCeo(context.getSource(), "microsoft")))
            .then(CommandManager.literal("meta").executes(context -> spawnFigureCeo(context.getSource(), "meta")))
            .then(CommandManager.literal("nvidia").executes(context -> spawnFigureCeo(context.getSource(), "nvidia"))));

        dispatcher.register(CommandManager.literal("figuredelete")
            .requires(source -> true)
            .executes(context -> deleteFigures(context.getSource())));

        dispatcher.register(CommandManager.literal("spawn")
            .requires(source -> true)
                .then(CommandManager.literal("ceo").executes(context -> spawnRandomCeo(context.getSource())))
                .then(CommandManager.literal("apple").executes(context -> spawnBrandCeo(context.getSource(), "apple")))
                .then(CommandManager.literal("alphabet").executes(context -> spawnBrandCeo(context.getSource(), "alphabet")))
                .then(CommandManager.literal("microsoft").executes(context -> spawnBrandCeo(context.getSource(), "microsoft")))
                .then(CommandManager.literal("meta").executes(context -> spawnBrandCeo(context.getSource(), "meta")))
                .then(CommandManager.literal("nvidia").executes(context -> spawnBrandCeo(context.getSource(), "nvidia"))));
    }

    private static int spawnFigureCeo(ServerCommandSource source, String inputBrand) {
        String brand = normalizeBrand(inputBrand);
        if (brand.isEmpty()) {
            source.sendError(Text.literal("Unknown brand. Use apple, alphabet, microsoft, meta, or nvidia."));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("Only players can use this command."));
            return 0;
        }

        Vec3d spawnPos = player.getPos();
        ServerPlayerEntity figure = createCeoNpc(source.getWorld(), brand, spawnPos, player.getYaw(), player.getPitch(), false);
        if (figure == null) {
            source.sendError(Text.literal("Failed to spawn figure."));
            return 0;
        }

        ServerWorld world = source.getWorld();
        boolean spawned = world.spawnEntity(figure);
        forceSyncCeoToClients(world, figure);
        TABLIST_REMOVAL_TICKS.put(figure.getUuid(), TABLIST_REMOVE_DELAY_TICKS);
        boolean tracked = !world.getPlayers(serverPlayer -> serverPlayer.getUuid().equals(figure.getUuid())).isEmpty();
        if (!spawned && !tracked) {
            source.sendError(Text.literal("Failed to spawn " + displayNameForBrand(brand) + " figure."));
            return 0;
        }

        source.sendFeedback(() -> Text.literal("Spawned " + displayNameForBrand(brand) + " figure at "
                + Math.round(spawnPos.x) + ", " + Math.round(spawnPos.y) + ", " + Math.round(spawnPos.z)), true);
        return 1;
    }

    private static int deleteFigures(ServerCommandSource source) {
        ServerWorld world = source.getWorld();
        List<ServerPlayerEntity> ceos = world.getPlayers(serverPlayer -> serverPlayer.getCommandTags().contains(CEO_TAG) || serverPlayer.getCommandTags().contains(FIGURE_TAG));
        if (ceos.isEmpty()) {
            source.sendFeedback(() -> Text.literal("No CEO entities found."), false);
            return 0;
        }

        int removed = 0;
        for (ServerPlayerEntity ceo : ceos) {
            TABLIST_REMOVAL_TICKS.remove(ceo.getUuid());
            CEO_MOVE_TARGETS.remove(ceo.getUuid());
            CEO_REPATH_TICKS.remove(ceo.getUuid());
            ceo.discard();
            removed++;
        }

        int removedCount = removed;
        source.sendFeedback(() -> Text.literal("Deleted " + removedCount + " CEO entities."), true);
        return removed;
    }

    private static int spawnRandomCeo(ServerCommandSource source) {
        String brand = BRANDS[source.getWorld().getRandom().nextInt(BRANDS.length)];
        return spawnBrandCeo(source, brand);
    }

    private static int spawnBrandCeo(ServerCommandSource source, String inputBrand) {
        String brand = normalizeBrand(inputBrand);
        if (brand.isEmpty()) {
            source.sendError(Text.literal("Unknown brand. Use apple, alphabet, microsoft, meta, or nvidia."));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();
        Vec3d forward = player.getRotationVec(1.0f).multiply(2.5);
        Vec3d roughPos = player.getPos().add(forward.x, 0.0, forward.z);
        int x = (int) Math.floor(roughPos.x);
        int z = (int) Math.floor(roughPos.z);
        int y = source.getWorld().getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
        Vec3d spawnPos = Vec3d.ofBottomCenter(new BlockPos(x, y, z));

        ServerPlayerEntity ceo = createCeoNpc(source.getWorld(), brand, spawnPos, player.getYaw(), player.getPitch(), true);
        if (ceo == null) {
            source.sendError(Text.literal("Failed to spawn CEO."));
            return 0;
        }

        ServerWorld world = source.getWorld();
        boolean spawned = world.spawnEntity(ceo);
        forceSyncCeoToClients(world, ceo);
        TABLIST_REMOVAL_TICKS.put(ceo.getUuid(), TABLIST_REMOVE_DELAY_TICKS);
        boolean tracked = !world.getPlayers(serverPlayer -> serverPlayer.getUuid().equals(ceo.getUuid())).isEmpty();
        if (!spawned && !tracked) {
            source.sendError(Text.literal("Failed to spawn " + displayNameForBrand(brand) + "."));
            return 0;
        }

        source.sendFeedback(() -> Text.literal("Spawned " + displayNameForBrand(brand) + " at "
            + Math.round(spawnPos.x) + ", " + Math.round(spawnPos.y) + ", " + Math.round(spawnPos.z)
            + " (spawnEntity=" + spawned + ", tracked=" + tracked + ")"), true);
        return 1;
    }

    private static String displayNameForBrand(String brand) {
        return switch (brand) {
            case "apple" -> "Apple CEO";
            case "alpahbet" -> "Alphabet CEO";
            case "microsoft" -> "Microsoft CEO";
            case "mark" -> "Meta CEO";
            case "jensen" -> "NVIDIA CEO";
            default -> "CEO";
        };
    }

    private static String getBrandFromTags(ServerPlayerEntity ceoNpc) {
        for (String tag : ceoNpc.getCommandTags()) {
            if (tag.startsWith(CEO_BRAND_TAG_PREFIX)) {
                return tag.substring(CEO_BRAND_TAG_PREFIX.length()).toLowerCase(Locale.ROOT);
            }
        }
        return "";
    }

    private static ServerPlayerEntity createCeoNpc(ServerWorld world, String brand, Vec3d spawnPos, float yaw, float pitch, boolean mobile) {
        brand = normalizeBrand(brand);
        if (brand.isEmpty()) {
            return null;
        }

        GameProfile profile = createCeoProfile(world, brand);
        ServerPlayerEntity ceo = FakePlayer.get(world, profile);

        ceo.teleport(world, spawnPos.x, spawnPos.y, spawnPos.z, Set.of(), yaw, pitch, false);
        ceo.refreshPositionAndAngles(spawnPos.x, spawnPos.y, spawnPos.z, yaw, pitch);
        ceo.changeGameMode(GameMode.SURVIVAL);
        ceo.setCustomName(Text.literal(displayNameForBrand(brand)));
        ceo.setCustomNameVisible(true);
        ceo.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        ceo.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 20 * 30, 0, false, false));
        if (mobile) {
            ceo.addCommandTag(CEO_TAG);
        } else {
            ceo.addCommandTag(FIGURE_TAG);
            ceo.setNoGravity(true);
            ceo.setVelocity(Vec3d.ZERO);
        }
        ceo.addCommandTag(CEO_BRAND_TAG_PREFIX + brand);

        return ceo;
    }

    private static GameProfile createCeoProfile(ServerWorld world, String brand) {
        UUID id = UUID.randomUUID();
        String profileName = "ceo_" + brand + "_" + Integer.toHexString(world.getRandom().nextInt());
        GameProfile profile = new GameProfile(id, profileName.substring(0, Math.min(profileName.length(), 16)));

        SkinTextureData skin = BRAND_SKINS.getOrDefault(brand, SkinTextureData.EMPTY);
        profile.getProperties().removeAll("textures");
        if (!skin.textureValue().isEmpty()) {
            if (skin.textureSignature().isEmpty()) {
                profile.getProperties().put("textures", new Property("textures", skin.textureValue()));
            } else {
                profile.getProperties().put("textures", new Property("textures", skin.textureValue(), skin.textureSignature()));
            }
        }
        return profile;
    }

    private static void tickNaturalSpawns(ServerWorld world) {
        tickPendingTabListCleanup(world);
        tickCeoMovement(world);

        if (world.getTime() % NATURAL_SPAWN_INTERVAL_TICKS != 0) {
            return;
        }

        int ceoCount = world.getPlayers(serverPlayer -> serverPlayer.getCommandTags().contains(CEO_TAG)).size();
        if (ceoCount >= NATURAL_CEO_CAP) {
            return;
        }

        ServerPlayerEntity anchor = pickAnchorPlayer(world);
        if (anchor == null) {
            return;
        }

        BlockPos spawnBlockPos = findNaturalSpawnPos(world, anchor.getBlockPos());
        if (spawnBlockPos == null) {
            return;
        }

        String brand = BRANDS[world.getRandom().nextInt(BRANDS.length)];
        ServerPlayerEntity ceo = createCeoNpc(world, brand, Vec3d.ofBottomCenter(spawnBlockPos), world.getRandom().nextFloat() * 360.0f, 0.0f, true);
        if (ceo == null) {
            return;
        }
        world.spawnEntity(ceo);
        forceSyncCeoToClients(world, ceo);
        TABLIST_REMOVAL_TICKS.put(ceo.getUuid(), TABLIST_REMOVE_DELAY_TICKS);
    }

    private static void tickCeoMovement(ServerWorld world) {
        List<ServerPlayerEntity> ceos = world.getPlayers(serverPlayer -> serverPlayer.getCommandTags().contains(CEO_TAG));
        if (ceos.isEmpty()) {
            CEO_MOVE_TARGETS.clear();
            CEO_REPATH_TICKS.clear();
            return;
        }

        for (ServerPlayerEntity ceo : ceos) {
            if (ceo.isRemoved() || !ceo.isAlive()) {
                CEO_MOVE_TARGETS.remove(ceo.getUuid());
                CEO_REPATH_TICKS.remove(ceo.getUuid());
                continue;
            }

            ServerPlayerEntity nearbyPlayer = findNearbyPlayerToLookAt(world, ceo);
            if (nearbyPlayer != null) {
                lookAtPlayer(ceo, nearbyPlayer);
            }

            UUID uuid = ceo.getUuid();
            int repathTicks = CEO_REPATH_TICKS.getOrDefault(uuid, 0) - 1;
            Vec3d target = CEO_MOVE_TARGETS.get(uuid);

            if (target == null || repathTicks <= 0 || target.squaredDistanceTo(ceo.getPos()) <= CEO_TARGET_REACHED_DISTANCE * CEO_TARGET_REACHED_DISTANCE) {
                Vec3d nextTarget = findNearbyWalkTarget(world, ceo.getPos());
                if (nextTarget == null) {
                    CEO_MOVE_TARGETS.remove(uuid);
                    CEO_REPATH_TICKS.put(uuid, 20);
                    continue;
                }

                target = nextTarget;
                CEO_MOVE_TARGETS.put(uuid, target);
                CEO_REPATH_TICKS.put(uuid, CEO_REPATH_MIN_TICKS + world.getRandom().nextInt(CEO_REPATH_MAX_TICKS - CEO_REPATH_MIN_TICKS + 1));
            } else {
                CEO_REPATH_TICKS.put(uuid, repathTicks);
            }

            moveCeoToward(world, ceo, target);
        }
    }

    private static Vec3d findNearbyWalkTarget(ServerWorld world, Vec3d from) {
        for (int attempt = 0; attempt < 8; attempt++) {
            double distance = 4.0D + world.getRandom().nextDouble() * 6.0D;
            double angle = world.getRandom().nextDouble() * Math.PI * 2.0D;
            double x = from.x + Math.cos(angle) * distance;
            double z = from.z + Math.sin(angle) * distance;
            int blockX = (int) Math.floor(x);
            int blockZ = (int) Math.floor(z);
            int blockY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, blockX, blockZ);
            BlockPos feetPos = new BlockPos(blockX, blockY, blockZ);

            if (!world.getBlockState(feetPos).isAir() || !world.getBlockState(feetPos.up()).isAir()) {
                continue;
            }

            return Vec3d.ofBottomCenter(feetPos);
        }

        return null;
    }

    private static void moveCeoToward(ServerWorld world, ServerPlayerEntity ceo, Vec3d target) {
        Vec3d current = ceo.getPos();
        double dx = target.x - current.x;
        double dz = target.z - current.z;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        if (horizontalDistance < 0.001D) {
            return;
        }

        double step = Math.min(CEO_STEP_DISTANCE, horizontalDistance);
        double nx = current.x + (dx / horizontalDistance) * step;
        double nz = current.z + (dz / horizontalDistance) * step;
        int topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(nx), (int) Math.floor(nz));
        double ny = Math.abs(topY - current.y) <= 1.25D ? topY : current.y;
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0D);

        ceo.teleport(world, nx, ny, nz, Set.of(), yaw, 0.0f, false);
        ceo.setHeadYaw(yaw);
    }

    private static ServerPlayerEntity findNearbyPlayerToLookAt(ServerWorld world, ServerPlayerEntity ceo) {
        ServerPlayerEntity nearest = null;
        double nearestDistance = CEO_LOOK_RADIUS * CEO_LOOK_RADIUS;

        for (ServerPlayerEntity player : world.getPlayers(serverPlayer -> !serverPlayer.getCommandTags().contains(CEO_TAG) && !serverPlayer.getUuid().equals(ceo.getUuid()))) {
            double distance = player.squaredDistanceTo(ceo);
            if (distance > nearestDistance) {
                continue;
            }
            nearest = player;
            nearestDistance = distance;
        }

        return nearest;
    }

    private static void lookAtPlayer(ServerPlayerEntity ceo, ServerPlayerEntity player) {
        Vec3d ceoPos = ceo.getEyePos();
        Vec3d playerPos = player.getEyePos();
        double dx = playerPos.x - ceoPos.x;
        double dy = playerPos.y - ceoPos.y;
        double dz = playerPos.z - ceoPos.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0D);
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, horizontal)));

        ceo.setYaw(yaw);
        ceo.setPitch(pitch);
        ceo.setHeadYaw(yaw);
    }

    private static void forceSyncCeoToClients(ServerWorld world, ServerPlayerEntity ceo) {
        PlayerListS2CPacket addToPlayerListPacket = PlayerListS2CPacket.entryFromPlayer(List.of(ceo));
        EntitySpawnS2CPacket spawnPacket = new EntitySpawnS2CPacket(
                ceo.getId(),
                ceo.getUuid(),
                ceo.getX(),
                ceo.getY(),
                ceo.getZ(),
                ceo.getPitch(),
                ceo.getYaw(),
                ceo.getType(),
                0,
                ceo.getVelocity(),
                ceo.getHeadYaw()
        );

        for (ServerPlayerEntity observer : world.getPlayers(serverPlayer -> !serverPlayer.getCommandTags().contains(CEO_TAG) && !serverPlayer.getUuid().equals(ceo.getUuid()))) {
            observer.networkHandler.sendPacket(addToPlayerListPacket);
            observer.networkHandler.sendPacket(spawnPacket);
        }
    }

    private static void tickPendingTabListCleanup(ServerWorld world) {
        if (TABLIST_REMOVAL_TICKS.isEmpty()) {
            return;
        }

        List<UUID> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : TABLIST_REMOVAL_TICKS.entrySet()) {
            int remaining = entry.getValue() - 1;
            if (remaining > 0) {
                entry.setValue(remaining);
                continue;
            }
            toRemove.add(entry.getKey());
        }

        for (UUID uuid : toRemove) {
            TABLIST_REMOVAL_TICKS.remove(uuid);
            if (!(world.getEntity(uuid) instanceof ServerPlayerEntity ceo)) {
                continue;
            }
            if (!ceo.getCommandTags().contains(CEO_TAG)) {
                continue;
            }

            PlayerRemoveS2CPacket removeFromPlayerListPacket = new PlayerRemoveS2CPacket(List.of(uuid));
            for (ServerPlayerEntity observer : world.getPlayers(serverPlayer -> !serverPlayer.getCommandTags().contains(CEO_TAG) && !serverPlayer.getUuid().equals(uuid))) {
                observer.networkHandler.sendPacket(removeFromPlayerListPacket);
            }
        }
    }

    private static ServerPlayerEntity pickAnchorPlayer(ServerWorld world) {
        var players = world.getPlayers(serverPlayer -> !serverPlayer.getCommandTags().contains(CEO_TAG));
        if (players.isEmpty()) {
            return null;
        }

        for (int i = 0; i < 8; i++) {
            ServerPlayerEntity candidate = players.get(world.getRandom().nextInt(players.size()));
            if (isInSiliconValley(world, candidate.getBlockPos())) {
                return candidate;
            }
        }
        return null;
    }

    private static BlockPos findNaturalSpawnPos(ServerWorld world, BlockPos anchor) {
        for (int attempt = 0; attempt < 10; attempt++) {
            double distance = SPAWN_MIN_DISTANCE + world.getRandom().nextDouble() * (SPAWN_MAX_DISTANCE - SPAWN_MIN_DISTANCE);
            double angle = world.getRandom().nextDouble() * Math.PI * 2.0D;
            int x = anchor.getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = anchor.getZ() + (int) Math.round(Math.sin(angle) * distance);
            int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos feetPos = new BlockPos(x, y, z);

            if (!isInSiliconValley(world, feetPos)) {
                continue;
            }

            if (!world.getBlockState(feetPos).isAir() || !world.getBlockState(feetPos.up()).isAir()) {
                continue;
            }

            return feetPos;
        }
        return null;
    }

    private static boolean isInSiliconValley(ServerWorld world, BlockPos pos) {
        RegistryEntry<Biome> biome = world.getBiome(pos);
        return biome.matchesKey(CapitalcraftBiomes.SILICON_VALLEY_KEY);
    }

    private static String normalizeBrand(String brand) {
        String normalized = brand.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "apple" -> "apple";
            case "alphabet", "alpahbet", "google" -> "alpahbet";
            case "microsoft", "msft" -> "microsoft";
            case "meta", "mark", "facebook" -> "mark";
            case "nvidia", "jensen" -> "jensen";
            default -> "";
        };
    }

    private static Map<String, SkinTextureData> loadBrandSkins() {
        Map<String, SkinTextureData> skins = new HashMap<>();
        SkinTextureData globalSkin = textureFromBundledPngPath(GLOBAL_CEO_SKIN_RESOURCE, "business");
        if (!globalSkin.textureValue().isEmpty()) {
            for (String brand : BRANDS) {
                skins.put(brand, globalSkin);
            }
        }

        for (String brand : BRANDS) {
            if (skins.containsKey(brand)) {
                continue;
            }
            SkinTextureData bundled = textureFromBundledPng(brand);
            if (!bundled.textureValue().isEmpty()) {
                skins.put(brand, bundled);
            }
        }

        Path customConfigPath = Path.of("config", "capitalcraft", "ceo_skins.json");
        if (!Files.exists(customConfigPath)) {
            return skins;
        }

        try (Reader reader = Files.newBufferedReader(customConfigPath, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            for (String brand : BRANDS) {
                JsonElement raw = root.get(brand);
                if (raw == null || !raw.isJsonObject()) {
                    continue;
                }

                SkinTextureData parsed = parseSkinTextureData(raw.getAsJsonObject(), brand);
                if (!parsed.textureValue().isEmpty()) {
                    skins.put(brand, parsed);
                }
            }
        } catch (Exception ignored) {
        }
        return skins;
    }

    private static SkinTextureData parseSkinTextureData(JsonObject data, String brand) {
        String textureValue = readString(data, "textureValue");
        if (!textureValue.isEmpty()) {
            return new SkinTextureData(textureValue, readString(data, "textureSignature"));
        }

        String skinUrl = readString(data, "skinUrl");
        if (skinUrl.isEmpty()) {
            String pngPath = readString(data, "skinPngPath");
            if (!pngPath.isEmpty()) {
                Path resolvedPath = Path.of(pngPath);
                if (!resolvedPath.isAbsolute()) {
                    resolvedPath = Path.of("config", "capitalcraft").resolve(pngPath);
                }
                skinUrl = dataUrlFromPng(resolvedPath);
            }
        }

        if (skinUrl.isEmpty()) {
            return SkinTextureData.EMPTY;
        }

        String model = readString(data, "model");
        String generatedValue = buildTextureValueFromUrl(skinUrl, brand, model);
        return new SkinTextureData(generatedValue, "");
    }

    private static SkinTextureData textureFromBundledPng(String brand) {
        String resourcePath = "/assets/capitalcraft/textures/entity/ceo/" + brand + "_skin.png";
        return textureFromBundledPngPath(resourcePath, brand);
    }

    private static SkinTextureData textureFromBundledPngPath(String resourcePath, String profileBrand) {
        try (InputStream stream = CeoCommand.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return SkinTextureData.EMPTY;
            }

            String encodedPng = Base64.getEncoder().encodeToString(stream.readAllBytes());
            String skinUrl = "data:image/png;base64," + encodedPng;
            return new SkinTextureData(buildTextureValueFromUrl(skinUrl, profileBrand, ""), "");
        } catch (IOException ignored) {
            return SkinTextureData.EMPTY;
        }
    }

    private static String dataUrlFromPng(Path pngPath) {
        if (!Files.exists(pngPath)) {
            return "";
        }
        try {
            String encodedPng = Base64.getEncoder().encodeToString(Files.readAllBytes(pngPath));
            return "data:image/png;base64," + encodedPng;
        } catch (IOException ignored) {
            return "";
        }
    }

    private static String buildTextureValueFromUrl(String skinUrl, String brand, String model) {
        String modelEntry = Objects.equals(model, "slim") ? "\"metadata\":{\"model\":\"slim\"}," : "";
        String payload = "{" +
                "\"timestamp\":" + System.currentTimeMillis() + "," +
                "\"profileId\":\"" + UUID.nameUUIDFromBytes(("capitalcraft-" + brand).getBytes(StandardCharsets.UTF_8)).toString().replace("-", "") + "\"," +
                "\"profileName\":\"" + brand + "\"," +
                "\"textures\":{\"SKIN\":{" + modelEntry + "\"url\":\"" + skinUrl + "\"}}" +
                "}";
        return Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    private static String readString(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element == null || element.isJsonNull() ? "" : element.getAsString();
    }

    private static String dialogueForBrand(String brand) {
        return switch (brand) {
            case "apple" -> "Apple CEO: I love the new iphone!";
            case "alpahbet" -> "Alphabet CEO: Google any questions!";
            case "microsoft" -> "Microsoft CEO: You love Excel and we love it too.";
            case "mark" -> "Meta CEO: Mark Zuckerberg you know who that is?";
            case "jensen" -> "NVIDIA CEO: We do the best chips ofcourse.";
            default -> "CEO: We are building the future.";
        };
    }

    private record SkinTextureData(String textureValue, String textureSignature) {
        private static final SkinTextureData EMPTY = new SkinTextureData("", "");
    }
}
