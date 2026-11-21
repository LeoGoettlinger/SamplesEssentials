package vrx.economy.vRXClans;

import org.bukkit.command.CommandSender;
import vrx.economy.vRXClans.libs.cloud.bukkit.CloudBukkitCapabilities;
import vrx.economy.vRXClans.libs.cloud.execution.ExecutionCoordinator;
import vrx.economy.vRXClans.libs.cloud.paper.LegacyPaperCommandManager;
import vrx.economy.vRXClans.libs.cloud.parser.standard.StringParser;

public final class CloudCommandBootstrap {
   private final VRXClans plugin;
   private final ClanCommand clanCommand;
   private final ClanAdminCommand adminCommand;
   private LegacyPaperCommandManager<CommandSender> commandManager;

   public CloudCommandBootstrap(VRXClans plugin) {
      this.plugin = plugin;
      this.clanCommand = new ClanCommand(plugin);
      this.adminCommand = new ClanAdminCommand(plugin);
   }

   public void register() {
      this.commandManager = LegacyPaperCommandManager.createNative(this.plugin, ExecutionCoordinator.simpleCoordinator());
      if (this.commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
         this.commandManager.registerBrigadier();
      } else if (this.commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
         this.commandManager.registerAsynchronousCompletions();
      }

      this.registerClanAlias("clan");
      this.registerClanAlias("clans");
      this.registerClanAlias("c");
      this.registerAdminAlias("clanadmin");
      this.registerAdminAlias("clana");
   }

   private void registerClanAlias(String alias) {
      this.commandManager
         .command(
            this.commandManager
               .commandBuilder(alias, new String[0])
               .handler(context -> this.clanCommand.onCommand(context.sender(), null, alias, new String[0]))
         );

      for (String subCommand : this.clanCommand.getRootTabCompletions()) {
         this.commandManager
            .command(
               this.commandManager
                  .commandBuilder(alias, new String[0])
                  .literal(subCommand)
                  .optional("args", StringParser.greedyStringParser())
                  .handler(
                     context -> this.clanCommand.onCommand(context.sender(), null, alias, prepend(subCommand, splitArgs(context.getOrDefault("args", ""))))
                  )
            );
      }

      for (String category : ClanCommand.TOP_CATEGORIES) {
         this.commandManager
            .command(
               this.commandManager
                  .commandBuilder(alias, new String[0])
                  .literal("top")
                  .literal(category)
                  .handler(context -> this.clanCommand.onCommand(context.sender(), null, alias, new String[]{"top", category}))
            );
      }

      for (String diplomacyCommand : new String[]{"ally", "rival", "neutral", "war"}) {
         this.commandManager
            .command(
               this.commandManager
                  .commandBuilder(alias, new String[0])
                  .literal(diplomacyCommand)
                  .literal("accept")
                  .handler(context -> this.clanCommand.onCommand(context.sender(), null, alias, new String[]{diplomacyCommand, "accept"}))
            );
         this.commandManager
            .command(
               this.commandManager
                  .commandBuilder(alias, new String[0])
                  .literal(diplomacyCommand)
                  .literal("deny")
                  .handler(context -> this.clanCommand.onCommand(context.sender(), null, alias, new String[]{diplomacyCommand, "deny"}))
            );
      }
   }

   private void registerAdminAlias(String alias) {
      this.commandManager
         .command(
            this.commandManager
               .commandBuilder(alias, new String[0])
               .permission("vrxclans.admin")
               .handler(context -> this.adminCommand.onCommand(context.sender(), null, alias, new String[0]))
         );

      for (String subCommand : this.adminCommand.getRootTabCompletions()) {
         this.commandManager
            .command(
               this.commandManager
                  .commandBuilder(alias, new String[0])
                  .permission("vrxclans.admin")
                  .literal(subCommand)
                  .optional("args", StringParser.greedyStringParser())
                  .handler(
                     context -> this.adminCommand.onCommand(context.sender(), null, alias, prepend(subCommand, splitArgs(context.getOrDefault("args", ""))))
                  )
            );
      }
   }

   private static String[] prepend(String first, String[] rest) {
      String[] out = new String[rest.length + 1];
      out[0] = first;
      System.arraycopy(rest, 0, out, 1, rest.length);
      return out;
   }

   private static String[] splitArgs(Object raw) {
      if (raw == null) {
         return new String[0];
      } else {
         String value = String.valueOf(raw);
         return value.isBlank() ? new String[0] : value.trim().split("\\s+");
      }
   }
}
