package com.capitalcraft.capitalcraft.block;

import com.capitalcraft.capitalcraft.Capitalcraft;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class CapitalcraftBlocks {

        private static final Identifier TRADING_TERMINAL_ID = Identifier.of(Capitalcraft.MOD_ID, "trading_terminal");
        private static final Identifier ATM_ID = Identifier.of(Capitalcraft.MOD_ID, "atm");
        private static final RegistryKey<Block> TRADING_TERMINAL_KEY = RegistryKey.of(RegistryKeys.BLOCK, TRADING_TERMINAL_ID);
        private static final RegistryKey<Item> TRADING_TERMINAL_ITEM_KEY = RegistryKey.of(RegistryKeys.ITEM, TRADING_TERMINAL_ID);
        private static final RegistryKey<Block> ATM_KEY = RegistryKey.of(RegistryKeys.BLOCK, ATM_ID);
        private static final RegistryKey<Item> ATM_ITEM_KEY = RegistryKey.of(RegistryKeys.ITEM, ATM_ID);

    public static final Block TRADING_TERMINAL = Registry.register(
            Registries.BLOCK,
            TRADING_TERMINAL_ID,
            new TradingTerminalBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).strength(3.0f).requiresTool().registryKey(TRADING_TERMINAL_KEY))
    );

    public static final Item TRADING_TERMINAL_ITEM = Registry.register(
            Registries.ITEM,
            TRADING_TERMINAL_ID,
            new BlockItem(TRADING_TERMINAL, new Item.Settings().registryKey(TRADING_TERMINAL_ITEM_KEY))
    );

    public static final Block ATM = Registry.register(
            Registries.BLOCK,
            ATM_ID,
            new AtmBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).strength(3.5f).requiresTool().registryKey(ATM_KEY))
    );

    public static final Item ATM_ITEM = Registry.register(
            Registries.ITEM,
            ATM_ID,
            new BlockItem(ATM, new Item.Settings().registryKey(ATM_ITEM_KEY))
    );

    private CapitalcraftBlocks() {
    }

    public static void init() {
    }
}