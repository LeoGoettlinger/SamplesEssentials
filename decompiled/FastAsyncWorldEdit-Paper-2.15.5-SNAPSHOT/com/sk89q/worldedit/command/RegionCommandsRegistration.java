package com.sk89q.worldedit.command;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.annotation.Confirm;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.annotation.Direction;
import com.sk89q.worldedit.internal.annotation.Offset;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.generation.TreeType;
import java.lang.annotation.Annotation;
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

public final class RegionCommandsRegistration implements CommandRegistration<RegionCommands>, CommandPermissionsConditionGenerator.Registration {
   private static final Key<Pattern> pattern_Key = Key.of(Pattern.class);
   private static final Key<Double> double_Key = Key.of(Double.class);
   private static final Key<Mask> mask_Key = Key.of(Mask.class);
   private static final Key<Integer> integer_Key = Key.of(Integer.class);
   private static final Key<BlockVector3> blockVector3_directionIncludeDiagonalstrue_Key = Key.of(BlockVector3.class, (new Object() {
      Annotation a(@Direction(includeDiagonals = true) Object ah) {
         try {
            return this.getClass().getDeclaredMethod("a", Object.class).getParameterAnnotations()[0][0];
         } catch (NoSuchMethodException var3) {
            throw new RuntimeException(var3);
         }
      }
   }).a(null));
   private static final Key<BlockStateHolder> blockStateHolder_Key = Key.of(BlockStateHolder.class);
   private static final Key<Integer> integer_confirmcom_sk89q_worldedit_command_util_annotation_Confirm_Processor_REGION_Key = Key.of(
      Integer.class, (new Object() {
         Annotation a(@Confirm(Confirm.Processor.REGION) Object ah) {
            try {
               return this.getClass().getDeclaredMethod("a", Object.class).getParameterAnnotations()[0][0];
            } catch (NoSuchMethodException var3) {
               throw new RuntimeException(var3);
            }
         }
      }).a(null)
   );
   private static final Key<BlockVector3> blockVector3_offset_Key = Key.of(BlockVector3.class, Offset.class);
   private static final Key<Long> long_Key = Key.of(Long.class);
   private static final Key<BiomeType> biomeType_Key = Key.of(BiomeType.class);
   private static final Key<String> string_Key = Key.of(String.class);
   private static final Key<TreeType> treeType_Key = Key.of(TreeType.class);
   private static final Key<Actor> actor_Key = Key.of(Actor.class);
   private static final Key<EditSession> editSession_Key = Key.of(EditSession.class);
   private static final Key<Region> region_selection_Key = Key.of(Region.class, Selection.class);
   private static final Key<LocalSession> localSession_Key = Key.of(LocalSession.class);
   private static final Key<Player> player_Key = Key.of(Player.class);
   private static final Key<World> world_Key = Key.of(World.class);
   private CommandManager commandManager;
   private RegionCommands containerInstance;
   private CommandPermissionsConditionGenerator commandPermissionsConditionGenerator;
   private ImmutableList<CommandCallListener> listeners;
   private final CommandArgument patternPart = CommandParts.arg(TranslatableComponent.of("pattern"), TextComponent.of("The pattern of blocks to set"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(pattern_Key))
      .build();
   private final CommandArgument testValuePart = CommandParts.arg(TranslatableComponent.of("testValue"), TextComponent.of("test"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final CommandArgument patternPart2 = CommandParts.arg(TranslatableComponent.of("pattern"), TextComponent.of("The pattern of blocks to place"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(pattern_Key))
      .build();
   private final CommandArgument thicknessPart = CommandParts.arg(TranslatableComponent.of("thickness"), TextComponent.of("The thickness of the line"))
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final NoArgCommandFlag shellPart = CommandParts.flag('h', TextComponent.of("Generate only a shell")).build();
   private final CommandArgument thicknessPart2 = CommandParts.arg(TranslatableComponent.of("thickness"), TextComponent.of("The thickness of the curve"))
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final CommandArgument fromPart = CommandParts.arg(TranslatableComponent.of("from"), TextComponent.of("The mask representing blocks to replace"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(mask_Key))
      .build();
   private final CommandArgument toPart = CommandParts.arg(TranslatableComponent.of("to"), TextComponent.of("The pattern of blocks to replace with"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(pattern_Key))
      .build();
   private final CommandArgument patternPart3 = CommandParts.arg(TranslatableComponent.of("pattern"), TextComponent.of("The pattern of blocks to overlay"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(pattern_Key))
      .build();
   private final CommandArgument patternArgPart = CommandParts.arg(TranslatableComponent.of("pattern"), TextComponent.of("The pattern of blocks to lay"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(pattern_Key))
      .build();
   private final CommandArgument iterationsPart = CommandParts.arg(TranslatableComponent.of("iterations"), TextComponent.of("# of iterations to perform"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument maskPart = CommandParts.arg(TranslatableComponent.of("mask"), TextComponent.of("The mask of blocks to use as the height map"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(mask_Key))
      .build();
   private final ArgAcceptingCommandFlag snowBlockCountPart = CommandParts.flag('l', TextComponent.of("Set the amount of snow blocks under the snow"))
      .withRequiredArg()
      .argNamed(TranslatableComponent.of("snowBlockCount"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final ArgAcceptingCommandFlag maskPart2 = CommandParts.flag('m', TextComponent.of("The mask of blocks to use as the height map"))
      .withRequiredArg()
      .argNamed(TranslatableComponent.of("mask"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(mask_Key))
      .build();
   private final CommandArgument countPart = CommandParts.arg(TranslatableComponent.of("count"), TextComponent.of("# of blocks to move"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument directionPart = CommandParts.arg(TranslatableComponent.of("direction"), TextComponent.of("The direction to move"))
      .defaultsTo(ImmutableList.of("me"))
      .ofTypes(ImmutableList.of(blockVector3_directionIncludeDiagonalstrue_Key))
      .build();
   private final CommandArgument replacePart = CommandParts.arg(TranslatableComponent.of("replace"), TextComponent.of("The pattern of blocks to leave"))
      .defaultsTo(ImmutableList.of("air"))
      .ofTypes(ImmutableList.of(pattern_Key))
      .build();
   private final NoArgCommandFlag moveSelectionPart = CommandParts.flag('s', TextComponent.of("Shift the selection to the target location")).build();
   private final NoArgCommandFlag ignoreAirBlocksPart = CommandParts.flag('a', TextComponent.of("Ignore air blocks")).build();
   private final NoArgCommandFlag copyEntitiesPart = CommandParts.flag('e', TextComponent.of("Also copy entities")).build();
   private final NoArgCommandFlag copyBiomesPart = CommandParts.flag('b', TextComponent.of("Also copy biomes")).build();
   private final ArgAcceptingCommandFlag maskPart3 = CommandParts.flag('m', TextComponent.of("Set the include mask, non-matching blocks become air"))
      .withRequiredArg()
      .argNamed(TranslatableComponent.of("mask"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(mask_Key))
      .build();
   private final CommandArgument replacePart2 = CommandParts.arg(TranslatableComponent.of("replace"), TextComponent.of("BlockStateHolder"))
      .defaultsTo(ImmutableList.of("air"))
      .ofTypes(ImmutableList.of(blockStateHolder_Key))
      .build();
   private final NoArgCommandFlag notFullHeightPart = CommandParts.flag('m', TextComponent.of("Only fall within the vertical selection")).build();
   private final CommandArgument countPart2 = CommandParts.arg(TranslatableComponent.of("count"), TextComponent.of("# of copies to stack"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(integer_confirmcom_sk89q_worldedit_command_util_annotation_Confirm_Processor_REGION_Key))
      .build();
   private final CommandArgument offsetPart = CommandParts.arg(TranslatableComponent.of("offset"), TextComponent.of("How far to move the contents each stack"))
      .defaultsTo(ImmutableList.of("forward"))
      .ofTypes(ImmutableList.of(blockVector3_offset_Key))
      .build();
   private final NoArgCommandFlag moveSelectionPart2 = CommandParts.flag('s', TextComponent.of("Shift the selection to the last stacked copy")).build();
   private final NoArgCommandFlag blockUnitsPart = CommandParts.flag('r', TextComponent.of("Use block units")).build();
   private final CommandArgument seedPart = CommandParts.arg(
         TranslatableComponent.of("seed"), TextComponent.of("The seed to regenerate with, otherwise uses world seed")
      )
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(long_Key))
      .build();
   private final NoArgCommandFlag regenBiomesPart = CommandParts.flag('b', TextComponent.of("Regenerate biomes as well")).build();
   private final NoArgCommandFlag randomSeedPart = CommandParts.flag('r', TextComponent.of("If the seed should be randomized")).build();
   private final CommandArgument biomeTypePart = CommandParts.arg(
         TranslatableComponent.of("biomeType"), TextComponent.of("Biome to apply for this regeneration (only works in overworld)")
      )
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(biomeType_Key))
      .build();
   private final CommandArgument expressionPart = CommandParts.arg(TranslatableComponent.of("expression"), TextComponent.of("The expression to use"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(string_Key))
      .variable(true)
      .build();
   private final NoArgCommandFlag useRawCoordsPart = CommandParts.flag('r', TextComponent.of("Use the game's coordinate origin")).build();
   private final NoArgCommandFlag offsetPart2 = CommandParts.flag('o', TextComponent.of("Use the placement's coordinate origin")).build();
   private final NoArgCommandFlag offsetCenterPart = CommandParts.flag('c', TextComponent.of("Use the selection's center as origin")).build();
   private final CommandArgument thicknessPart3 = CommandParts.arg(TranslatableComponent.of("thickness"), TextComponent.of("Thickness of the shell to leave"))
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument patternPart4 = CommandParts.arg(
         TranslatableComponent.of("pattern"), TextComponent.of("The pattern of blocks to replace the hollowed area with")
      )
      .defaultsTo(ImmutableList.of("air"))
      .ofTypes(ImmutableList.of(pattern_Key))
      .build();
   private final ArgAcceptingCommandFlag maskPart4 = CommandParts.flag('m', TextComponent.of("Mask to hollow with"))
      .withRequiredArg()
      .argNamed(TranslatableComponent.of("mask"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(mask_Key))
      .build();
   private final CommandArgument typePart = CommandParts.arg(TranslatableComponent.of("type"), TextComponent.of("The type of tree to place"))
      .defaultsTo(ImmutableList.of("tree"))
      .ofTypes(ImmutableList.of(treeType_Key))
      .build();
   private final CommandArgument densityPart = CommandParts.arg(TranslatableComponent.of("density"), TextComponent.of("The density of the forest"))
      .defaultsTo(ImmutableList.of("5"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();

   private RegionCommandsRegistration() {
      this.listeners = ImmutableList.of();
   }

   public static RegionCommandsRegistration builder() {
      return new RegionCommandsRegistration();
   }

   public RegionCommandsRegistration commandManager(CommandManager commandManager) {
      this.commandManager = commandManager;
      return this;
   }

   public RegionCommandsRegistration containerInstance(RegionCommands containerInstance) {
      this.containerInstance = containerInstance;
      return this;
   }

   public RegionCommandsRegistration commandPermissionsConditionGenerator(CommandPermissionsConditionGenerator commandPermissionsConditionGenerator) {
      this.commandPermissionsConditionGenerator = commandPermissionsConditionGenerator;
      return this;
   }

   public RegionCommandsRegistration listeners(Collection<CommandCallListener> listeners) {
      this.listeners = ImmutableList.copyOf(listeners);
      return this;
   }

   @Override
   public void build() {
      this.commandManager.register("/set", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Sets all the blocks in the region"));
         b.parts(ImmutableList.of(this.patternPart));
         b.action(this::cmd$_set);
         Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "set", Actor.class, EditSession.class, Region.class, Pattern.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/air", b -> {
         b.aliases(ImmutableList.of("/0"));
         b.description(TextComponent.of("Sets all the blocks in the region to air"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$_air);
         Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "air", Actor.class, EditSession.class, Region.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/test", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("test region"));
         b.parts(ImmutableList.of(this.testValuePart));
         b.action(this::cmd$_test);
         Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "test", Actor.class, EditSession.class, double.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/fixlighting", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Get the light at a position"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$_fixlighting);
         Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "fixLighting", Actor.class, LocalSession.class, Region.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/removelighting", b -> {
         b.aliases(ImmutableList.of("/removelight"));
         b.description(TextComponent.of("Removing lighting in a selection"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$_removelighting);
         Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "removeLighting", Actor.class, LocalSession.class, Region.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/nbtinfo", b -> {
         b.aliases(ImmutableList.of("/nbt"));
         b.description(TextComponent.of("View nbt info for a block"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$_nbtinfo);
         Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "nbtinfo", Player.class, EditSession.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/setblocklight", b -> {
         b.aliases(ImmutableList.of("/setlight"));
         b.description(TextComponent.of("Set block lighting in a selection"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$_setblocklight);
         Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "setlighting", Actor.class, EditSession.class, Region.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/setskylight", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Set sky lighting in a selection"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$_setskylight);
         Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "setskylighting", Actor.class, Region.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager
         .register(
            "/line",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Draws line segments between cuboid selection corners or convex polyhedral selection vertices"));
               b.footer(TextComponent.of("Can only be used with a cuboid selection or a convex polyhedral selection"));
               b.parts(ImmutableList.of(this.patternPart2, this.thicknessPart, this.shellPart));
               b.action(this::cmd$_line);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  RegionCommands.class, "line", Actor.class, EditSession.class, Region.class, Pattern.class, double.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/curve",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Draws a spline through selected points"));
               b.footer(TextComponent.of("Can only be used with a convex polyhedral selection"));
               b.parts(ImmutableList.of(this.patternPart2, this.thicknessPart2, this.shellPart));
               b.action(this::cmd$_curve);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  RegionCommands.class, "curve", Actor.class, EditSession.class, Region.class, Pattern.class, double.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/replace",
            b -> {
               b.aliases(ImmutableList.of("/re", "/rep"));
               b.description(TextComponent.of("Replace all blocks in the selection with another"));
               b.parts(ImmutableList.of(this.fromPart, this.toPart));
               b.action(this::cmd$_replace);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  RegionCommands.class, "replace", Actor.class, EditSession.class, Region.class, Mask.class, Pattern.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("/overlay", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Set a block on top of blocks in the region"));
         b.parts(ImmutableList.of(this.patternPart3));
         b.action(this::cmd$_overlay);
         Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "overlay", Actor.class, EditSession.class, Region.class, Pattern.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/lay", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Set the top block in the region"));
         b.parts(ImmutableList.of(this.patternArgPart));
         b.action(this::cmd$_lay);
         Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "lay", Actor.class, EditSession.class, Region.class, Pattern.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/center", b -> {
         b.aliases(ImmutableList.of("/middle"));
         b.description(TextComponent.of("Set the center block(s)"));
         b.parts(ImmutableList.of(this.patternPart));
         b.action(this::cmd$_center);
         Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "center", Actor.class, EditSession.class, Region.class, Pattern.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/naturalize", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("3 layers of dirt on top then rock below"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$_naturalize);
         Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "naturalize", Actor.class, EditSession.class, Region.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/walls", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Build the four sides of the selection"));
         b.parts(ImmutableList.of(this.patternPart));
         b.action(this::cmd$_walls);
         Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "walls", Actor.class, EditSession.class, Region.class, Pattern.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/faces", b -> {
         b.aliases(ImmutableList.of("/outline"));
         b.description(TextComponent.of("Build the walls, ceiling, and floor of a selection"));
         b.parts(ImmutableList.of(this.patternPart));
         b.action(this::cmd$_faces);
         Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "faces", Actor.class, EditSession.class, Region.class, Pattern.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager
         .register(
            "/smooth",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Smooth the elevation in the selection"));
               b.footer(TextComponent.of("Example: '//smooth 1 grass_block,dirt,stone' would only smooth natural surface terrain."));
               b.parts(ImmutableList.of(this.iterationsPart, this.maskPart));
               b.action(this::cmd$_smooth);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  RegionCommands.class, "smooth", Actor.class, EditSession.class, Region.class, int.class, Mask.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("/wea", b -> {
         b.aliases(ImmutableList.of("wea", "worldeditanywhere", "/worldeditanywhere", "/weanywhere"));
         b.description(TextComponent.of("Bypass region restrictions"));
         b.footer(TextComponent.of("Bypass region restrictions"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$_wea);
         Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "wea", Actor.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/wer", b -> {
         b.aliases(ImmutableList.of("wer", "worldeditregion", "/worldeditregion", "select", "/select"));
         b.description(TextComponent.of("Select your current allowed region"));
         b.footer(TextComponent.of("Select your current allowed region"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$_wer);
         Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "wer", Player.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager
         .register(
            "/snowsmooth",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Smooth the elevation in the selection with snow layers"));
               b.footer(TextComponent.of("Example: '//snowsmooth 1 -m snow_block,snow' would only smooth snow terrain."));
               b.parts(ImmutableList.of(this.iterationsPart, this.snowBlockCountPart, this.maskPart2));
               b.action(this::cmd$_snowsmooth);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  RegionCommands.class, "snowSmooth", Actor.class, EditSession.class, Region.class, int.class, int.class, Mask.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/move",
            b -> {
               b.aliases(ImmutableList.of("/mv"));
               b.description(TextComponent.of("Move the contents of the selection"));
               b.parts(
                  ImmutableList.of(
                     this.countPart,
                     this.directionPart,
                     this.replacePart,
                     this.moveSelectionPart,
                     this.ignoreAirBlocksPart,
                     this.copyEntitiesPart,
                     this.copyBiomesPart,
                     this.maskPart3
                  )
               );
               b.action(this::cmd$_move);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  RegionCommands.class,
                  "move",
                  Actor.class,
                  World.class,
                  EditSession.class,
                  LocalSession.class,
                  Region.class,
                  int.class,
                  BlockVector3.class,
                  Pattern.class,
                  boolean.class,
                  boolean.class,
                  boolean.class,
                  boolean.class,
                  Mask.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/fall",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Have the blocks in the selection fall"));
               b.footer(TextComponent.of("Make the blocks in the selection fall\n"));
               b.parts(ImmutableList.of(this.replacePart2, this.notFullHeightPart));
               b.action(this::cmd$_fall);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  RegionCommands.class, "fall", Actor.class, EditSession.class, Region.class, BlockStateHolder.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/stack",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Repeat the contents of the selection"));
               b.parts(
                  ImmutableList.of(
                     this.countPart2,
                     this.offsetPart,
                     this.moveSelectionPart2,
                     this.ignoreAirBlocksPart,
                     this.copyEntitiesPart,
                     this.copyBiomesPart,
                     this.blockUnitsPart,
                     this.maskPart3
                  )
               );
               b.action(this::cmd$_stack);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  RegionCommands.class,
                  "stack",
                  Actor.class,
                  World.class,
                  EditSession.class,
                  LocalSession.class,
                  Region.class,
                  int.class,
                  BlockVector3.class,
                  boolean.class,
                  boolean.class,
                  boolean.class,
                  boolean.class,
                  boolean.class,
                  Mask.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/regen",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Regenerates the contents of the selection"));
               b.footer(TextComponent.of("This command might affect things outside the selection,\nif they are within the same chunk."));
               b.parts(ImmutableList.of(this.seedPart, this.regenBiomesPart, this.randomSeedPart, this.biomeTypePart));
               b.action(this::cmd$_regen);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  RegionCommands.class,
                  "regenerate",
                  Actor.class,
                  World.class,
                  LocalSession.class,
                  EditSession.class,
                  Region.class,
                  Long.class,
                  boolean.class,
                  boolean.class,
                  BiomeType.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/deform",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Deforms a selected region with an expression"));
               b.footer(
                  TextComponent.of(
                     "The expression is executed for each block and is expected\nto modify the variables x, y and z to point to a new block\nto fetch. For details, see https://ehub.to/we/expr"
                  )
               );
               b.parts(ImmutableList.of(this.expressionPart, this.useRawCoordsPart, this.offsetPart2, this.offsetCenterPart));
               b.action(this::cmd$_deform);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  RegionCommands.class,
                  "deform",
                  Actor.class,
                  LocalSession.class,
                  EditSession.class,
                  Region.class,
                  List.class,
                  boolean.class,
                  boolean.class,
                  boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/hollow",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Hollows out the object contained in this selection"));
               b.footer(
                  TextComponent.of(
                     "Hollows out the object contained in this selection.\nOptionally fills the hollowed out part with the given block.\nThickness is measured in manhattan distance."
                  )
               );
               b.parts(ImmutableList.of(this.thicknessPart3, this.patternPart4, this.maskPart4));
               b.action(this::cmd$_hollow);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  RegionCommands.class, "hollow", Actor.class, EditSession.class, Region.class, int.class, Pattern.class, Mask.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/forest",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Make a forest within the region"));
               b.parts(ImmutableList.of(this.typePart, this.densityPart));
               b.action(this::cmd$_forest);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  RegionCommands.class, "forest", Actor.class, EditSession.class, Region.class, TreeType.class, double.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("/flora", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Make flora within the region"));
         b.parts(ImmutableList.of(this.densityPart));
         b.action(this::cmd$_flora);
         Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "flora", Actor.class, EditSession.class, Region.class, double.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager.register("/fixblocks", b -> {
         b.aliases(ImmutableList.of("/updateblocks", "/fixconnect"));
         b.description(TextComponent.of("\"Fixes\" all blocks in the region to the correct shape/connections based on surrounding blocks"));
         b.parts(ImmutableList.of());
         b.action(this::cmd$_fixblocks);
         Method commandMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "fixblocks", Actor.class, EditSession.class, Region.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
   }

   private int cmd$_set(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "set", Actor.class, EditSession.class, Region.class, Pattern.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .set(this.extract$actor(parameters), this.extract$editSession(parameters), this.extract$region(parameters), this.extract$pattern(parameters));
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_air(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "air", Actor.class, EditSession.class, Region.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.air(this.extract$actor(parameters), this.extract$editSession(parameters), this.extract$region(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_test(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "test", Actor.class, EditSession.class, double.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.test(this.extract$actor(parameters), this.extract$editSession(parameters), this.extract$testValue(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_fixlighting(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "fixLighting", Actor.class, LocalSession.class, Region.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.fixLighting(this.extract$actor(parameters), this.extract$session(parameters), this.extract$selection(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_removelighting(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "removeLighting", Actor.class, LocalSession.class, Region.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.removeLighting(this.extract$actor(parameters), this.extract$session(parameters), this.extract$selection(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_nbtinfo(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "nbtinfo", Player.class, EditSession.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.nbtinfo(this.extract$player(parameters), this.extract$editSession(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_setblocklight(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "setlighting", Actor.class, EditSession.class, Region.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.setlighting(this.extract$actor(parameters), this.extract$editSession(parameters), this.extract$region(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_setskylight(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "setskylighting", Actor.class, Region.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.setskylighting(this.extract$actor(parameters), this.extract$region(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_line(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         RegionCommands.class, "line", Actor.class, EditSession.class, Region.class, Pattern.class, double.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .line(
               this.extract$actor(parameters),
               this.extract$editSession(parameters),
               this.extract$region(parameters),
               this.extract$pattern2(parameters),
               this.extract$thickness(parameters),
               this.extract$shell(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_curve(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         RegionCommands.class, "curve", Actor.class, EditSession.class, Region.class, Pattern.class, double.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .curve(
               this.extract$actor(parameters),
               this.extract$editSession(parameters),
               this.extract$region(parameters),
               this.extract$pattern2(parameters),
               this.extract$thickness2(parameters),
               this.extract$shell(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_replace(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         RegionCommands.class, "replace", Actor.class, EditSession.class, Region.class, Mask.class, Pattern.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .replace(
               this.extract$actor(parameters),
               this.extract$editSession(parameters),
               this.extract$region(parameters),
               this.extract$from(parameters),
               this.extract$to(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_overlay(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "overlay", Actor.class, EditSession.class, Region.class, Pattern.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .overlay(this.extract$actor(parameters), this.extract$editSession(parameters), this.extract$region(parameters), this.extract$pattern3(parameters));
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_lay(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "lay", Actor.class, EditSession.class, Region.class, Pattern.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .lay(this.extract$actor(parameters), this.extract$editSession(parameters), this.extract$region(parameters), this.extract$patternArg(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_center(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "center", Actor.class, EditSession.class, Region.class, Pattern.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .center(this.extract$actor(parameters), this.extract$editSession(parameters), this.extract$region(parameters), this.extract$pattern(parameters));
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_naturalize(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "naturalize", Actor.class, EditSession.class, Region.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance.naturalize(this.extract$actor(parameters), this.extract$editSession(parameters), this.extract$region(parameters));
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_walls(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "walls", Actor.class, EditSession.class, Region.class, Pattern.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .walls(this.extract$actor(parameters), this.extract$editSession(parameters), this.extract$region(parameters), this.extract$pattern(parameters));
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_faces(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "faces", Actor.class, EditSession.class, Region.class, Pattern.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .faces(this.extract$actor(parameters), this.extract$editSession(parameters), this.extract$region(parameters), this.extract$pattern(parameters));
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_smooth(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "smooth", Actor.class, EditSession.class, Region.class, int.class, Mask.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .smooth(
               this.extract$actor(parameters),
               this.extract$editSession(parameters),
               this.extract$region(parameters),
               this.extract$iterations(parameters),
               this.extract$mask(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_wea(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "wea", Actor.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.wea(this.extract$actor(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_wer(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "wer", Player.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.wer(this.extract$player(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_snowsmooth(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         RegionCommands.class, "snowSmooth", Actor.class, EditSession.class, Region.class, int.class, int.class, Mask.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .snowSmooth(
               this.extract$actor(parameters),
               this.extract$editSession(parameters),
               this.extract$region(parameters),
               this.extract$iterations(parameters),
               this.extract$snowBlockCount(parameters),
               this.extract$mask2(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_move(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         RegionCommands.class,
         "move",
         Actor.class,
         World.class,
         EditSession.class,
         LocalSession.class,
         Region.class,
         int.class,
         BlockVector3.class,
         Pattern.class,
         boolean.class,
         boolean.class,
         boolean.class,
         boolean.class,
         Mask.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .move(
               this.extract$actor(parameters),
               this.extract$world(parameters),
               this.extract$editSession(parameters),
               this.extract$session(parameters),
               this.extract$region(parameters),
               this.extract$count(parameters),
               this.extract$direction(parameters),
               this.extract$replace(parameters),
               this.extract$moveSelection(parameters),
               this.extract$ignoreAirBlocks(parameters),
               this.extract$copyEntities(parameters),
               this.extract$copyBiomes(parameters),
               this.extract$mask3(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_fall(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         RegionCommands.class, "fall", Actor.class, EditSession.class, Region.class, BlockStateHolder.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .fall(
               this.extract$actor(parameters),
               this.extract$editSession(parameters),
               this.extract$region(parameters),
               this.extract$replace2(parameters),
               this.extract$notFullHeight(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_stack(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         RegionCommands.class,
         "stack",
         Actor.class,
         World.class,
         EditSession.class,
         LocalSession.class,
         Region.class,
         int.class,
         BlockVector3.class,
         boolean.class,
         boolean.class,
         boolean.class,
         boolean.class,
         boolean.class,
         Mask.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .stack(
               this.extract$actor(parameters),
               this.extract$world(parameters),
               this.extract$editSession(parameters),
               this.extract$session(parameters),
               this.extract$region(parameters),
               this.extract$count2(parameters),
               this.extract$offset(parameters),
               this.extract$moveSelection2(parameters),
               this.extract$ignoreAirBlocks(parameters),
               this.extract$copyEntities(parameters),
               this.extract$copyBiomes(parameters),
               this.extract$blockUnits(parameters),
               this.extract$mask3(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_regen(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         RegionCommands.class,
         "regenerate",
         Actor.class,
         World.class,
         LocalSession.class,
         EditSession.class,
         Region.class,
         Long.class,
         boolean.class,
         boolean.class,
         BiomeType.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .regenerate(
               this.extract$actor(parameters),
               this.extract$world(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$region(parameters),
               this.extract$seed(parameters),
               this.extract$regenBiomes(parameters),
               this.extract$randomSeed(parameters),
               this.extract$biomeType(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_deform(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         RegionCommands.class,
         "deform",
         Actor.class,
         LocalSession.class,
         EditSession.class,
         Region.class,
         List.class,
         boolean.class,
         boolean.class,
         boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .deform(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$region(parameters),
               this.extract$expression(parameters),
               this.extract$useRawCoords(parameters),
               this.extract$offset2(parameters),
               this.extract$offsetCenter(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_hollow(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         RegionCommands.class, "hollow", Actor.class, EditSession.class, Region.class, int.class, Pattern.class, Mask.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .hollow(
               this.extract$actor(parameters),
               this.extract$editSession(parameters),
               this.extract$region(parameters),
               this.extract$thickness3(parameters),
               this.extract$pattern4(parameters),
               this.extract$mask4(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_forest(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         RegionCommands.class, "forest", Actor.class, EditSession.class, Region.class, TreeType.class, double.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .forest(
               this.extract$actor(parameters),
               this.extract$editSession(parameters),
               this.extract$region(parameters),
               this.extract$type(parameters),
               this.extract$density(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_flora(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "flora", Actor.class, EditSession.class, Region.class, double.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .flora(this.extract$actor(parameters), this.extract$editSession(parameters), this.extract$region(parameters), this.extract$density(parameters));
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_fixblocks(CommandParameters parameters) {
      Method cmdMethod = RegistrationUtil.getCommandMethod(RegionCommands.class, "fixblocks", Actor.class, EditSession.class, Region.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance.fixblocks(this.extract$actor(parameters), this.extract$editSession(parameters), this.extract$region(parameters));
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

   private EditSession extract$editSession(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(editSession_Key, "editSession", parameters.injectedValue(editSession_Key));
   }

   private Region extract$region(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(region_selection_Key, "region", parameters.injectedValue(region_selection_Key));
   }

   private Pattern extract$pattern(CommandParameters parameters) {
      return this.patternPart.value(parameters).asSingle(pattern_Key);
   }

   private double extract$testValue(CommandParameters parameters) {
      return this.testValuePart.value(parameters).asSingle(double_Key);
   }

   private LocalSession extract$session(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(localSession_Key, "session", parameters.injectedValue(localSession_Key));
   }

   private Region extract$selection(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(region_selection_Key, "selection", parameters.injectedValue(region_selection_Key));
   }

   private Player extract$player(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(player_Key, "player", parameters.injectedValue(player_Key));
   }

   private Pattern extract$pattern2(CommandParameters parameters) {
      return this.patternPart2.value(parameters).asSingle(pattern_Key);
   }

   private double extract$thickness(CommandParameters parameters) {
      return this.thicknessPart.value(parameters).asSingle(double_Key);
   }

   private boolean extract$shell(CommandParameters parameters) {
      return this.shellPart.in(parameters);
   }

   private double extract$thickness2(CommandParameters parameters) {
      return this.thicknessPart2.value(parameters).asSingle(double_Key);
   }

   private Mask extract$from(CommandParameters parameters) {
      return this.fromPart.value(parameters).asSingle(mask_Key);
   }

   private Pattern extract$to(CommandParameters parameters) {
      return this.toPart.value(parameters).asSingle(pattern_Key);
   }

   private Pattern extract$pattern3(CommandParameters parameters) {
      return this.patternPart3.value(parameters).asSingle(pattern_Key);
   }

   private Pattern extract$patternArg(CommandParameters parameters) {
      return this.patternArgPart.value(parameters).asSingle(pattern_Key);
   }

   private int extract$iterations(CommandParameters parameters) {
      return this.iterationsPart.value(parameters).asSingle(integer_Key);
   }

   private Mask extract$mask(CommandParameters parameters) {
      return this.maskPart.value(parameters).asSingle(mask_Key);
   }

   private int extract$snowBlockCount(CommandParameters parameters) {
      return this.snowBlockCountPart.value(parameters).asSingle(integer_Key);
   }

   private Mask extract$mask2(CommandParameters parameters) {
      return this.maskPart2.value(parameters).asSingle(mask_Key);
   }

   private World extract$world(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(world_Key, "world", parameters.injectedValue(world_Key));
   }

   private int extract$count(CommandParameters parameters) {
      return this.countPart.value(parameters).asSingle(integer_Key);
   }

   private BlockVector3 extract$direction(CommandParameters parameters) {
      return this.directionPart.value(parameters).asSingle(blockVector3_directionIncludeDiagonalstrue_Key);
   }

   private Pattern extract$replace(CommandParameters parameters) {
      return this.replacePart.value(parameters).asSingle(pattern_Key);
   }

   private boolean extract$moveSelection(CommandParameters parameters) {
      return this.moveSelectionPart.in(parameters);
   }

   private boolean extract$ignoreAirBlocks(CommandParameters parameters) {
      return this.ignoreAirBlocksPart.in(parameters);
   }

   private boolean extract$copyEntities(CommandParameters parameters) {
      return this.copyEntitiesPart.in(parameters);
   }

   private boolean extract$copyBiomes(CommandParameters parameters) {
      return this.copyBiomesPart.in(parameters);
   }

   private Mask extract$mask3(CommandParameters parameters) {
      return this.maskPart3.value(parameters).asSingle(mask_Key);
   }

   private BlockStateHolder extract$replace2(CommandParameters parameters) {
      return this.replacePart2.value(parameters).asSingle(blockStateHolder_Key);
   }

   private boolean extract$notFullHeight(CommandParameters parameters) {
      return this.notFullHeightPart.in(parameters);
   }

   private int extract$count2(CommandParameters parameters) {
      return this.countPart2.value(parameters).asSingle(integer_confirmcom_sk89q_worldedit_command_util_annotation_Confirm_Processor_REGION_Key);
   }

   private BlockVector3 extract$offset(CommandParameters parameters) {
      return this.offsetPart.value(parameters).asSingle(blockVector3_offset_Key);
   }

   private boolean extract$moveSelection2(CommandParameters parameters) {
      return this.moveSelectionPart2.in(parameters);
   }

   private boolean extract$blockUnits(CommandParameters parameters) {
      return this.blockUnitsPart.in(parameters);
   }

   private Long extract$seed(CommandParameters parameters) {
      return this.seedPart.value(parameters).asSingle(long_Key);
   }

   private boolean extract$regenBiomes(CommandParameters parameters) {
      return this.regenBiomesPart.in(parameters);
   }

   private boolean extract$randomSeed(CommandParameters parameters) {
      return this.randomSeedPart.in(parameters);
   }

   private BiomeType extract$biomeType(CommandParameters parameters) {
      return this.biomeTypePart.value(parameters).asSingle(biomeType_Key);
   }

   private List<String> extract$expression(CommandParameters parameters) {
      return this.expressionPart.value(parameters).<String>asMultiple(string_Key);
   }

   private boolean extract$useRawCoords(CommandParameters parameters) {
      return this.useRawCoordsPart.in(parameters);
   }

   private boolean extract$offset2(CommandParameters parameters) {
      return this.offsetPart2.in(parameters);
   }

   private boolean extract$offsetCenter(CommandParameters parameters) {
      return this.offsetCenterPart.in(parameters);
   }

   private int extract$thickness3(CommandParameters parameters) {
      return this.thicknessPart3.value(parameters).asSingle(integer_Key);
   }

   private Pattern extract$pattern4(CommandParameters parameters) {
      return this.patternPart4.value(parameters).asSingle(pattern_Key);
   }

   private Mask extract$mask4(CommandParameters parameters) {
      return this.maskPart4.value(parameters).asSingle(mask_Key);
   }

   private TreeType extract$type(CommandParameters parameters) {
      return this.typePart.value(parameters).asSingle(treeType_Key);
   }

   private double extract$density(CommandParameters parameters) {
      return this.densityPart.value(parameters).asSingle(double_Key);
   }
}
