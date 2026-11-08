package com.example.flood;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
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
        ChunkPos chunkPos = chunk.getPos();
        int minX = chunkPos.getStartX();
        int minZ = chunkPos.getStartZ();
        int maxY = world.getTopY();
        int minY = world.getBottomY();

        // Проходим по всем блокам в чанке
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    BlockPos pos = new BlockPos(minX + x, y, minZ + z);
                    
                    // Если блок является любым видом воздуха, заменяем его на воду
                    if (chunk.getBlockState(pos).isAir()) {
                        // Флаг '2' (Block.NOTIFY_LISTENERS) обновляет клиент, 
                        // но НЕ запускает пересчет соседей. Это предотвращает падение песка/гравия и лаги.
                        chunk.setBlockState(pos, Blocks.WATER.getDefaultState(), false);
                    }
                }
            }
        }
    }
}