package com.sk89q.worldedit.command;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.annotation.Confirm;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.annotation.Radii;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;
import com.sk89q.worldedit.world.generation.StructureType;
import com.sk89q.worldedit.world.generation.TreeType;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
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

public final class GenerationCommandsRegistration implements CommandRegistration<GenerationCommands>, CommandPermissionsConditionGenerator.Registration {
   private static final Key<Pattern> pattern_Key = Key.of(Pattern.class);
   private static final Key<Double> double_radii2_Key = Key.of(Double.class, (new Object() {
      Annotation a(@Radii(2) Object ah) {
         try {
            return this.getClass().getDeclaredMethod("a", Object.class).getParameterAnnotations()[0][0];
         } catch (NoSuchMethodException var3) {
            throw new RuntimeException(var3);
         }
      }
   }).a(null));
   private static final Key<Integer> integer_Key = Key.of(Integer.class);
   private static final Key<Double> double_Key = Key.of(Double.class);
   private static final Key<Double> double_radii3_Key = Key.of(Double.class, (new Object() {
      Annotation a(@Radii(3) Object ah) {
         try {
            return this.getClass().getDeclaredMethod("a", Object.class).getParameterAnnotations()[0][0];
         } catch (NoSuchMethodException var3) {
            throw new RuntimeException(var3);
         }
      }
   }).a(null));
   private static final Key<TreeType> treeType_Key = Key.of(TreeType.class);
   private static final Key<ConfiguredFeatureType> configuredFeatureType_Key = Key.of(ConfiguredFeatureType.class);
   private static final Key<StructureType> structureType_Key = Key.of(StructureType.class);
   private static final Key<Integer> integer_confirmcom_sk89q_worldedit_command_util_annotation_Confirm_Processor_RADIUS_Key = Key.of(
      Integer.class, (new Object() {
         Annotation a(@Confirm(Confirm.Processor.RADIUS) Object ah) {
            try {
               return this.getClass().getDeclaredMethod("a", Object.class).getParameterAnnotations()[0][0];
            } catch (NoSuchMethodException var3) {
               throw new RuntimeException(var3);
            }
         }
      }).a(null)
   );
   private static final Key<String> string_Key = Key.of(String.class);
   private static final Key<BiomeType> biomeType_Key = Key.of(BiomeType.class);
   private static final Key<Mask> mask_Key = Key.of(Mask.class);
   private static final Key<Boolean> boolean_Key = Key.of(Boolean.class);
   private static final Key<BlockVector2> blockVector2_Key = Key.of(BlockVector2.class);
   private static final Key<Vector3> vector3_Key = Key.of(Vector3.class);
   private static final Key<Actor> actor_Key = Key.of(Actor.class);
   private static final Key<LocalSession> localSession_Key = Key.of(LocalSession.class);
   private static final Key<EditSession> editSession_Key = Key.of(EditSession.class);
   private static final Key<Region> region_selection_Key = Key.of(Region.class, Selection.class);
   private CommandManager commandManager;
   private GenerationCommands containerInstance;
   private CommandPermissionsConditionGenerator commandPermissionsConditionGenerator;
   private ImmutableList<CommandCallListener> listeners;
   private final CommandArgument patternPart = CommandParts.arg(TranslatableComponent.of("pattern"), TextComponent.of("The pattern of blocks to generate"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(pattern_Key))
      .build();
   private final CommandArgument radiiPart = CommandParts.arg(
         TranslatableComponent.of("radii"), TextComponent.of("The radii of the cylinder. 1st is N/S, 2nd is E/W")
      )
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(double_radii2_Key))
      .build();
   private final CommandArgument heightPart = CommandParts.arg(TranslatableComponent.of("height"), TextComponent.of("The height of the cylinder"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument thicknessPart = CommandParts.arg(
         TranslatableComponent.of("thickness"), TextComponent.of("Thickness of the cyclinder. 0 creates a normal //hcyl.")
      )
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final NoArgCommandFlag hollowPart = CommandParts.flag('h', TextComponent.of("Make a hollow cylinder")).build();
   private final CommandArgument radiiPart2 = CommandParts.arg(
         TranslatableComponent.of("radii"), TextComponent.of("The radii of the cone. 1st is N/S, 2nd is E/W")
      )
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(double_radii2_Key))
      .build();
   private final CommandArgument heightPart2 = CommandParts.arg(TranslatableComponent.of("height"), TextComponent.of("The height of the cone"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final NoArgCommandFlag hollowPart2 = CommandParts.flag('h', TextComponent.of("Make a hollow cone")).build();
   private final CommandArgument thicknessPart2 = CommandParts.arg(TranslatableComponent.of("thickness"), TextComponent.of("Thickness of the hollow cone"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final CommandArgument radiiPart3 = CommandParts.arg(
         TranslatableComponent.of("radii"), TextComponent.of("The radii of the sphere. Order is N/S, U/D, E/W")
      )
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(double_radii3_Key))
      .build();
   private final NoArgCommandFlag raisedPart = CommandParts.flag('r', TextComponent.of("Raise the bottom of the sphere to the placement position")).build();
   private final NoArgCommandFlag hollowPart3 = CommandParts.flag('h', TextComponent.of("Make a hollow sphere")).build();
   private final CommandArgument sizePart = CommandParts.arg(TranslatableComponent.of("size"), TextComponent.of("The size of the forest, in blocks"))
      .defaultsTo(ImmutableList.of("10"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument typePart = CommandParts.arg(TranslatableComponent.of("type"), TextComponent.of("The type of forest"))
      .defaultsTo(ImmutableList.of("tree"))
      .ofTypes(ImmutableList.of(treeType_Key))
      .build();
   private final CommandArgument densityPart = CommandParts.arg(
         TranslatableComponent.of("density"), TextComponent.of("The density of the forest, between 0 and 100")
      )
      .defaultsTo(ImmutableList.of("5"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final CommandArgument sizePart2 = CommandParts.arg(TranslatableComponent.of("size"), TextComponent.of("The size of the patch"))
      .defaultsTo(ImmutableList.of("10"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument densityPart2 = CommandParts.arg(TranslatableComponent.of("density"), TextComponent.of("//TODO "))
      .defaultsTo(ImmutableList.of("0.02"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final CommandArgument featurePart = CommandParts.arg(TranslatableComponent.of("feature"), TextComponent.of("Type of feature to place"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(configuredFeatureType_Key))
      .build();
   private final CommandArgument featurePart2 = CommandParts.arg(TranslatableComponent.of("feature"), TextComponent.of("The structure"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(structureType_Key))
      .build();
   private final CommandArgument patternPart2 = CommandParts.arg(TranslatableComponent.of("pattern"), TextComponent.of("The pattern of blocks to set"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(pattern_Key))
      .build();
   private final CommandArgument sizePart3 = CommandParts.arg(TranslatableComponent.of("size"), TextComponent.of("The size of the pyramid"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(integer_confirmcom_sk89q_worldedit_command_util_annotation_Confirm_Processor_RADIUS_Key))
      .build();
   private final NoArgCommandFlag hollowPart4 = CommandParts.flag('h', TextComponent.of("Make a hollow pyramid")).build();
   private final CommandArgument expressionPart = CommandParts.arg(
         TranslatableComponent.of("expression"), TextComponent.of("Expression to test block placement locations and set block type")
      )
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(string_Key))
      .variable(true)
      .build();
   private final NoArgCommandFlag hollowPart5 = CommandParts.flag('h', TextComponent.of("Generate a hollow shape")).build();
   private final NoArgCommandFlag useRawCoordsPart = CommandParts.flag('r', TextComponent.of("Use the game's coordinate origin")).build();
   private final NoArgCommandFlag offsetPart = CommandParts.flag('o', TextComponent.of("Use the placement's coordinate origin")).build();
   private final NoArgCommandFlag offsetCenterPart = CommandParts.flag('c', TextComponent.of("Use the selection's center as origin")).build();
   private final CommandArgument targetPart = CommandParts.arg(TranslatableComponent.of("target"), TextComponent.of("The biome type to set"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(biomeType_Key))
      .build();
   private final CommandArgument expressionPart2 = CommandParts.arg(
         TranslatableComponent.of("expression"),
         TextComponent.of("Expression to test block placement locations and set biome type. Requires x,y,z variables be present")
      )
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(string_Key))
      .variable(true)
      .build();
   private final CommandArgument sizeOptPart = CommandParts.arg(TranslatableComponent.of("size"), TextComponent.of("TODO"))
      .defaultsTo(ImmutableList.of("8"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument frequencyOptPart = CommandParts.arg(TranslatableComponent.of("frequency"), TextComponent.of("TODO"))
      .defaultsTo(ImmutableList.of("40"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument rarityOptPart = CommandParts.arg(TranslatableComponent.of("rarity"), TextComponent.of("TODO"))
      .defaultsTo(ImmutableList.of("7"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument minYOptPart = CommandParts.arg(TranslatableComponent.of("minY"), TextComponent.of("TODO"))
      .defaultsTo(ImmutableList.of("8"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument maxYOptPart = CommandParts.arg(TranslatableComponent.of("maxY"), TextComponent.of("TODO"))
      .defaultsTo(ImmutableList.of("127"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument systemFrequencyOptPart = CommandParts.arg(TranslatableComponent.of("systemFrequency"), TextComponent.of("TODO"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument individualRarityOptPart = CommandParts.arg(TranslatableComponent.of("individualRarity"), TextComponent.of("TODO"))
      .defaultsTo(ImmutableList.of("25"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument pocketChanceOptPart = CommandParts.arg(TranslatableComponent.of("pocketChance"), TextComponent.of("TODO"))
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument pocketMinOptPart = CommandParts.arg(TranslatableComponent.of("pocketMin"), TextComponent.of("TODO"))
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument pocketMaxOptPart = CommandParts.arg(TranslatableComponent.of("pocketMax"), TextComponent.of("TODO"))
      .defaultsTo(ImmutableList.of("3"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument maskPart = CommandParts.arg(TranslatableComponent.of("mask"), TextComponent.of("Mask"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(mask_Key))
      .build();
   private final NoArgCommandFlag deepslateBelowZeroPart = CommandParts.flag('b', TextComponent.of("Make all ores deepslate equivalent for y<0")).build();
   private final NoArgCommandFlag deepslateWhereDeepslatePart = CommandParts.flag(
         'd', TextComponent.of("Make all ores deepslate equivalent when placed into deepslate, tuff, etc.")
      )
      .build();
   private final CommandArgument imageURLPart = CommandParts.arg(TranslatableComponent.of("imageURL"), TextComponent.of("Image URL (imgur only)"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(string_Key))
      .build();
   private final CommandArgument randomizePart = CommandParts.arg(TranslatableComponent.of("randomize"), TextComponent.of("boolean"))
      .defaultsTo(ImmutableList.of("true"))
      .ofTypes(ImmutableList.of(boolean_Key))
      .build();
   private final CommandArgument thresholdPart = CommandParts.arg(TranslatableComponent.of("threshold"), TextComponent.of("TODO"))
      .defaultsTo(ImmutableList.of("100"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument dimensionsPart = CommandParts.arg(TranslatableComponent.of("dimensions"), TextComponent.of("BlockVector2"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(blockVector2_Key))
      .build();
   private final CommandArgument materialPart = CommandParts.arg(TranslatableComponent.of("material"), TextComponent.of("Pattern"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(pattern_Key))
      .build();
   private final CommandArgument sizePart4 = CommandParts.arg(TranslatableComponent.of("size"), TextComponent.of("Ore vein size"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument freqPart = CommandParts.arg(
         TranslatableComponent.of("freq"), TextComponent.of("Ore vein frequency (number of times to attempt to place ore)")
      )
      .defaultsTo(ImmutableList.of("10"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument rarityPart = CommandParts.arg(
         TranslatableComponent.of("rarity"), TextComponent.of("Ore vein rarity (% chance each attempt is placed)")
      )
      .defaultsTo(ImmutableList.of("100"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument minYPart = CommandParts.arg(TranslatableComponent.of("minY"), TextComponent.of("Ore vein min y"))
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument maxYPart = CommandParts.arg(TranslatableComponent.of("maxY"), TextComponent.of("Ore vein max y"))
      .defaultsTo(ImmutableList.of("63"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument patternPart3 = CommandParts.arg(TranslatableComponent.of("pattern"), TextComponent.of("Pattern"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(pattern_Key))
      .build();
   private final CommandArgument sizePart5 = CommandParts.arg(TranslatableComponent.of("size"), TextComponent.of("size"))
      .defaultsTo(ImmutableList.of("5"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final CommandArgument radiusPart = CommandParts.arg(TranslatableComponent.of("radius"), TextComponent.of("radius"))
      .defaultsTo(ImmutableList.of("5"))
      .ofTypes(ImmutableList.of(vector3_Key))
      .build();
   private final CommandArgument sphericityPart = CommandParts.arg(TranslatableComponent.of("roundness"), TextComponent.of("roundness"))
      .defaultsTo(ImmutableList.of("100"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final CommandArgument frequencyPart = CommandParts.arg(TranslatableComponent.of("frequency"), TextComponent.of("double"))
      .defaultsTo(ImmutableList.of("30"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final CommandArgument amplitudePart = CommandParts.arg(TranslatableComponent.of("amplitude"), TextComponent.of("double"))
      .defaultsTo(ImmutableList.of("50"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();

   private GenerationCommandsRegistration() {
      this.listeners = ImmutableList.of();
   }

   public static GenerationCommandsRegistration builder() {
      return new GenerationCommandsRegistration();
   }

   public GenerationCommandsRegistration commandManager(CommandManager commandManager) {
      this.commandManager = commandManager;
      return this;
   }

   public GenerationCommandsRegistration containerInstance(GenerationCommands containerInstance) {
      this.containerInstance = containerInstance;
      return this;
   }

   public GenerationCommandsRegistration commandPermissionsConditionGenerator(CommandPermissionsConditionGenerator commandPermissionsConditionGenerator) {
      this.commandPermissionsConditionGenerator = commandPermissionsConditionGenerator;
      return this;
   }

   public GenerationCommandsRegistration listeners(Collection<CommandCallListener> listeners) {
      this.listeners = ImmutableList.copyOf(listeners);
      return this;
   }

   @Override
   public void build() {
      this.commandManager
         .register(
            "/hcyl",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Generates a hollow cylinder."));
               b.parts(ImmutableList.of(this.patternPart, this.radiiPart, this.heightPart, this.thicknessPart));
               b.action(this::cmd$_hcyl);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GenerationCommands.class, "hcyl", Actor.class, LocalSession.class, EditSession.class, Pattern.class, List.class, int.class, double.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/cyl",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Generates a cylinder."));
               b.parts(ImmutableList.of(this.patternPart, this.radiiPart, this.heightPart, this.hollowPart));
               b.action(this::cmd$_cyl);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GenerationCommands.class, "cyl", Actor.class, LocalSession.class, EditSession.class, Pattern.class, List.class, int.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/cone",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Generates a cone."));
               b.parts(ImmutableList.of(this.patternPart, this.radiiPart2, this.heightPart2, this.hollowPart2, this.thicknessPart2));
               b.action(this::cmd$_cone);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GenerationCommands.class,
                  "cone",
                  Actor.class,
                  LocalSession.class,
                  EditSession.class,
                  Pattern.class,
                  List.class,
                  int.class,
                  boolean.class,
                  double.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/hsphere",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Generates a hollow sphere."));
               b.parts(ImmutableList.of(this.patternPart, this.radiiPart3, this.raisedPart));
               b.action(this::cmd$_hsphere);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GenerationCommands.class, "hsphere", Actor.class, LocalSession.class, EditSession.class, Pattern.class, List.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/sphere",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Generates a filled sphere."));
               b.parts(ImmutableList.of(this.patternPart, this.radiiPart3, this.raisedPart, this.hollowPart3));
               b.action(this::cmd$_sphere);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GenerationCommands.class,
                  "sphere",
                  Actor.class,
                  LocalSession.class,
                  EditSession.class,
                  Pattern.class,
                  List.class,
                  boolean.class,
                  boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "forestgen",
            b -> {
               b.aliases(ImmutableList.of("/forestgen"));
               b.description(TextComponent.of("Generate a forest"));
               b.parts(ImmutableList.of(this.sizePart, this.typePart, this.densityPart));
               b.action(this::cmd$forestgen);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GenerationCommands.class, "forestGen", Actor.class, LocalSession.class, EditSession.class, int.class, TreeType.class, double.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "pumpkins",
            b -> {
               b.aliases(ImmutableList.of("/pumpkins"));
               b.description(TextComponent.of("Generate pumpkin patches"));
               b.parts(ImmutableList.of(this.sizePart2, this.densityPart2));
               b.action(this::cmd$pumpkins);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GenerationCommands.class, "pumpkins", Actor.class, LocalSession.class, EditSession.class, int.class, double.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/feature",
            b -> {
               b.aliases(ImmutableList.of("/placefeature"));
               b.description(TextComponent.of("Generate Minecraft features"));
               b.parts(ImmutableList.of(this.featurePart));
               b.action(this::cmd$_feature);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GenerationCommands.class, "feature", Actor.class, LocalSession.class, EditSession.class, ConfiguredFeatureType.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/structure",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Generate Minecraft structures"));
               b.parts(ImmutableList.of(this.featurePart2));
               b.action(this::cmd$_structure);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GenerationCommands.class, "structure", Actor.class, LocalSession.class, EditSession.class, StructureType.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/hpyramid",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Generate a hollow pyramid"));
               b.parts(ImmutableList.of(this.patternPart2, this.sizePart3));
               b.action(this::cmd$_hpyramid);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GenerationCommands.class, "hollowPyramid", Actor.class, LocalSession.class, EditSession.class, Pattern.class, int.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/pyramid",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Generate a filled pyramid"));
               b.parts(ImmutableList.of(this.patternPart2, this.sizePart3, this.hollowPart4));
               b.action(this::cmd$_pyramid);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GenerationCommands.class, "pyramid", Actor.class, LocalSession.class, EditSession.class, Pattern.class, int.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/generate",
            b -> {
               b.aliases(ImmutableList.of("/gen", "/g"));
               b.description(TextComponent.of("Generates a shape according to a formula."));
               b.footer(TextComponent.of("For details, see https://ehub.to/we/expr"));
               b.parts(
                  ImmutableList.of(this.patternPart2, this.expressionPart, this.hollowPart5, this.useRawCoordsPart, this.offsetPart, this.offsetCenterPart)
               );
               b.action(this::cmd$_generate);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GenerationCommands.class,
                  "generate",
                  Actor.class,
                  LocalSession.class,
                  EditSession.class,
                  Region.class,
                  Pattern.class,
                  List.class,
                  boolean.class,
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
            "/generatebiome",
            b -> {
               b.aliases(ImmutableList.of("/genbiome", "/gb"));
               b.description(TextComponent.of("Sets biome according to a formula."));
               b.footer(
                  TextComponent.of(
                     "Formula must return positive numbers (true) if the point is inside the shape\nSets the biome of blocks in that shape.\nFor details, see https://ehub.to/we/expr"
                  )
               );
               b.parts(ImmutableList.of(this.targetPart, this.expressionPart2, this.hollowPart5, this.useRawCoordsPart, this.offsetPart, this.offsetCenterPart));
               b.action(this::cmd$_generatebiome);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GenerationCommands.class,
                  "generateBiome",
                  Actor.class,
                  LocalSession.class,
                  EditSession.class,
                  Region.class,
                  BiomeType.class,
                  List.class,
                  boolean.class,
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
            "/caves",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Generates a cave network"));
               b.parts(
                  ImmutableList.of(
                     this.sizeOptPart,
                     this.frequencyOptPart,
                     this.rarityOptPart,
                     this.minYOptPart,
                     this.maxYOptPart,
                     this.systemFrequencyOptPart,
                     this.individualRarityOptPart,
                     this.pocketChanceOptPart,
                     this.pocketMinOptPart,
                     this.pocketMaxOptPart
                  )
               );
               b.action(this::cmd$_caves);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GenerationCommands.class,
                  "caves",
                  Actor.class,
                  LocalSession.class,
                  EditSession.class,
                  Region.class,
                  int.class,
                  int.class,
                  int.class,
                  int.class,
                  int.class,
                  int.class,
                  int.class,
                  int.class,
                  int.class,
                  int.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/ores",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Generates ores"));
               b.parts(ImmutableList.of(this.maskPart, this.deepslateBelowZeroPart, this.deepslateWhereDeepslatePart));
               b.action(this::cmd$_ores);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GenerationCommands.class, "ores", Actor.class, LocalSession.class, EditSession.class, Region.class, Mask.class, boolean.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/img",
            b -> {
               b.aliases(ImmutableList.of("/image", "image"));
               b.description(TextComponent.of("Generate an image"));
               b.parts(ImmutableList.of(this.imageURLPart, this.randomizePart, this.thresholdPart, this.dimensionsPart));
               b.action(this::cmd$_img);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GenerationCommands.class,
                  "image",
                  Actor.class,
                  LocalSession.class,
                  EditSession.class,
                  String.class,
                  boolean.class,
                  int.class,
                  BlockVector2.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/ore",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Generates ores"));
               b.parts(ImmutableList.of(this.maskPart, this.materialPart, this.sizePart4, this.freqPart, this.rarityPart, this.minYPart, this.maxYPart));
               b.action(this::cmd$_ore);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GenerationCommands.class,
                  "ore",
                  Actor.class,
                  LocalSession.class,
                  EditSession.class,
                  Region.class,
                  Mask.class,
                  Pattern.class,
                  int.class,
                  int.class,
                  int.class,
                  int.class,
                  int.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "/blob",
            b -> {
               b.aliases(ImmutableList.of("/rock"));
               b.description(TextComponent.of("Creates a distorted sphere"));
               b.parts(ImmutableList.of(this.patternPart3, this.sizePart5, this.radiusPart, this.sphericityPart, this.frequencyPart, this.amplitudePart));
               b.action(this::cmd$_blob);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  GenerationCommands.class,
                  "blob",
                  Actor.class,
                  LocalSession.class,
                  EditSession.class,
                  Pattern.class,
                  double.class,
                  Vector3.class,
                  double.class,
                  double.class,
                  double.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
   }

   private int cmd$_hcyl(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GenerationCommands.class, "hcyl", Actor.class, LocalSession.class, EditSession.class, Pattern.class, List.class, int.class, double.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .hcyl(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$pattern(parameters),
               this.extract$radii(parameters),
               this.extract$height(parameters),
               this.extract$thickness(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_cyl(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GenerationCommands.class, "cyl", Actor.class, LocalSession.class, EditSession.class, Pattern.class, List.class, int.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .cyl(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$pattern(parameters),
               this.extract$radii(parameters),
               this.extract$height(parameters),
               this.extract$hollow(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_cone(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GenerationCommands.class,
         "cone",
         Actor.class,
         LocalSession.class,
         EditSession.class,
         Pattern.class,
         List.class,
         int.class,
         boolean.class,
         double.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .cone(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$pattern(parameters),
               this.extract$radii2(parameters),
               this.extract$height2(parameters),
               this.extract$hollow2(parameters),
               this.extract$thickness2(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_hsphere(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GenerationCommands.class, "hsphere", Actor.class, LocalSession.class, EditSession.class, Pattern.class, List.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .hsphere(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$pattern(parameters),
               this.extract$radii3(parameters),
               this.extract$raised(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_sphere(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GenerationCommands.class, "sphere", Actor.class, LocalSession.class, EditSession.class, Pattern.class, List.class, boolean.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .sphere(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$pattern(parameters),
               this.extract$radii3(parameters),
               this.extract$raised(parameters),
               this.extract$hollow3(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$forestgen(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GenerationCommands.class, "forestGen", Actor.class, LocalSession.class, EditSession.class, int.class, TreeType.class, double.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .forestGen(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$size(parameters),
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

   private int cmd$pumpkins(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GenerationCommands.class, "pumpkins", Actor.class, LocalSession.class, EditSession.class, int.class, double.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .pumpkins(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$size2(parameters),
               this.extract$density2(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_feature(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GenerationCommands.class, "feature", Actor.class, LocalSession.class, EditSession.class, ConfiguredFeatureType.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .feature(this.extract$actor(parameters), this.extract$session(parameters), this.extract$editSession(parameters), this.extract$feature(parameters));
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_structure(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GenerationCommands.class, "structure", Actor.class, LocalSession.class, EditSession.class, StructureType.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .structure(
               this.extract$actor(parameters), this.extract$session(parameters), this.extract$editSession(parameters), this.extract$feature2(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_hpyramid(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GenerationCommands.class, "hollowPyramid", Actor.class, LocalSession.class, EditSession.class, Pattern.class, int.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .hollowPyramid(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$pattern2(parameters),
               this.extract$size3(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_pyramid(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GenerationCommands.class, "pyramid", Actor.class, LocalSession.class, EditSession.class, Pattern.class, int.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .pyramid(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$pattern2(parameters),
               this.extract$size3(parameters),
               this.extract$hollow4(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_generate(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GenerationCommands.class,
         "generate",
         Actor.class,
         LocalSession.class,
         EditSession.class,
         Region.class,
         Pattern.class,
         List.class,
         boolean.class,
         boolean.class,
         boolean.class,
         boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .generate(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$region(parameters),
               this.extract$pattern2(parameters),
               this.extract$expression(parameters),
               this.extract$hollow5(parameters),
               this.extract$useRawCoords(parameters),
               this.extract$offset(parameters),
               this.extract$offsetCenter(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_generatebiome(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GenerationCommands.class,
         "generateBiome",
         Actor.class,
         LocalSession.class,
         EditSession.class,
         Region.class,
         BiomeType.class,
         List.class,
         boolean.class,
         boolean.class,
         boolean.class,
         boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .generateBiome(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$region(parameters),
               this.extract$target(parameters),
               this.extract$expression2(parameters),
               this.extract$hollow5(parameters),
               this.extract$useRawCoords(parameters),
               this.extract$offset(parameters),
               this.extract$offsetCenter(parameters)
            );
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_caves(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GenerationCommands.class,
         "caves",
         Actor.class,
         LocalSession.class,
         EditSession.class,
         Region.class,
         int.class,
         int.class,
         int.class,
         int.class,
         int.class,
         int.class,
         int.class,
         int.class,
         int.class,
         int.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .caves(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$region(parameters),
               this.extract$sizeOpt(parameters),
               this.extract$frequencyOpt(parameters),
               this.extract$rarityOpt(parameters),
               this.extract$minYOpt(parameters),
               this.extract$maxYOpt(parameters),
               this.extract$systemFrequencyOpt(parameters),
               this.extract$individualRarityOpt(parameters),
               this.extract$pocketChanceOpt(parameters),
               this.extract$pocketMinOpt(parameters),
               this.extract$pocketMaxOpt(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_ores(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GenerationCommands.class, "ores", Actor.class, LocalSession.class, EditSession.class, Region.class, Mask.class, boolean.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .ores(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$region(parameters),
               this.extract$mask(parameters),
               this.extract$deepslateBelowZero(parameters),
               this.extract$deepslateWhereDeepslate(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_img(CommandParameters parameters) throws WorldEditException, IOException, URISyntaxException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GenerationCommands.class, "image", Actor.class, LocalSession.class, EditSession.class, String.class, boolean.class, int.class, BlockVector2.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .image(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$imageURL(parameters),
               this.extract$randomize(parameters),
               this.extract$threshold(parameters),
               this.extract$dimensions(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_ore(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GenerationCommands.class,
         "ore",
         Actor.class,
         LocalSession.class,
         EditSession.class,
         Region.class,
         Mask.class,
         Pattern.class,
         int.class,
         int.class,
         int.class,
         int.class,
         int.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .ore(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$region(parameters),
               this.extract$mask(parameters),
               this.extract$material(parameters),
               this.extract$size4(parameters),
               this.extract$freq(parameters),
               this.extract$rarity(parameters),
               this.extract$minY(parameters),
               this.extract$maxY(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_blob(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         GenerationCommands.class,
         "blob",
         Actor.class,
         LocalSession.class,
         EditSession.class,
         Pattern.class,
         double.class,
         Vector3.class,
         double.class,
         double.class,
         double.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         int result = this.containerInstance
            .blob(
               this.extract$actor(parameters),
               this.extract$session(parameters),
               this.extract$editSession(parameters),
               this.extract$pattern3(parameters),
               this.extract$size5(parameters),
               this.extract$radius(parameters),
               this.extract$sphericity(parameters),
               this.extract$frequency(parameters),
               this.extract$amplitude(parameters)
            );
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

   private EditSession extract$editSession(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(editSession_Key, "editSession", parameters.injectedValue(editSession_Key));
   }

   private Pattern extract$pattern(CommandParameters parameters) {
      return this.patternPart.value(parameters).asSingle(pattern_Key);
   }

   private List<Double> extract$radii(CommandParameters parameters) {
      return this.radiiPart.value(parameters).<Double>asMultiple(double_radii2_Key);
   }

   private int extract$height(CommandParameters parameters) {
      return this.heightPart.value(parameters).asSingle(integer_Key);
   }

   private double extract$thickness(CommandParameters parameters) {
      return this.thicknessPart.value(parameters).asSingle(double_Key);
   }

   private boolean extract$hollow(CommandParameters parameters) {
      return this.hollowPart.in(parameters);
   }

   private List<Double> extract$radii2(CommandParameters parameters) {
      return this.radiiPart2.value(parameters).<Double>asMultiple(double_radii2_Key);
   }

   private int extract$height2(CommandParameters parameters) {
      return this.heightPart2.value(parameters).asSingle(integer_Key);
   }

   private boolean extract$hollow2(CommandParameters parameters) {
      return this.hollowPart2.in(parameters);
   }

   private double extract$thickness2(CommandParameters parameters) {
      return this.thicknessPart2.value(parameters).asSingle(double_Key);
   }

   private List<Double> extract$radii3(CommandParameters parameters) {
      return this.radiiPart3.value(parameters).<Double>asMultiple(double_radii3_Key);
   }

   private boolean extract$raised(CommandParameters parameters) {
      return this.raisedPart.in(parameters);
   }

   private boolean extract$hollow3(CommandParameters parameters) {
      return this.hollowPart3.in(parameters);
   }

   private int extract$size(CommandParameters parameters) {
      return this.sizePart.value(parameters).asSingle(integer_Key);
   }

   private TreeType extract$type(CommandParameters parameters) {
      return this.typePart.value(parameters).asSingle(treeType_Key);
   }

   private double extract$density(CommandParameters parameters) {
      return this.densityPart.value(parameters).asSingle(double_Key);
   }

   private int extract$size2(CommandParameters parameters) {
      return this.sizePart2.value(parameters).asSingle(integer_Key);
   }

   private double extract$density2(CommandParameters parameters) {
      return this.densityPart2.value(parameters).asSingle(double_Key);
   }

   private ConfiguredFeatureType extract$feature(CommandParameters parameters) {
      return this.featurePart.value(parameters).asSingle(configuredFeatureType_Key);
   }

   private StructureType extract$feature2(CommandParameters parameters) {
      return this.featurePart2.value(parameters).asSingle(structureType_Key);
   }

   private Pattern extract$pattern2(CommandParameters parameters) {
      return this.patternPart2.value(parameters).asSingle(pattern_Key);
   }

   private int extract$size3(CommandParameters parameters) {
      return this.sizePart3.value(parameters).asSingle(integer_confirmcom_sk89q_worldedit_command_util_annotation_Confirm_Processor_RADIUS_Key);
   }

   private boolean extract$hollow4(CommandParameters parameters) {
      return this.hollowPart4.in(parameters);
   }

   private Region extract$region(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(region_selection_Key, "region", parameters.injectedValue(region_selection_Key));
   }

   private List<String> extract$expression(CommandParameters parameters) {
      return this.expressionPart.value(parameters).<String>asMultiple(string_Key);
   }

   private boolean extract$hollow5(CommandParameters parameters) {
      return this.hollowPart5.in(parameters);
   }

   private boolean extract$useRawCoords(CommandParameters parameters) {
      return this.useRawCoordsPart.in(parameters);
   }

   private boolean extract$offset(CommandParameters parameters) {
      return this.offsetPart.in(parameters);
   }

   private boolean extract$offsetCenter(CommandParameters parameters) {
      return this.offsetCenterPart.in(parameters);
   }

   private BiomeType extract$target(CommandParameters parameters) {
      return this.targetPart.value(parameters).asSingle(biomeType_Key);
   }

   private List<String> extract$expression2(CommandParameters parameters) {
      return this.expressionPart2.value(parameters).<String>asMultiple(string_Key);
   }

   private int extract$sizeOpt(CommandParameters parameters) {
      return this.sizeOptPart.value(parameters).asSingle(integer_Key);
   }

   private int extract$frequencyOpt(CommandParameters parameters) {
      return this.frequencyOptPart.value(parameters).asSingle(integer_Key);
   }

   private int extract$rarityOpt(CommandParameters parameters) {
      return this.rarityOptPart.value(parameters).asSingle(integer_Key);
   }

   private int extract$minYOpt(CommandParameters parameters) {
      return this.minYOptPart.value(parameters).asSingle(integer_Key);
   }

   private int extract$maxYOpt(CommandParameters parameters) {
      return this.maxYOptPart.value(parameters).asSingle(integer_Key);
   }

   private int extract$systemFrequencyOpt(CommandParameters parameters) {
      return this.systemFrequencyOptPart.value(parameters).asSingle(integer_Key);
   }

   private int extract$individualRarityOpt(CommandParameters parameters) {
      return this.individualRarityOptPart.value(parameters).asSingle(integer_Key);
   }

   private int extract$pocketChanceOpt(CommandParameters parameters) {
      return this.pocketChanceOptPart.value(parameters).asSingle(integer_Key);
   }

   private int extract$pocketMinOpt(CommandParameters parameters) {
      return this.pocketMinOptPart.value(parameters).asSingle(integer_Key);
   }

   private int extract$pocketMaxOpt(CommandParameters parameters) {
      return this.pocketMaxOptPart.value(parameters).asSingle(integer_Key);
   }

   private Mask extract$mask(CommandParameters parameters) {
      return this.maskPart.value(parameters).asSingle(mask_Key);
   }

   private boolean extract$deepslateBelowZero(CommandParameters parameters) {
      return this.deepslateBelowZeroPart.in(parameters);
   }

   private boolean extract$deepslateWhereDeepslate(CommandParameters parameters) {
      return this.deepslateWhereDeepslatePart.in(parameters);
   }

   private String extract$imageURL(CommandParameters parameters) {
      return this.imageURLPart.value(parameters).asSingle(string_Key);
   }

   private boolean extract$randomize(CommandParameters parameters) {
      return this.randomizePart.value(parameters).asSingle(boolean_Key);
   }

   private int extract$threshold(CommandParameters parameters) {
      return this.thresholdPart.value(parameters).asSingle(integer_Key);
   }

   private BlockVector2 extract$dimensions(CommandParameters parameters) {
      return this.dimensionsPart.value(parameters).asSingle(blockVector2_Key);
   }

   private Pattern extract$material(CommandParameters parameters) {
      return this.materialPart.value(parameters).asSingle(pattern_Key);
   }

   private int extract$size4(CommandParameters parameters) {
      return this.sizePart4.value(parameters).asSingle(integer_Key);
   }

   private int extract$freq(CommandParameters parameters) {
      return this.freqPart.value(parameters).asSingle(integer_Key);
   }

   private int extract$rarity(CommandParameters parameters) {
      return this.rarityPart.value(parameters).asSingle(integer_Key);
   }

   private int extract$minY(CommandParameters parameters) {
      return this.minYPart.value(parameters).asSingle(integer_Key);
   }

   private int extract$maxY(CommandParameters parameters) {
      return this.maxYPart.value(parameters).asSingle(integer_Key);
   }

   private Pattern extract$pattern3(CommandParameters parameters) {
      return this.patternPart3.value(parameters).asSingle(pattern_Key);
   }

   private double extract$size5(CommandParameters parameters) {
      return this.sizePart5.value(parameters).asSingle(double_Key);
   }

   private Vector3 extract$radius(CommandParameters parameters) {
      return this.radiusPart.value(parameters).asSingle(vector3_Key);
   }

   private double extract$sphericity(CommandParameters parameters) {
      return this.sphericityPart.value(parameters).asSingle(double_Key);
   }

   private double extract$frequency(CommandParameters parameters) {
      return this.frequencyPart.value(parameters).asSingle(double_Key);
   }

   private double extract$amplitude(CommandParameters parameters) {
      return this.amplitudePart.value(parameters).asSingle(double_Key);
   }
}
