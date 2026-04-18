package com.capitalcraft.capitalcraft.item;

import com.capitalcraft.capitalcraft.Capitalcraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CapitalcraftItems {

    private static final Pattern VOUCHER_AMOUNT_PATTERN = Pattern.compile("(\\d+)");

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

    public static ItemStack createMoneyVoucher(int amount) {
        ItemStack stack = new ItemStack(MONEY_VOUCHER);
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME, Text.literal("Euro Voucher " + amount));
        return stack;
    }

    public static boolean isMoneyVoucher(ItemStack stack) {
        return !stack.isEmpty() && stack.isOf(MONEY_VOUCHER);
    }

    public static int getVoucherAmount(ItemStack stack) {
        if (!isMoneyVoucher(stack)) {
            return 0;
        }

        Matcher matcher = VOUCHER_AMOUNT_PATTERN.matcher(stack.getName().getString());
        if (!matcher.find()) {
            return 0;
        }

        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
