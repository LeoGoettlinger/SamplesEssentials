package ac.grim.grimac.command.commands;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.command.BuildableCommand;
import ac.grim.grimac.internal.storage.copy.BackendToBackendCopier;
import ac.grim.grimac.manager.datastore.DataStoreLifecycle;
import ac.grim.grimac.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.shaded.incendo.cloud.CommandManager;
import ac.grim.grimac.shaded.incendo.cloud.context.CommandContext;
import ac.grim.grimac.shaded.incendo.cloud.description.Description;
import ac.grim.grimac.shaded.incendo.cloud.parser.standard.StringParser;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import ac.grim.grimac.shaded.kyori.adventure.text.TextComponent;
import ac.grim.grimac.shaded.kyori.adventure.text.format.NamedTextColor;
import ac.grim.grimac.utils.anticheat.LogUtil;
import ac.grim.grimac.utils.anticheat.MessageUtil;
import java.util.Map;

public class GrimHistoryCopy implements BuildableCommand {
   @Override
   public void register(CommandManager<Sender> commandManager, CloudPlatformCommandArguments arguments) {
      commandManager.command(
         commandManager.commandBuilder("grim", new String[]{"grimac"})
            .literal("history")
            .literal("copy")
            .permission("grim.history.copy")
            .required("src", StringParser.stringParser())
            .required("dst", StringParser.stringParser())
            .flag(commandManager.flagBuilder("delete").withDescription(Description.of("Wipe the source backend after the copy completes")))
            .handler(this::handle)
      );
   }

   private void handle(CommandContext<Sender> context) {
      Sender sender = context.sender();
      String srcId = context.get("src");
      String dstId = context.get("dst");
      boolean delete = context.flags().hasFlag("delete");
      DataStoreLifecycle lifecycle = GrimAPI.INSTANCE.getDataStoreLifecycle();
      if (!lifecycle.isLoaded()) {
         sender.sendMessage(MessageUtil.miniMessage("%prefix% &cHistory subsystem not loaded!"));
      } else {
         Map<String, Backend> backends = lifecycle.allBackendsForCommands();
         Backend src = backends.get(srcId);
         Backend dst = backends.get(dstId);
         if (src == null) {
            sender.sendMessage(Component.text("Unknown source backend: " + srcId + "  (configured: " + backends.keySet() + ")", NamedTextColor.RED));
         } else if (dst == null) {
            sender.sendMessage(Component.text("Unknown destination backend: " + dstId + "  (configured: " + backends.keySet() + ")", NamedTextColor.RED));
         } else if (src == dst) {
            sender.sendMessage(Component.text("Source and destination are the same backend; refusing to copy.", NamedTextColor.RED));
         } else {
            logBoth(
               sender,
               Component.text()
                  .append(Component.text("Copying v1 history ", NamedTextColor.AQUA))
                  .append(Component.text(srcId, NamedTextColor.WHITE))
                  .append(Component.text(" → ", NamedTextColor.AQUA))
                  .append(Component.text(dstId, NamedTextColor.WHITE))
                  .build()
            );

            try {
               BackendToBackendCopier copier = new BackendToBackendCopier(src, dst);
               BackendToBackendCopier.Result result = copier.run(count -> {
                  if (count > 0L && count % 5000L == 0L) {
                     logBoth(sender, Component.text("… " + count + " violations copied so far", NamedTextColor.GRAY));
                  }
               });
               logBoth(
                  sender,
                  Component.text()
                     .append(Component.text("Copy complete: ", NamedTextColor.GREEN))
                     .append(Component.text(result.players() + " players, "))
                     .append(Component.text(result.sessions() + " sessions, "))
                     .append(Component.text(result.violations() + " violations in "))
                     .append(Component.text(result.elapsedMs() + "ms"))
                     .build()
               );
               if (delete) {
                  logBoth(sender, Component.text("--delete requested — wiping source " + srcId + "…", NamedTextColor.YELLOW));
                  copier.dropSource();
                  logBoth(sender, Component.text("Source " + srcId + " wiped.", NamedTextColor.GREEN));
               }
            } catch (BackendException var12) {
               logBoth(sender, Component.text("Copy failed: " + var12.getMessage(), NamedTextColor.RED));
               LogUtil.error("v1 copy failed via /grim history copy", var12);
            }
         }
      }
   }

   private static void logBoth(Sender sender, Component msg) {
      sender.sendMessage(msg);
      LogUtil.info(plain(msg));
   }

   private static String plain(Component c) {
      StringBuilder sb = new StringBuilder();
      flatten(c, sb);
      return sb.toString();
   }

   private static void flatten(Component c, StringBuilder sb) {
      if (c instanceof TextComponent tc) {
         sb.append(tc.content());
      }

      for (Component child : c.children()) {
         flatten(child, sb);
      }
   }
}
