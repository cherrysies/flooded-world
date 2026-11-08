package com.example.flood;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Главный класс мода.
 *
 * Вся реальная работа выполняется миксинами (см. пакет com.example.flood.mixin):
 *  - ServerWorldMixin — заменяет воздух на воду при генерации каждого чанка;
 *  - FluidMixin       — отключает испарение воды в Незере.
 *
 * Этот класс нужен только как точка входа (entrypoint) и для лога,
 * чтобы убедиться, что мод вообще загрузился. Он работает и на клиенте,
 * и на выделенном сервере (side "*" в fabric.mod.json).
 */
public class FloodMod implements ModInitializer {

    public static final String MOD_ID = "flood";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Никаких слушателей регистрировать не нужно: замена блоков происходит
        // внутри генерации чанков через миксин, а логика спавна игрока остаётся
        // полностью ванильной.
        LOGGER.info("[Total Flood] Мод загружен. Весь воздух при генерации чанков будет заменён водой.");
    }
}
