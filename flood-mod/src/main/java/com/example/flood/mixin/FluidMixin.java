package com.example.flood.mixin;

import net.minecraft.fluid.FlowableFluid;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Миксин, ОТМЕНЯЮЩИЙ испарение воды в Незере (требование №4).
 *
 * В ваниле это делает метод FlowableFluid#isInNether(RegistryKey<Dimension>):
 * он возвращает true, если измерение — DIMENSION_NETHER_TYPE_ID, и тогда
 * onRandomTick / tryToFlow удаляют воду («вода мгновенно превращается в пар»).
 *
 * Мы полностью переопределяем этот метод (@Inject at HEAD + cir.setReturnValue(false)),
 * чтобы проверка «мы в Незере?» всегда отвечала «нет». Вода в Незере ведёт себя
 * как обычная вода: не исчезает при тике.
 *
 * ВАЖНО (честное предупреждение): имя метода isInNether и его сигнатура
 * актуальны для 1.20.x–1.21.x по Yarn-маппингам. Если при сборке компилятор
 * не найдёт метод с таким именем, откройте класс
 * net/minecraft/fluid/FlowableFluid.java в декомпилированных исходниках
 * (выполните ./gradlew genSources, затем откройте build/loom-cache) и
 * поправьте имя/сигнатуру в @Inject ниже. Это единственное место, которое
 * теоретически может потребовать адаптации.
 */
@Mixin(FlowableFluid.class)
public abstract class FluidMixin {

    /**
     * Подменяем результат isInNether на ALWAYS false.
     * method = "isInNether" — имя по Yarn; at = HEAD — встаём в начало метода;
     * cancellable = true — мы имеем право оборвать оригинальную логику.
     */
    @Inject(method = "isInNether", at = @At("HEAD"), cancellable = true)
    private static void flood$disableNetherEvaporation(
            RegistryKey<?> dimensionKey,
            CallbackInfoReturnable<Boolean> cir) {
        // Всегда отвечаем «это НЕ Незер», независимо от измерения.
        // Следовательно, весь код вида `if (isInNether(...)) remove water`
        // никогда не удалит воду — испарение отключено в Overworld, Незере и Энде.
        cir.setReturnValue(Boolean.FALSE);
    }
}
