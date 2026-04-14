package com.capitalcraft.capitalcraft.item;

import com.capitalcraft.capitalcraft.Capitalcraft;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class CapitalcraftItems {

    private static final Identifier MONEY_VOUCHER_ID = Identifier.of(Capitalcraft.MOD_ID, "money_voucher");
    private static final RegistryKey<Item> MONEY_VOUCHER_KEY = RegistryKey.of(RegistryKeys.ITEM, MONEY_VOUCHER_ID);

    public static final Item MONEY_VOUCHER = Registry.register(
            Registries.ITEM,
            MONEY_VOUCHER_ID,
            new Item(new Item.Settings().maxCount(1).registryKey(MONEY_VOUCHER_KEY))
    );

    private CapitalcraftItems() {
    }

    public static void init() {
    }
}
