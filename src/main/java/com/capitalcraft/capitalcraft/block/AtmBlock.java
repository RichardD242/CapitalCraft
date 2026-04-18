package com.capitalcraft.capitalcraft.block;

import com.capitalcraft.capitalcraft.item.CapitalcraftItems;
import com.capitalcraft.capitalcraft.market.TradingLedger;
import com.capitalcraft.capitalcraft.screen.AtmScreenFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerData;

import java.util.Optional;

public class AtmBlock extends Block {

    public AtmBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        if (player instanceof ServerPlayerEntity serverPlayer && state.isOf(this)) {
            serverPlayer.openHandledScreen(new AtmScreenFactory(pos));
            TradingLedger.syncWallet(serverPlayer);
            return ActionResult.CONSUME;
        }

        return ActionResult.PASS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, net.minecraft.entity.LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        if (world.isClient) {
            return;
        }

        world.getEntitiesByClass(VillagerEntity.class, new Box(pos).expand(4.0D), villager -> villager.isAlive() && !villager.isRemoved()).stream()
                .findFirst()
                .ifPresent(this::applyMerchantTrades);
    }

    private void applyMerchantTrades(VillagerEntity villager) {
        VillagerData data = villager.getVillagerData().withLevel(5);
        villager.setVillagerData(data);
        villager.setBreedingAge(0);
        villager.setPersistent();
        villager.setAiDisabled(true);

        TradeOfferList offers = villager.getOffers();
        offers.clear();

        offers.add(createOffer(new ItemStack(CapitalcraftItems.MONEY_VOUCHER), Optional.empty(), new ItemStack(Items.DIAMOND_BLOCK, 64), 12, 20));
        offers.add(createOffer(new ItemStack(CapitalcraftItems.MONEY_VOUCHER), Optional.of(new ItemStack(CapitalcraftItems.MONEY_VOUCHER)), new ItemStack(Items.NETHERITE_BLOCK, 32), 6, 40));
        offers.add(createOffer(new ItemStack(CapitalcraftItems.MONEY_VOUCHER), Optional.empty(), new ItemStack(Items.EMERALD_BLOCK, 64), 16, 12));
        offers.add(createOffer(new ItemStack(CapitalcraftItems.MONEY_VOUCHER), Optional.of(new ItemStack(Items.DIAMOND, 64)), new ItemStack(Items.ELYTRA), 2, 100));
        offers.add(createOffer(new ItemStack(CapitalcraftItems.MONEY_VOUCHER), Optional.empty(), new ItemStack(Items.TOTEM_OF_UNDYING, 8), 8, 80));

        villager.setOffers(offers);
        villager.setCustomName(net.minecraft.text.Text.literal("ATM Dealer"));
        villager.setCustomNameVisible(true);
    }

    private TradeOffer createOffer(ItemStack firstBuy, Optional<ItemStack> secondBuy, ItemStack sell, int maxUses, int experience) {
        TradedItem primary = new TradedItem(firstBuy.getItem(), firstBuy.getCount());
        Optional<TradedItem> secondary = secondBuy
                .filter(stack -> !stack.isEmpty())
                .map(stack -> new TradedItem(stack.getItem(), stack.getCount()));
        return new TradeOffer(primary, secondary, sell, maxUses, experience, 0.05f);
    }
}
