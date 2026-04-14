package com.capitalcraft.capitalcraft.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class SkyscraperFeature extends Feature<DefaultFeatureConfig> {

    private static final BlockState PRIMARY = Blocks.BLACK_CONCRETE.getDefaultState();
    private static final BlockState ACCENT = Blocks.GRAY_CONCRETE.getDefaultState();
    private static final BlockState WINDOW = Blocks.TINTED_GLASS.getDefaultState();

    public SkyscraperFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();
        BlockPos surface = world.getTopPosition(Heightmap.Type.WORLD_SURFACE_WG, context.getOrigin());

        if (surface.getY() < world.getBottomY() + 8) {
            return false;
        }

        int widthX = 6 + random.nextBetween(0, 8);
        int widthZ = 6 + random.nextBetween(0, 8);
        int height = 22 + random.nextBetween(0, 46);

        int minX = surface.getX() - (widthX / 2);
        int maxX = minX + widthX;
        int minZ = surface.getZ() - (widthZ / 2);
        int maxZ = minZ + widthZ;

        if (!isBuildableFootprint(world, minX, maxX, minZ, maxZ, surface.getY())) {
            return false;
        }

        int topY = surface.getY() + height;

        for (int y = surface.getY(); y <= topY; y++) {
            boolean roof = y == topY;
            boolean stripe = (y - surface.getY()) % 5 == 0;

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean wall = x == minX || x == maxX || z == minZ || z == maxZ;
                    if (!wall && !roof) {
                        continue;
                    }

                    BlockPos pos = new BlockPos(x, y, z);
                    if (!canReplace(world, pos)) {
                        continue;
                    }

                    BlockState state;
                    if (roof) {
                        state = ACCENT;
                    } else if (stripe || random.nextFloat() < 0.32f) {
                        state = WINDOW;
                    } else {
                        state = PRIMARY;
                    }

                    setBlockState(world, pos, state);
                }
            }
        }

        addSideTower(world, random, surface, minX, maxX, minZ, maxZ, height);
        return true;
    }

    private void addSideTower(StructureWorldAccess world, Random random, BlockPos base, int minX, int maxX, int minZ, int maxZ, int parentHeight) {
        if (random.nextFloat() > 0.7f) {
            return;
        }

        int w = 3 + random.nextBetween(0, 3);
        int d = 3 + random.nextBetween(0, 3);
        int h = Math.max(12, parentHeight - random.nextBetween(6, 18));

        int offsetX = random.nextBoolean() ? minX - w : maxX + 1;
        int offsetZ = random.nextBoolean() ? minZ - d : maxZ + 1;

        for (int y = base.getY(); y <= base.getY() + h; y++) {
            boolean roof = y == base.getY() + h;
            for (int x = offsetX; x <= offsetX + w; x++) {
                for (int z = offsetZ; z <= offsetZ + d; z++) {
                    boolean wall = x == offsetX || x == offsetX + w || z == offsetZ || z == offsetZ + d;
                    if (!wall && !roof) {
                        continue;
                    }

                    BlockPos pos = new BlockPos(x, y, z);
                    if (!canReplace(world, pos)) {
                        continue;
                    }

                    if (roof) {
                        setBlockState(world, pos, ACCENT);
                    } else if ((y - base.getY()) % 4 == 0) {
                        setBlockState(world, pos, WINDOW);
                    } else {
                        setBlockState(world, pos, PRIMARY);
                    }
                }
            }
        }
    }

    private boolean isBuildableFootprint(StructureWorldAccess world, int minX, int maxX, int minZ, int maxZ, int y) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos floorPos = new BlockPos(x, y - 1, z);
                if (world.getBlockState(floorPos).isAir()) {
                    return false;
                }
                BlockPos firstPos = new BlockPos(x, y, z);
                if (!canReplace(world, firstPos)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean canReplace(StructureWorldAccess world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        return state.isAir() || state.isReplaceable() || block == Blocks.SHORT_GRASS || block == Blocks.TALL_GRASS || block == Blocks.SNOW;
    }
}
