package com.example.dailymodunlocker;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public final class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(DailyModUnlocker.modid, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private ModNetwork() {
    }

    public static void registerMessages() {
        CHANNEL.registerMessage(0, SyncUnlockState.class,
                SyncUnlockState::encode,
                SyncUnlockState::decode,
                SyncUnlockState::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void syncUnlockState(ServerPlayer player, Set<String> unlockedMods) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncUnlockState(unlockedMods));
    }

    public static void broadcastUnlockState(MinecraftServer server, Set<String> unlockedMods) {
        SyncUnlockState message = new SyncUnlockState(unlockedMods);
        server.getPlayerList().getPlayers().forEach(player ->
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message));
    }

    private static final class SyncUnlockState {
        private final Set<String> unlockedMods;

        private SyncUnlockState(Set<String> unlockedMods) {
            this.unlockedMods = Set.copyOf(unlockedMods);
        }

        private static void encode(SyncUnlockState message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.unlockedMods.size());
            message.unlockedMods.forEach(buffer::writeUtf);
        }

        private static SyncUnlockState decode(FriendlyByteBuf buffer) {
            int count = buffer.readVarInt();
            if (count < 0 || count > 10000) {
                throw new IllegalArgumentException("Invalid unlocked mod count: " + count);
            }

            Set<String> unlockedMods = new HashSet<>();
            for (int i = 0; i < count; i++) {
                unlockedMods.add(buffer.readUtf(256));
            }
            return new SyncUnlockState(unlockedMods);
        }

        private static void handle(SyncUnlockState message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                    Dist.CLIENT,
                    () -> () -> ClientUnlockState.setUnlockedMods(message.unlockedMods)));
            context.setPacketHandled(true);
        }
    }
}
