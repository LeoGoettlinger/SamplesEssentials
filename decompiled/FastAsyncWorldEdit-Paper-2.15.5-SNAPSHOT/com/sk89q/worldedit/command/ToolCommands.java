package com.sk89q.worldedit.command;

import com.fastasyncworldedit.core.command.tool.FeaturePlacer;
import com.fastasyncworldedit.core.command.tool.StructurePlacer;
import com.fastasyncworldedit.core.command.tool.brush.InspectBrush;
import com.fastasyncworldedit.core.configuration.Caption;
import com.google.common.collect.Collections2;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.command.tool.BlockDataCyler;
import com.sk89q.worldedit.command.tool.BlockReplacer;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.DistanceWand;
import com.sk89q.worldedit.command.tool.FloatingTreeRemover;
import com.sk89q.worldedit.command.tool.FloodFillTool;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.command.tool.LongRangeBuildTool;
import com.sk89q.worldedit.command.tool.NavigationWand;
import com.sk89q.worldedit.command.tool.QueryTool;
import com.sk89q.worldedit.command.tool.SelectionWand;
import com.sk89q.worldedit.command.tool.StackTool;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.command.tool.TreePlanter;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.SubCommandPermissionCondition;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.internal.command.CommandUtil;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;
import com.sk89q.worldedit.world.generation.StructureType;
import com.sk89q.worldedit.world.generation.TreeType;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandManagerService;
import org.enginehub.piston.CommandMetadata;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.part.SubCommandPart;

@CommandContainer(
   superTypes = {CommandPermissionsConditionGenerator.Registration.class}
)
public class ToolCommands {
   private static final Component UNBIND_COMMAND_COMPONENT = TextComponent.builder("/tool unbind", TextColor.AQUA)
      .clickEvent(ClickEvent.suggestCommand("/tool unbind"))
      .build();
   private final WorldEdit we;

   public static void register(
      CommandRegistrationHandler registration, CommandManager commandManager, CommandManagerService commandManagerService, WorldEdit worldEdit
   ) {
      CommandManager collect = commandManagerService.newCommandManager();
      registration.register(collect, ToolCommandsRegistration.builder(), new ToolCommands(worldEdit));
      Set<Command> commands = collect.getAllCommands().collect(Collectors.toSet());

      for (Command command : commands) {
         if (command.getAliases().contains("unbind")) {
            command = command.toBuilder().aliases(Collections2.filter(command.getAliases(), alias -> !"unbind".equals(alias))).build();
         }

         if (!command.getName().equals("stacker")) {
            commandManager.register(
               CommandUtil.deprecate(
                  command,
                  "Global tool names cause conflicts and will be removed in WorldEdit 8",
                  CommandUtil.ReplacementMessageGenerator.forNewCommand(ToolCommands::asNonGlobal)
               )
            );
         }
      }

      Set<Command> nonGlobalCommands = commands.stream()
         .map(commandx -> commandx.toBuilder().aliases(Collections2.filter(commandx.getAliases(), alias -> !alias.startsWith("/"))).build())
         .collect(Collectors.toSet());
      commandManager.register("tool", commandx -> {
         commandx.addPart(SubCommandPart.builder(Caption.of("tool"), TextComponent.of("The tool to bind")).withCommands(nonGlobalCommands).required().build());
         commandx.description(TextComponent.of("Binds a tool to the item in your hand"));
         commandx.condition(new SubCommandPermissionCondition.Generator(nonGlobalCommands).build());
      });
   }

   private static String asNonGlobal(Command oldCommand, CommandParameters oldParameters) {
      String name = Optional.ofNullable(oldParameters.getMetadata())
         .map(CommandMetadata::getCalledName)
         .filter(n -> !n.startsWith("/"))
         .orElseGet(oldCommand::getName);
      return "/tool " + name;
   }

   static void setToolNone(Player player, LocalSession session, boolean isBrush) throws InvalidToolBindException {
      isBrush = session.getTool(player) instanceof BrushTool;
      session.setTool(player.getItemInHand(HandSide.MAIN_HAND), null);
      player.print(Caption.of(isBrush ? "worldedit.brush.none.equip" : "worldedit.tool.none.equip"));
   }

