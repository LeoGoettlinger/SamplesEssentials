package com.sk89q.worldedit.command;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extent.ResettableExtent;
import com.fastasyncworldedit.core.util.CachedTextureUtil;
import com.fastasyncworldedit.core.util.CleanTextureUtil;
import com.fastasyncworldedit.core.util.MathMan;
import com.fastasyncworldedit.core.util.RandomTextureUtil;
import com.fastasyncworldedit.core.util.StringMan;
import com.fastasyncworldedit.core.util.TextureUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.HookMode;
import com.sk89q.worldedit.command.util.WorldEditAsyncCommandBuilder;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.internal.command.CommandUtil;
import com.sk89q.worldedit.internal.cui.ServerCUIHandler;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.Registry;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.component.PaginationBox;
import com.sk89q.worldedit.util.formatting.component.SideEffectBox;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.item.ItemType;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandManagerService;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;

@CommandContainer(
   superTypes = {CommandPermissionsConditionGenerator.Registration.class}
)
public class GeneralCommands {
   private final WorldEdit worldEdit;
   private static final Pattern ALLOWED_KEY_CHARACTERS = Pattern.compile("[a-z0-9_:?*/]+");

   public GeneralCommands(WorldEdit worldEdit) {
      Preconditions.checkNotNull(worldEdit);
      this.worldEdit = worldEdit;
   }

   public static void register(
      CommandRegistrationHandler registration, CommandManager commandManager, CommandManagerService commandManagerService, WorldEdit worldEdit
   ) {
      CommandManager collect = commandManagerService.newCommandManager();
      registration.register(collect, GeneralCommandsRegistration.builder(), new GeneralCommands(worldEdit));

      for (Command command : collect.getAllCommands().collect(Collectors.toSet())) {
         commandManager.register(command);
      }
   }

   private static Component replaceFastForPerf(Command oldCmd, CommandParameters oldParams) {
      if (oldParams.getMetadata() == null) {
         return CommandUtil.createNewCommandReplacementText("//perf");
      } else {
         ImmutableList<String> args = oldParams.getMetadata().getArguments();
         if (args.isEmpty()) {
            return TextComponent.of("There is not yet a replacement for //fast with no arguments");
         } else {
            String arg0 = ((String)args.get(0)).toLowerCase(Locale.ENGLISH);
            String flipped;
            switch (arg0) {
               case "on":
                  flipped = "off";
                  break;
               case "off":
                  flipped = "on";
                  break;
               default:
                  return TextComponent.of("There is no replacement for //fast " + arg0);
            }

            return CommandUtil.createNewCommandReplacementText("//perf " + flipped);
         }
      }
   }

   @org.enginehub.piston.annotation.Command(
      name = "/limit",
      desc = "Modify block change limit"
   )
   @CommandPermissions({"worldedit.limit"})
   public void limit(Actor actor, LocalSession session, @Arg(desc = "The limit to set",def = {""}) Integer limit) {
      LocalConfiguration config = this.worldEdit.getConfiguration();
      boolean mayDisable = actor.hasPermission("worldedit.limit.unrestricted");
      limit = limit == null ? config.defaultChangeLimit : Math.max(-1, limit);
      if (!mayDisable && config.maxChangeLimit > -1 && limit > config.maxChangeLimit) {
         actor.print(Caption.of("worldedit.limit.too-high", TextComponent.of(config.maxChangeLimit)));
      } else {
         session.setBlockChangeLimit(limit);
         Component component = TextComponent.empty().append(Caption.of("worldedit.limit.set", TextComponent.of(limit)));
         if (limit != config.defaultChangeLimit) {
            component.append(TextComponent.space()).append(Caption.of("worldedit.limit.return-to-default"));
         }

         actor.print(component);
      }
   }

   @org.enginehub.piston.annotation.Command(
      name = "/timeout",
      desc = "Modify evaluation timeout time."
   )
   @CommandPermissions({"worldedit.timeout"})
   public void timeout(Actor actor, LocalSession session, @Arg(desc = "The timeout time to set",def = {""}) Integer limit) {
      LocalConfiguration config = this.worldEdit.getConfiguration();
      boolean mayDisable = actor.hasPermission("worldedit.timeout.unrestricted");
      limit = limit == null ? config.calculationTimeout : Math.max(-1, limit);
      if (!mayDisable && config.maxCalculationTimeout > -1 && limit > config.maxCalculationTimeout) {
         actor.print(Caption.of("worldedit.timeout.too-high", TextComponent.of(config.maxCalculationTimeout)));
      } else {
         session.setTimeout(limit);
         Component component = TextComponent.empty().append(Caption.of("worldedit.timeout.set", TextComponent.of(limit)));
         if (limit != config.calculationTimeout) {
            component.append(Caption.of("worldedit.timeout.return-to-default"));
         }

         actor.print(component);
      }
   }

