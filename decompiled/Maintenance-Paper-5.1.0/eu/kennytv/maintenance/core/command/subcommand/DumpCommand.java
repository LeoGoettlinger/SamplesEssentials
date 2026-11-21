package eu.kennytv.maintenance.core.command.subcommand;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.util.ComponentUtil;
import eu.kennytv.maintenance.core.util.SenderInfo;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.ClickEvent.Action;
import net.kyori.adventure.text.format.NamedTextColor;

public final class DumpCommand extends CommandInfo {
   private long lastDump;

   public DumpCommand(MaintenancePlugin plugin) {
      super(plugin, "dump");
   }

   @Override
   public void execute(SenderInfo sender, String[] args) {
      if (!this.checkArgs(sender, args, 1)) {
         if (System.currentTimeMillis() - this.lastDump < TimeUnit.MINUTES.toMillis(5L)) {
            sender.sendPrefixedRich("<red>You can only create a dump every 5 minutes!");
         } else {
            this.lastDump = System.currentTimeMillis();
            sender.sendPrefixedRich("<gray>The dump is being created, this might take a moment.");
            this.plugin
               .async(
                  () -> {
                     String key = this.plugin.pasteDump();
                     if (key == null) {
                        if (sender.isPlayer()) {
                           sender.sendPrefixedRich("<red>Could not paste dump (see the console for details)");
                        }
                     } else {
                        String url = "https://pastes.dev/" + key;
                        sender.sendPrefixedRich("<red><click:open_url:'" + url + "'>" + url + "</click>");
                        if (sender.isPlayer()) {
                           TextComponent text = (TextComponent)((Builder)((Builder)((Builder)Component.text()
                                       .content("Click here to copy the link")
                                       .color(NamedTextColor.GRAY))
                                    .clickEvent(ComponentUtil.clickEvent(Action.COPY_TO_CLIPBOARD, url)))
                                 .hoverEvent(HoverEvent.showText(Component.text("Click here to copy the link to your clipboard").color(NamedTextColor.GREEN))))
                              .build();
                           sender.send(this.plugin.prefix().append(text));
                        }
                     }
                  }
               );
         }
      }
   }
}
