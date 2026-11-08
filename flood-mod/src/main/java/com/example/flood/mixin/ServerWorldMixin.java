package com.example.flood.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Миксин «тотального затопления».
 *
 * Цепляемся за ServerWorld.createChunk(ChunkRegion, ChunkGenerator) — именно
 * этот метод вызывается ПОСЛЕ того, как генератор чанков (шейп, биомы, населённые
 * пункты, структуры, decoration-steps) полностью завершил работу, но ДО того,
 * как чанк попадёт в мир. Поэтому:
 *   - мы НЕ трогаем сами структуры (они уже сгенерированы);
 *   - заполняем ВОДОЙ любой оставшийся воздух — внутри зданий, пещер,
 *     под островами Энда и в пустоте Незера;
 *   - работает для ЛЮБОГО измерения, потому что все измерения проходят
 *     через этот единственный метод.
 *
 * Ключевая оптимизация (требование №6): используем
 * Chunk.setBlockState(pos, state, Flag.UPDATE_CLIENTS_ONLY).
 * Этот флаг соответствует «флагу 2» из старых версий игры: блок просто
 * записывается в данные чанка, НИКАКИХ обновлений соседей, НИКАКИХ тиков
 * жидкостей/гравитации (песок, гравий) не запускается. Чанк ещё не
 * прогружен, поэтому нам не нужно даже уведомление клиента — достаточно
 * UPDATE_CLIENTS_ONLY (или даже NONE, если хотите максимальную скорость;
 * UPDATE_CLIENTS_ONLY безопаснее, чтобы клиент синхронизировался).
 */
@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    /**
     * Инжект в HEAD метода createChunk: генерация чанка уже завершена,
     * теперь превращаем каждый AIR / CAVE_AIR / VOID_AIR в воду.
     */
    @Inject(method = "createChunk", at = @At("HEAD"))
    private void flood$replaceAirWithWater(ChunkRegion region, ChunkGenerator generator,
                                           CallbackInfoReturnable<Chunk> cir) {
        // Достаём готовый чанк из region — это тот самый чанк, который сейчас
        // будет зарегистрирован в мире.
        Chunk chunk = region.getChunk(generator.getSpawnPos(region));

        // Полный диапазон высот текущего измерения (например, для Overworld
        // от -64 до 319, для Незера 0..127). Берём из типа измерений мира,
        // чтобы не хардкодить значения.
        final int minY = chunk.getBottomY();
        final int maxY = chunk.getWorld().getTopY(); // верхняя граница ИСКЛЮЧИТЕЛЬНО (exclusive)

        // Перебираем блоки по чанку. Используем BlockPos.Mutable для экономии GC.
        BlockPos.Mutable cursor = new BlockPos.Mutable();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    cursor.set(chunk.getPos().getStartX() + x, y, chunk.getPos().getStartZ() + z);

                    // Состояние блока без обновления соседей (быстрый путь чтения)
                    var state = chunk.getBlockState(cursor);

                    // Заменяем ТОЛЬКО три вида «пустоты». Все остальные блоки
                    // (камни, руды, блоки структур, лава и т.д.) не трогаем.
                    if (state.isAir()) { // true для AIR, CAVE_AIR и VOID_AIR
                        // Флаг UPDATE_CLIENTS ONLY = «только запись, никаких соседей/тиков».
                        // Это аналог флага 2 из setBlockOldFlags. Ноль побочных эффектов.
                        chunk.setBlockState(cursor, Blocks.WATER.getDefaultState(),
                                Chunk.SetBlockType.UPDATE_CLIENTS_ONLY);
                    }
                }
            }
        }
    }
}