   @org.enginehub.piston.annotation.Command(
      name = "/perf",
      desc = "Toggle side effects for performance",
      descFooter = "Note that this command is GOING to change in the future. Do not depend on the exact format of this command yet."
   )
   @CommandPermissions({"worldedit.perf"})
   void perf(
      Actor actor,
      LocalSession session,
      @Arg(desc = "The side effect",def = {""}) SideEffect sideEffect,
      @Arg(desc = "The new side effect state",def = {""}) SideEffect.State newState,
      @Switch(name = 'h',desc = "Show the info box") boolean showInfoBox
   ) throws WorldEditException {
      if (sideEffect != null) {
         SideEffect.State currentState = session.getSideEffectSet().getState(sideEffect);
         if (newState != null && newState == currentState) {
            if (!showInfoBox) {
               actor.print(
                  Caption.of(
                     "worldedit.perf.sideeffect.already-set",
                     TranslatableComponent.of(sideEffect.getDisplayName()),
                     TranslatableComponent.of(newState.getDisplayName())
                  )
               );
            }

            return;
         }

         if (newState != null) {
            session.setSideEffectSet(session.getSideEffectSet().with(sideEffect, newState));
            if (!showInfoBox) {
               actor.print(
                  Caption.of(
                     "worldedit.perf.sideeffect.set",
                     TranslatableComponent.of(sideEffect.getDisplayName()),
                     TranslatableComponent.of(newState.getDisplayName())
                  )
               );
            }
         } else {
            actor.print(
               Caption.of(
                  "worldedit.perf.sideeffect.get",
                  TranslatableComponent.of(sideEffect.getDisplayName()),
                  TranslatableComponent.of(currentState.getDisplayName())
               )
            );
         }
      } else if (newState != null) {
         SideEffectSet applier = session.getSideEffectSet();

         for (SideEffect sideEffectEntry : SideEffect.values()) {
            applier = applier.with(sideEffectEntry, newState);
         }

         session.setSideEffectSet(applier);
         if (!showInfoBox) {
            actor.print(Caption.of("worldedit.perf.sideeffect.set-all", TranslatableComponent.of(newState.getDisplayName())));
         }
      }

      if (sideEffect == null || showInfoBox) {
         SideEffectBox sideEffectBox = new SideEffectBox(session.getSideEffectSet());
         actor.print(sideEffectBox.create(1));
      }
   }

   @org.enginehub.piston.annotation.Command(
      name = "/reorder",
      desc = "Sets the reorder mode of WorldEdit"
   )
   @CommandPermissions({"worldedit.reorder"})
   public void reorderMode(Actor actor, LocalSession session, @Arg(desc = "The reorder mode",def = {""}) EditSession.ReorderMode reorderMode) {
      if (reorderMode == null) {
         actor.print(Caption.of("worldedit.reorder.current", TextComponent.of(session.getReorderMode().getDisplayName())));
      } else {
         session.setReorderMode(reorderMode);
         actor.print(Caption.of("worldedit.reorder.set", TextComponent.of(session.getReorderMode().getDisplayName())));
      }
   }

   @org.enginehub.piston.annotation.Command(
      name = "/drawsel",
      desc = "Toggle drawing the current selection"
   )
   @CommandPermissions({"worldedit.drawsel"})
   public void drawSelection(Player player, LocalSession session, @Arg(desc = "The new draw selection state",def = {""}) Boolean drawSelection) throws WorldEditException {
      if (!WorldEdit.getInstance().getConfiguration().serverSideCUI) {
         throw new AuthorizationException(Caption.of("worldedit.error.disabled"));
      } else {
         boolean useServerCui = session.shouldUseServerCUI();
         if (drawSelection != null && drawSelection == useServerCui) {
            player.print(Caption.of("worldedit.drawsel." + (useServerCui ? "enabled" : "disabled") + ".already"));
         } else {
            if (useServerCui) {
               session.setUseServerCUI(false);
               session.updateServerCUI(player);
               player.print(Caption.of("worldedit.drawsel.disabled"));
            } else {
               session.setUseServerCUI(true);
               session.updateServerCUI(player);
               int maxSize = ServerCUIHandler.getMaxServerCuiSize();
               player.print(Caption.of("worldedit.drawsel.enabled", TextComponent.of(maxSize), TextComponent.of(maxSize), TextComponent.of(maxSize)));
            }
         }
      }
   }

