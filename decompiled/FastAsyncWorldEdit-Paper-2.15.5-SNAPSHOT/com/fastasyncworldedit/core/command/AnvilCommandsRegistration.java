package com.fastasyncworldedit.core.command;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.biome.BiomeType;
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
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandParts;
import org.enginehub.piston.part.NoArgCommandFlag;

public final class AnvilCommandsRegistration implements CommandRegistration<AnvilCommands>, CommandPermissionsConditionGenerator.Registration {
   private static final Key<String> string_Key = Key.of(String.class);
   private static final Key<Integer> integer_Key = Key.of(Integer.class);
   private static final Key<Player> player_Key = Key.of(Player.class);
   private static final Key<EditSession> editSession_Key = Key.of(EditSession.class);
   private static final Key<Region> region_selection_Key = Key.of(Region.class, Selection.class);
   private static final Key<BiomeType> biomeType_Key = Key.of(BiomeType.class);
   private static final Key<Pattern> pattern_Key = Key.of(Pattern.class);
   private static final Key<LocalSession> localSession_Key = Key.of(LocalSession.class);
   private CommandManager commandManager;
   private AnvilCommands containerInstance;
   private CommandPermissionsConditionGenerator commandPermissionsConditionGenerator;
   private ImmutableList<CommandCallListener> listeners;
   private final CommandArgument fromPatternPart = CommandParts.arg(TranslatableComponent.of("from"), TextComponent.of("String"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(string_Key))
      .build();
   private final NoArgCommandFlag useDataPart = CommandParts.flag('d', TextComponent.of("Disable wildcard data matching")).build();
   private final CommandArgument fileDurationMillisPart = CommandParts.arg(TranslatableComponent.of("fileDurationMillis"), TextComponent.of("int"))
      .defaultsTo(ImmutableList.of("60000"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final NoArgCommandFlag debugPart = CommandParts.flag('d', TextComponent.of("The flag will debug the task")).build();
   private final NoArgCommandFlag deleteUnvisitedPart = CommandParts.flag('v', TextComponent.of("Delete unvisited chunks")).build();
   private final NoArgCommandFlag unsafePart = CommandParts.flag('u', TextComponent.of("The flag will run the task unsafe")).build();
   private final CommandArgument fromPart = CommandParts.arg(TranslatableComponent.of("from"), TextComponent.of("String"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(string_Key))
      .build();
   private final NoArgCommandFlag useDataPart2 = CommandParts.flag('d', TextComponent.of("The flag specifies the data to use")).build();
   private final NoArgCommandFlag useMapPart = CommandParts.flag('m', TextComponent.of("The flag specifies the map to use")).build();
   private final NoArgCommandFlag alignChunkPart = CommandParts.flag('c', TextComponent.of("Align to chunks")).build();

   private AnvilCommandsRegistration() {
      this.listeners = ImmutableList.of();
   }

   public static AnvilCommandsRegistration builder() {
      return new AnvilCommandsRegistration();
   }

   public AnvilCommandsRegistration commandManager(CommandManager commandManager) {
      this.commandManager = commandManager;
      return this;
   }

   public AnvilCommandsRegistration containerInstance(AnvilCommands containerInstance) {
      this.containerInstance = containerInstance;
      return this;
   }

   public AnvilCommandsRegistration commandPermissionsConditionGenerator(CommandPermissionsConditionGenerator commandPermissionsConditionGenerator) {
      this.commandPermissionsConditionGenerator = commandPermissionsConditionGenerator;
      return this;
   }

   public AnvilCommandsRegistration listeners(Collection<CommandCallListener> listeners) {
      this.listeners = ImmutableList.copyOf(listeners);
      return this;
   }

   @Override
   public void build() {
      this.commandManager
         .register(
            "replaceall",
            b -> {
               b.aliases(ImmutableList.of("rea", "repall"));
               b.description(TextComponent.of("Replace all blocks in the selection with another"));
               b.parts(ImmutableList.of(this.fromPatternPart, this.useDataPart));
               b.action(this::cmd$replaceall);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  AnvilCommands.class, "replaceAll", Player.class, String.class, String.class, String.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("remapall", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Remap the world between MCPE/PC values"));
         b.footer(TextComponent.of("Remap the world between MCPE/PC values"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$remapall);
         Method commandMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "remapall", Player.class, String.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager
         .register(
            "deleteallunvisited",
            b -> {
               b.aliases(ImmutableList.of("delunvisited"));
               b.description(TextComponent.of("Delete all chunks which haven't been occupied"));
               b.footer(
                  TextComponent.of(
                     "occupied for `age-ticks` (20t = 1s) and \nHave not been accessed since `file-duration` (ms) after creation and\nHave not been used in the past `chunk-inactivity` (ms)The auto-save interval is the recommended value for `file-duration` and `chunk-inactivity`"
                  )
               );
               b.parts(ImmutableList.of(this.fileDurationMillisPart));
               b.action(this::cmd$deleteallunvisited);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  AnvilCommands.class, "deleteAllUnvisited", Player.class, String.class, int.class, int.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "deleteallunclaimed",
            b -> {
               b.aliases(ImmutableList.of("delallunclaimed"));
               b.description(TextComponent.of("Delete all chunks which haven't been occupied"));
               b.footer(
                  TextComponent.of(
                     "Supports: WorldGuard, PlotSquared, GriefPrevention:\nDelete all chunks which aren't claimed AND haven't been occupied for `age-ticks` (20t = 1s) and \nHave not been accessed since `file-duration` (ms) after creation and\nHave not been used in the past `chunk-inactivity` (ms)The auto-save interval is the recommended value for `file-duration` and `chunk-inactivity`"
                  )
               );
               b.parts(ImmutableList.of(this.fileDurationMillisPart, this.debugPart));
               b.action(this::cmd$deleteallunclaimed);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  AnvilCommands.class, "deleteAllUnclaimed", Player.class, int.class, int.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "deleteunclaimed",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Delete all chunks which haven't been occupied"));
               b.footer(
                  TextComponent.of(
                     "(Supports: WorldGuard, PlotSquared, GriefPrevention):\nIs not claimed\nHas not been occupied for `age-ticks` (20t = 1s) and \nHave not been accessed since `file-duration` (ms) after creation and\nHave not been used in the past `chunk-inactivity` (ms)The auto-save interval is the recommended value for `file-duration` and `chunk-inactivity`"
                  )
               );
               b.parts(ImmutableList.of(this.fileDurationMillisPart, this.debugPart));
               b.action(this::cmd$deleteunclaimed);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  AnvilCommands.class, "deleteUnclaimed", Player.class, EditSession.class, Region.class, int.class, int.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "deletealloldregions",
            b -> {
               b.aliases(ImmutableList.of("deloldreg"));
               b.description(TextComponent.of("Delete regions which haven't been accessed in a certain amount of time"));
               b.footer(
                  TextComponent.of(
                     "You can use seconds (s), minutes (m), hours (h), days (d), weeks (w), years (y)\n(months are not a unit of time) e.g., 8h5m12s\n"
                  )
               );
               b.parts(ImmutableList.of());
               b.action(this::cmd$deletealloldregions);
               Method commandMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "deleteAllOldRegions", Player.class, String.class, String.class);
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("trimallplots", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Trim chunks in a Plot World"));
         b.footer(TextComponent.of("Unclaimed chunks will be deleted\nUnmodified chunks will be deleted\n"));
         b.parts(ImmutableList.of(this.deleteUnvisitedPart));
         b.action(this::cmd$trimallplots);
         Method commandMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "trimAllPlots", Player.class, boolean.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager
         .register(
            "deletebiomechunks",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Delete chunks matching a specific biome"));
               b.parts(ImmutableList.of(this.unsafePart));
               b.action(this::cmd$deletebiomechunks);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  AnvilCommands.class, "deleteBiome", Player.class, String.class, BiomeType.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("trimallair", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Trim all air in the world"));
         b.parts(ImmutableList.of(this.unsafePart));
         b.action(this::cmd$trimallair);
         Method commandMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "trimAllAir", Player.class, String.class, boolean.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("debugfixroads", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("debug - do not use"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$debugfixroads);
         Method commandMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "debugfixroads", Player.class, String.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager
         .register(
            "replaceallpattern",
            b -> {
               b.aliases(ImmutableList.of("reap", "repallpat"));
               b.description(TextComponent.of("Replace all blocks in the selection with another"));
               b.parts(ImmutableList.of(this.fromPart, this.useDataPart2, this.useMapPart));
               b.action(this::cmd$replaceallpattern);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  AnvilCommands.class, "replaceAllPattern", Player.class, String.class, String.class, Pattern.class, boolean.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "countall",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Count all blocks in a world"));
               b.parts(ImmutableList.of(this.useDataPart2));
               b.action(this::cmd$countall);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  AnvilCommands.class, "countAll", Player.class, EditSession.class, String.class, String.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("clear", b -> {
         b.aliases(ImmutableList.of("unset"));
         b.description(TextComponent.of("Clear the chunks in a selection (delete without defrag)"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$clear);
         Method commandMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "unset", Player.class, EditSession.class, Region.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager
         .register(
            "count",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Count blocks in a selection"));
               b.parts(ImmutableList.of(this.useDataPart2));
               b.action(this::cmd$count);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  AnvilCommands.class, "count", Player.class, EditSession.class, Region.class, String.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("distr", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Replace all blocks in the selection with another"));
         b.parts(ImmutableList.of(this.useDataPart2));
         b.action(this::cmd$distr);
         Method commandMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "distr", Player.class, EditSession.class, Region.class, boolean.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager
         .register(
            "replace",
            b -> {
               b.aliases(ImmutableList.of("r"));
               b.description(TextComponent.of("Replace all blocks in the selection with another"));
               b.parts(ImmutableList.of(this.fromPart, this.useDataPart2));
               b.action(this::cmd$replace);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  AnvilCommands.class, "replace", Player.class, EditSession.class, Region.class, String.class, String.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "replacepattern",
            b -> {
               b.aliases(ImmutableList.of("preplace", "rp"));
               b.description(TextComponent.of("Replace all blocks in the selection with a pattern"));
               b.parts(ImmutableList.of(this.fromPart, this.useDataPart2, this.useMapPart));
               b.action(this::cmd$replacepattern);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  AnvilCommands.class,
                  "replacePattern",
                  Player.class,
                  EditSession.class,
                  Region.class,
                  String.class,
                  Pattern.class,
                  boolean.class,
                  boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("set", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Set all blocks in the selection with a pattern"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$set);
         Method commandMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "set", Player.class, EditSession.class, Region.class, Pattern.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager
         .register(
            "removelayers",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Removes matching chunk layers"));
               b.footer(TextComponent.of("Only if a chunk matches the provided id"));
               b.parts(ImmutableList.of());
               b.action(this::cmd$removelayers);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  AnvilCommands.class, "removeLayers", Player.class, EditSession.class, Region.class, int.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "copy",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Lazily copy chunks to your anvil clipboard"));
               b.parts(ImmutableList.of());
               b.action(this::cmd$copy);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  AnvilCommands.class, "copy", Player.class, LocalSession.class, EditSession.class, Region.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "paste",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Paste chunks from your anvil clipboard"));
               b.footer(TextComponent.of("Paste the chunks from your anvil clipboard."));
               b.parts(ImmutableList.of(this.alignChunkPart));
               b.action(this::cmd$paste);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  AnvilCommands.class, "paste", Player.class, LocalSession.class, EditSession.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
   }

   private int cmd$replaceall(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         AnvilCommands.class, "replaceAll", Player.class, String.class, String.class, String.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .replaceAll(
               this.extract$player(parameters),
               this.extract$folder(parameters),
               this.extract$fromPattern(parameters),
               this.extract$toPatternStr(parameters),
               this.extract$useData(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$remapall(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "remapall", Player.class, String.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.remapall(this.extract$player(parameters), this.extract$folder(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$deleteallunvisited(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "deleteAllUnvisited", Player.class, String.class, int.class, int.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .deleteAllUnvisited(
               this.extract$player(parameters),
               this.extract$folder(parameters),
               this.extract$inhabitedTicks(parameters),
               this.extract$fileDurationMillis(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$deleteallunclaimed(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "deleteAllUnclaimed", Player.class, int.class, int.class, boolean.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .deleteAllUnclaimed(
               this.extract$player(parameters),
               this.extract$inhabitedTicks(parameters),
               this.extract$fileDurationMillis(parameters),
               this.extract$debug(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$deleteunclaimed(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         AnvilCommands.class, "deleteUnclaimed", Player.class, EditSession.class, Region.class, int.class, int.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .deleteUnclaimed(
               this.extract$player(parameters),
               this.extract$editSession(parameters),
               this.extract$selection(parameters),
               this.extract$inhabitedTicks(parameters),
               this.extract$fileDurationMillis(parameters),
               this.extract$debug(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$deletealloldregions(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "deleteAllOldRegions", Player.class, String.class, String.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.deleteAllOldRegions(this.extract$player(parameters), this.extract$folder(parameters), this.extract$time(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$trimallplots(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "trimAllPlots", Player.class, boolean.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.trimAllPlots(this.extract$player(parameters), this.extract$deleteUnvisited(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$deletebiomechunks(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "deleteBiome", Player.class, String.class, BiomeType.class, boolean.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .deleteBiome(this.extract$player(parameters), this.extract$folder(parameters), this.extract$biome(parameters), this.extract$unsafe(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$trimallair(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "trimAllAir", Player.class, String.class, boolean.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.trimAllAir(this.extract$player(parameters), this.extract$folder(parameters), this.extract$unsafe(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$debugfixroads(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "debugfixroads", Player.class, String.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.debugfixroads(this.extract$player(parameters), this.extract$folder(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$replaceallpattern(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         AnvilCommands.class, "replaceAllPattern", Player.class, String.class, String.class, Pattern.class, boolean.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .replaceAllPattern(
               this.extract$player(parameters),
               this.extract$folder(parameters),
               this.extract$from(parameters),
               this.extract$toPattern(parameters),
               this.extract$useData2(parameters),
               this.extract$useMap(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$countall(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         AnvilCommands.class, "countAll", Player.class, EditSession.class, String.class, String.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .countAll(
               this.extract$player(parameters),
               this.extract$editSession(parameters),
               this.extract$folder(parameters),
               this.extract$argStr(parameters),
               this.extract$useData2(parameters)
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
      Method cmdMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "unset", Player.class, EditSession.class, Region.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.unset(this.extract$player(parameters), this.extract$editSession(parameters), this.extract$selection(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$count(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         AnvilCommands.class, "count", Player.class, EditSession.class, Region.class, String.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .count(
               this.extract$player(parameters),
               this.extract$editSession(parameters),
               this.extract$selection(parameters),
               this.extract$argStr(parameters),
               this.extract$useData2(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$distr(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "distr", Player.class, EditSession.class, Region.class, boolean.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .distr(this.extract$player(parameters), this.extract$editSession(parameters), this.extract$selection(parameters), this.extract$useData2(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$replace(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         AnvilCommands.class, "replace", Player.class, EditSession.class, Region.class, String.class, String.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .replace(
               this.extract$player(parameters),
               this.extract$editSession(parameters),
               this.extract$selection(parameters),
               this.extract$from(parameters),
               this.extract$toStr(parameters),
               this.extract$useData2(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$replacepattern(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         AnvilCommands.class, "replacePattern", Player.class, EditSession.class, Region.class, String.class, Pattern.class, boolean.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .replacePattern(
               this.extract$player(parameters),
               this.extract$editSession(parameters),
               this.extract$selection(parameters),
               this.extract$from(parameters),
               this.extract$toPattern(parameters),
               this.extract$useData2(parameters),
               this.extract$useMap(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$set(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "set", Player.class, EditSession.class, Region.class, Pattern.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .set(this.extract$player(parameters), this.extract$editSession(parameters), this.extract$selection(parameters), this.extract$toPattern(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$removelayers(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "removeLayers", Player.class, EditSession.class, Region.class, int.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .removeLayers(
               this.extract$player(parameters), this.extract$editSession(parameters), this.extract$selection(parameters), this.extract$id(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$copy(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "copy", Player.class, LocalSession.class, EditSession.class, Region.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .copy(this.extract$player(parameters), this.extract$session(parameters), this.extract$editSession(parameters), this.extract$selection(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$paste(CommandParameters parameters) throws WorldEditException, IOException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(AnvilCommands.class, "paste", Player.class, LocalSession.class, EditSession.class, boolean.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .paste(this.extract$player(parameters), this.extract$session(parameters), this.extract$editSession(parameters), this.extract$alignChunk(parameters));
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

   private String extract$folder(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(string_Key, "folder", parameters.injectedValue(string_Key));
   }

   private String extract$fromPattern(CommandParameters parameters) {
      return this.fromPatternPart.value(parameters).asSingle(string_Key);
   }

   private String extract$toPatternStr(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(string_Key, "toPatternStr", parameters.injectedValue(string_Key));
   }

   private boolean extract$useData(CommandParameters parameters) {
      return this.useDataPart.in(parameters);
   }

   private int extract$inhabitedTicks(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(integer_Key, "inhabitedTicks", parameters.injectedValue(integer_Key));
   }

   private int extract$fileDurationMillis(CommandParameters parameters) {
      return this.fileDurationMillisPart.value(parameters).asSingle(integer_Key);
   }

   private boolean extract$debug(CommandParameters parameters) {
      return this.debugPart.in(parameters);
   }

   private EditSession extract$editSession(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(editSession_Key, "editSession", parameters.injectedValue(editSession_Key));
   }

   private Region extract$selection(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(region_selection_Key, "selection", parameters.injectedValue(region_selection_Key));
   }

   private String extract$time(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(string_Key, "time", parameters.injectedValue(string_Key));
   }

   private boolean extract$deleteUnvisited(CommandParameters parameters) {
      return this.deleteUnvisitedPart.in(parameters);
   }

   private BiomeType extract$biome(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(biomeType_Key, "biome", parameters.injectedValue(biomeType_Key));
   }

   private boolean extract$unsafe(CommandParameters parameters) {
      return this.unsafePart.in(parameters);
   }

   private String extract$from(CommandParameters parameters) {
      return this.fromPart.value(parameters).asSingle(string_Key);
   }

   private Pattern extract$toPattern(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(pattern_Key, "toPattern", parameters.injectedValue(pattern_Key));
   }

   private boolean extract$useData2(CommandParameters parameters) {
      return this.useDataPart2.in(parameters);
   }

   private boolean extract$useMap(CommandParameters parameters) {
      return this.useMapPart.in(parameters);
   }

   private String extract$argStr(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(string_Key, "argStr", parameters.injectedValue(string_Key));
   }

   private String extract$toStr(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(string_Key, "toStr", parameters.injectedValue(string_Key));
   }

   private int extract$id(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(integer_Key, "id", parameters.injectedValue(integer_Key));
   }

   private LocalSession extract$session(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(localSession_Key, "session", parameters.injectedValue(localSession_Key));
   }

   private boolean extract$alignChunk(CommandParameters parameters) {
      return this.alignChunkPart.in(parameters);
   }
}
