package de.oliver.fancysitula.versions.v1_21_6.packets;

import de.oliver.fancysitula.api.entities.FS_RealPlayer;
import de.oliver.fancysitula.api.packets.FS_ClientboundPlayerInfoUpdatePacket;
import de.oliver.fancysitula.versions.v1_21_6.utils.GameProfileImpl;
import de.oliver.fancysitula.versions.v1_21_6.utils.VanillaPlayerAdapter;
import io.papermc.paper.adventure.PaperAdventure;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class ClientboundPlayerInfoUpdatePacketImpl extends FS_ClientboundPlayerInfoUpdatePacket {
   public ClientboundPlayerInfoUpdatePacketImpl(
      EnumSet<FS_ClientboundPlayerInfoUpdatePacket.Action> actions, List<FS_ClientboundPlayerInfoUpdatePacket.Entry> entries
   ) {
      super(actions, entries);
   }

   @Override
   public Object createPacket() {
      EnumSet<net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action> vanillaActions = EnumSet.noneOf(
         net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action.class
      );

      for (FS_ClientboundPlayerInfoUpdatePacket.Action action : this.actions) {
         vanillaActions.add(net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action.valueOf(action.name()));
      }

      List<net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry> entries = new ArrayList<>();

      for (FS_ClientboundPlayerInfoUpdatePacket.Entry entry : this.entries) {
         entries.add(
            new net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry(
               entry.uuid(),
               GameProfileImpl.asVanilla(entry.profile()),
               entry.listed(),
               entry.latency(),
               GameType.byId(entry.gameMode().getId()),
               PaperAdventure.asVanilla(entry.displayName()),
               true,
               -1,
               null
            )
         );
      }

      return new ClientboundPlayerInfoUpdatePacket(vanillaActions, entries);
   }

   @Override
   public void sendPacketTo(FS_RealPlayer player) {
      ClientboundPlayerInfoUpdatePacket packet = (ClientboundPlayerInfoUpdatePacket)this.createPacket();
      ServerPlayer vanillaPlayer = VanillaPlayerAdapter.asVanilla(player.getBukkitPlayer());
      vanillaPlayer.connection.send(packet);
   }
}