   @org.enginehub.piston.annotation.Command(
      name = "/world",
      desc = "Sets the world override"
   )
   @CommandPermissions({"worldedit.world"})
   public void world(Actor actor, LocalSession session, @Arg(desc = "The world override",def = {""}) World world) {
      session.setWorldOverride(world);
      if (world == null) {
         actor.print(Caption.of("worldedit.world.remove"));
      } else {
         actor.print(Caption.of("worldedit.world.set", TextComponent.of(world.id())));
      }
   }

   @org.enginehub.piston.annotation.Command(
      name = "/watchdog",
      desc = "Changes watchdog hook state.",
      descFooter = "This is dependent on platform implementation. Not all platforms support watchdog hooks, or contain a watchdog."
   )
   @CommandPermissions(
      value = {"worldedit.watchdog"},
      queued = false
   )
   public void watchdog(Actor actor, LocalSession session, @Arg(desc = "The mode to set the watchdog hook to",def = {""}) HookMode hookMode) {
      if (WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getWatchdog() == null) {
         actor.print(Caption.of("worldedit.watchdog.no-hook"));
      } else {
         boolean previousMode = session.isTickingWatchdog();
         if (hookMode != null && hookMode == HookMode.ACTIVE == previousMode) {
            actor.print(Caption.of(previousMode ? "worldedit.watchdog.active.already" : "worldedit.watchdog.inactive.already"));
         } else {
            session.setTickingWatchdog(!previousMode);
            actor.print(Caption.of(previousMode ? "worldedit.watchdog.inactive" : "worldedit.watchdog.active"));
         }
      }
   }

   @org.enginehub.piston.annotation.Command(
      name = "gmask",
      aliases = {"/gmask"},
      desc = "Set the global mask"
   )
   @CommandPermissions({"worldedit.global-mask"})
   public void gmask(Actor actor, LocalSession session, @Arg(desc = "The mask to set",def = {""}) Mask mask) {
      if (mask == null) {
         session.setMask(null);
         actor.print(Caption.of("worldedit.gmask.disabled"));
      } else {
         session.setMask(mask);
         actor.print(Caption.of("worldedit.gmask.set"));
      }
   }

   @org.enginehub.piston.annotation.Command(
      name = "toggleplace",
      aliases = {"/toggleplace"},
      desc = "Switch between your position and pos1 for placement"
   )
   @CommandPermissions({"worldedit.toggleplace"})
   public void togglePlace(Actor actor, LocalSession session) {
      if (!(actor instanceof Locatable)) {
         actor.print(Caption.of("worldedit.toggleplace.not-locatable"));
      } else {
         if (session.togglePlacementPosition()) {
            actor.print(Caption.of("worldedit.toggleplace.pos1"));
         } else {
            actor.print(Caption.of("worldedit.toggleplace.player"));
         }
      }
   }

   @org.enginehub.piston.annotation.Command(
      name = "searchitem",
      aliases = {"/searchitem", "/l", "/search"},
      desc = "Search for an item"
   )
   @CommandPermissions(
      value = {"worldedit.searchitem"},
      queued = false
   )
   public void searchItem(
      Actor actor,
      @Switch(name = 'b',desc = "Only search for blocks") boolean blocksOnly,
      @Switch(name = 'i',desc = "Only search for items") boolean itemsOnly,
      @ArgFlag(name = 'p',desc = "Page of results to return",def = {"1"}) int page,
      @Arg(desc = "Search query",variable = true) List<String> query
   ) {
      String search = String.join(" ", query);
      if (search.length() <= 2) {
         actor.print(Caption.of("worldedit.searchitem.too-short"));
      } else if (blocksOnly && itemsOnly) {
         actor.print(Caption.of("worldedit.searchitem.either-b-or-i"));
      } else {
         WorldEditAsyncCommandBuilder.createAndSendMessage(
            actor, new GeneralCommands.ItemSearcher(search, blocksOnly, itemsOnly, page), Caption.of("worldedit.searchitem.searching")
         );
      }
   }

