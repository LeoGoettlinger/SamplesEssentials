package com.sk89q.worldedit.command;

import com.fastasyncworldedit.core.command.tool.scroll.Scroll;
import com.fastasyncworldedit.core.extent.ResettableExtent;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.argument.Arguments;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.gen.CommandCallListener;
import org.enginehub.piston.gen.CommandRegistration;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.internal.RegistrationUtil;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandParts;
import org.enginehub.piston.part.NoArgCommandFlag;

public final class ToolUtilCommandsRegistration implements CommandRegistration<ToolUtilCommands>, CommandPermissionsConditionGenerator.Registration {
   private static final Key<Mask> mask_Key = Key.of(Mask.class);
   private static final Key<Pattern> pattern_Key = Key.of(Pattern.class);
   private static final Key<Integer> integer_Key = Key.of(Integer.class);
   private static final Key<Boolean> boolean_Key = Key.of(Boolean.class);
   private static final Key<String> string_Key = Key.of(String.class);
   private static final Key<Scroll.Action> action_Key = Key.of(Scroll.Action.class);
   private static final Key<ResettableExtent> resettableExtent_Key = Key.of(ResettableExtent.class);
   private static final Key<Player> player_Key = Key.of(Player.class);
   private static final Key<LocalSession> localSession_Key = Key.of(LocalSession.class);
   private static final Key<Arguments> arguments_Key = Key.of(Arguments.class);
   private static final Key<EditSession> editSession_Key = Key.of(EditSession.class);
   private CommandManager commandManager;
   private ToolUtilCommands containerInstance;
   private CommandPermissionsConditionGenerator commandPermissionsConditionGenerator;
   private ImmutableList<CommandCallListener> listeners;
   private final NoArgCommandFlag offHandPart = CommandParts.flag('h', TextComponent.of("Whether the offhand should be considered or not")).build();
   private final CommandArgument maskOptPart = CommandParts.arg(TranslatableComponent.of("maskOpt"), TextComponent.of("The destination mask"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(mask_Key))
      .build();
   private final CommandArgument patternPart = CommandParts.arg(TranslatableComponent.of("pattern"), TextComponent.of("The pattern of blocks to use"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(pattern_Key))
      .build();
   private final CommandArgument rangePart = CommandParts.arg(TranslatableComponent.of("range"), TextComponent.of("The range of the brush"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument sizePart = CommandParts.arg(TranslatableComponent.of("size"), TextComponent.of("The size of the brush"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument maskOptPart2 = CommandParts.arg(TranslatableComponent.of("maskOpt"), TextComponent.of("The trace mask to set"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(mask_Key))
      .build();
   private final CommandArgument superPickaxePart = CommandParts.arg(TranslatableComponent.of("superPickaxe"), TextComponent.of("The new super pickaxe state"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(boolean_Key))
      .build();
   private final CommandArgument commandStrPart = CommandParts.arg(TranslatableComponent.of("commandStr"), TextComponent.of("The brush command"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(string_Key))
      .variable(true)
      .build();
   private final CommandArgument modePart = CommandParts.arg(TranslatableComponent.of("mode"), TextComponent.of("int"))
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument offsetPart = CommandParts.arg(TranslatableComponent.of("offset"), TextComponent.of("offset"))
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument modePart2 = CommandParts.arg(TranslatableComponent.of("mode"), TextComponent.of("Target Modes"))
      .defaultsTo(ImmutableList.of("none"))
      .ofTypes(ImmutableList.of(action_Key))
      .build();
   private final CommandArgument commandStrPart2 = CommandParts.arg(TranslatableComponent.of("commandStr"), TextComponent.of("The scroll action"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(string_Key))
      .variable(true)
      .build();
   private final CommandArgument maskArgPart = CommandParts.arg(TranslatableComponent.of("maskArg"), TextComponent.of("The destination mask"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(mask_Key))
      .build();
   private final CommandArgument transformPart = CommandParts.arg(TranslatableComponent.of("transform"), TextComponent.of("The transform"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(resettableExtent_Key))
      .build();

   private ToolUtilCommandsRegistration() {
      this.listeners = ImmutableList.of();
   }

   public static ToolUtilCommandsRegistration builder() {
      return new ToolUtilCommandsRegistration();
   }

   public ToolUtilCommandsRegistration commandManager(CommandManager commandManager) {
      this.commandManager = commandManager;
      return this;
   }

   public ToolUtilCommandsRegistration containerInstance(ToolUtilCommands containerInstance) {
      this.containerInstance = containerInstance;
      return this;
   }

   public ToolUtilCommandsRegistration commandPermissionsConditionGenerator(CommandPermissionsConditionGenerator commandPermissionsConditionGenerator) {
      this.commandPermissionsConditionGenerator = commandPermissionsConditionGenerator;
      return this;
   }

   public ToolUtilCommandsRegistration listeners(Collection<CommandCallListener> listeners) {
      this.listeners = ImmutableList.copyOf(listeners);
      return this;
   }

   @Override
   public void build() {
      this.commandManager
         .register(
            "mask",
            b -> {
               b.aliases(ImmutableList.of("/mask"));
               b.description(TextComponent.of("Set the brush destination mask"));
               b.parts(ImmutableList.of(this.offHandPart, this.maskOptPart));
               b.action(this::cmd$mask);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  ToolUtilCommands.class, "mask", Player.class, LocalSession.class, boolean.class, Mask.class, Arguments.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "material",
            b -> {
               b.aliases(ImmutableList.of("mat", "/material", "pattern"));
               b.description(TextComponent.of("Set the brush material"));
               b.parts(ImmutableList.of(this.patternPart, this.offHandPart));
               b.action(this::cmd$material);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  ToolUtilCommands.class, "material", Player.class, LocalSession.class, Pattern.class, boolean.class, Arguments.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("range", b -> {
         b.aliases(ImmutableList.of("/range"));
         b.description(TextComponent.of("Set the brush range"));
         b.parts(ImmutableList.of(this.rangePart));
         b.action(this::cmd$range);
         Method commandMethod = RegistrationUtil.getCommandMethod(ToolUtilCommands.class, "range", Player.class, LocalSession.class, int.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("size", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Set the brush size"));
         b.parts(ImmutableList.of(this.sizePart));
         b.action(this::cmd$size);
         Method commandMethod = RegistrationUtil.getCommandMethod(ToolUtilCommands.class, "size", Player.class, LocalSession.class, int.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("tracemask", b -> {
         b.aliases(ImmutableList.of("tarmask", "tm", "targetmask"));
         b.description(TextComponent.of("Set the mask used to stop tool traces"));
         b.parts(ImmutableList.of(this.maskOptPart2));
         b.action(this::cmd$tracemask);
         Method commandMethod = RegistrationUtil.getCommandMethod(ToolUtilCommands.class, "traceMask", Player.class, LocalSession.class, Mask.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/", b -> {
         b.aliases(ImmutableList.of(","));
         b.description(TextComponent.of("Toggle the super pickaxe function"));
         b.parts(ImmutableList.of(this.superPickaxePart));
         b.action(this::cmd$_);
         Method commandMethod = RegistrationUtil.getCommandMethod(ToolUtilCommands.class, "togglePickaxe", Player.class, LocalSession.class, Boolean.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("primary", b -> {
         b.aliases(ImmutableList.of("/primary"));
         b.description(TextComponent.of("Set the right click brush"));
         b.footer(TextComponent.of("Set the right click brush"));
         b.parts(ImmutableList.of(this.commandStrPart));
         b.action(this::cmd$primary);
         Method commandMethod = RegistrationUtil.getCommandMethod(ToolUtilCommands.class, "primary", Player.class, LocalSession.class, List.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("secondary", b -> {
         b.aliases(ImmutableList.of("/secondary"));
         b.description(TextComponent.of("Set the left click brush"));
         b.footer(TextComponent.of("Set the left click brush"));
         b.parts(ImmutableList.of(this.commandStrPart));
         b.action(this::cmd$secondary);
         Method commandMethod = RegistrationUtil.getCommandMethod(ToolUtilCommands.class, "secondary", Player.class, LocalSession.class, List.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("target", b -> {
         b.aliases(ImmutableList.of("tar", "/target", "/tar"));
         b.description(TextComponent.of("Toggle between different target modes"));
         b.parts(ImmutableList.of(this.modePart));
         b.action(this::cmd$target);
         Method commandMethod = RegistrationUtil.getCommandMethod(ToolUtilCommands.class, "target", Player.class, LocalSession.class, int.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager
         .register(
            "targetoffset",
            b -> {
               b.aliases(ImmutableList.of("to"));
               b.description(TextComponent.of("Set the targeting mask"));
               b.parts(ImmutableList.of(this.offsetPart));
               b.action(this::cmd$targetoffset);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  ToolUtilCommands.class, "targetOffset", Player.class, EditSession.class, LocalSession.class, int.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "scroll",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Toggle between different target modes"));
               b.parts(ImmutableList.of(this.offHandPart, this.modePart2, this.commandStrPart2));
               b.action(this::cmd$scroll);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  ToolUtilCommands.class, "scroll", Player.class, EditSession.class, LocalSession.class, boolean.class, Scroll.Action.class, List.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "smask",
            b -> {
               b.aliases(ImmutableList.of("/smask", "/sourcemask", "sourcemask"));
               b.description(TextComponent.of("Set the brush source mask"));
               b.footer(TextComponent.of("Set the brush source mask"));
               b.parts(ImmutableList.of(this.maskArgPart, this.offHandPart));
               b.action(this::cmd$smask);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  ToolUtilCommands.class, "smask", Player.class, LocalSession.class, EditSession.class, Mask.class, boolean.class, Arguments.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "transform",
            b -> {
               b.aliases(ImmutableList.of("/transform"));
               b.description(TextComponent.of("Set the brush transform"));
               b.parts(ImmutableList.of(this.transformPart, this.offHandPart));
               b.action(this::cmd$transform);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  ToolUtilCommands.class,
                  "transform",
                  Player.class,
                  LocalSession.class,
                  EditSession.class,
                  ResettableExtent.class,
                  boolean.class,
                  Arguments.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
   }

   private int cmd$mask(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         ToolUtilCommands.class, "mask", Player.class, LocalSession.class, boolean.class, Mask.class, Arguments.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .mask(
               this.extract$player(parameters),
               this.extract$session(parameters),
               this.extract$offHand(parameters),
               this.extract$maskOpt(parameters),
               this.extract$arguments(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$material(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         ToolUtilCommands.class, "material", Player.class, LocalSession.class, Pattern.class, boolean.class, Arguments.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .material(
               this.extract$player(parameters),
               this.extract$session(parameters),
               this.extract$pattern(parameters),
               this.extract$offHand(parameters),
               this.extract$arguments(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$range(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(ToolUtilCommands.class, "range", Player.class, LocalSession.class, int.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.range(this.extract$player(parameters), this.extract$session(parameters), this.extract$range(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$size(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(ToolUtilCommands.class, "size", Player.class, LocalSession.class, int.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.size(this.extract$player(parameters), this.extract$session(parameters), this.extract$size(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$tracemask(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(ToolUtilCommands.class, "traceMask", Player.class, LocalSession.class, Mask.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.traceMask(this.extract$player(parameters), this.extract$session(parameters), this.extract$maskOpt2(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(ToolUtilCommands.class, "togglePickaxe", Player.class, LocalSession.class, Boolean.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.togglePickaxe(this.extract$player(parameters), this.extract$session(parameters), this.extract$superPickaxe(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$primary(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(ToolUtilCommands.class, "primary", Player.class, LocalSession.class, List.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.primary(this.extract$player(parameters), this.extract$session(parameters), this.extract$commandStr(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$secondary(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(ToolUtilCommands.class, "secondary", Player.class, LocalSession.class, List.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.secondary(this.extract$player(parameters), this.extract$session(parameters), this.extract$commandStr(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$target(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(ToolUtilCommands.class, "target", Player.class, LocalSession.class, int.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.target(this.extract$player(parameters), this.extract$session(parameters), this.extract$mode(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$targetoffset(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         ToolUtilCommands.class, "targetOffset", Player.class, EditSession.class, LocalSession.class, int.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .targetOffset(
               this.extract$player(parameters), this.extract$editSession(parameters), this.extract$session(parameters), this.extract$offset(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$scroll(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         ToolUtilCommands.class, "scroll", Player.class, EditSession.class, LocalSession.class, boolean.class, Scroll.Action.class, List.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .scroll(
               this.extract$player(parameters),
               this.extract$editSession(parameters),
               this.extract$session(parameters),
               this.extract$offHand(parameters),
               this.extract$mode2(parameters),
               this.extract$commandStr2(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$smask(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         ToolUtilCommands.class, "smask", Player.class, LocalSession.class, EditSession.class, Mask.class, boolean.class, Arguments.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .smask(
               this.extract$player(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$maskArg(parameters),
               this.extract$offHand(parameters),
               this.extract$arguments(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$transform(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         ToolUtilCommands.class, "transform", Player.class, LocalSession.class, EditSession.class, ResettableExtent.class, boolean.class, Arguments.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .transform(
               this.extract$player(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$transform(parameters),
               this.extract$offHand(parameters),
               this.extract$arguments(parameters)
            );
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

   private LocalSession extract$session(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(localSession_Key, "session", parameters.injectedValue(localSession_Key));
   }

   private boolean extract$offHand(CommandParameters parameters) {
      return this.offHandPart.in(parameters);
   }

   private Mask extract$maskOpt(CommandParameters parameters) {
      return this.maskOptPart.value(parameters).asSingle(mask_Key);
   }

   private Arguments extract$arguments(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(arguments_Key, "arguments", parameters.injectedValue(arguments_Key));
   }

   private Pattern extract$pattern(CommandParameters parameters) {
      return this.patternPart.value(parameters).asSingle(pattern_Key);
   }

   private int extract$range(CommandParameters parameters) {
      return this.rangePart.value(parameters).asSingle(integer_Key);
   }

   private int extract$size(CommandParameters parameters) {
      return this.sizePart.value(parameters).asSingle(integer_Key);
   }

   private Mask extract$maskOpt2(CommandParameters parameters) {
      return this.maskOptPart2.value(parameters).asSingle(mask_Key);
   }

   private Boolean extract$superPickaxe(CommandParameters parameters) {
      return this.superPickaxePart.value(parameters).asSingle(boolean_Key);
   }

   private List<String> extract$commandStr(CommandParameters parameters) {
      return this.commandStrPart.value(parameters).<String>asMultiple(string_Key);
   }

   private int extract$mode(CommandParameters parameters) {
      return this.modePart.value(parameters).asSingle(integer_Key);
   }

   private EditSession extract$editSession(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(editSession_Key, "editSession", parameters.injectedValue(editSession_Key));
   }

   private int extract$offset(CommandParameters parameters) {
      return this.offsetPart.value(parameters).asSingle(integer_Key);
   }

   private Scroll.Action extract$mode2(CommandParameters parameters) {
      return this.modePart2.value(parameters).asSingle(action_Key);
   }

   private List<String> extract$commandStr2(CommandParameters parameters) {
      return this.commandStrPart2.value(parameters).<String>asMultiple(string_Key);
   }

   private Mask extract$maskArg(CommandParameters parameters) {
      return this.maskArgPart.value(parameters).asSingle(mask_Key);
   }

   private ResettableExtent extract$transform(CommandParameters parameters) {
      return this.transformPart.value(parameters).asSingle(resettableExtent_Key);
   }
}
