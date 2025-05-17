package com.example.dailymodunlocker.gui;

import com.example.dailymodunlocker.ModUnlockManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ModUnlockScreen extends Screen {

    private final List<IModInfo> displayedMods;
    private final ModUnlockManager manager;
    private final Set<String> hiddenMods;

    public ModUnlockScreen() {
        super(Component.literal("MOD解禁状況"));

        Level level = Minecraft.getInstance().level;
        MinecraftServer server = (level != null) ? level.getServer() : null;

        if (server != null) {
            this.manager = ModUnlockManager.getInstance(server);
            this.hiddenMods = ModUnlockManager.loadAlwaysUnlockedMods();
        } else {
            this.manager = null;
            this.hiddenMods = Collections.emptySet();
        }

        this.displayedMods = ModList.get().getMods().stream()
                .filter(container -> !hiddenMods.contains(container.getModId()))
                .sorted((a, b) -> a.getModId().compareToIgnoreCase(b.getModId()))
                .collect(Collectors.toList());
    }

    @Override
    protected void init() {
        // UI要素が必要な場合はここに追加可能です
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        int y = 30;
        for (IModInfo container : displayedMods) {
            if (y > this.height - 20)
                break;

            String modid = container.getModId();
            boolean unlocked = (manager != null) && manager.isUnlocked(modid);
            String display = (unlocked ? "✓ " : "✗ ") + modid;
            graphics.drawString(this.font, display, 20, y, unlocked ? 0x00FF00 : 0xFF5555);
            y += 10;
        }
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
