package com.sk89q.worldedit.command;

import com.fastasyncworldedit.core.database.RollbackDatabase;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.argument.Arguments;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.annotation.Time;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.gen.CommandCallListener;
import org.enginehub.piston.gen.CommandRegistration;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.internal.RegistrationUtil;
import org.enginehub.piston.part.ArgAcceptingCommandFlag;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandParts;
import org.enginehub.piston.part.NoArgCommandFlag;

public final class HistorySubCommandsRegistration implements CommandRegistration<HistorySubCommands>, CommandPermissionsConditionGenerator.Registration {
   private static final Key<UUID> uUID_Key = Key.of(UUID.class);
   private static final Key<Integer> integer_Key = Key.of(Integer.class);
   private static final Key<Long> long_time_Key = Key.of(Long.class, Time.class);
   private static final Key<Player> player_Key = Key.of(Player.class);
   private static final Key<World> world_Key = Key.of(World.class);
   private static final Key<RollbackDatabase> rollbackDatabase_Key = Key.of(RollbackDatabase.class);
   private static final Key<Actor> actor_Key = Key.of(Actor.class);
   private static final Key<Arguments> arguments_Key = Key.of(Arguments.class);
   private static final Key<LocalSession> localSession_Key = Key.of(LocalSession.class);
   private CommandManager commandManager;
   private HistorySubCommands containerInstance;
   private CommandPermissionsConditionGenerator commandPermissionsConditionGenerator;
   private ImmutableList<CommandCallListener> listeners;
   private final ArgAcceptingCommandFlag otherPart = CommandParts.flag('u', TextComponent.of("String user"))
      .withRequiredArg()
      .argNamed(TranslatableComponent.of("other"))
      .defaultsTo(ImmutableList.of("me"))
      .ofTypes(ImmutableList.of(uUID_Key))
      .build();
   private final ArgAcceptingCommandFlag radiusPart = CommandParts.flag('r', TextComponent.of("radius"))
      .withRequiredArg()
      .argNamed(TranslatableComponent.of("radius"))
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final ArgAcceptingCommandFlag timeDiffPart = CommandParts.flag('t', TextComponent.of("Time e.g. 20s"))
      .withRequiredArg()
      .argNamed(TranslatableComponent.of("timeDiff"))
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(long_time_Key))
      .build();
   private final ArgAcceptingCommandFlag otherPart2 = CommandParts.flag('u', TextComponent.of("String user"))
      .withRequiredArg()
      .argNamed(TranslatableComponent.of("other"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(uUID_Key))
      .build();
   private final NoArgCommandFlag restorePart = CommandParts.flag('f', TextComponent.of("Restore instead of rollback")).build();
   private final CommandArgument otherPart3 = CommandParts.arg(TranslatableComponent.of("other"), TextComponent.of("Player uuid/name"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(uUID_Key))
      .build();
   private final CommandArgument indexPart = CommandParts.arg(TranslatableComponent.of("index"), TextComponent.of("edit index"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final ArgAcceptingCommandFlag radiusPart2 = CommandParts.flag('r', TextComponent.of("radius"))
      .withRequiredArg()
      .argNamed(TranslatableComponent.of("radius"))
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final ArgAcceptingCommandFlag timeDiffPart2 = CommandParts.flag('t', TextComponent.of("Time e.g. 20s"))
      .withRequiredArg()
      .argNamed(TranslatableComponent.of("timeDiff"))
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(long_time_Key))
      .build();
   private final ArgAcceptingCommandFlag pagePart = CommandParts.flag('p', TextComponent.of("Page to view."))
      .withRequiredArg()
      .argNamed(TranslatableComponent.of("page"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();

   private HistorySubCommandsRegistration() {
      this.listeners = ImmutableList.of();
   }

   public static HistorySubCommandsRegistration builder() {
      return new HistorySubCommandsRegistration();
   }

   public HistorySubCommandsRegistration commandManager(CommandManager commandManager) {
      this.commandManager = commandManager;
      return this;
   }

   public HistorySubCommandsRegistration containerInstance(HistorySubCommands containerInstance) {
      this.containerInstance = containerInstance;
      return this;
   }

   public HistorySubCommandsRegistration commandPermissionsConditionGenerator(CommandPermissionsConditionGenerator commandPermissionsConditionGenerator) {
      this.commandPermissionsConditionGenerator = commandPermissionsConditionGenerator;
      return this;
   }

   public HistorySubCommandsRegistration listeners(Collection<CommandCallListener> listeners) {
      this.listeners = ImmutableList.copyOf(listeners);
      return this;
   }

   @Override
   public void build() {
      this.commandManager
         .register(
            "restore",
            b -> {
               b.aliases(ImmutableList.of("rerun"));
               b.description(TextComponent.of("Rerun edits - The time uses s, m, h, d, y.\n - Import from disk: /history import"));
               b.parts(ImmutableList.of(this.otherPart, this.radiusPart, this.timeDiffPart));
               b.action(this::cmd$restore);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  HistorySubCommands.class, "rerun", Player.class, World.class, RollbackDatabase.class, UUID.class, int.class, long.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "rollback",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Undo a specific edit.  - The time uses s, m, h, d, y."));
               b.parts(ImmutableList.of(this.otherPart2, this.radiusPart, this.timeDiffPart, this.restorePart));
               b.action(this::cmd$rollback);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  HistorySubCommands.class, "rollback", Player.class, World.class, RollbackDatabase.class, UUID.class, int.class, long.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("import", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Import history into the database - The time uses s, m, h, d, y.\n - Import from disk: /history import"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$import);
         Method commandMethod = RegistrationUtil.getCommandMethod(HistorySubCommands.class, "importdb", Actor.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager
         .register(
            "info",
            b -> {
               b.aliases(ImmutableList.of("summary", "summarize"));
               b.description(TextComponent.of("Summarize an edit"));
               b.parts(ImmutableList.of(this.otherPart3, this.indexPart));
               b.action(this::cmd$info);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  HistorySubCommands.class, "summary", Player.class, RollbackDatabase.class, Arguments.class, UUID.class, Integer.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "find",
            b -> {
               b.aliases(ImmutableList.of("inspect", "search", "near"));
               b.description(TextComponent.of("Find nearby edits"));
               b.parts(ImmutableList.of(this.otherPart2, this.radiusPart2, this.timeDiffPart2, this.pagePart));
               b.action(this::cmd$find);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  HistorySubCommands.class,
                  "find",
                  Player.class,
                  World.class,
                  RollbackDatabase.class,
                  Arguments.class,
                  UUID.class,
                  Integer.class,
                  Long.class,
                  Integer.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "distr",
            b -> {
               b.aliases(ImmutableList.of("distribution"));
               b.description(TextComponent.of("View block distribution for an edit"));
               b.parts(ImmutableList.of(this.otherPart3, this.indexPart, this.pagePart));
               b.action(this::cmd$distr);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  HistorySubCommands.class,
                  "distr",
                  Player.class,
                  LocalSession.class,
                  RollbackDatabase.class,
                  Arguments.class,
                  UUID.class,
                  Integer.class,
                  Integer.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "list",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("List your history"));
               b.parts(ImmutableList.of(this.otherPart3, this.pagePart));
               b.action(this::cmd$list);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  HistorySubCommands.class, "list", Player.class, LocalSession.class, RollbackDatabase.class, Arguments.class, UUID.class, Integer.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("clear", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Clear your history"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$clear);
         Method commandMethod = RegistrationUtil.getCommandMethod(HistorySubCommands.class, "clearHistory", Actor.class, LocalSession.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
   }

   private int cmd$restore(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         HistorySubCommands.class, "rerun", Player.class, World.class, RollbackDatabase.class, UUID.class, int.class, long.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .rerun(
               this.extract$player(parameters),
               this.extract$world(parameters),
               this.extract$database(parameters),
               this.extract$other(parameters),
               this.extract$radius(parameters),
               this.extract$timeDiff(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$rollback(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         HistorySubCommands.class, "rollback", Player.class, World.class, RollbackDatabase.class, UUID.class, int.class, long.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .rollback(
               this.extract$player(parameters),
               this.extract$world(parameters),
               this.extract$database(parameters),
               this.extract$other2(parameters),
               this.extract$radius(parameters),
               this.extract$timeDiff(parameters),
               this.extract$restore(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$import(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(HistorySubCommands.class, "importdb", Actor.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.importdb(this.extract$actor(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$info(CommandParameters parameters) throws WorldEditException, ExecutionException, InterruptedException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         HistorySubCommands.class, "summary", Player.class, RollbackDatabase.class, Arguments.class, UUID.class, Integer.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .summary(
               this.extract$player(parameters),
               this.extract$database(parameters),
               this.extract$arguments(parameters),
               this.extract$other3(parameters),
               this.extract$index(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$find(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         HistorySubCommands.class,
         "find",
         Player.class,
         World.class,
         RollbackDatabase.class,
         Arguments.class,
         UUID.class,
         Integer.class,
         Long.class,
         Integer.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .find(
               this.extract$player(parameters),
               this.extract$world(parameters),
               this.extract$database(parameters),
               this.extract$arguments(parameters),
               this.extract$other2(parameters),
               this.extract$radius2(parameters),
               this.extract$timeDiff2(parameters),
               this.extract$page(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$distr(CommandParameters parameters) throws ExecutionException, InterruptedException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         HistorySubCommands.class, "distr", Player.class, LocalSession.class, RollbackDatabase.class, Arguments.class, UUID.class, Integer.class, Integer.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .distr(
               this.extract$player(parameters),
               this.extract$session(parameters),
               this.extract$database(parameters),
               this.extract$arguments(parameters),
               this.extract$other3(parameters),
               this.extract$index(parameters),
               this.extract$page(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$list(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         HistorySubCommands.class, "list", Player.class, LocalSession.class, RollbackDatabase.class, Arguments.class, UUID.class, Integer.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .list(
               this.extract$player(parameters),
               this.extract$session(parameters),
               this.extract$database(parameters),
               this.extract$arguments(parameters),
               this.extract$other3(parameters),
               this.extract$page(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$clear(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(HistorySubCommands.class, "clearHistory", Actor.class, LocalSession.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.clearHistory(this.extract$actor(parameters), this.extract$session(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private Player extract$player(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(player_Key, "player", parameters.injectedValue(player_Key));
   }

   private World extract$world(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(world_Key, "world", parameters.injectedValue(world_Key));
   }

   private RollbackDatabase extract$database(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(rollbackDatabase_Key, "database", parameters.injectedValue(rollbackDatabase_Key));
   }

   private UUID extract$other(CommandParameters parameters) {
      return this.otherPart.value(parameters).asSingle(uUID_Key);
   }

   private int extract$radius(CommandParameters parameters) {
      return this.radiusPart.value(parameters).asSingle(integer_Key);
   }

   private long extract$timeDiff(CommandParameters parameters) {
      return this.timeDiffPart.value(parameters).asSingle(long_time_Key);
   }

   private UUID extract$other2(CommandParameters parameters) {
      return this.otherPart2.value(parameters).asSingle(uUID_Key);
   }

   private boolean extract$restore(CommandParameters parameters) {
      return this.restorePart.in(parameters);
   }

   private Actor extract$actor(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(actor_Key, "actor", parameters.injectedValue(actor_Key));
   }

   private Arguments extract$arguments(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(arguments_Key, "arguments", parameters.injectedValue(arguments_Key));
   }

   private UUID extract$other3(CommandParameters parameters) {
      return this.otherPart3.value(parameters).asSingle(uUID_Key);
   }

   private Integer extract$index(CommandParameters parameters) {
      return this.indexPart.value(parameters).asSingle(integer_Key);
   }

   private Integer extract$radius2(CommandParameters parameters) {
      return this.radiusPart2.value(parameters).asSingle(integer_Key);
   }

   private Long extract$timeDiff2(CommandParameters parameters) {
      return this.timeDiffPart2.value(parameters).asSingle(long_time_Key);
   }

   private Integer extract$page(CommandParameters parameters) {
      return this.pagePart.value(parameters).asSingle(integer_Key);
   }

   private LocalSession extract$session(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(localSession_Key, "session", parameters.injectedValue(localSession_Key));
   }
}
