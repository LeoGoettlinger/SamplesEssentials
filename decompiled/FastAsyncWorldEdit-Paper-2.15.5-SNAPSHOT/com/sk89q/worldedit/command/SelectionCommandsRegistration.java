package com.sk89q.worldedit.command;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.argument.SelectorChoice;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.annotation.MultiDirection;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;
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
import org.enginehub.piston.part.ArgAcceptingCommandFlag;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandParts;
import org.enginehub.piston.part.NoArgCommandFlag;

public final class SelectionCommandsRegistration implements CommandRegistration<SelectionCommands>, CommandPermissionsConditionGenerator.Registration {
   private static final Key<BlockVector3> blockVector3_Key = Key.of(BlockVector3.class);
   private static final Key<Integer> integer_Key = Key.of(Integer.class);
   private static final Key<BlockVector3> blockVector3_multiDirection_Key = Key.of(BlockVector3.class, MultiDirection.class);
   private static final Key<Mask> mask_Key = Key.of(Mask.class);
   private static final Key<SelectorChoice> selectorChoice_Key = Key.of(SelectorChoice.class);
   private static final Key<Actor> actor_Key = Key.of(Actor.class);
   private static final Key<World> world_Key = Key.of(World.class);
   private static final Key<LocalSession> localSession_Key = Key.of(LocalSession.class);
   private static final Key<Player> player_Key = Key.of(Player.class);
   private static final Key<EditSession> editSession_Key = Key.of(EditSession.class);
   private CommandManager commandManager;
   private SelectionCommands containerInstance;
   private CommandPermissionsConditionGenerator commandPermissionsConditionGenerator;
   private ImmutableList<CommandCallListener> listeners;
   private final CommandArgument coordinatesPart = CommandParts.arg(
         TranslatableComponent.of("coordinates"), TextComponent.of("Coordinates to set position 1 to")
      )
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(blockVector3_Key))
      .build();
   private final CommandArgument coordinatesPart2 = CommandParts.arg(
         TranslatableComponent.of("coordinates"), TextComponent.of("Coordinates to set position 2 to")
      )
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(blockVector3_Key))
      .build();
   private final CommandArgument coordinatesPart3 = CommandParts.arg(TranslatableComponent.of("coordinates"), TextComponent.of("The chunk to select"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(blockVector3_Key))
      .build();
   private final NoArgCommandFlag expandSelectionPart = CommandParts.flag(
         's', TextComponent.of("Expand your selection to encompass all chunks that are part of it")
      )
      .build();
   private final NoArgCommandFlag useChunkCoordinatesPart = CommandParts.flag('c', TextComponent.of("Use chunk coordinates instead of block coordinates"))
      .build();
   private final NoArgCommandFlag navWandPart = CommandParts.flag('n', TextComponent.of("Get a navigation wand")).build();
   private final CommandArgument amountPart = CommandParts.arg(TranslatableComponent.of("amount"), TextComponent.of("Amount to contract the selection by"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument reverseAmountPart = CommandParts.arg(
         TranslatableComponent.of("reverseAmount"), TextComponent.of("Amount to contract the selection by in the other direction")
      )
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument directionPart = CommandParts.arg(TranslatableComponent.of("direction"), TextComponent.of("Direction to contract"))
      .defaultsTo(ImmutableList.of("me"))
      .ofTypes(ImmutableList.of(blockVector3_multiDirection_Key))
      .build();
   private final CommandArgument amountPart2 = CommandParts.arg(TranslatableComponent.of("amount"), TextComponent.of("Amount to shift the selection by"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument amountPart3 = CommandParts.arg(
         TranslatableComponent.of("amount"), TextComponent.of("Amount to expand the selection by in all directions")
      )
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final NoArgCommandFlag onlyHorizontalPart = CommandParts.flag('h', TextComponent.of("Only expand horizontally")).build();
   private final NoArgCommandFlag onlyVerticalPart = CommandParts.flag('v', TextComponent.of("Only expand vertically")).build();
   private final CommandArgument amountPart4 = CommandParts.arg(
         TranslatableComponent.of("amount"), TextComponent.of("Amount to contract the selection by in all directions")
      )
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final NoArgCommandFlag onlyHorizontalPart2 = CommandParts.flag('h', TextComponent.of("Only contract horizontally")).build();
   private final NoArgCommandFlag onlyVerticalPart2 = CommandParts.flag('v', TextComponent.of("Only contract vertically")).build();
   private final CommandArgument maskPart = CommandParts.arg(TranslatableComponent.of("mask"), TextComponent.of("Mask of blocks to keep within the selection"))
      .defaultsTo(ImmutableList.of("#existing"))
      .ofTypes(ImmutableList.of(mask_Key))
      .build();
   private final NoArgCommandFlag clipboardInfoPart = CommandParts.flag('c', TextComponent.of("Get clipboard info instead")).build();
   private final CommandArgument maskPart2 = CommandParts.arg(TranslatableComponent.of("mask"), TextComponent.of("The mask of blocks to match"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(mask_Key))
      .build();
   private final NoArgCommandFlag clipboardDistrPart = CommandParts.flag('c', TextComponent.of("Get the distribution of the clipboard instead")).build();
   private final NoArgCommandFlag separateStatesPart = CommandParts.flag('d', TextComponent.of("Separate blocks by state")).build();
   private final ArgAcceptingCommandFlag pagePart = CommandParts.flag('p', TextComponent.of("Gets page from a previous distribution."))
      .withRequiredArg()
      .argNamed(TranslatableComponent.of("page"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument selectorPart = CommandParts.arg(TranslatableComponent.of("selector"), TextComponent.of("Selector to switch to"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(selectorChoice_Key))
      .build();
   private final NoArgCommandFlag setDefaultSelectorPart = CommandParts.flag('d', TextComponent.of("Set default selector")).build();

   private SelectionCommandsRegistration() {
      this.listeners = ImmutableList.of();
   }

   public static SelectionCommandsRegistration builder() {
      return new SelectionCommandsRegistration();
   }

   public SelectionCommandsRegistration commandManager(CommandManager commandManager) {
      this.commandManager = commandManager;
      return this;
   }

   public SelectionCommandsRegistration containerInstance(SelectionCommands containerInstance) {
      this.containerInstance = containerInstance;
      return this;
   }

   public SelectionCommandsRegistration commandPermissionsConditionGenerator(CommandPermissionsConditionGenerator commandPermissionsConditionGenerator) {
      this.commandPermissionsConditionGenerator = commandPermissionsConditionGenerator;
      return this;
   }

   public SelectionCommandsRegistration listeners(Collection<CommandCallListener> listeners) {
      this.listeners = ImmutableList.copyOf(listeners);
      return this;
   }

   @Override
   public void build() {
      this.commandManager
         .register(
            "/pos1",
            b -> {
               b.aliases(ImmutableList.of("/1"));
               b.description(TextComponent.of("Set position 1"));
               b.parts(ImmutableList.of(this.coordinatesPart));
               b.action(this::cmd$_pos1);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  SelectionCommands.class, "pos1", Actor.class, World.class, LocalSession.class, BlockVector3.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/pos2",
            b -> {
               b.aliases(ImmutableList.of("/2"));
               b.description(TextComponent.of("Set position 2"));
               b.parts(ImmutableList.of(this.coordinatesPart2));
               b.action(this::cmd$_pos2);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  SelectionCommands.class, "pos2", Actor.class, World.class, LocalSession.class, BlockVector3.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("/hpos1", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Set position 1 to targeted block"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$_hpos1);
         Method commandMethod = RegistrationUtil.getCommandMethod(SelectionCommands.class, "hpos1", Player.class, LocalSession.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/hpos2", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Set position 2 to targeted block"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$_hpos2);
         Method commandMethod = RegistrationUtil.getCommandMethod(SelectionCommands.class, "hpos2", Player.class, LocalSession.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager
         .register(
            "/chunk",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Set the selection to your current chunk."));
               b.footer(
                  TextComponent.of(
                     "This command selects 256-block-tall areas,\nwhich can be specified by the y-coordinate.\nE.g. -c x,1,z will select from y=256 to y=511."
                  )
               );
               b.parts(ImmutableList.of(this.coordinatesPart3, this.expandSelectionPart, this.useChunkCoordinatesPart));
               b.action(this::cmd$_chunk);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  SelectionCommands.class, "chunk", Actor.class, World.class, LocalSession.class, BlockVector3.class, boolean.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("/wand", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Get the wand object"));
         b.parts(ImmutableList.of(this.navWandPart));
         b.action(this::cmd$_wand);
         Method commandMethod = RegistrationUtil.getCommandMethod(SelectionCommands.class, "wand", Player.class, LocalSession.class, boolean.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("toggleeditwand", b -> {
         b.aliases(ImmutableList.of("/toggleeditwand"));
         b.description(TextComponent.of("Remind the user that the wand is now a tool and can be unbound with /tool none."));
         b.parts(ImmutableList.of());
         b.action(this::cmd$toggleeditwand);
         Method commandMethod = RegistrationUtil.getCommandMethod(SelectionCommands.class, "toggleWand", Player.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager
         .register(
            "/contract",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Contract the selection area"));
               b.parts(ImmutableList.of(this.amountPart, this.reverseAmountPart, this.directionPart));
               b.action(this::cmd$_contract);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  SelectionCommands.class, "contract", Actor.class, World.class, LocalSession.class, int.class, int.class, List.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/shift",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Shift the selection area"));
               b.parts(ImmutableList.of(this.amountPart2, this.directionPart));
               b.action(this::cmd$_shift);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  SelectionCommands.class, "shift", Actor.class, World.class, LocalSession.class, int.class, List.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/outset",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Outset the selection area"));
               b.parts(ImmutableList.of(this.amountPart3, this.onlyHorizontalPart, this.onlyVerticalPart));
               b.action(this::cmd$_outset);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  SelectionCommands.class, "outset", Actor.class, World.class, LocalSession.class, int.class, boolean.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/inset",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Inset the selection area"));
               b.parts(ImmutableList.of(this.amountPart4, this.onlyHorizontalPart2, this.onlyVerticalPart2));
               b.action(this::cmd$_inset);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  SelectionCommands.class, "inset", Actor.class, World.class, LocalSession.class, int.class, boolean.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/trim",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Minimize the selection to encompass matching blocks"));
               b.parts(ImmutableList.of(this.maskPart));
               b.action(this::cmd$_trim);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  SelectionCommands.class, "trim", Actor.class, World.class, LocalSession.class, EditSession.class, Mask.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("/size", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Get information about the selection"));
         b.parts(ImmutableList.of(this.clipboardInfoPart));
         b.action(this::cmd$_size);
         Method commandMethod = RegistrationUtil.getCommandMethod(SelectionCommands.class, "size", Actor.class, World.class, LocalSession.class, boolean.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager
         .register(
            "/count",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Counts the number of blocks matching a mask"));
               b.parts(ImmutableList.of(this.maskPart2));
               b.action(this::cmd$_count);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  SelectionCommands.class, "count", Actor.class, World.class, LocalSession.class, EditSession.class, Mask.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/distr",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Get the distribution of blocks in the selection"));
               b.parts(ImmutableList.of(this.clipboardDistrPart, this.separateStatesPart, this.pagePart));
               b.action(this::cmd$_distr);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  SelectionCommands.class,
                  "distr",
                  Actor.class,
                  World.class,
                  LocalSession.class,
                  EditSession.class,
                  boolean.class,
                  boolean.class,
                  Integer.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/sel",
            b -> {
               b.aliases(ImmutableList.of(";", "/desel", "/deselect"));
               b.description(TextComponent.of("Choose a region selector"));
               b.parts(ImmutableList.of(this.selectorPart, this.setDefaultSelectorPart));
               b.action(this::cmd$_sel);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  SelectionCommands.class, "select", Actor.class, World.class, LocalSession.class, SelectorChoice.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
   }

   private int cmd$_pos1(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(SelectionCommands.class, "pos1", Actor.class, World.class, LocalSession.class, BlockVector3.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .pos1(this.extract$actor(parameters), this.extract$world(parameters), this.extract$session(parameters), this.extract$coordinates(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_pos2(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(SelectionCommands.class, "pos2", Actor.class, World.class, LocalSession.class, BlockVector3.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .pos2(this.extract$actor(parameters), this.extract$world(parameters), this.extract$session(parameters), this.extract$coordinates2(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_hpos1(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(SelectionCommands.class, "hpos1", Player.class, LocalSession.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.hpos1(this.extract$player(parameters), this.extract$session(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_hpos2(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(SelectionCommands.class, "hpos2", Player.class, LocalSession.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.hpos2(this.extract$player(parameters), this.extract$session(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_chunk(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         SelectionCommands.class, "chunk", Actor.class, World.class, LocalSession.class, BlockVector3.class, boolean.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .chunk(
               this.extract$actor(parameters),
               this.extract$world(parameters),
               this.extract$session(parameters),
               this.extract$coordinates3(parameters),
               this.extract$expandSelection(parameters),
               this.extract$useChunkCoordinates(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_wand(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(SelectionCommands.class, "wand", Player.class, LocalSession.class, boolean.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.wand(this.extract$player(parameters), this.extract$session(parameters), this.extract$navWand(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$toggleeditwand(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(SelectionCommands.class, "toggleWand", Player.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.toggleWand(this.extract$player(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_contract(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         SelectionCommands.class, "contract", Actor.class, World.class, LocalSession.class, int.class, int.class, List.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .contract(
               this.extract$actor(parameters),
               this.extract$world(parameters),
               this.extract$session(parameters),
               this.extract$amount(parameters),
               this.extract$reverseAmount(parameters),
               this.extract$direction(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_shift(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         SelectionCommands.class, "shift", Actor.class, World.class, LocalSession.class, int.class, List.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .shift(
               this.extract$actor(parameters),
               this.extract$world(parameters),
               this.extract$session(parameters),
               this.extract$amount2(parameters),
               this.extract$direction(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_outset(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         SelectionCommands.class, "outset", Actor.class, World.class, LocalSession.class, int.class, boolean.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .outset(
               this.extract$actor(parameters),
               this.extract$world(parameters),
               this.extract$session(parameters),
               this.extract$amount3(parameters),
               this.extract$onlyHorizontal(parameters),
               this.extract$onlyVertical(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_inset(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         SelectionCommands.class, "inset", Actor.class, World.class, LocalSession.class, int.class, boolean.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .inset(
               this.extract$actor(parameters),
               this.extract$world(parameters),
               this.extract$session(parameters),
               this.extract$amount4(parameters),
               this.extract$onlyHorizontal2(parameters),
               this.extract$onlyVertical2(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_trim(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         SelectionCommands.class, "trim", Actor.class, World.class, LocalSession.class, EditSession.class, Mask.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .trim(
               this.extract$actor(parameters),
               this.extract$world(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$mask(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_size(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(SelectionCommands.class, "size", Actor.class, World.class, LocalSession.class, boolean.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .size(this.extract$actor(parameters), this.extract$world(parameters), this.extract$session(parameters), this.extract$clipboardInfo(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_count(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         SelectionCommands.class, "count", Actor.class, World.class, LocalSession.class, EditSession.class, Mask.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .count(
               this.extract$actor(parameters),
               this.extract$world(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$mask2(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_distr(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         SelectionCommands.class, "distr", Actor.class, World.class, LocalSession.class, EditSession.class, boolean.class, boolean.class, Integer.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .distr(
               this.extract$actor(parameters),
               this.extract$world(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$clipboardDistr(parameters),
               this.extract$separateStates(parameters),
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

   private int cmd$_sel(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         SelectionCommands.class, "select", Actor.class, World.class, LocalSession.class, SelectorChoice.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .select(
               this.extract$actor(parameters),
               this.extract$world(parameters),
               this.extract$session(parameters),
               this.extract$selector(parameters),
               this.extract$setDefaultSelector(parameters)
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

   private World extract$world(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(world_Key, "world", parameters.injectedValue(world_Key));
   }

   private LocalSession extract$session(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(localSession_Key, "session", parameters.injectedValue(localSession_Key));
   }

   private BlockVector3 extract$coordinates(CommandParameters parameters) {
      return this.coordinatesPart.value(parameters).asSingle(blockVector3_Key);
   }

   private BlockVector3 extract$coordinates2(CommandParameters parameters) {
      return this.coordinatesPart2.value(parameters).asSingle(blockVector3_Key);
   }

   private Player extract$player(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(player_Key, "player", parameters.injectedValue(player_Key));
   }

   private BlockVector3 extract$coordinates3(CommandParameters parameters) {
      return this.coordinatesPart3.value(parameters).asSingle(blockVector3_Key);
   }

   private boolean extract$expandSelection(CommandParameters parameters) {
      return this.expandSelectionPart.in(parameters);
   }

   private boolean extract$useChunkCoordinates(CommandParameters parameters) {
      return this.useChunkCoordinatesPart.in(parameters);
   }

   private boolean extract$navWand(CommandParameters parameters) {
      return this.navWandPart.in(parameters);
   }

   private int extract$amount(CommandParameters parameters) {
      return this.amountPart.value(parameters).asSingle(integer_Key);
   }

   private int extract$reverseAmount(CommandParameters parameters) {
      return this.reverseAmountPart.value(parameters).asSingle(integer_Key);
   }

   private List<BlockVector3> extract$direction(CommandParameters parameters) {
      return this.directionPart.value(parameters).<BlockVector3>asMultiple(blockVector3_multiDirection_Key);
   }

   private int extract$amount2(CommandParameters parameters) {
      return this.amountPart2.value(parameters).asSingle(integer_Key);
   }

   private int extract$amount3(CommandParameters parameters) {
      return this.amountPart3.value(parameters).asSingle(integer_Key);
   }

   private boolean extract$onlyHorizontal(CommandParameters parameters) {
      return this.onlyHorizontalPart.in(parameters);
   }

   private boolean extract$onlyVertical(CommandParameters parameters) {
      return this.onlyVerticalPart.in(parameters);
   }

   private int extract$amount4(CommandParameters parameters) {
      return this.amountPart4.value(parameters).asSingle(integer_Key);
   }

   private boolean extract$onlyHorizontal2(CommandParameters parameters) {
      return this.onlyHorizontalPart2.in(parameters);
   }

   private boolean extract$onlyVertical2(CommandParameters parameters) {
      return this.onlyVerticalPart2.in(parameters);
   }

   private EditSession extract$editSession(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(editSession_Key, "editSession", parameters.injectedValue(editSession_Key));
   }

   private Mask extract$mask(CommandParameters parameters) {
      return this.maskPart.value(parameters).asSingle(mask_Key);
   }

   private boolean extract$clipboardInfo(CommandParameters parameters) {
      return this.clipboardInfoPart.in(parameters);
   }

   private Mask extract$mask2(CommandParameters parameters) {
      return this.maskPart2.value(parameters).asSingle(mask_Key);
   }

   private boolean extract$clipboardDistr(CommandParameters parameters) {
      return this.clipboardDistrPart.in(parameters);
   }

   private boolean extract$separateStates(CommandParameters parameters) {
      return this.separateStatesPart.in(parameters);
   }

   private Integer extract$page(CommandParameters parameters) {
      return this.pagePart.value(parameters).asSingle(integer_Key);
   }

   private SelectorChoice extract$selector(CommandParameters parameters) {
      return this.selectorPart.value(parameters).asSingle(selectorChoice_Key);
   }

   private boolean extract$setDefaultSelector(CommandParameters parameters) {
      return this.setDefaultSelectorPart.in(parameters);
   }
}
