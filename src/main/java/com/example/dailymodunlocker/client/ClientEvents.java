package com.example.dailymodunlocker.client;

import com.example.dailymodunlocker.command.OpenUnlockGuiCommand;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "dailymodunlocker", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        OpenUnlockGuiCommand.register(event.getDispatcher());
    }
}
