package com.example.dailymodunlocker.event;

import com.example.dailymodunlocker.ModUnlockManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.time.LocalDate;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DailyTickEventHandler {

    private static LocalDate lastCheckedDate = null;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        var server = event.getServer();
        if (server == null) return;

        LocalDate currentDate = LocalDate.now();

        if (lastCheckedDate == null) {
            lastCheckedDate = currentDate;
            return;
        }

        if (!currentDate.equals(lastCheckedDate)) {
            // 日付が変わった（現実世界の新しい日になった）
            lastCheckedDate = currentDate;

            var manager = ModUnlockManager.getInstance(server);
            manager.tryUnlockNewMod(server);
        }
    }
}
