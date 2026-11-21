package de.oliver.fancynpcs.v26_1_1;

import java.util.function.Predicate;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerEntity.Synchronizer;

public class FakeSynchronizer implements Synchronizer {
   public static final FakeSynchronizer INSTANCE = new FakeSynchronizer();

   private FakeSynchronizer() {
   }

   public void sendToTrackingPlayers(Packet<? super ClientGamePacketListener> packet) {
   }

   public void sendToTrackingPlayersAndSelf(Packet<? super ClientGamePacketListener> packet) {
   }

   public void sendToTrackingPlayersFiltered(Packet<? super ClientGamePacketListener> packet, Predicate<ServerPlayer> predicate) {
   }
}
