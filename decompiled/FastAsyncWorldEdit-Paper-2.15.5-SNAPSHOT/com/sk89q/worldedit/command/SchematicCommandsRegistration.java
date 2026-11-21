package com.sk89q.worldedit.command;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.argument.Arguments;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.share.ClipboardShareDestination;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.io.file.FilenameException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
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

public final class SchematicCommandsRegistration implements CommandRegistration<SchematicCommands>, CommandPermissionsConditionGenerator.Registration {
   private static final Key<String> string_Key = Key.of(String.class);
   private static final Key<ClipboardFormat> clipboardFormat_Key = Key.of(ClipboardFormat.class);
   private static final Key<ClipboardShareDestination> clipboardShareDestination_Key = Key.of(ClipboardShareDestination.class);
   private static final Key<Integer> integer_Key = Key.of(Integer.class);
   private static final Key<Actor> actor_Key = Key.of(Actor.class);
   private static final Key<LocalSession> localSession_Key = Key.of(LocalSession.class);
   private static final Key<Arguments> arguments_Key = Key.of(Arguments.class);
   private CommandManager commandManager;
   private SchematicCommands containerInstance;
   private CommandPermissionsConditionGenerator commandPermissionsConditionGenerator;
   private ImmutableList<CommandCallListener> listeners;
   private final CommandArgument formatNamePart = CommandParts.arg(TranslatableComponent.of("formatName"), TextComponent.of("Format name."))
      .defaultsTo(ImmutableList.of("fast"))
      .ofTypes(ImmutableList.of(string_Key))
      .build();
   private final CommandArgument filenamePart = CommandParts.arg(TranslatableComponent.of("filename"), TextComponent.of("File name."))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(string_Key))
      .build();
   private final NoArgCommandFlag overwritePart = CommandParts.flag('o', TextComponent.of("Overwrite/replace existing clipboard(s)")).build();
   private final NoArgCommandFlag randomRotatePart = CommandParts.flag('r', TextComponent.of("Apply random rotation (static by default)")).build();
   private final NoArgCommandFlag dynamicRandomPart = CommandParts.flag('d', TextComponent.of("Random rotation is dynamic, changing each use")).build();
   private final CommandArgument fileNamePart = CommandParts.arg(TranslatableComponent.of("fileName"), TextComponent.of("File name, requires extension."))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(string_Key))
      .build();
   private final CommandArgument directoryPart = CommandParts.arg(TranslatableComponent.of("directory"), TextComponent.of("Directory."))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(string_Key))
      .build();
   private final CommandArgument formatNamePart2 = CommandParts.arg(TranslatableComponent.of("formatName"), TextComponent.of("Format name."))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(string_Key))
      .build();
   private final NoArgCommandFlag randomRotatePart2 = CommandParts.flag('r', TextComponent.of("Apply random rotation to the clipboard")).build();
   private final CommandArgument formatPart = CommandParts.arg(TranslatableComponent.of("format"), TextComponent.of("Format name."))
      .defaultsTo(ImmutableList.of("fast"))
      .ofTypes(ImmutableList.of(clipboardFormat_Key))
      .build();
   private final NoArgCommandFlag allowOverwritePart = CommandParts.flag('f', TextComponent.of("Overwrite an existing file.")).build();
   private final NoArgCommandFlag globalPart = CommandParts.flag('g', TextComponent.of("Bypasses per-player-schematic folders")).build();
   private final CommandArgument schematicNamePart = CommandParts.arg(
         TranslatableComponent.of("schematicName"), TextComponent.of("Schematic name. Defaults to name-millis")
      )
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(string_Key))
      .build();
   private final CommandArgument destinationPart = CommandParts.arg(TranslatableComponent.of("destination"), TextComponent.of("Share location"))
      .defaultsTo(ImmutableList.of("arkitektonika"))
      .ofTypes(ImmutableList.of(clipboardShareDestination_Key))
      .build();
   private final CommandArgument formatPart2 = CommandParts.arg(TranslatableComponent.of("format"), TextComponent.of("Format name"))
      .defaultsTo(ImmutableList.of("fast"))
      .ofTypes(ImmutableList.of(clipboardFormat_Key))
      .build();
   private final ArgAcceptingCommandFlag pagePart = CommandParts.flag('p', TextComponent.of("Page to view."))
      .withRequiredArg()
      .argNamed(TranslatableComponent.of("page"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final NoArgCommandFlag oldFirstPart = CommandParts.flag('d', TextComponent.of("Sort by date, oldest first")).build();
   private final NoArgCommandFlag newFirstPart = CommandParts.flag('n', TextComponent.of("Sort by date, newest first")).build();
   private final ArgAcceptingCommandFlag formatNamePart3 = CommandParts.flag('f', TextComponent.of("Restricts by format."))
      .withRequiredArg()
      .argNamed(TranslatableComponent.of("formatName"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(string_Key))
      .build();
   private final CommandArgument filterPart = CommandParts.arg(TranslatableComponent.of("filter"), TextComponent.of("Filter for schematics"))
      .defaultsTo(ImmutableList.of("all"))
      .ofTypes(ImmutableList.of(string_Key))
      .build();

   private SchematicCommandsRegistration() {
      this.listeners = ImmutableList.of();
   }

   public static SchematicCommandsRegistration builder() {
      return new SchematicCommandsRegistration();
   }

   public SchematicCommandsRegistration commandManager(CommandManager commandManager) {
      this.commandManager = commandManager;
      return this;
   }

   public SchematicCommandsRegistration containerInstance(SchematicCommands containerInstance) {
      this.containerInstance = containerInstance;
      return this;
   }

   public SchematicCommandsRegistration commandPermissionsConditionGenerator(CommandPermissionsConditionGenerator commandPermissionsConditionGenerator) {
      this.commandPermissionsConditionGenerator = commandPermissionsConditionGenerator;
      return this;
   }

   public SchematicCommandsRegistration listeners(Collection<CommandCallListener> listeners) {
      this.listeners = ImmutableList.copyOf(listeners);
      return this;
   }

   @Override
   public void build() {
      this.commandManager
         .register(
            "loadall",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Load multiple clipboards (paste will randomly choose one)"));
               b.parts(ImmutableList.of(this.formatNamePart, this.filenamePart, this.overwritePart, this.randomRotatePart, this.dynamicRandomPart));
               b.action(this::cmd$loadall);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  SchematicCommands.class, "loadall", Actor.class, LocalSession.class, String.class, String.class, boolean.class, boolean.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("clear", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Clear your clipboard"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$clear);
         Method commandMethod = RegistrationUtil.getCommandMethod(SchematicCommands.class, "clear", Actor.class, LocalSession.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("unload", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Remove a clipboard from your multi-clipboard"));
         b.parts(ImmutableList.of(this.fileNamePart));
         b.action(this::cmd$unload);
         Method commandMethod = RegistrationUtil.getCommandMethod(SchematicCommands.class, "unload", Actor.class, LocalSession.class, String.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("move", b -> {
         b.aliases(ImmutableList.of("m"));
         b.description(TextComponent.of("Move your loaded schematic"));
         b.parts(ImmutableList.of(this.directoryPart));
         b.action(this::cmd$move);
         Method commandMethod = RegistrationUtil.getCommandMethod(SchematicCommands.class, "move", Actor.class, LocalSession.class, String.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager
         .register(
            "load",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Load a schematic into your clipboard"));
               b.parts(ImmutableList.of(this.filenamePart, this.formatNamePart2, this.randomRotatePart2, this.dynamicRandomPart));
               b.action(this::cmd$load);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  SchematicCommands.class, "load", Actor.class, LocalSession.class, String.class, String.class, boolean.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "save",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Save your clipboard into a schematic file"));
               b.parts(ImmutableList.of(this.filenamePart, this.formatPart, this.allowOverwritePart, this.globalPart));
               b.action(this::cmd$save);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  SchematicCommands.class, "save", Actor.class, LocalSession.class, String.class, ClipboardFormat.class, boolean.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "share",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Share your clipboard as a schematic online"));
               b.parts(ImmutableList.of(this.schematicNamePart, this.destinationPart, this.formatPart2));
               b.action(this::cmd$share);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  SchematicCommands.class, "share", Actor.class, LocalSession.class, String.class, ClipboardShareDestination.class, ClipboardFormat.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("delete", b -> {
         b.aliases(ImmutableList.of("d"));
         b.description(TextComponent.of("Delete a saved schematic"));
         b.parts(ImmutableList.of(this.filenamePart));
         b.action(this::cmd$delete);
         Method commandMethod = RegistrationUtil.getCommandMethod(SchematicCommands.class, "delete", Actor.class, LocalSession.class, String.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("formats", b -> {
         b.aliases(ImmutableList.of("listformats", "f"));
         b.description(TextComponent.of("List available formats"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$formats);
         Method commandMethod = RegistrationUtil.getCommandMethod(SchematicCommands.class, "formats", Actor.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager
         .register(
            "list",
            b -> {
               b.aliases(ImmutableList.of("all", "ls"));
               b.description(TextComponent.of("List saved schematics"));
               b.footer(TextComponent.of("Note: Format is not fully verified until loading."));
               b.parts(ImmutableList.of(this.pagePart, this.oldFirstPart, this.newFirstPart, this.formatNamePart3, this.filterPart));
               b.action(this::cmd$list);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  SchematicCommands.class,
                  "list",
                  Actor.class,
                  LocalSession.class,
                  int.class,
                  boolean.class,
                  boolean.class,
                  String.class,
                  String.class,
                  Arguments.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
   }

   private int cmd$loadall(CommandParameters parameters) throws FilenameException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         SchematicCommands.class, "loadall", Actor.class, LocalSession.class, String.class, String.class, boolean.class, boolean.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .loadall(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$formatName(parameters),
               this.extract$filename(parameters),
               this.extract$overwrite(parameters),
               this.extract$randomRotate(parameters),
               this.extract$dynamicRandom(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$clear(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(SchematicCommands.class, "clear", Actor.class, LocalSession.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.clear(this.extract$actor(parameters), this.extract$session(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$unload(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(SchematicCommands.class, "unload", Actor.class, LocalSession.class, String.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.unload(this.extract$actor(parameters), this.extract$session(parameters), this.extract$fileName(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$move(CommandParameters parameters) throws WorldEditException, IOException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(SchematicCommands.class, "move", Actor.class, LocalSession.class, String.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.move(this.extract$actor(parameters), this.extract$session(parameters), this.extract$directory(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$load(CommandParameters parameters) throws FilenameException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         SchematicCommands.class, "load", Actor.class, LocalSession.class, String.class, String.class, boolean.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .load(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$filename(parameters),
               this.extract$formatName2(parameters),
               this.extract$randomRotate2(parameters),
               this.extract$dynamicRandom(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$save(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         SchematicCommands.class, "save", Actor.class, LocalSession.class, String.class, ClipboardFormat.class, boolean.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .save(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$filename(parameters),
               this.extract$format(parameters),
               this.extract$allowOverwrite(parameters),
               this.extract$global(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$share(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         SchematicCommands.class, "share", Actor.class, LocalSession.class, String.class, ClipboardShareDestination.class, ClipboardFormat.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .share(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$schematicName(parameters),
               this.extract$destination(parameters),
               this.extract$format2(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$delete(CommandParameters parameters) throws WorldEditException, IOException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(SchematicCommands.class, "delete", Actor.class, LocalSession.class, String.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.delete(this.extract$actor(parameters), this.extract$session(parameters), this.extract$filename(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$formats(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(SchematicCommands.class, "formats", Actor.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.formats(this.extract$actor(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$list(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         SchematicCommands.class, "list", Actor.class, LocalSession.class, int.class, boolean.class, boolean.class, String.class, String.class, Arguments.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .list(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$page(parameters),
               this.extract$oldFirst(parameters),
               this.extract$newFirst(parameters),
               this.extract$formatName3(parameters),
               this.extract$filter(parameters),
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

   private Actor extract$actor(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(actor_Key, "actor", parameters.injectedValue(actor_Key));
   }

   private LocalSession extract$session(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(localSession_Key, "session", parameters.injectedValue(localSession_Key));
   }

   private String extract$formatName(CommandParameters parameters) {
      return this.formatNamePart.value(parameters).asSingle(string_Key);
   }

   private String extract$filename(CommandParameters parameters) {
      return this.filenamePart.value(parameters).asSingle(string_Key);
   }

   private boolean extract$overwrite(CommandParameters parameters) {
      return this.overwritePart.in(parameters);
   }

   private boolean extract$randomRotate(CommandParameters parameters) {
      return this.randomRotatePart.in(parameters);
   }

   private boolean extract$dynamicRandom(CommandParameters parameters) {
      return this.dynamicRandomPart.in(parameters);
   }

   private String extract$fileName(CommandParameters parameters) {
      return this.fileNamePart.value(parameters).asSingle(string_Key);
   }

   private String extract$directory(CommandParameters parameters) {
      return this.directoryPart.value(parameters).asSingle(string_Key);
   }

   private String extract$formatName2(CommandParameters parameters) {
      return this.formatNamePart2.value(parameters).asSingle(string_Key);
   }

   private boolean extract$randomRotate2(CommandParameters parameters) {
      return this.randomRotatePart2.in(parameters);
   }

   private ClipboardFormat extract$format(CommandParameters parameters) {
      return this.formatPart.value(parameters).asSingle(clipboardFormat_Key);
   }

   private boolean extract$allowOverwrite(CommandParameters parameters) {
      return this.allowOverwritePart.in(parameters);
   }

   private boolean extract$global(CommandParameters parameters) {
      return this.globalPart.in(parameters);
   }

   private String extract$schematicName(CommandParameters parameters) {
      return this.schematicNamePart.value(parameters).asSingle(string_Key);
   }

   private ClipboardShareDestination extract$destination(CommandParameters parameters) {
      return this.destinationPart.value(parameters).asSingle(clipboardShareDestination_Key);
   }

   private ClipboardFormat extract$format2(CommandParameters parameters) {
      return this.formatPart2.value(parameters).asSingle(clipboardFormat_Key);
   }

   private int extract$page(CommandParameters parameters) {
      return this.pagePart.value(parameters).asSingle(integer_Key);
   }

   private boolean extract$oldFirst(CommandParameters parameters) {
      return this.oldFirstPart.in(parameters);
   }

   private boolean extract$newFirst(CommandParameters parameters) {
      return this.newFirstPart.in(parameters);
   }

   private String extract$formatName3(CommandParameters parameters) {
      return this.formatNamePart3.value(parameters).asSingle(string_Key);
   }

   private String extract$filter(CommandParameters parameters) {
      return this.filterPart.value(parameters).asSingle(string_Key);
   }

   private Arguments extract$arguments(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(arguments_Key, "arguments", parameters.injectedValue(arguments_Key));
   }
}
