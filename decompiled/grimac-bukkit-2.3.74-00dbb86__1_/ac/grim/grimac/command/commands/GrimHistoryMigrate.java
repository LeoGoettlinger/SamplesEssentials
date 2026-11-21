package ac.grim.grimac.command.commands;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.command.BuildableCommand;
import ac.grim.grimac.internal.storage.backend.sqlite.SqliteBackend;
import ac.grim.grimac.internal.storage.checks.CheckRegistry;
import ac.grim.grimac.internal.storage.migrate.LegacyMigrator;
import ac.grim.grimac.internal.storage.migrate.V0Reader;
import ac.grim.grimac.manager.datastore.ClientVersionResolver;
import ac.grim.grimac.manager.datastore.DataStoreLifecycle;
import ac.grim.grimac.manager.datastore.V0Sources;
import ac.grim.grimac.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.shaded.incendo.cloud.CommandManager;
import ac.grim.grimac.shaded.incendo.cloud.context.CommandContext;
import ac.grim.grimac.shaded.incendo.cloud.description.Description;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import ac.grim.grimac.shaded.kyori.adventure.text.TextComponent;
import ac.grim.grimac.shaded.kyori.adventure.text.format.NamedTextColor;
import ac.grim.grimac.utils.anticheat.LogUtil;
import ac.grim.grimac.utils.anticheat.MessageUtil;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class GrimHistoryMigrate implements BuildableCommand {
   @Override
   public void register(CommandManager<Sender> commandManager, CloudPlatformCommandArguments arguments) {
      commandManager.command(
         commandManager.commandBuilder("grim", new String[]{"grimac"})
            .literal("history")
            .literal("migrate")
            .permission("grim.history.migrate")
            .flag(commandManager.flagBuilder("delete").withDescription(Description.of("Drop the legacy v0 tables after migration completes")))
            .handler(this::handle)
      );
   }

   private void handle(CommandContext<Sender> context) {
      Sender sender = context.sender();
      boolean delete = context.flags().hasFlag("delete");
      DataStoreLifecycle lifecycle = GrimAPI.INSTANCE.getDataStoreLifecycle();
      if (!lifecycle.isEnabled()) {
         sender.sendMessage(MessageUtil.miniMessage("%prefix% &cHistory subsystem is disabled!"));
      } else if (!lifecycle.isLoaded()) {
         sender.sendMessage(MessageUtil.miniMessage("%prefix% &cHistory subsystem failed to load!"));
      } else {
         V0Sources.V0Source source = V0Sources.detect(
            GrimAPI.INSTANCE.getGrimPlugin().getDataFolder().toPath(), GrimAPI.INSTANCE.getConfigManager().getConfig()
         );
         if (source == null) {
            logBoth(sender, Component.text("No legacy v0 source detected — nothing to migrate.", NamedTextColor.YELLOW));
         } else {
            logBoth(
               sender,
               Component.text()
                  .append(Component.text("Starting v0 → v1 migration from ", NamedTextColor.AQUA))
                  .append(Component.text(source.summary(), NamedTextColor.WHITE))
                  .build()
            );

            try {
               LegacyMigrator.Result result = this.runLegacy(lifecycle, source, sender);
               logBoth(
                  sender,
                  Component.text()
                     .append(Component.text("Migration complete: ", NamedTextColor.GREEN))
                     .append(Component.text(result.sessionsWritten() + " sessions, "))
                     .append(Component.text(result.violationsWritten() + " violations in "))
                     .append(Component.text(result.elapsedMs() + "ms"))
                     .append(result.resumed() ? Component.text(" (resumed)", NamedTextColor.GRAY) : Component.empty())
                     .build()
               );
               if (delete) {
                  this.dropLegacy(source, sender);
               }
            } catch (BackendException var7) {
               logBoth(sender, Component.text("Migration failed: " + var7.getMessage(), NamedTextColor.RED));
               LogUtil.error("Legacy migration failed via /grim history migrate", var7);
            }
         }
      }
   }

   private LegacyMigrator.Result runLegacy(DataStoreLifecycle lifecycle, V0Sources.V0Source source, Sender sender) throws BackendException {
      V0Reader reader = new V0Reader(source.jdbcUrl(), source.username(), source.password());
      SqliteBackend v1 = lifecycle.sqliteBackendForCommands();
      if (v1 == null) {
         throw new BackendException(
            "no SQLite backend in routing — legacy migration needs SQLite as its target; switch a category to sqlite in database.yml or use /grim history copy instead"
         );
      } else {
         CheckRegistry registry = lifecycle.checkRegistryForCommands();
         long gapMs = lifecycle.config().session().gapMs();
         LegacyMigrator migrator = new LegacyMigrator(
            reader, v1, registry, ClientVersionResolver::legacyStringToPvn, gapMs, Logger.getLogger("grim-history-migrate")
         );
         return migrator.run(count -> {
            if (count > 0L && count % 5000L == 0L) {
               logBoth(sender, Component.text("… " + count + " violations migrated so far", NamedTextColor.GRAY));
            }
         });
      }
   }

   private void dropLegacy(V0Sources.V0Source source, Sender sender) {
      logBoth(sender, Component.text("--delete requested — dropping legacy v0 tables…", NamedTextColor.YELLOW));
      String[] tables = new String[]{
         "grim_history_violations",
         "grim_history_check_names",
         "grim_history_servers",
         "grim_history_versions",
         "grim_history_client_brands",
         "grim_history_client_versions",
         "grim_history_server_versions"
      };

      try (
         Connection c = open(source);
         Statement s = c.createStatement();
      ) {
         for (String t : tables) {
            try {
               s.executeUpdate("DROP TABLE IF EXISTS " + t);
            } catch (SQLException var13) {
               logBoth(sender, Component.text("  drop " + t + " failed: " + var13.getMessage(), NamedTextColor.RED));
            }
         }

         logBoth(sender, Component.text("Legacy v0 tables dropped.", NamedTextColor.GREEN));
      } catch (SQLException var16) {
         logBoth(sender, Component.text("Failed to open legacy source for --delete: " + var16.getMessage(), NamedTextColor.RED));
      }
   }

   private static Connection open(V0Sources.V0Source source) throws SQLException {
      return source.username() == null && source.password() == null
         ? DriverManager.getConnection(source.jdbcUrl())
         : DriverManager.getConnection(source.jdbcUrl(), source.username(), source.password());
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
