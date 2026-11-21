package com.sk89q.worldedit.command;

import com.fastasyncworldedit.core.extent.ResettableExtent;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.HookMode;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.registry.Registry;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;
import java.io.FileNotFoundException;
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

public final class GeneralCommandsRegistration implements CommandRegistration<GeneralCommands>, CommandPermissionsConditionGenerator.Registration {
   private static final Key<Integer> integer_Key = Key.of(Integer.class);
   private static final Key<SideEffect> sideEffect_Key = Key.of(SideEffect.class);
   private static final Key<SideEffect.State> state_Key = Key.of(SideEffect.State.class);
   private static final Key<EditSession.ReorderMode> reorderMode_Key = Key.of(EditSession.ReorderMode.class);
   private static final Key<Boolean> boolean_Key = Key.of(Boolean.class);
   private static final Key<World> world_Key = Key.of(World.class);
   private static final Key<HookMode> hookMode_Key = Key.of(HookMode.class);
   private static final Key<Mask> mask_Key = Key.of(Mask.class);
   private static final Key<String> string_Key = Key.of(String.class);
   private static final Key<ResettableExtent> resettableExtent_Key = Key.of(ResettableExtent.class);
   private static final Key<Registry<?>> registry$__Key = Key.of(new TypeToken<Registry<?>>() {});
   private static final Key<Actor> actor_Key = Key.of(Actor.class);
   private static final Key<LocalSession> localSession_Key = Key.of(LocalSession.class);
   private static final Key<Player> player_Key = Key.of(Player.class);
   private static final Key<EditSession> editSession_Key = Key.of(EditSession.class);
   private CommandManager commandManager;
   private GeneralCommands containerInstance;
   private CommandPermissionsConditionGenerator commandPermissionsConditionGenerator;
   private ImmutableList<CommandCallListener> listeners;
   private final CommandArgument limitPart = CommandParts.arg(TranslatableComponent.of("limit"), TextComponent.of("The limit to set"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument limitPart2 = CommandParts.arg(TranslatableComponent.of("limit"), TextComponent.of("The timeout time to set"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument sideEffectPart = CommandParts.arg(TranslatableComponent.of("sideEffect"), TextComponent.of("The side effect"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(sideEffect_Key))
      .build();
   private final CommandArgument newStatePart = CommandParts.arg(TranslatableComponent.of("newState"), TextComponent.of("The new side effect state"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(state_Key))
      .build();
   private final NoArgCommandFlag showInfoBoxPart = CommandParts.flag('h', TextComponent.of("Show the info box")).build();
   private final CommandArgument reorderModePart = CommandParts.arg(TranslatableComponent.of("reorderMode"), TextComponent.of("The reorder mode"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(reorderMode_Key))
      .build();
   private final CommandArgument drawSelectionPart = CommandParts.arg(
         TranslatableComponent.of("drawSelection"), TextComponent.of("The new draw selection state")
      )
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(boolean_Key))
      .build();
   private final CommandArgument worldPart = CommandParts.arg(TranslatableComponent.of("world"), TextComponent.of("The world override"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(world_Key))
      .build();
   private final CommandArgument hookModePart = CommandParts.arg(TranslatableComponent.of("hookMode"), TextComponent.of("The mode to set the watchdog hook to"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(hookMode_Key))
      .build();
   private final CommandArgument maskPart = CommandParts.arg(TranslatableComponent.of("mask"), TextComponent.of("The mask to set"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(mask_Key))
      .build();
   private final NoArgCommandFlag blocksOnlyPart = CommandParts.flag('b', TextComponent.of("Only search for blocks")).build();
   private final NoArgCommandFlag itemsOnlyPart = CommandParts.flag('i', TextComponent.of("Only search for items")).build();
   private final ArgAcceptingCommandFlag pagePart = CommandParts.flag('p', TextComponent.of("Page of results to return"))
      .withRequiredArg()
      .argNamed(TranslatableComponent.of("page"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument queryPart = CommandParts.arg(TranslatableComponent.of("query"), TextComponent.of("Search query"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(string_Key))
      .variable(true)
      .build();
   private final CommandArgument argumentsPart = CommandParts.arg(TranslatableComponent.of("context"), TextComponent.of("InjectedValueAccess"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(string_Key))
      .build();
   private final CommandArgument maskOptPart = CommandParts.arg(TranslatableComponent.of("maskOpt"), TextComponent.of("The mask to set"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(mask_Key))
      .build();
   private final CommandArgument transformPart = CommandParts.arg(TranslatableComponent.of("transform"), TextComponent.of("The transform to set"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(resettableExtent_Key))
      .build();
   private final CommandArgument fastModePart = CommandParts.arg(TranslatableComponent.of("fastMode"), TextComponent.of("The new fast mode state"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(boolean_Key))
      .build();
   private final CommandArgument registryPart = CommandParts.arg(TranslatableComponent.of("registry"), TextComponent.of("The registry to search through"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(registry$__Key))
      .build();
   private final CommandArgument queryBitsPart = CommandParts.arg(TranslatableComponent.of("queryBits"), TextComponent.of("Search query"))
      .defaultsTo(ImmutableList.of("*"))
      .ofTypes(ImmutableList.of(string_Key))
      .variable(true)
      .build();

   private GeneralCommandsRegistration() {
      this.listeners = ImmutableList.of();
   }

   public static GeneralCommandsRegistration builder() {
      return new GeneralCommandsRegistration();
   }

   public GeneralCommandsRegistration commandManager(CommandManager commandManager) {
      this.commandManager = commandManager;
      return this;
   }

   public GeneralCommandsRegistration containerInstance(GeneralCommands containerInstance) {
      this.containerInstance = containerInstance;
      return this;
   }

   public GeneralCommandsRegistration commandPermissionsConditionGenerator(CommandPermissionsConditionGenerator commandPermissionsConditionGenerator) {
      this.commandPermissionsConditionGenerator = commandPermissionsConditionGenerator;
      return this;
   }

   public GeneralCommandsRegistration listeners(Collection<CommandCallListener> listeners) {
      this.listeners = ImmutableList.copyOf(listeners);
      return this;
   }

   @Override
   public void build() {
      this.commandManager.register("/limit", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Modify block change limit"));
         b.parts(ImmutableList.of(this.limitPart));
         b.action(this::cmd$_limit);
         Method commandMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "limit", Actor.class, LocalSession.class, Integer.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/timeout", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Modify evaluation timeout time."));
         b.parts(ImmutableList.of(this.limitPart2));
         b.action(this::cmd$_timeout);
         Method commandMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "timeout", Actor.class, LocalSession.class, Integer.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager
         .register(
            "/perf",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Toggle side effects for performance"));
               b.footer(TextComponent.of("Note that this command is GOING to change in the future. Do not depend on the exact format of this command yet."));
               b.parts(ImmutableList.of(this.sideEffectPart, this.newStatePart, this.showInfoBoxPart));
               b.action(this::cmd$_perf);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GeneralCommands.class, "perf", Actor.class, LocalSession.class, SideEffect.class, SideEffect.State.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/reorder",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Sets the reorder mode of WorldEdit"));
               b.parts(ImmutableList.of(this.reorderModePart));
               b.action(this::cmd$_reorder);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GeneralCommands.class, "reorderMode", Actor.class, LocalSession.class, EditSession.ReorderMode.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("/drawsel", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Toggle drawing the current selection"));
         b.parts(ImmutableList.of(this.drawSelectionPart));
         b.action(this::cmd$_drawsel);
         Method commandMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "drawSelection", Player.class, LocalSession.class, Boolean.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/world", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Sets the world override"));
         b.parts(ImmutableList.of(this.worldPart));
         b.action(this::cmd$_world);
         Method commandMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "world", Actor.class, LocalSession.class, World.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/watchdog", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Changes watchdog hook state."));
         b.footer(TextComponent.of("This is dependent on platform implementation. Not all platforms support watchdog hooks, or contain a watchdog."));
         b.parts(ImmutableList.of(this.hookModePart));
         b.action(this::cmd$_watchdog);
         Method commandMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "watchdog", Actor.class, LocalSession.class, HookMode.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("gmask", b -> {
         b.aliases(ImmutableList.of("/gmask"));
         b.description(TextComponent.of("Set the global mask"));
         b.parts(ImmutableList.of(this.maskPart));
         b.action(this::cmd$gmask);
         Method commandMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "gmask", Actor.class, LocalSession.class, Mask.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("toggleplace", b -> {
         b.aliases(ImmutableList.of("/toggleplace"));
         b.description(TextComponent.of("Switch between your position and pos1 for placement"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$toggleplace);
         Method commandMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "togglePlace", Actor.class, LocalSession.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager
         .register(
            "searchitem",
            b -> {
               b.aliases(ImmutableList.of("/searchitem", "/l", "/search"));
               b.description(TextComponent.of("Search for an item"));
               b.parts(ImmutableList.of(this.blocksOnlyPart, this.itemsOnlyPart, this.pagePart, this.queryPart));
               b.action(this::cmd$searchitem);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GeneralCommands.class, "searchItem", Actor.class, boolean.class, boolean.class, int.class, List.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/gtexture",
            b -> {
               b.aliases(ImmutableList.of("gtexture"));
               b.description(TextComponent.of("Set the global mask"));
               b.footer(
                  TextComponent.of(
                     "The global destination mask applies to all edits you do and masks based on the destination blocks (i.e., the blocks in the world)."
                  )
               );
               b.parts(ImmutableList.of(this.argumentsPart));
               b.action(this::cmd$_gtexture);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GeneralCommands.class, "gtexture", Actor.class, World.class, LocalSession.class, EditSession.class, List.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/gsmask",
            b -> {
               b.aliases(ImmutableList.of("gsmask", "globalsourcemask", "/globalsourcemask"));
               b.description(TextComponent.of("Set the global source mask"));
               b.footer(
                  TextComponent.of(
                     "The global source mask applies to all edits you do and masks based on the source blocks (e.g., the blocks in your clipboard)"
                  )
               );
               b.parts(ImmutableList.of(this.maskOptPart));
               b.action(this::cmd$_gsmask);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GeneralCommands.class, "gsmask", Actor.class, LocalSession.class, EditSession.class, Mask.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/gtransform",
            b -> {
               b.aliases(ImmutableList.of("gtransform"));
               b.description(TextComponent.of("Set the global transform"));
               b.parts(ImmutableList.of(this.transformPart));
               b.action(this::cmd$_gtransform);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GeneralCommands.class, "gtransform", Actor.class, EditSession.class, LocalSession.class, ResettableExtent.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("/tips", b -> {
         b.aliases(ImmutableList.of("tips"));
         b.description(TextComponent.of("Toggle FAWE tips"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$_tips);
         Method commandMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "tips", Actor.class, LocalSession.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/fast", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Toggle fast mode"));
         b.parts(ImmutableList.of(this.fastModePart));
         b.action(this::cmd$_fast);
         Method commandMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "fast", Actor.class, LocalSession.class, Boolean.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/registry", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Search through the given registry"));
         b.parts(ImmutableList.of(this.registryPart, this.pagePart, this.queryBitsPart));
         b.action(this::cmd$_registry);
         Method commandMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "registry", Actor.class, Registry.class, int.class, List.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
   }

   private int cmd$_limit(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "limit", Actor.class, LocalSession.class, Integer.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.limit(this.extract$actor(parameters), this.extract$session(parameters), this.extract$limit(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_timeout(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "timeout", Actor.class, LocalSession.class, Integer.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.timeout(this.extract$actor(parameters), this.extract$session(parameters), this.extract$limit2(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_perf(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GeneralCommands.class, "perf", Actor.class, LocalSession.class, SideEffect.class, SideEffect.State.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .perf(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$sideEffect(parameters),
               this.extract$newState(parameters),
               this.extract$showInfoBox(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_reorder(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "reorderMode", Actor.class, LocalSession.class, EditSession.ReorderMode.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.reorderMode(this.extract$actor(parameters), this.extract$session(parameters), this.extract$reorderMode(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_drawsel(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "drawSelection", Player.class, LocalSession.class, Boolean.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.drawSelection(this.extract$player(parameters), this.extract$session(parameters), this.extract$drawSelection(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_world(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "world", Actor.class, LocalSession.class, World.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.world(this.extract$actor(parameters), this.extract$session(parameters), this.extract$world(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_watchdog(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "watchdog", Actor.class, LocalSession.class, HookMode.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.watchdog(this.extract$actor(parameters), this.extract$session(parameters), this.extract$hookMode(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$gmask(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "gmask", Actor.class, LocalSession.class, Mask.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.gmask(this.extract$actor(parameters), this.extract$session(parameters), this.extract$mask(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$toggleplace(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "togglePlace", Actor.class, LocalSession.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.togglePlace(this.extract$actor(parameters), this.extract$session(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$searchitem(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GeneralCommands.class, "searchItem", Actor.class, boolean.class, boolean.class, int.class, List.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .searchItem(
               this.extract$actor(parameters),
               this.extract$blocksOnly(parameters),
               this.extract$itemsOnly(parameters),
               this.extract$page(parameters),
               this.extract$query(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_gtexture(CommandParameters parameters) throws WorldEditException, FileNotFoundException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GeneralCommands.class, "gtexture", Actor.class, World.class, LocalSession.class, EditSession.class, List.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .gtexture(
               this.extract$actor(parameters),
               this.extract$worldArg(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
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

   private int cmd$_gsmask(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "gsmask", Actor.class, LocalSession.class, EditSession.class, Mask.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .gsmask(this.extract$actor(parameters), this.extract$session(parameters), this.extract$editSession(parameters), this.extract$maskOpt(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_gtransform(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GeneralCommands.class, "gtransform", Actor.class, EditSession.class, LocalSession.class, ResettableExtent.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .gtransform(
               this.extract$actor(parameters), this.extract$editSession(parameters), this.extract$session(parameters), this.extract$transform(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_tips(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "tips", Actor.class, LocalSession.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.tips(this.extract$actor(parameters), this.extract$session(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_fast(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "fast", Actor.class, LocalSession.class, Boolean.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.fast(this.extract$actor(parameters), this.extract$session(parameters), this.extract$fastMode(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_registry(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(GeneralCommands.class, "registry", Actor.class, Registry.class, int.class, List.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .registry(this.extract$actor(parameters), this.extract$registry(parameters), this.extract$page(parameters), this.extract$queryBits(parameters));
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

   private Integer extract$limit(CommandParameters parameters) {
      return this.limitPart.value(parameters).asSingle(integer_Key);
   }

   private Integer extract$limit2(CommandParameters parameters) {
      return this.limitPart2.value(parameters).asSingle(integer_Key);
   }

   private SideEffect extract$sideEffect(CommandParameters parameters) {
      return this.sideEffectPart.value(parameters).asSingle(sideEffect_Key);
   }

   private SideEffect.State extract$newState(CommandParameters parameters) {
      return this.newStatePart.value(parameters).asSingle(state_Key);
   }

   private boolean extract$showInfoBox(CommandParameters parameters) {
      return this.showInfoBoxPart.in(parameters);
   }

   private EditSession.ReorderMode extract$reorderMode(CommandParameters parameters) {
      return this.reorderModePart.value(parameters).asSingle(reorderMode_Key);
   }

   private Player extract$player(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(player_Key, "player", parameters.injectedValue(player_Key));
   }

   private Boolean extract$drawSelection(CommandParameters parameters) {
      return this.drawSelectionPart.value(parameters).asSingle(boolean_Key);
   }

   private World extract$world(CommandParameters parameters) {
      return this.worldPart.value(parameters).asSingle(world_Key);
   }

   private HookMode extract$hookMode(CommandParameters parameters) {
      return this.hookModePart.value(parameters).asSingle(hookMode_Key);
   }

   private Mask extract$mask(CommandParameters parameters) {
      return this.maskPart.value(parameters).asSingle(mask_Key);
   }

   private boolean extract$blocksOnly(CommandParameters parameters) {
      return this.blocksOnlyPart.in(parameters);
   }

   private boolean extract$itemsOnly(CommandParameters parameters) {
      return this.itemsOnlyPart.in(parameters);
   }

   private int extract$page(CommandParameters parameters) {
      return this.pagePart.value(parameters).asSingle(integer_Key);
   }

   private List<String> extract$query(CommandParameters parameters) {
      return this.queryPart.value(parameters).<String>asMultiple(string_Key);
   }

   private World extract$worldArg(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(world_Key, "worldArg", parameters.injectedValue(world_Key));
   }

   private EditSession extract$editSession(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(editSession_Key, "editSession", parameters.injectedValue(editSession_Key));
   }

   private List<String> extract$arguments(CommandParameters parameters) {
      return this.argumentsPart.value(parameters).<String>asMultiple(string_Key);
   }

   private Mask extract$maskOpt(CommandParameters parameters) {
      return this.maskOptPart.value(parameters).asSingle(mask_Key);
   }

   private ResettableExtent extract$transform(CommandParameters parameters) {
      return this.transformPart.value(parameters).asSingle(resettableExtent_Key);
   }

   private Boolean extract$fastMode(CommandParameters parameters) {
      return this.fastModePart.value(parameters).asSingle(boolean_Key);
   }

   private Registry<?> extract$registry(CommandParameters parameters) {
      return this.registryPart.value(parameters).asSingle(registry$__Key);
   }

   private List<String> extract$queryBits(CommandParameters parameters) {
      return this.queryBitsPart.value(parameters).<String>asMultiple(string_Key);
   }
}
