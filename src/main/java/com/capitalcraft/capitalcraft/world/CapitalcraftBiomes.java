package com.capitalcraft.capitalcraft.world;

import com.capitalcraft.capitalcraft.Capitalcraft;
import com.mojang.datafixers.util.Pair;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

public final class CapitalcraftBiomes {

    public static final RegistryKey<Biome> SILICON_VALLEY_KEY = RegistryKey.of(
            RegistryKeys.BIOME,
            Identifier.of(Capitalcraft.MOD_ID, "silicon_valley")
    );

    public static final Identifier SILICON_VALLEY_PRESET_ID = Identifier.of(Capitalcraft.MOD_ID, "silicon_valley_overworld");

    private CapitalcraftBiomes() {
    }

    public static void init() {
        registerSiliconValleyPreset();
    }

    @SuppressWarnings("unchecked")
    private static void registerSiliconValleyPreset() {
        try {
            Field field = MultiNoiseBiomeSourceParameterList.Preset.class.getDeclaredField("BY_IDENTIFIER");
            field.setAccessible(true);

            Map<Identifier, MultiNoiseBiomeSourceParameterList.Preset> presets = (Map<Identifier, MultiNoiseBiomeSourceParameterList.Preset>) field.get(null);
            if (presets.containsKey(SILICON_VALLEY_PRESET_ID)) {
                return;
            }

            Class<?> biomeSourceFunctionClass = Class.forName("net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList$Preset$BiomeSourceFunction");
            Method biomeSourceFunctionMethod = MultiNoiseBiomeSourceParameterList.Preset.class.getDeclaredMethod("biomeSourceFunction");
            Object overworldFunction = biomeSourceFunctionMethod.invoke(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD);

            InvocationHandler handler = (proxy, method, args) -> {
                if (!"apply".equals(method.getName()) || args == null || args.length != 1) {
                    return method.invoke(overworldFunction, args);
                }

                @SuppressWarnings("unchecked")
                java.util.function.Function<RegistryKey<Biome>, Object> biomeLookup = (java.util.function.Function<RegistryKey<Biome>, Object>) args[0];
                @SuppressWarnings("unchecked")
                MultiNoiseUtil.Entries<Object> overworldEntries = (MultiNoiseUtil.Entries<Object>) biomeSourceFunctionClass.getMethod("apply", java.util.function.Function.class).invoke(overworldFunction, biomeLookup);
                List<Pair<MultiNoiseUtil.NoiseHypercube, Object>> entries = new ArrayList<>(overworldEntries.getEntries());
                entries.add(Pair.of(
                        MultiNoiseUtil.createNoiseHypercube(0.85f, 0.15f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
                        biomeLookup.apply(SILICON_VALLEY_KEY)
                ));
                return new MultiNoiseUtil.Entries<>(entries);
            };

            Object biomeSourceFunction = Proxy.newProxyInstance(
                    CapitalcraftBiomes.class.getClassLoader(),
                    new Class<?>[] { biomeSourceFunctionClass },
                    handler
            );

            @SuppressWarnings("unchecked")
            Constructor<MultiNoiseBiomeSourceParameterList.Preset> constructor = (Constructor<MultiNoiseBiomeSourceParameterList.Preset>) MultiNoiseBiomeSourceParameterList.Preset.class.getDeclaredConstructor(Identifier.class, biomeSourceFunctionClass);
            constructor.setAccessible(true);

            MultiNoiseBiomeSourceParameterList.Preset customPreset = constructor.newInstance(SILICON_VALLEY_PRESET_ID, biomeSourceFunction);

            presets.put(SILICON_VALLEY_PRESET_ID, customPreset);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to register CapitalCraft multi-noise preset", exception);
        }
    }
}
