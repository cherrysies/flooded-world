package com.example.flood;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FloodMod implements ModInitializer {
    public static final String MOD_ID = "flood";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Мод 'Затопленный мир' инициализирован!");

        // Регистрируем событие, которое срабатывает сразу после генерации каждого чанка
        ServerChunkEvents.CHUNK_GENERATE.register(this::floodChunk);
    }

    private void floodChunk(ServerWorld world, WorldChunk chunk) {
        int minY = world.getBottomY();
        int maxY = world.getTopY();

        // Проходим по высоте мира
        for (int y = minY; y < maxY; y++) {
            // Получаем индекс секции чанка для текущей высоты Y
            int sectionIndex = chunk.getSectionIndex(y);
            ChunkSection section = chunk.getSection(sectionIndex);
            
            // Локальная координата Y внутри секции (всегда от 0 до 15)
            int localY = y & 15;

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    // Проверяем, является ли блок воздухом
                    if (section.getBlockState(x, localY, z).isAir()) {
                        // Прямая запись в секцию чанка. 
                        // Параметр false отключает любые обновления соседей и колбэки блок-сущностей.
                        // Это предотвращает цепную реакцию и краши при сохранении.
                        section.setBlockState(x, localY, z, Blocks.WATER.getDefaultState(), false);
                    }
                }
            }
        }
    }
}