package ac.grim.grimac.platform.bukkit.sender;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.platform.api.sender.SenderFactory;
import ac.grim.grimac.platform.bukkit.GrimACBukkitLoaderPlugin;
import ac.grim.grimac.shaded.incendo.cloud.SenderMapper;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.kyori.adventure.platform.bukkit.BukkitAudiences;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class BukkitSenderFactory extends SenderFactory<CommandSender> implements SenderMapper<CommandSender, Sender> {
   private final BukkitAudiences audiences = BukkitAudiences.create(GrimACBukkitLoaderPlugin.LOADER);

   protected String getName(CommandSender sender) {
      return sender instanceof Player ? sender.getName() : "Console";
   }

   protected UUID getUniqueId(CommandSender sender) {
      return sender instanceof Player player ? player.getUniqueId() : Sender.CONSOLE_UUID;
   }

   protected void sendMessage(CommandSender sender, String message) {
      sender.sendMessage(message);
   }

   protected void sendMessage(CommandSender sender, Component message) {
      if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender) && !(sender instanceof RemoteConsoleCommandSender)) {
         GrimAPI.INSTANCE
            .getScheduler()
            .getGlobalRegionScheduler()
            .run(GrimAPI.INSTANCE.getGrimPlugin(), () -> this.audiences.sender(sender).sendMessage(message));
      } else {
         this.audiences.sender(sender).sendMessage(message);
      }
   }

   protected boolean hasPermission(CommandSender sender, String node) {
      return sender.hasPermission(node);
   }

   protected boolean hasPermission(CommandSender sender, String node, boolean defaultIfUnset) {
      return sender.hasPermission(new Permission(node, defaultIfUnset ? PermissionDefault.TRUE : PermissionDefault.FALSE));
   }

   protected void performCommand(CommandSender sender, String command) {
      Bukkit.dispatchCommand(sender, command);
   }

   protected boolean isConsole(CommandSender sender) {
      return sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender;
   }

   protected boolean isPlayer(CommandSender sender) {
      return sender instanceof Player;
   }

   @NotNull
   public Sender map(@NotNull CommandSender base) {
      return this.wrap(base);
   }

   @NotNull
   public CommandSender reverse(@NotNull Sender mapped) {
      return this.unwrap(mapped);
   }
}
