package eu.kennytv.maintenance.paper.util;

import eu.kennytv.maintenance.core.util.SenderInfo;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;

public final class PaperOfflinePlayerInfo implements SenderInfo {
   private final OfflinePlayer player;

   public PaperOfflinePlayerInfo(OfflinePlayer player) {
      this.player = player;
   }

   @Override
   public UUID uuid() {
      return this.player.getUniqueId();
   }

   @Override
   public String name() {
      return this.player.getName();
   }

   @Override
   public boolean hasPermission(String permission) {
      return false;
   }

   @Override
   public void send(Component component) {
   }

   @Override
   public boolean isPlayer() {
      return true;
   }
}