   static void sendUnbindInstruction(Player sender, Component commandComponent) {
      sender.print(Caption.of("worldedit.tool.unbind-instruction", commandComponent));
   }

   private static void setTool(Player player, LocalSession session, Tool tool, String translationKey) throws InvalidToolBindException {
      BaseItemStack itemStack = player.getItemInHand(HandSide.MAIN_HAND);
      session.setTool(itemStack, tool);
      player.print(Caption.of(translationKey, itemStack.getRichName()));
      sendUnbindInstruction(player, UNBIND_COMMAND_COMPONENT);
   }

   public ToolCommands(WorldEdit we) {
      this.we = we;
   }

   @org.enginehub.piston.annotation.Command(
      name = "none",
      aliases = {"unbind"},
      desc = "Unbind a bound tool from your current item"
   )
   @CommandPermissions({"worldedit.tool.none"})
   public void none(Player player, LocalSession session) throws WorldEditException {
      setToolNone(player, session, false);
   }

   @org.enginehub.piston.annotation.Command(
      name = "selwand",
      aliases = {"/selwand"},
      desc = "Selection wand tool"
   )
   @CommandPermissions({"worldedit.setwand"})
   public void selwand(Player player, LocalSession session) throws WorldEditException {
      setTool(player, session, SelectionWand.INSTANCE, "worldedit.tool.selwand.equip");
   }

   @org.enginehub.piston.annotation.Command(
      name = "navwand",
      aliases = {"/navwand"},
      desc = "Navigation wand tool"
   )
   @CommandPermissions({"worldedit.setwand"})
   public void navwand(Player player, LocalSession session) throws WorldEditException {
      setTool(player, session, NavigationWand.INSTANCE, "worldedit.tool.navwand.equip");
   }

   @org.enginehub.piston.annotation.Command(
      name = "info",
      aliases = {"/info"},
      desc = "Block information tool"
   )
   @CommandPermissions({"worldedit.tool.info"})
   public void info(Player player, LocalSession session) throws WorldEditException {
      setTool(player, session, new QueryTool(), "worldedit.tool.info.equip");
   }

   @org.enginehub.piston.annotation.Command(
      name = "inspect",
      aliases = {"/inspect"},
      desc = "Inspect edits within a radius"
   )
   @CommandPermissions({"worldedit.tool.inspect"})
   public void inspectBrush(Player player, LocalSession session) throws WorldEditException {
      setTool(player, session, new InspectBrush(), "worldedit.tool.inspect.equip");
   }

   @org.enginehub.piston.annotation.Command(
      name = "tree",
      aliases = {"/tree"},
      desc = "Tree generator tool"
   )
   @CommandPermissions({"worldedit.tool.tree"})
   public void tree(Player player, LocalSession session, @Arg(desc = "Type of tree to generate",def = {"tree"}) TreeType type) throws WorldEditException {
      setTool(player, session, new TreePlanter(type), "worldedit.tool.tree.equip");
   }

   @org.enginehub.piston.annotation.Command(
      name = "featureplacer",
      aliases = {"/featureplacer", "featuretool", "/featuretool"},
      desc = "Feature placer tool"
   )
   @CommandPermissions({"worldedit.tool.feature"})
   public void feature(Player player, LocalSession session, @Arg(desc = "Type of feature to place") ConfiguredFeatureType feature) throws WorldEditException {
      setTool(player, session, new FeaturePlacer(feature), "worldedit.tool.feature.equip");
   }

   @org.enginehub.piston.annotation.Command(
      name = "structureplacer",
      aliases = {"/structureplacer", "structuretool", "/structuretool"},
      desc = "Structure placer tool"
   )
   @CommandPermissions({"worldedit.tool.structure"})
   public void structure(Player player, LocalSession session, @Arg(desc = "Type of structure to place") StructureType feature) throws WorldEditException {
      setTool(player, session, new StructurePlacer(feature), "worldedit.tool.structure.equip");
   }

