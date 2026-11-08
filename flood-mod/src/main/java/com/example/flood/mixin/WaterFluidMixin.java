package com.example.flood.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidBlock.class)
public class WaterFluidMixin {
    
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void preventWaterEvaporation(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        // Проверяем, что блок, который пытается обновиться (испариться), это именно вода
        if (state.getFluidState().getFluid() == Fluids.WATER) {
            // Отменяем стандартное поведение, чтобы вода не испарялась в Незере
            ci.cancel();
        }
    }
}