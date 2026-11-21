package com.sk89q.worldedit.command;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.annotation.Confirm;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.gen.CommandCallListener;
import org.enginehub.piston.gen.CommandRegistration;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.internal.RegistrationUtil;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandParts;

public final class HistoryCommandsRegistration implements CommandRegistration<HistoryCommands>, CommandPermissionsConditionGenerator.Registration {
   private static final Key<Integer> integer_confirmcom_sk89q_worldedit_command_util_annotation_Confirm_Processor_LIMIT_Key = Key.of(
      Integer.class, (new Object() {
         Annotation a(@Confirm(Confirm.Processor.LIMIT) Object ah) {
            try {
               return this.getClass().getDeclaredMethod("a", Object.class).getParameterAnnotations()[0][0];
            } catch (NoSuchMethodException var3) {
               throw new RuntimeException(var3);
            }
         }
      }).a(null)
   );
   private static final Key<String> string_Key = Key.of(String.class);
   private static final Key<Actor> actor_Key = Key.of(Actor.class);
   private static final Key<LocalSession> localSession_Key = Key.of(LocalSession.class);
   private CommandManager commandManager;
   private HistoryCommands containerInstance;
   private CommandPermissionsConditionGenerator commandPermissionsConditionGenerator;
   private ImmutableList<CommandCallListener> listeners;
   private final CommandArgument timesPart = CommandParts.arg(TranslatableComponent.of("times"), TextComponent.of("Number of undoes to perform"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(integer_confirmcom_sk89q_worldedit_command_util_annotation_Confirm_Processor_LIMIT_Key))
      .build();
   private final CommandArgument playerNamePart = CommandParts.arg(TranslatableComponent.of("player"), TextComponent.of("Undo this player's operations"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(string_Key))
      .build();
   private final CommandArgument timesPart2 = CommandParts.arg(TranslatableComponent.of("times"), TextComponent.of("Number of redoes to perform"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(integer_confirmcom_sk89q_worldedit_command_util_annotation_Confirm_Processor_LIMIT_Key))
      .build();
   private final CommandArgument playerNamePart2 = CommandParts.arg(TranslatableComponent.of("player"), TextComponent.of("Redo this player's operations"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(string_Key))
      .build();

   private HistoryCommandsRegistration() {
      this.listeners = ImmutableList.of();
   }

   public static HistoryCommandsRegistration builder() {
      return new HistoryCommandsRegistration();
   }

   public HistoryCommandsRegistration commandManager(CommandManager commandManager) {
      this.commandManager = commandManager;
      return this;
   }

   public HistoryCommandsRegistration containerInstance(HistoryCommands containerInstance) {
      this.containerInstance = containerInstance;
      return this;
   }

   public HistoryCommandsRegistration commandPermissionsConditionGenerator(CommandPermissionsConditionGenerator commandPermissionsConditionGenerator) {
      this.commandPermissionsConditionGenerator = commandPermissionsConditionGenerator;
      return this;
   }

   public HistoryCommandsRegistration listeners(Collection<CommandCallListener> listeners) {
      this.listeners = ImmutableList.copyOf(listeners);
      return this;
   }

   @Override
   public void build() {
      this.commandManager.register("undo", b -> {
         b.aliases(ImmutableList.of("/undo"));
         b.description(TextComponent.of("Undoes the last action (from history)"));
         b.parts(ImmutableList.of(this.timesPart, this.playerNamePart));
         b.action(this::cmd$undo);
         Method commandMethod = RegistrationUtil.getCommandMethod(HistoryCommands.class, "undo", Actor.class, LocalSession.class, int.class, String.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("redo", b -> {
         b.aliases(ImmutableList.of("/redo"));
         b.description(TextComponent.of("Redoes the last action (from history)"));
         b.parts(ImmutableList.of(this.timesPart2, this.playerNamePart2));
         b.action(this::cmd$redo);
         Method commandMethod = RegistrationUtil.getCommandMethod(HistoryCommands.class, "redo", Actor.class, LocalSession.class, int.class, String.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("clearhistory", b -> {
         b.aliases(ImmutableList.of("/clearhistory"));
         b.description(TextComponent.of("Clear your history"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$clearhistory);
         Method commandMethod = RegistrationUtil.getCommandMethod(HistoryCommands.class, "clearHistory", Actor.class, LocalSession.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
   }

   private int cmd$undo(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(HistoryCommands.class, "undo", Actor.class, LocalSession.class, int.class, String.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .undo(this.extract$actor(parameters), this.extract$session(parameters), this.extract$times(parameters), this.extract$playerName(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$redo(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(HistoryCommands.class, "redo", Actor.class, LocalSession.class, int.class, String.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .redo(this.extract$actor(parameters), this.extract$session(parameters), this.extract$times2(parameters), this.extract$playerName2(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$clearhistory(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(HistoryCommands.class, "clearHistory", Actor.class, LocalSession.class);
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

   private Actor extract$actor(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(actor_Key, "actor", parameters.injectedValue(actor_Key));
   }

   private LocalSession extract$session(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(localSession_Key, "session", parameters.injectedValue(localSession_Key));
   }

   private int extract$times(CommandParameters parameters) {
      return this.timesPart.value(parameters).asSingle(integer_confirmcom_sk89q_worldedit_command_util_annotation_Confirm_Processor_LIMIT_Key);
   }

   private String extract$playerName(CommandParameters parameters) {
      return this.playerNamePart.value(parameters).asSingle(string_Key);
   }

   private int extract$times2(CommandParameters parameters) {
      return this.timesPart2.value(parameters).asSingle(integer_confirmcom_sk89q_worldedit_command_util_annotation_Confirm_Processor_LIMIT_Key);
   }

   private String extract$playerName2(CommandParameters parameters) {
      return this.playerNamePart2.value(parameters).asSingle(string_Key);
   }
}
