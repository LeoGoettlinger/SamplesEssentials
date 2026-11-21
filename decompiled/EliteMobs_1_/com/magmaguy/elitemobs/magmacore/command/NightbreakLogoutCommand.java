package com.magmaguy.elitemobs.magmacore.command;

import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakAccount;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakLogoutMessages;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.function.Supplier;
import org.bukkit.plugin.java.JavaPlugin;

public class NightbreakLogoutCommand extends AdvancedCommand {
   private final JavaPlugin plugin;
   private final Supplier<NightbreakLogoutMessages> messagesSupplier;

   public NightbreakLogoutCommand(JavaPlugin plugin) {
      this(plugin, NightbreakLogoutMessages::defaults);
   }

   public NightbreakLogoutCommand(JavaPlugin plugin, Supplier<NightbreakLogoutMessages> messagesSupplier) {
      super(new ArrayList<>());
      this.setUsage("/nightbreaklogout");
      this.messagesSupplier = messagesSupplier == null ? NightbreakLogoutMessages::defaults : messagesSupplier;
      this.setDescription(this.messages().commandDescription());
      this.setSenderType(SenderType.ANY);
      this.setPermission("nightbreak.login");
      this.plugin = plugin;
   }

   @Override
   public String getDescription() {
      return this.messages().commandDescription();
   }

   @Override
   public String getPermissionMessage() {
      return this.messages().permissionDenied();
   }

   @Override
   public void execute(CommandData commandData) {
      NightbreakLogoutMessages messages = this.messages();
      if (!commandData.getCommandSender().hasPermission("nightbreak.login")) {
         Logger.sendMessage(commandData.getCommandSender(), messages.permissionDenied());
      } else {
         if (NightbreakAccount.clearToken(this.plugin)) {
            Logger.sendSimpleMessage(commandData.getCommandSender(), messages.success());
            Logger.sendSimpleMessage(commandData.getCommandSender(), messages.reconnect());
         } else {
            Logger.sendSimpleMessage(commandData.getCommandSender(), messages.failure());
         }
      }
   }

   private NightbreakLogoutMessages messages() {
      try {
         NightbreakLogoutMessages messages = this.messagesSupplier.get();
         return messages == null ? NightbreakLogoutMessages.defaults() : messages;
      } catch (RuntimeException var2) {
         return NightbreakLogoutMessages.defaults();
      }
   }
}