   @org.enginehub.piston.annotation.Command(
      name = "stacker",
      desc = "Block stacker tool"
   )
   @CommandPermissions({"worldedit.tool.stack"})
   public void stacker(
      Player player,
      LocalSession session,
      @Arg(desc = "The max range of the stack",def = {"10"}) int range,
      @Arg(desc = "The mask to stack until",def = {"!#existing"}) Mask mask
   ) throws WorldEditException {
      setTool(player, session, new StackTool(range, mask), "worldedit.tool.stack.equip");
   }

   @org.enginehub.piston.annotation.Command(
      name = "repl",
      aliases = {"/repl"},
      desc = "Block replacer tool"
   )
   @CommandPermissions({"worldedit.tool.replacer"})
   public void repl(Player player, LocalSession session, @Arg(desc = "The pattern of blocks to place") Pattern pattern) throws WorldEditException {
      setTool(player, session, new BlockReplacer(pattern), "worldedit.tool.repl.equip");
   }

   @org.enginehub.piston.annotation.Command(
      name = "cycler",
      aliases = {"/cycler"},
      desc = "Block data cycler tool"
   )
   @CommandPermissions({"worldedit.tool.data-cycler"})
   public void cycler(Player player, LocalSession session) throws WorldEditException {
      setTool(player, session, new BlockDataCyler(), "worldedit.tool.data-cycler.equip");
   }

   @org.enginehub.piston.annotation.Command(
      name = "floodfill",
      aliases = {"flood", "/flood", "/floodfill"},
      desc = "Flood fill tool"
   )
   @CommandPermissions({"worldedit.tool.flood-fill"})
   public void floodFill(
      Player player, LocalSession session, @Arg(desc = "The pattern to flood fill") Pattern pattern, @Arg(desc = "The range to perform the fill") int range
   ) throws WorldEditException {
      if (range > player.getLimit().MAX_SUPER_PICKAXE_SIZE) {
         player.print(Caption.of("worldedit.tool.superpickaxe.max-range", TextComponent.of(player.getLimit().MAX_SUPER_PICKAXE_SIZE)));
      } else {
         setTool(player, session, new FloodFillTool(range, pattern), "worldedit.tool.floodfill.equip");
      }
   }

   @org.enginehub.piston.annotation.Command(
      name = "deltree",
      aliases = {"/deltree"},
      desc = "Floating tree remover tool"
   )
   @CommandPermissions({"worldedit.tool.deltree"})
   public void deltree(Player player, LocalSession session) throws WorldEditException {
      setTool(player, session, new FloatingTreeRemover(), "worldedit.tool.deltree.equip");
   }

   @org.enginehub.piston.annotation.Command(
      name = "farwand",
      aliases = {"/warwand"},
      desc = "Wand at a distance tool"
   )
   @CommandPermissions({"worldedit.tool.farwand"})
   public void farwand(Player player, LocalSession session) throws WorldEditException {
      setTool(player, session, new DistanceWand(), "worldedit.tool.farwand.equip");
   }

   @org.enginehub.piston.annotation.Command(
      name = "lrbuild",
      aliases = {"/lrbuild"},
      desc = "Long-range building tool"
   )
   @CommandPermissions({"worldedit.tool.lrbuild"})
   public void longrangebuildtool(
      Player player,
      LocalSession session,
      @Arg(desc = "Pattern to set on left-click") Pattern primary,
      @Arg(desc = "Pattern to set on right-click") Pattern secondary
   ) throws WorldEditException {
      setTool(player, session, new LongRangeBuildTool(primary, secondary), "worldedit.tool.lrbuild.equip");
      Component primaryName;
      if (primary instanceof BlockStateHolder) {
         primaryName = ((BlockStateHolder)primary).getBlockType().getRichName();
      } else {
         primaryName = TextComponent.of("pattern");
      }

      Component secondaryName;
      if (secondary instanceof BlockStateHolder) {
         secondaryName = ((BlockStateHolder)secondary).getBlockType().getRichName();
      } else {
         secondaryName = TextComponent.of("pattern");
      }

      player.print(Caption.of("worldedit.tool.lrbuild.set", primaryName, secondaryName));
   }
}