   @org.enginehub.piston.annotation.Command(
      name = "/gtexture",
      aliases = {"gtexture"},
      descFooter = "The global destination mask applies to all edits you do and masks based on the destination blocks (i.e., the blocks in the world).",
      desc = "Set the global mask"
   )
   @CommandPermissions({"worldedit.global-texture"})
   public void gtexture(
      Actor actor,
      World worldArg,
      LocalSession session,
      EditSession editSession,
      @Arg(name = "context",desc = "InjectedValueAccess",def = {""}) List<String> arguments
   ) throws WorldEditException, FileNotFoundException {
      if (arguments.isEmpty()) {
         session.setTextureUtil(null);
         actor.print(Caption.of("fawe.worldedit.general.texture.disabled"));
      } else {
         String arg = arguments.get(0);
         String argLower = arg.toLowerCase(Locale.ROOT);
         TextureUtil util = Fawe.instance().getTextureUtil();
         int randomIndex = 1;
         boolean checkRandomization = true;
         if (arguments.size() >= 2 && MathMan.isInteger(arguments.get(0)) && MathMan.isInteger(arguments.get(1))) {
            int min = Integer.parseInt(arguments.get(0));
            int max = Integer.parseInt(arguments.get(1));
            if (min < 0 || max > 100) {
               throw new InputParseException(Caption.of("fawe.error.too-simple"));
            }

            if (min != 0 || max != 100) {
               util = new CleanTextureUtil(util, min, max);
            }

            randomIndex = 2;
         } else if ((arguments.size() != 1 || !argLower.equals("true")) && !argLower.equals("false")) {
            if (argLower.equals("#copy") || argLower.equals("#clipboard")) {
               Clipboard clipboard = session.getClipboard().getClipboard();
               util = TextureUtil.fromClipboard(clipboard);
            } else if (!argLower.equals("*") && !argLower.equals("true")) {
               ParserContext parserContext = new ParserContext();
               parserContext.setActor(actor);
               parserContext.setWorld(worldArg);
               parserContext.setSession(session);
               parserContext.setExtent(editSession);
               parserContext.setTryLegacy(actor.getLimit().ALLOW_LEGACY);
               Mask mask = this.worldEdit.getMaskFactory().parseFromInput(arg, parserContext);
               util = TextureUtil.fromMask(mask);
            } else {
               util = Fawe.instance().getTextureUtil();
            }
         } else {
            if (argLower.equals("true")) {
               util = new RandomTextureUtil(util);
            }

            checkRandomization = false;
         }

         if (checkRandomization && arguments.size() > randomIndex) {
            boolean random = Boolean.parseBoolean(arguments.get(randomIndex));
            if (random) {
               util = new RandomTextureUtil(util);
            }
         }

         if (!(util instanceof CachedTextureUtil)) {
            util = new CachedTextureUtil(util);
         }

         session.setTextureUtil(util);
         actor.print(Caption.of("fawe.worldedit.general.texture.set", StringMan.join(arguments, " ")));
      }
   }

   @org.enginehub.piston.annotation.Command(
      name = "/gsmask",
      aliases = {"gsmask", "globalsourcemask", "/globalsourcemask"},
      desc = "Set the global source mask",
      descFooter = "The global source mask applies to all edits you do and masks based on the source blocks (e.g., the blocks in your clipboard)"
   )
   @CommandPermissions({"worldedit.global-mask", "worldedit.mask.global"})
   public void gsmask(Actor actor, LocalSession session, EditSession editSession, @Arg(desc = "The mask to set",def = {""}) Mask maskOpt) throws WorldEditException {
      session.setSourceMask(maskOpt);
      if (maskOpt == null) {
         actor.print(Caption.of("fawe.worldedit.general.source.mask.disabled"));
      } else {
         actor.print(Caption.of("fawe.worldedit.general.source.mask"));
      }
   }

   @org.enginehub.piston.annotation.Command(
      name = "/gtransform",
      aliases = {"gtransform"},
      desc = "Set the global transform"
   )
   @CommandPermissions({"worldedit.global-transform", "worldedit.transform.global"})
   public void gtransform(Actor actor, EditSession editSession, LocalSession session, @Arg(desc = "The transform to set",def = {""}) ResettableExtent transform) throws WorldEditException {
      session.setTransform(transform);
      if (transform == null) {
         actor.print(Caption.of("fawe.worldedit.general.transform.disabled"));
      } else {
         actor.print(Caption.of("fawe.worldedit.general.transform"));
      }
   }

   @org.enginehub.piston.annotation.Command(
      name = "/tips",
      aliases = {"tips"},
      desc = "Toggle FAWE tips"
   )
   @CommandPermissions(
      value = {"fawe.tips"},
      queued = false
   )
   public void tips(Actor actor, LocalSession session) throws WorldEditException {
      if (actor.togglePermission("fawe.tips")) {
         actor.print(Caption.of("fawe.info.worldedit.toggle.tips.on"));
      } else {
         actor.print(Caption.of("fawe.info.worldedit.toggle.tips.off"));
      }
   }

