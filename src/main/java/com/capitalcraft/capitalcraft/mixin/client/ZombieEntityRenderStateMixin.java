package com.capitalcraft.capitalcraft.mixin.client;

import com.capitalcraft.capitalcraft.client.CeoZombieRenderStateAccessor;
import net.minecraft.client.render.entity.state.ZombieEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ZombieEntityRenderState.class)
public class ZombieEntityRenderStateMixin implements CeoZombieRenderStateAccessor {

    @Unique
    private String capitalcraft$brand = "";

    @Override
    public void capitalcraft$setBrand(String brand) {
        this.capitalcraft$brand = brand;
    }

    @Override
    public String capitalcraft$getBrand() {
        return this.capitalcraft$brand;
    }
}
