package com.capitalcraft.capitalcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ServerWorldAccess;

import java.util.Locale;

public final class CeoCommand {

    private static final String CEO_TAG = "capitalcraft_ceo";
    private static final String CEO_BRAND_TAG_PREFIX = "capitalcraft_ceo_brand_";

    private static final String[] BRANDS = {
            "apple",
            "alpahbet",
            "microsoft",
            "mark",
            "jensen"
    };

    private CeoCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() || hand != Hand.MAIN_HAND || !(entity instanceof ZombieEntity zombie)) {
                return ActionResult.PASS;
            }

            if (!zombie.getCommandTags().contains(CEO_TAG)) {
                return ActionResult.PASS;
            }

            String brand = getBrandFromTags(zombie);
            player.sendMessage(Text.literal(dialogueForBrand(brand)), false);
            return ActionResult.SUCCESS;
        });
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("spawnceo")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> spawnCeo(context.getSource())));

        dispatcher.register(CommandManager.literal("spawn")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("ceo")
                        .executes(context -> spawnCeo(context.getSource()))));
    }

    private static int spawnCeo(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        Vec3d forward = player.getRotationVec(1.0f).multiply(2.5);
        Vec3d spawnPos = player.getPos().add(forward.x, 0.0, forward.z);

        ZombieEntity ceo = EntityType.ZOMBIE.create(source.getWorld(), SpawnReason.COMMAND);
        if (ceo == null) {
            source.sendError(Text.literal("Failed to spawn CEO."));
            return 0;
        }

        String brand = BRANDS[source.getWorld().getRandom().nextInt(BRANDS.length)];

        ceo.refreshPositionAndAngles(spawnPos.x, spawnPos.y, spawnPos.z, player.getYaw(), 0.0f);
        ceo.setCustomName(Text.literal(displayNameForBrand(brand)));
        ceo.setCustomNameVisible(true);
        ceo.setPersistent();
        ceo.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        ceo.setCanPickUpLoot(false);
        ceo.setBaby(false);
        ceo.addCommandTag(CEO_TAG);
        ceo.addCommandTag(CEO_BRAND_TAG_PREFIX + brand);

        ((ServerWorldAccess) source.getWorld()).spawnEntity(ceo);
        source.sendFeedback(() -> Text.literal("Spawned " + displayNameForBrand(brand) + " at your position."), true);
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

    private static String getBrandFromTags(ZombieEntity zombie) {
        for (String tag : zombie.getCommandTags()) {
            if (tag.startsWith(CEO_BRAND_TAG_PREFIX)) {
                return tag.substring(CEO_BRAND_TAG_PREFIX.length()).toLowerCase(Locale.ROOT);
            }
        }
        return "";
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
}
