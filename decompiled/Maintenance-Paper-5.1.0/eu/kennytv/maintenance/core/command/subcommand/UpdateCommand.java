package eu.kennytv.maintenance.core.command.subcommand;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.util.SenderInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class UpdateCommand extends CommandInfo {
   public UpdateCommand(MaintenancePlugin plugin) {
      super(plugin, "update");
   }

   @Override
   public void execute(SenderInfo sender, String[] args) {
      if (!this.checkArgs(sender, args, 1)) {
         if (args[0].equalsIgnoreCase("update")) {
            this.plugin.async(() -> this.checkForUpdate(sender));
         } else if (!this.plugin.updateAvailable()) {
            sender.sendPrefixedRich("<green>You already have the latest version of the plugin!");
         } else {
            sender.send(this.getMessage("updateDownloading", new String[0]));

            try {
               if (this.plugin.installUpdate()) {
                  sender.send(this.getMessage("updateFinished", new String[0]));
               } else {
                  sender.send(this.getMessage("updateFailed", new String[0]));
               }
            } catch (Exception var4) {
               sender.send(this.getMessage("updateFailed", new String[0]));
               var4.printStackTrace();
            }
         }
      }
   }

   private void checkForUpdate(SenderInfo sender) {
      if (!this.plugin.updateAvailable()) {
         sender.sendPrefixedRich("<green>You already have the latest version of the plugin!");
      } else {
         sender.sendPrefixedRich(
            "<red>Newest version available: <green>Version " + this.plugin.getNewestVersion() + "<red>, you're on <green>" + this.plugin.getVersion()
         );
         sender.sendPrefixedRich(
            "<b><red>WARNING: <red>You will have to restart the server to prevent further issues and to complete the update! If you can't do that, don't update!"
         );
         sender.send(
            ((Builder)((Builder)((Builder)((Builder)((Builder)Component.text()
                              .append(MiniMessage.miniMessage().deserialize("<gold>× <dark_gray>[<green>Update<dark_gray>]")))
                           .clickEvent(ClickEvent.runCommand("/maintenance forceupdate")))
                        .hoverEvent(HoverEvent.showText(Component.text().content("Click here to update the plugin").color(NamedTextColor.GREEN))))
                     .append(Component.text().content(", or manually run ").color(NamedTextColor.GRAY)))
                  .append(Component.text().content("/maintenance forceupdate").color(NamedTextColor.RED)))
               .build()
         );
      }
   }
}
