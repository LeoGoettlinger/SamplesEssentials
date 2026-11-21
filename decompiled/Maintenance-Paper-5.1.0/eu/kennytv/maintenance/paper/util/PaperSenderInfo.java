package eu.kennytv.maintenance.paper.util;

import eu.kennytv.maintenance.core.util.SenderInfo;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class PaperSenderInfo implements SenderInfo {
   private final CommandSender sender;

   public PaperSenderInfo(CommandSender sender) {
      this.sender = sender;
   }

   @Override
   public UUID uuid() {
      return this.sender instanceof Player player ? player.getUniqueId() : null;
   }

   @Override
   public String name() {
      return this.sender.getName();
   }

   @Override
   public boolean hasPermission(String permission) {
      return this.sender.hasPermission(permission);
   }

   @Override
   public void send(Component component) {
      this.sender.sendMessage(component);
   }

   @Override
   public boolean isPlayer() {
      return this.sender instanceof Player;
   }
}
