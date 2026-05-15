package com.capitalcraft.capitalcraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class safeblockentity extends BlockEntity implements NamedScreenHandlerFactory {

    private final SimpleInventory inventory = new SimpleInventory(27);
    private String pin = "";
    private boolean locked = false;

    public safeblockentity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SAFE_BLOCK_ENTITY, pos, state);
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
        markDirty();
    }

    public boolean hasPin() {
        return !pin.isEmpty();
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        markDirty();
    }

    public boolean checkPin(String input) {
        return pin.equals(input);
    }

    public SimpleInventory getInventory() {
        return inventory;
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putString("Pin", pin);
        view.putBoolean("Locked", locked);

        NbtCompound itemsTag = new NbtCompound();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                NbtCompound stackTag = new NbtCompound();
                stackTag.put("Stack", ItemStack.CODEC, stack);
                itemsTag.put(Integer.toString(i), stackTag);
            }
        }
        view.put("Items", NbtCompound.CODEC, itemsTag);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        pin = view.getString("Pin", "");
        locked = view.getBoolean("Locked", false);

        for (int i = 0; i < inventory.size(); i++) {
            inventory.setStack(i, ItemStack.EMPTY);
        }

        NbtCompound itemsTag = view.read("Items", NbtCompound.CODEC).orElse(new NbtCompound());
        for (String key : itemsTag.getKeys()) {
            int slot;
            try {
                slot = Integer.parseInt(key);
            } catch (NumberFormatException ignored) {
                continue;
            }

            if (slot >= 0 && slot < inventory.size()) {
                NbtCompound stackTag = itemsTag.getCompoundOrEmpty(key);
                inventory.setStack(slot, stackTag.get("Stack", ItemStack.CODEC).orElse(ItemStack.EMPTY));
            }
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Safe");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, inventory);
    }

}