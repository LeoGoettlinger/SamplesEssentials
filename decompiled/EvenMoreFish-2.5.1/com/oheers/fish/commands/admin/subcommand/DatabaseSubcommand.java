package com.oheers.fish.commands.admin.subcommand;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.utils.Scheduling;
import com.oheers.fish.commands.DangerousCommandConfirmation;
import com.oheers.fish.commands.EMFCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;

public class DatabaseSubcommand implements EMFCommand {
   private final String name;

   public DatabaseSubcommand(@NonNull String name) {
      this.name = name;
   }

   @NonNull
   public LiteralArgumentBuilder<CommandSourceStack> get() {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                              this.name
                           )
                           .requires(source -> source.getSender().hasPermission("emf.admin.debug.database")))
                        .then(this.dropFlyway()))
                     .then(this.repairFlyway()))
                  .then(this.cleanFlyway()))
               .then(this.migrateToLatest()))
            .then(this.reset()))
         .then(this.help());
   }

   @NonNull
   private ArgumentBuilder<CommandSourceStack, ?> dropFlyway() {
      return ((LiteralArgumentBuilder)Commands.literal("drop-flyway").requires(source -> source.getSender().hasPermission("emf.admin.debug.database.flyway")))
         .executes(
            ctx -> {
               CommandSender sender = ((CommandSourceStack)ctx.getSource()).getSender();
               if (this.isLogDbError(sender)) {
                  return 1;
               } else {
                  EvenMoreFish.getInstance()
                     .getPluginDataManager()
                     .getDatabaseWorker()
                     .write(() -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().getMigrationManager().dropFlywaySchemaHistory())
                     .thenRun(() -> Scheduling.getInstance().runTask(() -> sender.sendMessage("Dropped flyway schema history.")));
                  return 1;
               }
            }
         );
   }

   @NonNull
   private ArgumentBuilder<CommandSourceStack, ?> repairFlyway() {
      return ((LiteralArgumentBuilder)Commands.literal("repair-flyway").requires(source -> source.getSender().hasPermission("emf.admin.debug.database.flyway")))
         .executes(
            ctx -> {
               CommandSender sender = ((CommandSourceStack)ctx.getSource()).getSender();
               if (this.isLogDbError(sender)) {
                  return 1;
               } else {
                  sender.sendMessage("Attempting to repair the migrations, check the logs.");
                  EvenMoreFish.getInstance()
                     .getPluginDataManager()
                     .getDatabaseWorker()
                     .write(() -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().getMigrationManager().repairFlyway());
                  return 1;
               }
            }
         );
   }

   @NonNull
   private ArgumentBuilder<CommandSourceStack, ?> cleanFlyway() {
      return ((LiteralArgumentBuilder)Commands.literal("clean-flyway").requires(source -> source.getSender().hasPermission("emf.admin.debug.database.clean")))
         .executes(
            ctx -> {
               CommandSender sender = ((CommandSourceStack)ctx.getSource()).getSender();
               if (this.isLogDbError(sender)) {
                  return 1;
               } else {
                  sender.sendMessage("Attempting to clean flyway, check the logs.");
                  EvenMoreFish.getInstance()
                     .getPluginDataManager()
                     .getDatabaseWorker()
                     .write(() -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().getMigrationManager().cleanFlyway());
                  return 1;
               }
            }
         );
   }

   @NonNull
   private ArgumentBuilder<CommandSourceStack, ?> migrateToLatest() {
      return ((LiteralArgumentBuilder)Commands.literal("migrate-to-latest")
            .requires(source -> source.getSender().hasPermission("emf.admin.debug.database.migrate")))
         .executes(
            ctx -> {
               CommandSender sender = ((CommandSourceStack)ctx.getSource()).getSender();
               if (this.isLogDbError(sender)) {
                  return 1;
               } else {
                  EvenMoreFish.getInstance()
                     .getPluginDataManager()
                     .getDatabaseWorker()
                     .write(() -> EvenMoreFish.getInstance().getPluginDataManager().getDatabase().migrateFromDatabaseVersionToLatest());
                  return 1;
               }
            }
         );
   }

   @NonNull
   private ArgumentBuilder<CommandSourceStack, ?> reset() {
      return ((LiteralArgumentBuilder)Commands.literal("reset").requires(source -> source.getSender().hasPermission("emf.admin.debug.database.reset")))
         .executes(
            ctx -> {
               CommandSender sender = ((CommandSourceStack)ctx.getSource()).getSender();
               if (this.isLogDbError(sender)) {
                  return 1;
               } else if (!DangerousCommandConfirmation.confirmOrRequest(sender, "database-reset", "/emf admin database reset")) {
                  return 1;
               } else {
                  sender.sendMessage("Resetting EMF database tables, check the logs.");
                  EvenMoreFish.getInstance()
                     .getPluginDataManager()
                     .getDatabaseWorker()
                     .writeResult(() -> EvenMoreFish.getInstance().getPluginDataManager().resetDatabaseData())
                     .thenAccept(
                        resetTableCount -> Scheduling.getInstance()
                           .runTask(
                              () -> sender.sendMessage(
                                 resetTableCount < 0
                                    ? "Failed to reset the EMF database. Check the logs."
                                    : "Reset " + resetTableCount + " EMF database table(s)."
                              )
                           )
                     );
                  return 1;
               }
            }
         );
   }

   @NonNull
   private ArgumentBuilder<CommandSourceStack, ?> help() {
      return Commands.literal("help")
         .executes(
            ctx -> {
               ((CommandSourceStack)ctx.getSource())
                  .getSender()
                  .sendPlainMessage("Available Commands: migrate-to-latest, reset, clean-flyway, repair-flyway, drop-flyway");
               return 1;
            }
         );
   }
}