   @org.enginehub.piston.annotation.Command(
      name = "/fast",
      desc = "Toggle fast mode"
   )
   @CommandPermissions({"worldedit.fast"})
   @Deprecated
   void fast(Actor actor, LocalSession session, @Arg(desc = "The new fast mode state",def = {""}) Boolean fastMode) {
      boolean hasFastMode = session.hasFastMode();
      if (fastMode != null && fastMode == hasFastMode) {
         actor.print(Caption.of(fastMode ? "worldedit.fast.enabled.already" : "worldedit.fast.disabled.already"));
      } else {
         if (hasFastMode) {
            session.setFastMode(false);
            actor.print(Caption.of("worldedit.fast.disabled"));
         } else {
            session.setFastMode(true);
            actor.print(Caption.of("worldedit.fast.enabled"));
         }
      }
   }

   @org.enginehub.piston.annotation.Command(
      name = "/registry",
      desc = "Search through the given registry"
   )
   @CommandPermissions({"worldedit.registry"})
   public void registry(
      Actor actor,
      @Arg(desc = "The registry to search through") Registry<?> registry,
      @ArgFlag(name = 'p',desc = "Page of results to return",def = {"1"}) int page,
      @Arg(desc = "Search query",variable = true,def = {"*"}) List<String> queryBits
   ) {
      String query = String.join("_", queryBits);
      if (!ALLOWED_KEY_CHARACTERS.matcher(query).matches()) {
         actor.printError(TranslatableComponent.of("worldedit.registry.error.invalid-key"));
      }

      WorldEditAsyncCommandBuilder.createAndSendMessage(
         actor, new GeneralCommands.RegistrySearcher(registry, query, page), TranslatableComponent.of("worldedit.registry.searching", TextComponent.of(query))
      );
   }

   private static class ItemSearcher implements Callable<Component> {
      private final boolean blocksOnly;
      private final boolean itemsOnly;
      private final String search;
      private final int page;

      ItemSearcher(String search, boolean blocksOnly, boolean itemsOnly, int page) {
         this.blocksOnly = blocksOnly;
         this.itemsOnly = itemsOnly;
         this.search = search;
         this.page = page;
      }

      public Component call() throws Exception {
         String command = "/searchitem " + (this.blocksOnly ? "-b " : "") + (this.itemsOnly ? "-i " : "") + "-p %page% " + this.search;
         Map<String, Component> results = new TreeMap<>();
         String idMatch = this.search.replace(' ', '_');

         for (ItemType searchType : ItemType.REGISTRY) {
            if ((!this.blocksOnly || searchType.hasBlockType()) && (!this.itemsOnly || !searchType.hasBlockType())) {
               String id = searchType.id();
               if (id.contains(idMatch)) {
                  Component name = searchType.getRichName();
                  results.put(id, TextComponent.builder().append(name).append(" (" + id + ")").build());
               }
            }
         }

         List<Component> list = new ArrayList<>(results.values());
         return PaginationBox.fromComponents("Search results for '" + this.search + "'", command, list).create(this.page);
      }
   }

   private static class RegistrySearcher implements Callable<Component> {
      private final Registry<?> registry;
      private final String search;
      private final int page;
      private final Pattern matcher;

      RegistrySearcher(Registry<?> registry, String search, int page) {
         this.registry = registry;
         this.search = search;
         this.page = page;
         String matcherQuery = search;
         if (!search.contains("*") && !search.contains("?")) {
            matcherQuery = "*" + search + "*";
         }

         this.matcher = StringUtil.convertGlobToRegex(matcherQuery);
      }

      public Component call() throws Exception {
         String command = "//registry " + this.registry.id() + " -p %page% " + this.search;
         Map<String, Component> results = new TreeMap<>();

         for (Keyed searchType : this.registry) {
            String id = searchType.id();
            if (this.matcher.matcher(id).matches()) {
               TextComponent.Builder builder = TextComponent.builder().append(searchType.id()).clickEvent(ClickEvent.copyToClipboard(searchType.id()));
               switch (searchType) {
                  case ItemType itemType:
                     builder.hoverEvent(HoverEvent.showText(itemType.getRichName()));
                     break;
                  case BlockType blockType:
                     builder.hoverEvent(HoverEvent.showText(blockType.getRichName()));
                     break;
                  case BiomeType biomeType:
                     builder.hoverEvent(HoverEvent.showText(biomeType.getRichName()));
                     break;
                  default:
               }

               results.put(id, builder.build());
            }
         }

         List<Component> list = new ArrayList<>(results.values());
         boolean isBlank = this.search.isBlank() || this.search.equals("*");
         String title = isBlank ? "Registry contents" : "Search results for '" + this.search + "'";
         return PaginationBox.fromComponents(title, command, list).create(this.page);
      }
   }
}
