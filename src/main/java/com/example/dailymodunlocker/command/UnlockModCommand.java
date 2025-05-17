package com.example.dailymodunlocker.command;

import com.example.dailymodunlocker.ModUnlockManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class UnlockModCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("unlockmod")
                        .requires(source -> source.hasPermission(2)) // OPレベル2以上
                        .then(Commands.argument("modid", StringArgumentType.word())
                                .executes(context -> {
                                    String modid = StringArgumentType.getString(context, "modid");
                                    CommandSourceStack source = context.getSource();

                                    ModUnlockManager manager = ModUnlockManager.getInstance(source.getServer());

                                    if (manager.isUnlocked(modid)) {
                                        source.sendSuccess(() -> Component.literal("MOD '" + modid + "' はすでに解禁されています。"), false);
                                        return 0;
                                    }

                                    manager.unlockMod(modid);
                                    source.sendSuccess(() -> Component.literal("MOD '" + modid + "' を解禁しました。"), true);

                                    if (source.getEntity() instanceof ServerPlayer player) {
                                        player.sendSystemMessage(Component.literal("あなたは MOD '" + modid + "' を手動で解禁しました。"));
                                    }

                                    return 1;
                                })));
    }
}
