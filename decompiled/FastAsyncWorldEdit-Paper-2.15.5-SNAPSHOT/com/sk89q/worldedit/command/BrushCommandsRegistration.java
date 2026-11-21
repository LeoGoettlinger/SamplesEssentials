package com.sk89q.worldedit.command;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.annotation.PatternList;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.annotation.ClipboardMask;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.factory.RegionFactory;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;
import com.sk89q.worldedit.world.generation.StructureType;
import com.sk89q.worldedit.world.generation.TreeType;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.gen.CommandCallListener;
import org.enginehub.piston.gen.CommandRegistration;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.internal.RegistrationUtil;
import org.enginehub.piston.part.ArgAcceptingCommandFlag;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandParts;
import org.enginehub.piston.part.NoArgCommandFlag;

public final class BrushCommandsRegistration implements CommandRegistration<BrushCommands>, CommandPermissionsConditionGenerator.Registration {
   private static final Key<Expression> expression_Key = Key.of(Expression.class);
   private static final Key<Integer> integer_Key = Key.of(Integer.class);
   private static final Key<Mask> mask_Key = Key.of(Mask.class);
   private static final Key<Pattern> pattern_Key = Key.of(Pattern.class);
   private static final Key<Boolean> boolean_Key = Key.of(Boolean.class);
   private static final Key<Double> double_Key = Key.of(Double.class);
   private static final Key<Vector3> vector3_Key = Key.of(Vector3.class);
   private static final Key<String> string_Key = Key.of(String.class);
   private static final Key<Pattern> pattern_patternList_Key = Key.of(Pattern.class, PatternList.class);
   private static final Key<RegionFactory> regionFactory_Key = Key.of(RegionFactory.class);
   private static final Key<TreeType> treeType_Key = Key.of(TreeType.class);
   private static final Key<Mask> mask_clipboardMask_Key = Key.of(Mask.class, ClipboardMask.class);
   private static final Key<BiomeType> biomeType_Key = Key.of(BiomeType.class);
   private static final Key<ConfiguredFeatureType> configuredFeatureType_Key = Key.of(ConfiguredFeatureType.class);
   private static final Key<StructureType> structureType_Key = Key.of(StructureType.class);
   private static final Key<InjectedValueAccess> injectedValueAccess_Key = Key.of(InjectedValueAccess.class);
   private static final Key<EditSession> editSession_Key = Key.of(EditSession.class);
   private static final Key<Player> player_Key = Key.of(Player.class);
   private static final Key<LocalSession> localSession_Key = Key.of(LocalSession.class);
   private static final Key<Actor> actor_Key = Key.of(Actor.class);
   private CommandManager commandManager;
   private BrushCommands containerInstance;
   private CommandPermissionsConditionGenerator commandPermissionsConditionGenerator;
   private ImmutableList<CommandCallListener> listeners;
   private final CommandArgument radiusPart = CommandParts.arg(TranslatableComponent.of("radius"), TextComponent.of("The radius to sample for blending"))
      .defaultsTo(ImmutableList.of("5"))
      .ofTypes(ImmutableList.of(expression_Key))
      .build();
   private final CommandArgument minFreqDiffPart = CommandParts.arg(
         TranslatableComponent.of("minFreqDiff"), TextComponent.of("Minimum difference in frequency to change block")
      )
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final NoArgCommandFlag onlyAirPart = CommandParts.flag('a', TextComponent.of("Compare only air vs existing blocks")).build();
   private final ArgAcceptingCommandFlag maskPart = CommandParts.flag('m', TextComponent.of("Mask to limit blocks being considered"))
      .withRequiredArg()
      .argNamed(TranslatableComponent.of("mask"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(mask_Key))
      .build();
   private final CommandArgument radiusPart2 = CommandParts.arg(TranslatableComponent.of("radius"), TextComponent.of("The radius for eroding"))
      .defaultsTo(ImmutableList.of("5"))
      .ofTypes(ImmutableList.of(expression_Key))
      .build();
   private final CommandArgument erodefacesPart = CommandParts.arg(TranslatableComponent.of("erodefaces"), TextComponent.of("erodeFaces"))
      .defaultsTo(ImmutableList.of("2"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument erodeRecPart = CommandParts.arg(TranslatableComponent.of("erodeRec"), TextComponent.of("erodeRec"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument fillFacesPart = CommandParts.arg(TranslatableComponent.of("fillFaces"), TextComponent.of("fillFaces"))
      .defaultsTo(ImmutableList.of("5"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument fillRecPart = CommandParts.arg(TranslatableComponent.of("fillRec"), TextComponent.of("fillRec"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument erodefacesPart2 = CommandParts.arg(TranslatableComponent.of("erodefaces"), TextComponent.of("erodeFaces"))
      .defaultsTo(ImmutableList.of("6"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument erodeRecPart2 = CommandParts.arg(TranslatableComponent.of("erodeRec"), TextComponent.of("erodeRec"))
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument fillFacesPart2 = CommandParts.arg(TranslatableComponent.of("fillFaces"), TextComponent.of("fillFaces"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument fillPart = CommandParts.arg(TranslatableComponent.of("fill"), TextComponent.of("Pattern"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(pattern_Key))
      .build();
   private final CommandArgument filledPart = CommandParts.arg(TranslatableComponent.of("filled"), TextComponent.of("Whether the circle should be filled"))
      .defaultsTo(ImmutableList.of("false"))
      .ofTypes(ImmutableList.of(boolean_Key))
      .build();
   private final NoArgCommandFlag depthFirstPart = CommandParts.flag('d', TextComponent.of("Apply in depth first order")).build();
   private final CommandArgument radiusPart3 = CommandParts.arg(TranslatableComponent.of("radius"), TextComponent.of("The radius to sample for blending"))
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(expression_Key))
      .build();
   private final NoArgCommandFlag shellPart = CommandParts.flag('h', TextComponent.of("Create only a shell")).build();
   private final NoArgCommandFlag selectPart = CommandParts.flag('s', TextComponent.of("Selects the clicked point after drawing")).build();
   private final NoArgCommandFlag flatPart = CommandParts.flag('f', TextComponent.of("Create a flat line")).build();
   private final CommandArgument radiusPart4 = CommandParts.arg(TranslatableComponent.of("radius"), TextComponent.of("The radius to sample for blending"))
      .defaultsTo(ImmutableList.of("25"))
      .ofTypes(ImmutableList.of(expression_Key))
      .build();
   private final CommandArgument copiesPart = CommandParts.arg(TranslatableComponent.of("copies"), TextComponent.of("int"))
      .defaultsTo(ImmutableList.of("-1"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument lengthFactorPart = CommandParts.arg(
         TranslatableComponent.of("lengthFactor"), TextComponent.of("Length of wire compared to distance between points")
      )
      .defaultsTo(ImmutableList.of("1.2"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final NoArgCommandFlag selectPart2 = CommandParts.flag('s', TextComponent.of("Select the clicked point after drawing")).build();
   private final NoArgCommandFlag facingDirectionPart = CommandParts.flag('d', TextComponent.of("sags the catenary toward the facing direction")).build();
   private final CommandArgument tensionPart = CommandParts.arg(TranslatableComponent.of("tension"), TextComponent.of("double"))
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final CommandArgument biasPart = CommandParts.arg(TranslatableComponent.of("bias"), TextComponent.of("double"))
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final CommandArgument continuityPart = CommandParts.arg(TranslatableComponent.of("continuity"), TextComponent.of("double"))
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final CommandArgument qualityPart = CommandParts.arg(TranslatableComponent.of("quality"), TextComponent.of("double"))
      .defaultsTo(ImmutableList.of("10"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final CommandArgument radiusPart5 = CommandParts.arg(TranslatableComponent.of("radius"), TextComponent.of("radii to multiply x,y,z by"))
      .defaultsTo(ImmutableList.of("10"))
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
   private final CommandArgument radiusPart6 = CommandParts.arg(TranslatableComponent.of("radius"), TextComponent.of("The radius to sample for blending"))
      .defaultsTo(ImmutableList.of("10"))
      .ofTypes(ImmutableList.of(expression_Key))
      .build();
   private final CommandArgument countPart = CommandParts.arg(TranslatableComponent.of("count"), TextComponent.of("Lines"))
      .defaultsTo(ImmutableList.of("10"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument radiusPart7 = CommandParts.arg(TranslatableComponent.of("radius"), TextComponent.of("Expression"))
      .defaultsTo(ImmutableList.of("5"))
      .ofTypes(ImmutableList.of(expression_Key))
      .build();
   private final CommandArgument imagePart = CommandParts.arg(TranslatableComponent.of("image"), TextComponent.of("String"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(string_Key))
      .build();
   private final CommandArgument rotationPart = CommandParts.arg(TranslatableComponent.of("rotation"), TextComponent.of("rotation"))
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument yscalePart = CommandParts.arg(TranslatableComponent.of("yscale"), TextComponent.of("double"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final NoArgCommandFlag onlyWhitePart = CommandParts.flag('w', TextComponent.of("Apply at maximum saturation")).build();
   private final NoArgCommandFlag randomRotatePart = CommandParts.flag('r', TextComponent.of("Apply random rotation")).build();
   private final CommandArgument imageURLPart = CommandParts.arg(TranslatableComponent.of("imageURL"), TextComponent.of("Image URL (imgur only)"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(string_Key))
      .build();
   private final CommandArgument radiusPart8 = CommandParts.arg(TranslatableComponent.of("radius"), TextComponent.of("The size of the brush"))
      .defaultsTo(ImmutableList.of("5"))
      .ofTypes(ImmutableList.of(expression_Key))
      .build();
   private final CommandArgument yscalePart2 = CommandParts.arg(TranslatableComponent.of("yscale"), TextComponent.of("scale height"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final NoArgCommandFlag alphaPart = CommandParts.flag('a', TextComponent.of("Use image Alpha")).build();
   private final NoArgCommandFlag fadeOutPart = CommandParts.flag('f', TextComponent.of("Blend the image with existing terrain")).build();
   private final CommandArgument radiusPart9 = CommandParts.arg(TranslatableComponent.of("radius"), TextComponent.of("radius"))
      .defaultsTo(ImmutableList.of("5"))
      .ofTypes(ImmutableList.of(expression_Key))
      .build();
   private final CommandArgument pointsPart = CommandParts.arg(TranslatableComponent.of("points"), TextComponent.of("points"))
      .defaultsTo(ImmutableList.of("5"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final CommandArgument distancePart = CommandParts.arg(TranslatableComponent.of("distance"), TextComponent.of("distance"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final NoArgCommandFlag overlayPart = CommandParts.flag('o', TextComponent.of("Overlay the block")).build();
   private final CommandArgument clipboardStrPart = CommandParts.arg(TranslatableComponent.of("clipboard"), TextComponent.of("Clipboard uri"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(string_Key))
      .build();
   private final CommandArgument maskPart2 = CommandParts.arg(
         TranslatableComponent.of("mask"), TextComponent.of("Mask of block to place on. Defaults to solid blocks.")
      )
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(mask_Key))
      .build();
   private final CommandArgument radiusPart10 = CommandParts.arg(TranslatableComponent.of("radius"), TextComponent.of("Expression"))
      .defaultsTo(ImmutableList.of("30"))
      .ofTypes(ImmutableList.of(expression_Key))
      .build();
   private final CommandArgument densityPart = CommandParts.arg(TranslatableComponent.of("density"), TextComponent.of("double"))
      .defaultsTo(ImmutableList.of("50"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final NoArgCommandFlag rotatePart = CommandParts.flag('r', TextComponent.of("Apply random rotation")).build();
   private final CommandArgument radiusPart11 = CommandParts.arg(TranslatableComponent.of("radius"), TextComponent.of("Expression"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(expression_Key))
      .build();
   private final CommandArgument patternLayersPart = CommandParts.arg(
         TranslatableComponent.of("patternLayers"), TextComponent.of("List of comma-separated patterns")
      )
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(pattern_patternList_Key))
      .build();
   private final CommandArgument pointsPart2 = CommandParts.arg(TranslatableComponent.of("points"), TextComponent.of("double"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final CommandArgument recursionPart = CommandParts.arg(TranslatableComponent.of("recursion"), TextComponent.of("double"))
      .defaultsTo(ImmutableList.of("5"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final CommandArgument solidPart = CommandParts.arg(TranslatableComponent.of("solid"), TextComponent.of("boolean"))
      .defaultsTo(ImmutableList.of("true"))
      .ofTypes(ImmutableList.of(boolean_Key))
      .build();
   private final CommandArgument radiusPart12 = CommandParts.arg(
         TranslatableComponent.of("radius"), TextComponent.of("The minimum distance between each point")
      )
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(expression_Key))
      .build();
   private final CommandArgument distancePart2 = CommandParts.arg(TranslatableComponent.of("distance"), TextComponent.of("double"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final CommandArgument commandStrPart = CommandParts.arg(TranslatableComponent.of("commandStr"), TextComponent.of("List of comma-separated commands"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(string_Key))
      .build();
   private final NoArgCommandFlag printPart = CommandParts.flag('p', TextComponent.of("Show any printed output")).build();
   private final NoArgCommandFlag randomRotatePart2 = CommandParts.flag('r', TextComponent.of("Random off-axis rotation")).build();
   private final NoArgCommandFlag layersPart = CommandParts.flag('l', TextComponent.of("Work on snow layers")).build();
   private final NoArgCommandFlag dontSmoothPart = CommandParts.flag('s', TextComponent.of("Disable smoothing")).build();
   private final NoArgCommandFlag randomRotatePart3 = CommandParts.flag('r', TextComponent.of("Enables random off-axis rotation")).build();
   private final NoArgCommandFlag layersPart2 = CommandParts.flag('l', TextComponent.of("Will work on snow layers")).build();
   private final NoArgCommandFlag dontSmoothPart2 = CommandParts.flag('s', TextComponent.of("Disables smoothing")).build();
   private final NoArgCommandFlag randomRotatePart4 = CommandParts.flag('r', TextComponent.of("Apply random rotation on paste")).build();
   private final NoArgCommandFlag autoRotatePart = CommandParts.flag('a', TextComponent.of("Apply auto view based rotation on paste")).build();
   private final CommandArgument inputPart = CommandParts.arg(TranslatableComponent.of("input"), TextComponent.of("Command to run"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(string_Key))
      .build();
   private final NoArgCommandFlag hidePart = CommandParts.flag('h', TextComponent.of("Hide any printed output")).build();
   private final CommandArgument namePart = CommandParts.arg(TranslatableComponent.of("name"), TextComponent.of("String name"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(string_Key))
      .build();
   private final NoArgCommandFlag rootPart = CommandParts.flag('g', TextComponent.of("Save the brush globally")).build();
   private final ArgAcceptingCommandFlag pagePart = CommandParts.flag('p', TextComponent.of("Prints the requested page"))
      .withRequiredArg()
      .argNamed(TranslatableComponent.of("page"))
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument shapePart = CommandParts.arg(TranslatableComponent.of("shape"), TextComponent.of("The shape of the region"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(regionFactory_Key))
      .build();
   private final CommandArgument radiusPart13 = CommandParts.arg(TranslatableComponent.of("radius"), TextComponent.of("The size of the brush"))
      .defaultsTo(ImmutableList.of("5"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final CommandArgument expressionPart = CommandParts.arg(TranslatableComponent.of("expression"), TextComponent.of("Expression to apply"))
      .defaultsTo(ImmutableList.of("y-=0.2"))
      .ofTypes(ImmutableList.of(string_Key))
      .build();
   private final NoArgCommandFlag useRawCoordsPart = CommandParts.flag('r', TextComponent.of("Use the game's coordinate origin")).build();
   private final NoArgCommandFlag usePlacementPart = CommandParts.flag('o', TextComponent.of("Use the placement position as the origin")).build();
   private final CommandArgument patternPart = CommandParts.arg(TranslatableComponent.of("pattern"), TextComponent.of("The pattern of blocks to set"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(pattern_Key))
      .build();
   private final CommandArgument densityPart2 = CommandParts.arg(TranslatableComponent.of("density"), TextComponent.of("The density of the brush"))
      .defaultsTo(ImmutableList.of("20"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final CommandArgument typePart = CommandParts.arg(TranslatableComponent.of("type"), TextComponent.of("The type of tree to use"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(treeType_Key))
      .build();
   private final NoArgCommandFlag stackPart = CommandParts.flag('s', TextComponent.of("Whether to stack snow")).build();
   private final CommandArgument radiusPart14 = CommandParts.arg(TranslatableComponent.of("radius"), TextComponent.of("The radius of the sphere"))
      .defaultsTo(ImmutableList.of("2"))
      .ofTypes(ImmutableList.of(expression_Key))
      .build();
   private final NoArgCommandFlag hollowPart = CommandParts.flag('h', TextComponent.of("Create hollow spheres instead")).build();
   private final NoArgCommandFlag fallingPart = CommandParts.flag('f', TextComponent.of("Create falling spheres instead")).build();
   private final CommandArgument radiusPart15 = CommandParts.arg(TranslatableComponent.of("radius"), TextComponent.of("The radius of the cylinder"))
      .defaultsTo(ImmutableList.of("2"))
      .ofTypes(ImmutableList.of(expression_Key))
      .build();
   private final CommandArgument heightPart = CommandParts.arg(TranslatableComponent.of("height"), TextComponent.of("The height of the cylinder"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument thicknessPart = CommandParts.arg(
         TranslatableComponent.of("thickness"),
         TextComponent.of("The thickness of the cylinder. Requires -h switch be given. 0 creates a standard hollow cylinder.")
      )
      .defaultsTo(ImmutableList.of("0"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final NoArgCommandFlag hollowPart2 = CommandParts.flag('h', TextComponent.of("Create hollow cylinders instead")).build();
   private final NoArgCommandFlag ignoreAirPart = CommandParts.flag('a', TextComponent.of("Don't paste air from the clipboard")).build();
   private final NoArgCommandFlag usingOriginPart = CommandParts.flag(
         'o', TextComponent.of("Paste starting at the target location, instead of centering on it")
      )
      .build();
   private final NoArgCommandFlag pasteEntitiesPart = CommandParts.flag('e', TextComponent.of("Paste entities if available")).build();
   private final NoArgCommandFlag pasteBiomesPart = CommandParts.flag('b', TextComponent.of("Paste biomes if available")).build();
   private final ArgAcceptingCommandFlag sourceMaskPart = CommandParts.flag('m', TextComponent.of("Skip blocks matching this mask in the clipboard"))
      .withRequiredArg()
      .argNamed(TranslatableComponent.of("sourceMask"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(mask_clipboardMask_Key))
      .build();
   private final NoArgCommandFlag randomRotatePart5 = CommandParts.flag(
         'r', TextComponent.of("Apply random rotation on paste, combines with existing clipboard transforms")
      )
      .build();
   private final CommandArgument radiusPart16 = CommandParts.arg(TranslatableComponent.of("radius"), TextComponent.of("The radius to sample for softening"))
      .defaultsTo(ImmutableList.of("2"))
      .ofTypes(ImmutableList.of(expression_Key))
      .build();
   private final CommandArgument iterationsPart = CommandParts.arg(
         TranslatableComponent.of("iterations"), TextComponent.of("The number of iterations to perform")
      )
      .defaultsTo(ImmutableList.of("4"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument maskPart3 = CommandParts.arg(TranslatableComponent.of("mask"), TextComponent.of("The mask of blocks to use for the heightmap"))
      .defaultsTo(ImmutableList.of(""))
      .ofTypes(ImmutableList.of(mask_Key))
      .build();
   private final CommandArgument iterationsPart2 = CommandParts.arg(
         TranslatableComponent.of("iterations"), TextComponent.of("The number of iterations to perform")
      )
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final ArgAcceptingCommandFlag snowBlockCountPart = CommandParts.flag('l', TextComponent.of("The number of snow blocks under snow"))
      .withRequiredArg()
      .argNamed(TranslatableComponent.of("snowBlockCount"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final ArgAcceptingCommandFlag maskPart4 = CommandParts.flag('m', TextComponent.of("The mask of blocks to use for the heightmap"))
      .withRequiredArg()
      .argNamed(TranslatableComponent.of("mask"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(mask_Key))
      .build();
   private final CommandArgument radiusPart17 = CommandParts.arg(TranslatableComponent.of("radius"), TextComponent.of("The radius to extinguish"))
      .defaultsTo(ImmutableList.of("5"))
      .ofTypes(ImmutableList.of(expression_Key))
      .build();
   private final CommandArgument radiusPart18 = CommandParts.arg(TranslatableComponent.of("radius"), TextComponent.of("The radius to apply gravity in"))
      .defaultsTo(ImmutableList.of("5"))
      .ofTypes(ImmutableList.of(expression_Key))
      .build();
   private final NoArgCommandFlag fromMaxYPart = CommandParts.flag(
         'h', TextComponent.of("Affect blocks starting at max Y, rather than the target location Y + radius")
      )
      .build();
   private final CommandArgument radiusPart19 = CommandParts.arg(TranslatableComponent.of("radius"), TextComponent.of("Radius to kill mobs in"))
      .defaultsTo(ImmutableList.of("5"))
      .ofTypes(ImmutableList.of(expression_Key))
      .build();
   private final NoArgCommandFlag killPetsPart = CommandParts.flag('p', TextComponent.of("Also kill pets")).build();
   private final NoArgCommandFlag killNpcsPart = CommandParts.flag('n', TextComponent.of("Also kill NPCs")).build();
   private final NoArgCommandFlag killGolemsPart = CommandParts.flag('g', TextComponent.of("Also kill golems")).build();
   private final NoArgCommandFlag killAnimalsPart = CommandParts.flag('a', TextComponent.of("Also kill animals")).build();
   private final NoArgCommandFlag killAmbientPart = CommandParts.flag('b', TextComponent.of("Also kill ambient mobs")).build();
   private final NoArgCommandFlag killWithNamePart = CommandParts.flag('t', TextComponent.of("Also kill mobs with name tags")).build();
   private final NoArgCommandFlag killFriendlyPart = CommandParts.flag('f', TextComponent.of("Also kill all friendly mobs (Applies the flags `-abgnpt`)"))
      .build();
   private final NoArgCommandFlag killArmorStandsPart = CommandParts.flag('r', TextComponent.of("Also destroy armor stands")).build();
   private final NoArgCommandFlag killWaterPart = CommandParts.flag('w', TextComponent.of("Also kill water mobs")).build();
   private final CommandArgument biomeTypePart = CommandParts.arg(TranslatableComponent.of("biomeType"), TextComponent.of("The biome type"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(biomeType_Key))
      .build();
   private final NoArgCommandFlag columnPart = CommandParts.flag('c', TextComponent.of("Whether to set the full column")).build();
   private final CommandArgument brushSizePart = CommandParts.arg(TranslatableComponent.of("brushSize"), TextComponent.of("The size of the brush"))
      .defaultsTo(ImmutableList.of("5"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final CommandArgument minErodeFacesPart = CommandParts.arg(
         TranslatableComponent.of("minErodeFaces"), TextComponent.of("Minimum number of faces for erosion")
      )
      .defaultsTo(ImmutableList.of("3"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument numErodeIterationsPart = CommandParts.arg(TranslatableComponent.of("numErodeIterations"), TextComponent.of("Erode iterations"))
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument minDilateFacesPart = CommandParts.arg(
         TranslatableComponent.of("minDilateFaces"), TextComponent.of("Minimum number of faces for dilation")
      )
      .defaultsTo(ImmutableList.of("3"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument numDilateIterationsPart = CommandParts.arg(
         TranslatableComponent.of("numDilateIterations"), TextComponent.of("Dilate iterations")
      )
      .defaultsTo(ImmutableList.of("1"))
      .ofTypes(ImmutableList.of(integer_Key))
      .build();
   private final CommandArgument densityPart3 = CommandParts.arg(TranslatableComponent.of("density"), TextComponent.of("The density of the brush"))
      .defaultsTo(ImmutableList.of("5"))
      .ofTypes(ImmutableList.of(double_Key))
      .build();
   private final CommandArgument typePart2 = CommandParts.arg(TranslatableComponent.of("type"), TextComponent.of("The type of feature to use"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(configuredFeatureType_Key))
      .build();
   private final CommandArgument typePart3 = CommandParts.arg(TranslatableComponent.of("type"), TextComponent.of("The type of feature to use"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(structureType_Key))
      .build();

   private BrushCommandsRegistration() {
      this.listeners = ImmutableList.of();
   }

   public static BrushCommandsRegistration builder() {
      return new BrushCommandsRegistration();
   }

   public BrushCommandsRegistration commandManager(CommandManager commandManager) {
      this.commandManager = commandManager;
      return this;
   }

   public BrushCommandsRegistration containerInstance(BrushCommands containerInstance) {
      this.containerInstance = containerInstance;
      return this;
   }

   public BrushCommandsRegistration commandPermissionsConditionGenerator(CommandPermissionsConditionGenerator commandPermissionsConditionGenerator) {
      this.commandPermissionsConditionGenerator = commandPermissionsConditionGenerator;
      return this;
   }

   public BrushCommandsRegistration listeners(Collection<CommandCallListener> listeners) {
      this.listeners = ImmutableList.copyOf(listeners);
      return this;
   }

   @Override
   public void build() {
      this.commandManager
         .register(
            "blendball",
            b -> {
               b.aliases(ImmutableList.of("bb", "blend"));
               b.description(TextComponent.of("Smooths and blends terrain"));
               b.footer(TextComponent.of("Smooths and blends terrain\nPic: https://i.imgur.com/cNUQUkj.png → https://i.imgur.com/hFOFsNf.png"));
               b.parts(ImmutableList.of(this.radiusPart, this.minFreqDiffPart, this.onlyAirPart, this.maskPart));
               b.action(this::cmd$blendball);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "blendBallBrush", InjectedValueAccess.class, Expression.class, int.class, boolean.class, Mask.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "erode",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Erodes terrain"));
               b.parts(ImmutableList.of(this.radiusPart2, this.erodefacesPart, this.erodeRecPart, this.fillFacesPart, this.fillRecPart));
               b.action(this::cmd$erode);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "erodeBrush", InjectedValueAccess.class, Expression.class, int.class, int.class, int.class, int.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "pull",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Pull terrain towards you"));
               b.parts(ImmutableList.of(this.radiusPart, this.erodefacesPart2, this.erodeRecPart2, this.fillFacesPart2, this.fillRecPart));
               b.action(this::cmd$pull);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "pullBrush", InjectedValueAccess.class, Expression.class, int.class, int.class, int.class, int.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "circle",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Creates a circle, which revolves around your facing direction"));
               b.parts(ImmutableList.of(this.fillPart, this.radiusPart, this.filledPart));
               b.action(this::cmd$circle);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "circleBrush", InjectedValueAccess.class, Pattern.class, Expression.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "recursive",
            b -> {
               b.aliases(ImmutableList.of("recurse", "r"));
               b.description(TextComponent.of("Set all connected blocks"));
               b.footer(TextComponent.of("Set all connected blocks\nNote: Set a mask to recurse along specific blocks"));
               b.parts(ImmutableList.of(this.fillPart, this.radiusPart, this.depthFirstPart));
               b.action(this::cmd$recursive);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "recursiveBrush", InjectedValueAccess.class, EditSession.class, Pattern.class, Expression.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "line",
            b -> {
               b.aliases(ImmutableList.of("l"));
               b.description(TextComponent.of("Create lines"));
               b.parts(ImmutableList.of(this.fillPart, this.radiusPart3, this.shellPart, this.selectPart, this.flatPart));
               b.action(this::cmd$line);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "lineBrush", InjectedValueAccess.class, Pattern.class, Expression.class, boolean.class, boolean.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "spline",
            b -> {
               b.aliases(ImmutableList.of("spl", "curve"));
               b.description(TextComponent.of("Join multiple objects together in a curve"));
               b.footer(
                  TextComponent.of(
                     "Click to select some objects,click the same block twice to connect the objects.\nInsufficient brush radius, or clicking the the wrong spot will result in undesired shapes. The shapes must be simple lines or loops.\nPic1: http://i.imgur.com/CeRYAoV.jpg -> http://i.imgur.com/jtM0jA4.png\nPic2: http://i.imgur.com/bUeyc72.png -> http://i.imgur.com/tg6MkcF.pngTutorial: https://www.planetminecraft.com/blog/fawe-tutorial/"
                  )
               );
               b.parts(ImmutableList.of(this.fillPart, this.radiusPart4));
               b.action(this::cmd$spline);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "splineBrush", Player.class, InjectedValueAccess.class, Pattern.class, Expression.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "sweep",
            b -> {
               b.aliases(ImmutableList.of("sw", "vaesweep"));
               b.description(TextComponent.of("Sweep your clipboard content along a curve"));
               b.footer(
                  TextComponent.of(
                     "Sweeps your clipboard content along a curve.\nDefine a curve by selecting the individual points with a brush\nSet [copies] to a value > 0 if you want to have your selection pasted a limited amount of times equally spaced on the curve"
                  )
               );
               b.parts(ImmutableList.of(this.copiesPart));
               b.action(this::cmd$sweep);
               Method commandMethod = RegistrationUtil.getCommandMethod(BrushCommands.class, "sweepBrush", Player.class, InjectedValueAccess.class, int.class);
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "catenary",
            b -> {
               b.aliases(ImmutableList.of("cat", "gravityline", "saggedline"));
               b.description(TextComponent.of("Create a hanging line between two points"));
               b.parts(ImmutableList.of(this.fillPart, this.lengthFactorPart, this.radiusPart3, this.shellPart, this.selectPart2, this.facingDirectionPart));
               b.action(this::cmd$catenary);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class,
                  "catenaryBrush",
                  InjectedValueAccess.class,
                  Pattern.class,
                  double.class,
                  Expression.class,
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
            "surfacespline",
            b -> {
               b.aliases(ImmutableList.of("sspline", "sspl"));
               b.description(TextComponent.of("Draws a spline (curved line) on the surface"));
               b.footer(TextComponent.of("Create a spline on the surface\nVideo: https://www.youtube.com/watch?v=CukvVOi4jv8"));
               b.parts(ImmutableList.of(this.fillPart, this.radiusPart3, this.tensionPart, this.biasPart, this.continuityPart, this.qualityPart));
               b.action(this::cmd$surfacespline);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class,
                  "surfaceSpline",
                  Player.class,
                  InjectedValueAccess.class,
                  Pattern.class,
                  Expression.class,
                  double.class,
                  double.class,
                  double.class,
                  double.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "rock",
            b -> {
               b.aliases(ImmutableList.of("blob"));
               b.description(TextComponent.of("Creates a distorted sphere"));
               b.parts(ImmutableList.of(this.fillPart, this.radiusPart5, this.sphericityPart, this.frequencyPart, this.amplitudePart));
               b.action(this::cmd$rock);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "blobBrush", InjectedValueAccess.class, Pattern.class, Vector3.class, double.class, double.class, double.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "shatter",
            b -> {
               b.aliases(ImmutableList.of("partition", "split"));
               b.description(TextComponent.of("Creates random lines to break the terrain into pieces"));
               b.footer(TextComponent.of("Creates uneven lines separating terrain into multiple pieces\nPic: https://i.imgur.com/2xKsZf2.png"));
               b.parts(ImmutableList.of(this.fillPart, this.radiusPart6, this.countPart));
               b.action(this::cmd$shatter);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "shatterBrush", EditSession.class, InjectedValueAccess.class, Pattern.class, Expression.class, int.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "stencil",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Use a height map to paint a surface"));
               b.footer(TextComponent.of("Use a height map to paint any surface."));
               b.parts(
                  ImmutableList.of(
                     this.fillPart, this.radiusPart7, this.imagePart, this.rotationPart, this.yscalePart, this.onlyWhitePart, this.randomRotatePart
                  )
               );
               b.action(this::cmd$stencil);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class,
                  "stencilBrush",
                  Player.class,
                  LocalSession.class,
                  InjectedValueAccess.class,
                  Pattern.class,
                  Expression.class,
                  String.class,
                  int.class,
                  double.class,
                  boolean.class,
                  boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "image",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Use a height map to paint a surface"));
               b.footer(TextComponent.of("Use a height map to paint any surface.\n"));
               b.parts(ImmutableList.of(this.imageURLPart, this.radiusPart8, this.yscalePart2, this.alphaPart, this.fadeOutPart));
               b.action(this::cmd$image);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class,
                  "imageBrush",
                  LocalSession.class,
                  InjectedValueAccess.class,
                  String.class,
                  Expression.class,
                  double.class,
                  boolean.class,
                  boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "surface",
            b -> {
               b.aliases(ImmutableList.of("surf"));
               b.description(TextComponent.of("Use a height map to paint a surface"));
               b.footer(TextComponent.of("Use a height map to paint any surface."));
               b.parts(ImmutableList.of(this.fillPart, this.radiusPart7));
               b.action(this::cmd$surface);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "surfaceBrush", InjectedValueAccess.class, Pattern.class, Expression.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "scatter",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Scatter a pattern on a surface"));
               b.footer(
                  TextComponent.of("Set a number of blocks randomly on a surface each a certain distance apart.\nVideo: https://youtu.be/RPZIaTbqoZw?t=34s")
               );
               b.parts(ImmutableList.of(this.fillPart, this.radiusPart9, this.pointsPart, this.distancePart, this.overlayPart));
               b.action(this::cmd$scatter);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "scatterBrush", InjectedValueAccess.class, Pattern.class, Expression.class, double.class, double.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "populateschematic",
            b -> {
               b.aliases(ImmutableList.of("populateschem", "popschem", "pschem", "ps"));
               b.description(TextComponent.of("Scatter a schematic on a surface"));
               b.parts(ImmutableList.of(this.clipboardStrPart, this.maskPart2, this.radiusPart10, this.densityPart, this.rotatePart));
               b.action(this::cmd$populateschematic);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class,
                  "scatterSchemBrush",
                  Player.class,
                  InjectedValueAccess.class,
                  String.class,
                  Mask.class,
                  Expression.class,
                  double.class,
                  boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "layer",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Replaces terrain with a layer."));
               b.footer(
                  TextComponent.of(
                     "Replaces terrain with a layer.\nExample: /br layer 5 oak_planks,orange_stained_glass,magenta_stained_glass,black_wool - Places several layers on a surface\nPic: https://i.imgur.com/XV0vYoX.png"
                  )
               );
               b.parts(ImmutableList.of(this.radiusPart11, this.patternLayersPart));
               b.action(this::cmd$layer);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "surfaceLayer", InjectedValueAccess.class, Expression.class, List.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "splatter",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Splatter a pattern on a surface"));
               b.footer(
                  TextComponent.of(
                     "Sets a bunch of blocks randomly on a surface.\nPic: https://i.imgur.com/hMD29oO.png\nExample: /br splatter stone,dirt 30 15\nNote: The seeds define how many splotches there are, recursion defines how large, solid defines whether the pattern is applied per seed, else per block."
                  )
               );
               b.parts(ImmutableList.of(this.fillPart, this.radiusPart7, this.pointsPart2, this.recursionPart, this.solidPart));
               b.action(this::cmd$splatter);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "splatterBrush", InjectedValueAccess.class, Pattern.class, Expression.class, double.class, double.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "scattercommand",
            b -> {
               b.aliases(ImmutableList.of("scattercmd", "scmd", "scommand"));
               b.description(TextComponent.of("Run commands at random points on a surface"));
               b.footer(
                  TextComponent.of(
                     "Run commands at random points on a surface\n - Your selection will be expanded to the specified size around each point\n - Placeholders: {x}, {y}, {z}, {world}, {size}"
                  )
               );
               b.parts(ImmutableList.of(this.radiusPart12, this.pointsPart2, this.distancePart2, this.commandStrPart, this.printPart));
               b.action(this::cmd$scattercommand);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class,
                  "scatterCommandBrush",
                  Player.class,
                  InjectedValueAccess.class,
                  Expression.class,
                  double.class,
                  double.class,
                  List.class,
                  boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "height",
            b -> {
               b.aliases(ImmutableList.of("heightmap"));
               b.description(TextComponent.of("Raise or lower terrain using a heightmap"));
               b.footer(
                  TextComponent.of(
                     "This brush raises and lowers land.\nNote: Use a negative yscale to reduce height\nSnow Pic: https://i.imgur.com/Hrzn0I4.png"
                  )
               );
               b.parts(
                  ImmutableList.of(
                     this.radiusPart7, this.yscalePart, this.imagePart, this.rotationPart, this.randomRotatePart2, this.layersPart, this.dontSmoothPart
                  )
               );
               b.action(this::cmd$height);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class,
                  "heightBrush",
                  Player.class,
                  LocalSession.class,
                  Expression.class,
                  double.class,
                  String.class,
                  int.class,
                  boolean.class,
                  boolean.class,
                  boolean.class,
                  InjectedValueAccess.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "cliff",
            b -> {
               b.aliases(ImmutableList.of("flatcylinder"));
               b.description(TextComponent.of("Cliff brush"));
               b.footer(TextComponent.of("This brush flattens terrain and creates cliffs."));
               b.parts(
                  ImmutableList.of(
                     this.radiusPart7, this.imagePart, this.rotationPart, this.yscalePart, this.randomRotatePart3, this.layersPart2, this.dontSmoothPart2
                  )
               );
               b.action(this::cmd$cliff);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class,
                  "cliffBrush",
                  Player.class,
                  LocalSession.class,
                  Expression.class,
                  String.class,
                  int.class,
                  double.class,
                  boolean.class,
                  boolean.class,
                  boolean.class,
                  InjectedValueAccess.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "flatten",
            b -> {
               b.aliases(ImmutableList.of("flatmap", "flat"));
               b.description(TextComponent.of("This brush raises or lowers land towards the clicked point"));
               b.parts(
                  ImmutableList.of(
                     this.radiusPart7, this.imagePart, this.rotationPart, this.yscalePart, this.randomRotatePart3, this.layersPart2, this.dontSmoothPart2
                  )
               );
               b.action(this::cmd$flatten);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class,
                  "flattenBrush",
                  Player.class,
                  LocalSession.class,
                  Expression.class,
                  String.class,
                  int.class,
                  double.class,
                  boolean.class,
                  boolean.class,
                  boolean.class,
                  InjectedValueAccess.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "copypaste",
            b -> {
               b.aliases(ImmutableList.of("cp", "copypasta"));
               b.description(TextComponent.of("Copy Paste brush"));
               b.footer(
                  TextComponent.of(
                     "Left click the base of an object to copy.\nRight click to paste\nNote: Works well with the clipboard scroll action\nVideo: https://www.youtube.com/watch?v=RPZIaTbqoZw"
                  )
               );
               b.parts(ImmutableList.of(this.radiusPart7, this.randomRotatePart4, this.autoRotatePart));
               b.action(this::cmd$copypaste);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "copy", Player.class, LocalSession.class, InjectedValueAccess.class, Expression.class, boolean.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "command",
            b -> {
               b.aliases(ImmutableList.of("cmd"));
               b.description(TextComponent.of("Command brush"));
               b.footer(
                  TextComponent.of(
                     "Run the commands at the clicked position.\n - Your selection will be expanded to the specified size around each point\n - Placeholders: {x}, {y}, {z}, {world}, {size}"
                  )
               );
               b.parts(ImmutableList.of(this.radiusPart11, this.inputPart, this.hidePart));
               b.action(this::cmd$command);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "command", InjectedValueAccess.class, Expression.class, List.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "savebrush",
            b -> {
               b.aliases(ImmutableList.of("save"));
               b.description(TextComponent.of("Save your current brush"));
               b.parts(ImmutableList.of(this.namePart, this.rootPart));
               b.action(this::cmd$savebrush);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "saveBrush", Player.class, LocalSession.class, String.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("/listbrush", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("List saved brushes"));
         b.footer(TextComponent.of("List all brushes in the brush directory"));
         b.parts(ImmutableList.of(this.pagePart));
         b.action(this::cmd$_listbrush);
         Method commandMethod = RegistrationUtil.getCommandMethod(BrushCommands.class, "list", Actor.class, InjectedValueAccess.class, int.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager
         .register(
            "deform",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Deform brush, applies an expression to an area"));
               b.parts(ImmutableList.of(this.shapePart, this.radiusPart13, this.expressionPart, this.useRawCoordsPart, this.usePlacementPart));
               b.action(this::cmd$deform);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class,
                  "deform",
                  Player.class,
                  LocalSession.class,
                  RegionFactory.class,
                  double.class,
                  String.class,
                  boolean.class,
                  boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "set",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Set brush, sets all blocks in the area"));
               b.parts(ImmutableList.of(this.shapePart, this.radiusPart8, this.patternPart));
               b.action(this::cmd$set);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "set", Player.class, LocalSession.class, RegionFactory.class, Expression.class, Pattern.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "forest",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Forest brush, creates a forest in the area"));
               b.parts(ImmutableList.of(this.shapePart, this.radiusPart8, this.densityPart2, this.typePart));
               b.action(this::cmd$forest);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "forest", Player.class, LocalSession.class, RegionFactory.class, Expression.class, double.class, TreeType.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "raise",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Raise brush, raise all blocks by one"));
               b.parts(ImmutableList.of(this.shapePart, this.radiusPart8));
               b.action(this::cmd$raise);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "raise", Player.class, LocalSession.class, RegionFactory.class, Expression.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "lower",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Lower brush, lower all blocks by one"));
               b.parts(ImmutableList.of(this.shapePart, this.radiusPart8));
               b.action(this::cmd$lower);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "lower", Player.class, LocalSession.class, RegionFactory.class, Expression.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "snow",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Snow brush, sets snow in the area"));
               b.parts(ImmutableList.of(this.shapePart, this.radiusPart13, this.stackPart));
               b.action(this::cmd$snow);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "snow", Player.class, LocalSession.class, RegionFactory.class, double.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "sphere",
            b -> {
               b.aliases(ImmutableList.of("s"));
               b.description(TextComponent.of("Choose the sphere brush"));
               b.parts(ImmutableList.of(this.patternPart, this.radiusPart14, this.hollowPart, this.fallingPart));
               b.action(this::cmd$sphere);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "sphereBrush", Player.class, InjectedValueAccess.class, Pattern.class, Expression.class, boolean.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "cylinder",
            b -> {
               b.aliases(ImmutableList.of("cyl", "c"));
               b.description(TextComponent.of("Choose the cylinder brush"));
               b.parts(ImmutableList.of(this.patternPart, this.radiusPart15, this.heightPart, this.thicknessPart, this.hollowPart2));
               b.action(this::cmd$cylinder);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "cylinderBrush", InjectedValueAccess.class, Pattern.class, Expression.class, int.class, double.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "clipboard",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Paste your clipboard at the brush location. Includes any transforms."));
               b.footer(
                  TextComponent.of(
                     "Choose the clipboard brush.\nWithout the -o flag, the paste will appear centered at the target location. With the flag, then the paste will appear relative to where you had stood relative to the copied area when you copied it."
                  )
               );
               b.parts(
                  ImmutableList.of(
                     this.ignoreAirPart, this.usingOriginPart, this.pasteEntitiesPart, this.pasteBiomesPart, this.sourceMaskPart, this.randomRotatePart5
                  )
               );
               b.action(this::cmd$clipboard);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class,
                  "clipboardBrush",
                  Player.class,
                  LocalSession.class,
                  boolean.class,
                  boolean.class,
                  boolean.class,
                  boolean.class,
                  Mask.class,
                  InjectedValueAccess.class,
                  boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "smooth",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Choose the terrain softener brush"));
               b.footer(TextComponent.of("Example: '/brush smooth 2 4 grass_block,dirt,stone'"));
               b.parts(ImmutableList.of(this.radiusPart16, this.iterationsPart, this.maskPart3));
               b.action(this::cmd$smooth);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "smoothBrush", Player.class, LocalSession.class, Expression.class, int.class, Mask.class, InjectedValueAccess.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "snowsmooth",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Choose the snow terrain softener brush"));
               b.footer(TextComponent.of("Example: '/brush snowsmooth 5 1 -l 3'"));
               b.parts(ImmutableList.of(this.radiusPart16, this.iterationsPart2, this.snowBlockCountPart, this.maskPart4));
               b.action(this::cmd$snowsmooth);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class,
                  "snowSmoothBrush",
                  Player.class,
                  LocalSession.class,
                  Expression.class,
                  int.class,
                  int.class,
                  Mask.class,
                  InjectedValueAccess.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "extinguish",
            b -> {
               b.aliases(ImmutableList.of("ex"));
               b.description(TextComponent.of("Shortcut fire extinguisher brush"));
               b.parts(ImmutableList.of(this.radiusPart17));
               b.action(this::cmd$extinguish);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "extinguishBrush", InjectedValueAccess.class, EditSession.class, Expression.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "gravity",
            b -> {
               b.aliases(ImmutableList.of("grav"));
               b.description(TextComponent.of("Gravity brush, simulates the effect of gravity"));
               b.parts(ImmutableList.of(this.radiusPart18, this.fromMaxYPart));
               b.action(this::cmd$gravity);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "gravityBrush", InjectedValueAccess.class, Expression.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "butcher",
            b -> {
               b.aliases(ImmutableList.of("kill"));
               b.description(TextComponent.of("Butcher brush, kills mobs within a radius"));
               b.parts(
                  ImmutableList.of(
                     this.radiusPart19,
                     this.killPetsPart,
                     this.killNpcsPart,
                     this.killGolemsPart,
                     this.killAnimalsPart,
                     this.killAmbientPart,
                     this.killWithNamePart,
                     this.killFriendlyPart,
                     this.killArmorStandsPart,
                     this.killWaterPart
                  )
               );
               b.action(this::cmd$butcher);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class,
                  "butcherBrush",
                  Player.class,
                  LocalSession.class,
                  Expression.class,
                  boolean.class,
                  boolean.class,
                  boolean.class,
                  boolean.class,
                  boolean.class,
                  boolean.class,
                  boolean.class,
                  boolean.class,
                  boolean.class,
                  InjectedValueAccess.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "biome",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Biome brush, sets biomes in the area"));
               b.parts(ImmutableList.of(this.shapePart, this.radiusPart13, this.biomeTypePart, this.columnPart));
               b.action(this::cmd$biome);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "biome", Player.class, LocalSession.class, RegionFactory.class, double.class, BiomeType.class, boolean.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "morph",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Morph brush, morphs blocks in the area"));
               b.parts(
                  ImmutableList.of(
                     this.brushSizePart, this.minErodeFacesPart, this.numErodeIterationsPart, this.minDilateFacesPart, this.numDilateIterationsPart
                  )
               );
               b.action(this::cmd$morph);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "morph", Player.class, LocalSession.class, double.class, int.class, int.class, int.class, int.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager.register("dilate", b -> {
         b.aliases(ImmutableList.of());
         b.description(TextComponent.of("Dilate preset for morph brush, dilates blocks in the area"));
         b.parts(ImmutableList.of(this.brushSizePart));
         b.action(this::cmd$dilate);
         Method commandMethod = RegistrationUtil.getCommandMethod(BrushCommands.class, "dilate", Player.class, LocalSession.class, double.class);
         Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
         b.condition(condition);
      });
      this.commandManager
         .register(
            "feature",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Feature brush, paints Minecraft generation features"));
               b.parts(ImmutableList.of(this.shapePart, this.radiusPart13, this.densityPart3, this.typePart2));
               b.action(this::cmd$feature);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class,
                  "feature",
                  Player.class,
                  LocalSession.class,
                  RegionFactory.class,
                  double.class,
                  double.class,
                  ConfiguredFeatureType.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
      this.commandManager
         .register(
            "structure",
            b -> {
               b.aliases(ImmutableList.of());
               b.description(TextComponent.of("Structure brush, paints Minecraft generation structures"));
               b.parts(ImmutableList.of(this.shapePart, this.radiusPart13, this.densityPart3, this.typePart3));
               b.action(this::cmd$structure);
               Method commandMethod = RegistrationUtil.getCommandMethod(
                  BrushCommands.class, "structure", Player.class, LocalSession.class, RegionFactory.class, double.class, double.class, StructureType.class
               );
               Command.Condition condition = this.commandPermissionsConditionGenerator.generateCondition(commandMethod);
               b.condition(condition);
            }
         );
   }

   private int cmd$blendball(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "blendBallBrush", InjectedValueAccess.class, Expression.class, int.class, boolean.class, Mask.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .blendBallBrush(
               this.extract$context(parameters),
               this.extract$radius(parameters),
               this.extract$minFreqDiff(parameters),
               this.extract$onlyAir(parameters),
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

   private int cmd$erode(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "erodeBrush", InjectedValueAccess.class, Expression.class, int.class, int.class, int.class, int.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .erodeBrush(
               this.extract$context(parameters),
               this.extract$radius2(parameters),
               this.extract$erodefaces(parameters),
               this.extract$erodeRec(parameters),
               this.extract$fillFaces(parameters),
               this.extract$fillRec(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$pull(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "pullBrush", InjectedValueAccess.class, Expression.class, int.class, int.class, int.class, int.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .pullBrush(
               this.extract$context(parameters),
               this.extract$radius(parameters),
               this.extract$erodefaces2(parameters),
               this.extract$erodeRec2(parameters),
               this.extract$fillFaces2(parameters),
               this.extract$fillRec(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$circle(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "circleBrush", InjectedValueAccess.class, Pattern.class, Expression.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .circleBrush(this.extract$context(parameters), this.extract$fill(parameters), this.extract$radius(parameters), this.extract$filled(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$recursive(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "recursiveBrush", InjectedValueAccess.class, EditSession.class, Pattern.class, Expression.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .recursiveBrush(
               this.extract$context(parameters),
               this.extract$editSession(parameters),
               this.extract$fill(parameters),
               this.extract$radius(parameters),
               this.extract$depthFirst(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$line(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "lineBrush", InjectedValueAccess.class, Pattern.class, Expression.class, boolean.class, boolean.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .lineBrush(
               this.extract$context(parameters),
               this.extract$fill(parameters),
               this.extract$radius3(parameters),
               this.extract$shell(parameters),
               this.extract$select(parameters),
               this.extract$flat(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$spline(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "splineBrush", Player.class, InjectedValueAccess.class, Pattern.class, Expression.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .splineBrush(this.extract$player(parameters), this.extract$context(parameters), this.extract$fill(parameters), this.extract$radius4(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$sweep(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(BrushCommands.class, "sweepBrush", Player.class, InjectedValueAccess.class, int.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.sweepBrush(this.extract$player(parameters), this.extract$context(parameters), this.extract$copies(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$catenary(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class,
         "catenaryBrush",
         InjectedValueAccess.class,
         Pattern.class,
         double.class,
         Expression.class,
         boolean.class,
         boolean.class,
         boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .catenaryBrush(
               this.extract$context(parameters),
               this.extract$fill(parameters),
               this.extract$lengthFactor(parameters),
               this.extract$radius3(parameters),
               this.extract$shell(parameters),
               this.extract$select2(parameters),
               this.extract$facingDirection(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$surfacespline(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class,
         "surfaceSpline",
         Player.class,
         InjectedValueAccess.class,
         Pattern.class,
         Expression.class,
         double.class,
         double.class,
         double.class,
         double.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .surfaceSpline(
               this.extract$player(parameters),
               this.extract$context(parameters),
               this.extract$fill(parameters),
               this.extract$radius3(parameters),
               this.extract$tension(parameters),
               this.extract$bias(parameters),
               this.extract$continuity(parameters),
               this.extract$quality(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$rock(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "blobBrush", InjectedValueAccess.class, Pattern.class, Vector3.class, double.class, double.class, double.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .blobBrush(
               this.extract$context(parameters),
               this.extract$fill(parameters),
               this.extract$radius5(parameters),
               this.extract$sphericity(parameters),
               this.extract$frequency(parameters),
               this.extract$amplitude(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$shatter(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "shatterBrush", EditSession.class, InjectedValueAccess.class, Pattern.class, Expression.class, int.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .shatterBrush(
               this.extract$editSession(parameters),
               this.extract$context(parameters),
               this.extract$fill(parameters),
               this.extract$radius6(parameters),
               this.extract$count(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$stencil(CommandParameters parameters) throws WorldEditException, FileNotFoundException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class,
         "stencilBrush",
         Player.class,
         LocalSession.class,
         InjectedValueAccess.class,
         Pattern.class,
         Expression.class,
         String.class,
         int.class,
         double.class,
         boolean.class,
         boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .stencilBrush(
               this.extract$player(parameters),
               this.extract$session(parameters),
               this.extract$context(parameters),
               this.extract$fill(parameters),
               this.extract$radius7(parameters),
               this.extract$image(parameters),
               this.extract$rotation(parameters),
               this.extract$yscale(parameters),
               this.extract$onlyWhite(parameters),
               this.extract$randomRotate(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$image(CommandParameters parameters) throws WorldEditException, IOException, URISyntaxException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class,
         "imageBrush",
         LocalSession.class,
         InjectedValueAccess.class,
         String.class,
         Expression.class,
         double.class,
         boolean.class,
         boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .imageBrush(
               this.extract$session(parameters),
               this.extract$context(parameters),
               this.extract$imageURL(parameters),
               this.extract$radius8(parameters),
               this.extract$yscale2(parameters),
               this.extract$alpha(parameters),
               this.extract$fadeOut(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$surface(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(BrushCommands.class, "surfaceBrush", InjectedValueAccess.class, Pattern.class, Expression.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.surfaceBrush(this.extract$context(parameters), this.extract$fill(parameters), this.extract$radius7(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$scatter(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "scatterBrush", InjectedValueAccess.class, Pattern.class, Expression.class, double.class, double.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .scatterBrush(
               this.extract$context(parameters),
               this.extract$fill(parameters),
               this.extract$radius9(parameters),
               this.extract$points(parameters),
               this.extract$distance(parameters),
               this.extract$overlay(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$populateschematic(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class,
         "scatterSchemBrush",
         Player.class,
         InjectedValueAccess.class,
         String.class,
         Mask.class,
         Expression.class,
         double.class,
         boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .scatterSchemBrush(
               this.extract$player(parameters),
               this.extract$context(parameters),
               this.extract$clipboardStr(parameters),
               this.extract$mask2(parameters),
               this.extract$radius10(parameters),
               this.extract$density(parameters),
               this.extract$rotate(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$layer(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(BrushCommands.class, "surfaceLayer", InjectedValueAccess.class, Expression.class, List.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.surfaceLayer(this.extract$context(parameters), this.extract$radius11(parameters), this.extract$patternLayers(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$splatter(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "splatterBrush", InjectedValueAccess.class, Pattern.class, Expression.class, double.class, double.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .splatterBrush(
               this.extract$context(parameters),
               this.extract$fill(parameters),
               this.extract$radius7(parameters),
               this.extract$points2(parameters),
               this.extract$recursion(parameters),
               this.extract$solid(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$scattercommand(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class,
         "scatterCommandBrush",
         Player.class,
         InjectedValueAccess.class,
         Expression.class,
         double.class,
         double.class,
         List.class,
         boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .scatterCommandBrush(
               this.extract$player(parameters),
               this.extract$context(parameters),
               this.extract$radius12(parameters),
               this.extract$points2(parameters),
               this.extract$distance2(parameters),
               this.extract$commandStr(parameters),
               this.extract$print(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$height(CommandParameters parameters) throws WorldEditException, FileNotFoundException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class,
         "heightBrush",
         Player.class,
         LocalSession.class,
         Expression.class,
         double.class,
         String.class,
         int.class,
         boolean.class,
         boolean.class,
         boolean.class,
         InjectedValueAccess.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .heightBrush(
               this.extract$player(parameters),
               this.extract$session(parameters),
               this.extract$radius7(parameters),
               this.extract$yscale(parameters),
               this.extract$image(parameters),
               this.extract$rotation(parameters),
               this.extract$randomRotate2(parameters),
               this.extract$layers(parameters),
               this.extract$dontSmooth(parameters),
               this.extract$context(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$cliff(CommandParameters parameters) throws WorldEditException, FileNotFoundException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class,
         "cliffBrush",
         Player.class,
         LocalSession.class,
         Expression.class,
         String.class,
         int.class,
         double.class,
         boolean.class,
         boolean.class,
         boolean.class,
         InjectedValueAccess.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .cliffBrush(
               this.extract$player(parameters),
               this.extract$session(parameters),
               this.extract$radius7(parameters),
               this.extract$image(parameters),
               this.extract$rotation(parameters),
               this.extract$yscale(parameters),
               this.extract$randomRotate3(parameters),
               this.extract$layers2(parameters),
               this.extract$dontSmooth2(parameters),
               this.extract$context(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$flatten(CommandParameters parameters) throws WorldEditException, FileNotFoundException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class,
         "flattenBrush",
         Player.class,
         LocalSession.class,
         Expression.class,
         String.class,
         int.class,
         double.class,
         boolean.class,
         boolean.class,
         boolean.class,
         InjectedValueAccess.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .flattenBrush(
               this.extract$player(parameters),
               this.extract$session(parameters),
               this.extract$radius7(parameters),
               this.extract$image(parameters),
               this.extract$rotation(parameters),
               this.extract$yscale(parameters),
               this.extract$randomRotate3(parameters),
               this.extract$layers2(parameters),
               this.extract$dontSmooth2(parameters),
               this.extract$context(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$copypaste(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "copy", Player.class, LocalSession.class, InjectedValueAccess.class, Expression.class, boolean.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .copy(
               this.extract$player(parameters),
               this.extract$session(parameters),
               this.extract$context(parameters),
               this.extract$radius7(parameters),
               this.extract$randomRotate4(parameters),
               this.extract$autoRotate(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$command(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "command", InjectedValueAccess.class, Expression.class, List.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .command(this.extract$context(parameters), this.extract$radius11(parameters), this.extract$input(parameters), this.extract$hide(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$savebrush(CommandParameters parameters) throws WorldEditException, IOException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(BrushCommands.class, "saveBrush", Player.class, LocalSession.class, String.class, boolean.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .saveBrush(this.extract$player(parameters), this.extract$session(parameters), this.extract$name(parameters), this.extract$root(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$_listbrush(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(BrushCommands.class, "list", Actor.class, InjectedValueAccess.class, int.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.list(this.extract$actor(parameters), this.extract$args(parameters), this.extract$page(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$deform(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "deform", Player.class, LocalSession.class, RegionFactory.class, double.class, String.class, boolean.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .deform(
               this.extract$player(parameters),
               this.extract$localSession(parameters),
               this.extract$shape(parameters),
               this.extract$radius13(parameters),
               this.extract$expression(parameters),
               this.extract$useRawCoords(parameters),
               this.extract$usePlacement(parameters)
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
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "set", Player.class, LocalSession.class, RegionFactory.class, Expression.class, Pattern.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .set(
               this.extract$player(parameters),
               this.extract$localSession(parameters),
               this.extract$shape(parameters),
               this.extract$radius8(parameters),
               this.extract$pattern(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$forest(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "forest", Player.class, LocalSession.class, RegionFactory.class, Expression.class, double.class, TreeType.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .forest(
               this.extract$player(parameters),
               this.extract$localSession(parameters),
               this.extract$shape(parameters),
               this.extract$radius8(parameters),
               this.extract$density2(parameters),
               this.extract$type(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$raise(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "raise", Player.class, LocalSession.class, RegionFactory.class, Expression.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .raise(this.extract$player(parameters), this.extract$localSession(parameters), this.extract$shape(parameters), this.extract$radius8(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$lower(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "lower", Player.class, LocalSession.class, RegionFactory.class, Expression.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .lower(this.extract$player(parameters), this.extract$localSession(parameters), this.extract$shape(parameters), this.extract$radius8(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$snow(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "snow", Player.class, LocalSession.class, RegionFactory.class, double.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .snow(
               this.extract$player(parameters),
               this.extract$localSession(parameters),
               this.extract$shape(parameters),
               this.extract$radius13(parameters),
               this.extract$stack(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$sphere(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "sphereBrush", Player.class, InjectedValueAccess.class, Pattern.class, Expression.class, boolean.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .sphereBrush(
               this.extract$player(parameters),
               this.extract$context(parameters),
               this.extract$pattern(parameters),
               this.extract$radius14(parameters),
               this.extract$hollow(parameters),
               this.extract$falling(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$cylinder(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "cylinderBrush", InjectedValueAccess.class, Pattern.class, Expression.class, int.class, double.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .cylinderBrush(
               this.extract$context(parameters),
               this.extract$pattern(parameters),
               this.extract$radius15(parameters),
               this.extract$height(parameters),
               this.extract$thickness(parameters),
               this.extract$hollow2(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$clipboard(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class,
         "clipboardBrush",
         Player.class,
         LocalSession.class,
         boolean.class,
         boolean.class,
         boolean.class,
         boolean.class,
         Mask.class,
         InjectedValueAccess.class,
         boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .clipboardBrush(
               this.extract$player(parameters),
               this.extract$session(parameters),
               this.extract$ignoreAir(parameters),
               this.extract$usingOrigin(parameters),
               this.extract$pasteEntities(parameters),
               this.extract$pasteBiomes(parameters),
               this.extract$sourceMask(parameters),
               this.extract$context(parameters),
               this.extract$randomRotate5(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$smooth(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "smoothBrush", Player.class, LocalSession.class, Expression.class, int.class, Mask.class, InjectedValueAccess.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .smoothBrush(
               this.extract$player(parameters),
               this.extract$session(parameters),
               this.extract$radius16(parameters),
               this.extract$iterations(parameters),
               this.extract$mask3(parameters),
               this.extract$context(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$snowsmooth(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class,
         "snowSmoothBrush",
         Player.class,
         LocalSession.class,
         Expression.class,
         int.class,
         int.class,
         Mask.class,
         InjectedValueAccess.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .snowSmoothBrush(
               this.extract$player(parameters),
               this.extract$session(parameters),
               this.extract$radius16(parameters),
               this.extract$iterations2(parameters),
               this.extract$snowBlockCount(parameters),
               this.extract$mask4(parameters),
               this.extract$context(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$extinguish(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "extinguishBrush", InjectedValueAccess.class, EditSession.class, Expression.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.extinguishBrush(this.extract$context(parameters), this.extract$editSession(parameters), this.extract$radius17(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$gravity(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(BrushCommands.class, "gravityBrush", InjectedValueAccess.class, Expression.class, boolean.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.gravityBrush(this.extract$context(parameters), this.extract$radius18(parameters), this.extract$fromMaxY(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$butcher(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class,
         "butcherBrush",
         Player.class,
         LocalSession.class,
         Expression.class,
         boolean.class,
         boolean.class,
         boolean.class,
         boolean.class,
         boolean.class,
         boolean.class,
         boolean.class,
         boolean.class,
         boolean.class,
         InjectedValueAccess.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .butcherBrush(
               this.extract$player(parameters),
               this.extract$session(parameters),
               this.extract$radius19(parameters),
               this.extract$killPets(parameters),
               this.extract$killNpcs(parameters),
               this.extract$killGolems(parameters),
               this.extract$killAnimals(parameters),
               this.extract$killAmbient(parameters),
               this.extract$killWithName(parameters),
               this.extract$killFriendly(parameters),
               this.extract$killArmorStands(parameters),
               this.extract$killWater(parameters),
               this.extract$context(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$biome(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "biome", Player.class, LocalSession.class, RegionFactory.class, double.class, BiomeType.class, boolean.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .biome(
               this.extract$player(parameters),
               this.extract$localSession(parameters),
               this.extract$shape(parameters),
               this.extract$radius13(parameters),
               this.extract$biomeType(parameters),
               this.extract$column(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$morph(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "morph", Player.class, LocalSession.class, double.class, int.class, int.class, int.class, int.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .morph(
               this.extract$player(parameters),
               this.extract$session(parameters),
               this.extract$brushSize(parameters),
               this.extract$minErodeFaces(parameters),
               this.extract$numErodeIterations(parameters),
               this.extract$minDilateFaces(parameters),
               this.extract$numDilateIterations(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$dilate(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(BrushCommands.class, "dilate", Player.class, LocalSession.class, double.class);
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance.dilate(this.extract$player(parameters), this.extract$session(parameters), this.extract$brushSize(parameters));
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$feature(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "feature", Player.class, LocalSession.class, RegionFactory.class, double.class, double.class, ConfiguredFeatureType.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .feature(
               this.extract$player(parameters),
               this.extract$localSession(parameters),
               this.extract$shape(parameters),
               this.extract$radius13(parameters),
               this.extract$density3(parameters),
               this.extract$type2(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private int cmd$structure(CommandParameters parameters) throws WorldEditException {
      Method cmdMethod = RegistrationUtil.getCommandMethod(
         BrushCommands.class, "structure", Player.class, LocalSession.class, RegionFactory.class, double.class, double.class, StructureType.class
      );
      RegistrationUtil.listenersBeforeCall(this.listeners, cmdMethod, parameters);

      try {
         this.containerInstance
            .structure(
               this.extract$player(parameters),
               this.extract$localSession(parameters),
               this.extract$shape(parameters),
               this.extract$radius13(parameters),
               this.extract$density3(parameters),
               this.extract$type3(parameters)
            );
         int result = 1;
         RegistrationUtil.listenersAfterCall(this.listeners, cmdMethod, parameters);
         return result;
      } catch (Throwable var4) {
         RegistrationUtil.listenersAfterThrow(this.listeners, cmdMethod, parameters, var4);
         throw var4;
      }
   }

   private InjectedValueAccess extract$context(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(injectedValueAccess_Key, "context", parameters.injectedValue(injectedValueAccess_Key));
   }

   private Expression extract$radius(CommandParameters parameters) {
      return this.radiusPart.value(parameters).asSingle(expression_Key);
   }

   private int extract$minFreqDiff(CommandParameters parameters) {
      return this.minFreqDiffPart.value(parameters).asSingle(integer_Key);
   }

   private boolean extract$onlyAir(CommandParameters parameters) {
      return this.onlyAirPart.in(parameters);
   }

   private Mask extract$mask(CommandParameters parameters) {
      return this.maskPart.value(parameters).asSingle(mask_Key);
   }

   private Expression extract$radius2(CommandParameters parameters) {
      return this.radiusPart2.value(parameters).asSingle(expression_Key);
   }

   private int extract$erodefaces(CommandParameters parameters) {
      return this.erodefacesPart.value(parameters).asSingle(integer_Key);
   }

   private int extract$erodeRec(CommandParameters parameters) {
      return this.erodeRecPart.value(parameters).asSingle(integer_Key);
   }

   private int extract$fillFaces(CommandParameters parameters) {
      return this.fillFacesPart.value(parameters).asSingle(integer_Key);
   }

   private int extract$fillRec(CommandParameters parameters) {
      return this.fillRecPart.value(parameters).asSingle(integer_Key);
   }

   private int extract$erodefaces2(CommandParameters parameters) {
      return this.erodefacesPart2.value(parameters).asSingle(integer_Key);
   }

   private int extract$erodeRec2(CommandParameters parameters) {
      return this.erodeRecPart2.value(parameters).asSingle(integer_Key);
   }

   private int extract$fillFaces2(CommandParameters parameters) {
      return this.fillFacesPart2.value(parameters).asSingle(integer_Key);
   }

   private Pattern extract$fill(CommandParameters parameters) {
      return this.fillPart.value(parameters).asSingle(pattern_Key);
   }

   private boolean extract$filled(CommandParameters parameters) {
      return this.filledPart.value(parameters).asSingle(boolean_Key);
   }

   private EditSession extract$editSession(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(editSession_Key, "editSession", parameters.injectedValue(editSession_Key));
   }

   private boolean extract$depthFirst(CommandParameters parameters) {
      return this.depthFirstPart.in(parameters);
   }

   private Expression extract$radius3(CommandParameters parameters) {
      return this.radiusPart3.value(parameters).asSingle(expression_Key);
   }

   private boolean extract$shell(CommandParameters parameters) {
      return this.shellPart.in(parameters);
   }

   private boolean extract$select(CommandParameters parameters) {
      return this.selectPart.in(parameters);
   }

   private boolean extract$flat(CommandParameters parameters) {
      return this.flatPart.in(parameters);
   }

   private Player extract$player(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(player_Key, "player", parameters.injectedValue(player_Key));
   }

   private Expression extract$radius4(CommandParameters parameters) {
      return this.radiusPart4.value(parameters).asSingle(expression_Key);
   }

   private int extract$copies(CommandParameters parameters) {
      return this.copiesPart.value(parameters).asSingle(integer_Key);
   }

   private double extract$lengthFactor(CommandParameters parameters) {
      return this.lengthFactorPart.value(parameters).asSingle(double_Key);
   }

   private boolean extract$select2(CommandParameters parameters) {
      return this.selectPart2.in(parameters);
   }

   private boolean extract$facingDirection(CommandParameters parameters) {
      return this.facingDirectionPart.in(parameters);
   }

   private double extract$tension(CommandParameters parameters) {
      return this.tensionPart.value(parameters).asSingle(double_Key);
   }

   private double extract$bias(CommandParameters parameters) {
      return this.biasPart.value(parameters).asSingle(double_Key);
   }

   private double extract$continuity(CommandParameters parameters) {
      return this.continuityPart.value(parameters).asSingle(double_Key);
   }

   private double extract$quality(CommandParameters parameters) {
      return this.qualityPart.value(parameters).asSingle(double_Key);
   }

   private Vector3 extract$radius5(CommandParameters parameters) {
      return this.radiusPart5.value(parameters).asSingle(vector3_Key);
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

   private Expression extract$radius6(CommandParameters parameters) {
      return this.radiusPart6.value(parameters).asSingle(expression_Key);
   }

   private int extract$count(CommandParameters parameters) {
      return this.countPart.value(parameters).asSingle(integer_Key);
   }

   private LocalSession extract$session(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(localSession_Key, "session", parameters.injectedValue(localSession_Key));
   }

   private Expression extract$radius7(CommandParameters parameters) {
      return this.radiusPart7.value(parameters).asSingle(expression_Key);
   }

   private String extract$image(CommandParameters parameters) {
      return this.imagePart.value(parameters).asSingle(string_Key);
   }

   private int extract$rotation(CommandParameters parameters) {
      return this.rotationPart.value(parameters).asSingle(integer_Key);
   }

   private double extract$yscale(CommandParameters parameters) {
      return this.yscalePart.value(parameters).asSingle(double_Key);
   }

   private boolean extract$onlyWhite(CommandParameters parameters) {
      return this.onlyWhitePart.in(parameters);
   }

   private boolean extract$randomRotate(CommandParameters parameters) {
      return this.randomRotatePart.in(parameters);
   }

   private String extract$imageURL(CommandParameters parameters) {
      return this.imageURLPart.value(parameters).asSingle(string_Key);
   }

   private Expression extract$radius8(CommandParameters parameters) {
      return this.radiusPart8.value(parameters).asSingle(expression_Key);
   }

   private double extract$yscale2(CommandParameters parameters) {
      return this.yscalePart2.value(parameters).asSingle(double_Key);
   }

   private boolean extract$alpha(CommandParameters parameters) {
      return this.alphaPart.in(parameters);
   }

   private boolean extract$fadeOut(CommandParameters parameters) {
      return this.fadeOutPart.in(parameters);
   }

   private Expression extract$radius9(CommandParameters parameters) {
      return this.radiusPart9.value(parameters).asSingle(expression_Key);
   }

   private double extract$points(CommandParameters parameters) {
      return this.pointsPart.value(parameters).asSingle(double_Key);
   }

   private double extract$distance(CommandParameters parameters) {
      return this.distancePart.value(parameters).asSingle(double_Key);
   }

   private boolean extract$overlay(CommandParameters parameters) {
      return this.overlayPart.in(parameters);
   }

   private String extract$clipboardStr(CommandParameters parameters) {
      return this.clipboardStrPart.value(parameters).asSingle(string_Key);
   }

   private Mask extract$mask2(CommandParameters parameters) {
      return this.maskPart2.value(parameters).asSingle(mask_Key);
   }

   private Expression extract$radius10(CommandParameters parameters) {
      return this.radiusPart10.value(parameters).asSingle(expression_Key);
   }

   private double extract$density(CommandParameters parameters) {
      return this.densityPart.value(parameters).asSingle(double_Key);
   }

   private boolean extract$rotate(CommandParameters parameters) {
      return this.rotatePart.in(parameters);
   }

   private Expression extract$radius11(CommandParameters parameters) {
      return this.radiusPart11.value(parameters).asSingle(expression_Key);
   }

   private List<Pattern> extract$patternLayers(CommandParameters parameters) {
      return this.patternLayersPart.value(parameters).<Pattern>asMultiple(pattern_patternList_Key);
   }

   private double extract$points2(CommandParameters parameters) {
      return this.pointsPart2.value(parameters).asSingle(double_Key);
   }

   private double extract$recursion(CommandParameters parameters) {
      return this.recursionPart.value(parameters).asSingle(double_Key);
   }

   private boolean extract$solid(CommandParameters parameters) {
      return this.solidPart.value(parameters).asSingle(boolean_Key);
   }

   private Expression extract$radius12(CommandParameters parameters) {
      return this.radiusPart12.value(parameters).asSingle(expression_Key);
   }

   private double extract$distance2(CommandParameters parameters) {
      return this.distancePart2.value(parameters).asSingle(double_Key);
   }

   private List<String> extract$commandStr(CommandParameters parameters) {
      return this.commandStrPart.value(parameters).<String>asMultiple(string_Key);
   }

   private boolean extract$print(CommandParameters parameters) {
      return this.printPart.in(parameters);
   }

   private boolean extract$randomRotate2(CommandParameters parameters) {
      return this.randomRotatePart2.in(parameters);
   }

   private boolean extract$layers(CommandParameters parameters) {
      return this.layersPart.in(parameters);
   }

   private boolean extract$dontSmooth(CommandParameters parameters) {
      return this.dontSmoothPart.in(parameters);
   }

   private boolean extract$randomRotate3(CommandParameters parameters) {
      return this.randomRotatePart3.in(parameters);
   }

   private boolean extract$layers2(CommandParameters parameters) {
      return this.layersPart2.in(parameters);
   }

   private boolean extract$dontSmooth2(CommandParameters parameters) {
      return this.dontSmoothPart2.in(parameters);
   }

   private boolean extract$randomRotate4(CommandParameters parameters) {
      return this.randomRotatePart4.in(parameters);
   }

   private boolean extract$autoRotate(CommandParameters parameters) {
      return this.autoRotatePart.in(parameters);
   }

   private List<String> extract$input(CommandParameters parameters) {
      return this.inputPart.value(parameters).<String>asMultiple(string_Key);
   }

   private boolean extract$hide(CommandParameters parameters) {
      return this.hidePart.in(parameters);
   }

   private String extract$name(CommandParameters parameters) {
      return this.namePart.value(parameters).asSingle(string_Key);
   }

   private boolean extract$root(CommandParameters parameters) {
      return this.rootPart.in(parameters);
   }

   private Actor extract$actor(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(actor_Key, "actor", parameters.injectedValue(actor_Key));
   }

   private InjectedValueAccess extract$args(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(injectedValueAccess_Key, "args", parameters.injectedValue(injectedValueAccess_Key));
   }

   private int extract$page(CommandParameters parameters) {
      return this.pagePart.value(parameters).asSingle(integer_Key);
   }

   private LocalSession extract$localSession(CommandParameters parameters) {
      return RegistrationUtil.requireOptional(localSession_Key, "localSession", parameters.injectedValue(localSession_Key));
   }

   private RegionFactory extract$shape(CommandParameters parameters) {
      return this.shapePart.value(parameters).asSingle(regionFactory_Key);
   }

   private double extract$radius13(CommandParameters parameters) {
      return this.radiusPart13.value(parameters).asSingle(double_Key);
   }

   private String extract$expression(CommandParameters parameters) {
      return this.expressionPart.value(parameters).asSingle(string_Key);
   }

   private boolean extract$useRawCoords(CommandParameters parameters) {
      return this.useRawCoordsPart.in(parameters);
   }

   private boolean extract$usePlacement(CommandParameters parameters) {
      return this.usePlacementPart.in(parameters);
   }

   private Pattern extract$pattern(CommandParameters parameters) {
      return this.patternPart.value(parameters).asSingle(pattern_Key);
   }

   private double extract$density2(CommandParameters parameters) {
      return this.densityPart2.value(parameters).asSingle(double_Key);
   }

   private TreeType extract$type(CommandParameters parameters) {
      return this.typePart.value(parameters).asSingle(treeType_Key);
   }

   private boolean extract$stack(CommandParameters parameters) {
      return this.stackPart.in(parameters);
   }

   private Expression extract$radius14(CommandParameters parameters) {
      return this.radiusPart14.value(parameters).asSingle(expression_Key);
   }

   private boolean extract$hollow(CommandParameters parameters) {
      return this.hollowPart.in(parameters);
   }

   private boolean extract$falling(CommandParameters parameters) {
      return this.fallingPart.in(parameters);
   }

   private Expression extract$radius15(CommandParameters parameters) {
      return this.radiusPart15.value(parameters).asSingle(expression_Key);
   }

   private int extract$height(CommandParameters parameters) {
      return this.heightPart.value(parameters).asSingle(integer_Key);
   }

   private double extract$thickness(CommandParameters parameters) {
      return this.thicknessPart.value(parameters).asSingle(double_Key);
   }

   private boolean extract$hollow2(CommandParameters parameters) {
      return this.hollowPart2.in(parameters);
   }

   private boolean extract$ignoreAir(CommandParameters parameters) {
      return this.ignoreAirPart.in(parameters);
   }

   private boolean extract$usingOrigin(CommandParameters parameters) {
      return this.usingOriginPart.in(parameters);
   }

   private boolean extract$pasteEntities(CommandParameters parameters) {
      return this.pasteEntitiesPart.in(parameters);
   }

   private boolean extract$pasteBiomes(CommandParameters parameters) {
      return this.pasteBiomesPart.in(parameters);
   }

   private Mask extract$sourceMask(CommandParameters parameters) {
      return this.sourceMaskPart.value(parameters).asSingle(mask_clipboardMask_Key);
   }

   private boolean extract$randomRotate5(CommandParameters parameters) {
      return this.randomRotatePart5.in(parameters);
   }

   private Expression extract$radius16(CommandParameters parameters) {
      return this.radiusPart16.value(parameters).asSingle(expression_Key);
   }

   private int extract$iterations(CommandParameters parameters) {
      return this.iterationsPart.value(parameters).asSingle(integer_Key);
   }

   private Mask extract$mask3(CommandParameters parameters) {
      return this.maskPart3.value(parameters).asSingle(mask_Key);
   }

   private int extract$iterations2(CommandParameters parameters) {
      return this.iterationsPart2.value(parameters).asSingle(integer_Key);
   }

   private int extract$snowBlockCount(CommandParameters parameters) {
      return this.snowBlockCountPart.value(parameters).asSingle(integer_Key);
   }

   private Mask extract$mask4(CommandParameters parameters) {
      return this.maskPart4.value(parameters).asSingle(mask_Key);
   }

   private Expression extract$radius17(CommandParameters parameters) {
      return this.radiusPart17.value(parameters).asSingle(expression_Key);
   }

   private Expression extract$radius18(CommandParameters parameters) {
      return this.radiusPart18.value(parameters).asSingle(expression_Key);
   }

   private boolean extract$fromMaxY(CommandParameters parameters) {
      return this.fromMaxYPart.in(parameters);
   }

   private Expression extract$radius19(CommandParameters parameters) {
      return this.radiusPart19.value(parameters).asSingle(expression_Key);
   }

   private boolean extract$killPets(CommandParameters parameters) {
      return this.killPetsPart.in(parameters);
   }

   private boolean extract$killNpcs(CommandParameters parameters) {
      return this.killNpcsPart.in(parameters);
   }

   private boolean extract$killGolems(CommandParameters parameters) {
      return this.killGolemsPart.in(parameters);
   }

   private boolean extract$killAnimals(CommandParameters parameters) {
      return this.killAnimalsPart.in(parameters);
   }

   private boolean extract$killAmbient(CommandParameters parameters) {
      return this.killAmbientPart.in(parameters);
   }

   private boolean extract$killWithName(CommandParameters parameters) {
      return this.killWithNamePart.in(parameters);
   }

   private boolean extract$killFriendly(CommandParameters parameters) {
      return this.killFriendlyPart.in(parameters);
   }

   private boolean extract$killArmorStands(CommandParameters parameters) {
      return this.killArmorStandsPart.in(parameters);
   }

   private boolean extract$killWater(CommandParameters parameters) {
      return this.killWaterPart.in(parameters);
   }

   private BiomeType extract$biomeType(CommandParameters parameters) {
      return this.biomeTypePart.value(parameters).asSingle(biomeType_Key);
   }

   private boolean extract$column(CommandParameters parameters) {
      return this.columnPart.in(parameters);
   }

   private double extract$brushSize(CommandParameters parameters) {
      return this.brushSizePart.value(parameters).asSingle(double_Key);
   }

   private int extract$minErodeFaces(CommandParameters parameters) {
      return this.minErodeFacesPart.value(parameters).asSingle(integer_Key);
   }

   private int extract$numErodeIterations(CommandParameters parameters) {
      return this.numErodeIterationsPart.value(parameters).asSingle(integer_Key);
   }

   private int extract$minDilateFaces(CommandParameters parameters) {
      return this.minDilateFacesPart.value(parameters).asSingle(integer_Key);
   }

   private int extract$numDilateIterations(CommandParameters parameters) {
      return this.numDilateIterationsPart.value(parameters).asSingle(integer_Key);
   }

   private double extract$density3(CommandParameters parameters) {
      return this.densityPart3.value(parameters).asSingle(double_Key);
   }

   private ConfiguredFeatureType extract$type2(CommandParameters parameters) {
      return this.typePart2.value(parameters).asSingle(configuredFeatureType_Key);
   }

   private StructureType extract$type3(CommandParameters parameters) {
      return this.typePart3.value(parameters).asSingle(structureType_Key);
   }
}
