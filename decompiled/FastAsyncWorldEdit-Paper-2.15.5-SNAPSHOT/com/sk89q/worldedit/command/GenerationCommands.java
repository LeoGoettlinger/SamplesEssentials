package com.sk89q.worldedit.command;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.function.generator.CavesGen;
import com.fastasyncworldedit.core.internal.exception.FaweException;
import com.fastasyncworldedit.core.util.MainUtil;
import com.fastasyncworldedit.core.util.MaskTraverser;
import com.fastasyncworldedit.core.util.MathMan;
import com.fastasyncworldedit.core.util.TextureUtil;
import com.fastasyncworldedit.core.util.image.ImageUtil;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.command.util.annotation.Confirm;
import com.sk89q.worldedit.command.util.annotation.Preload;
import com.sk89q.worldedit.command.util.annotation.SynchronousSettingExpected;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.internal.annotation.Radii;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.internal.command.CommandUtil;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;
import com.sk89q.worldedit.world.generation.StructureType;
import com.sk89q.worldedit.world.generation.TreeType;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.Switch;
import org.jetbrains.annotations.Range;

@CommandContainer(
   superTypes = {CommandPermissionsConditionGenerator.Registration.class}
)
public class GenerationCommands {
   private final WorldEdit worldEdit;

   public GenerationCommands(WorldEdit worldEdit) {
      Preconditions.checkNotNull(worldEdit);
      this.worldEdit = worldEdit;
   }

   @Command(
      name = "/hcyl",
      desc = "Generates a hollow cylinder."
   )
   @CommandPermissions({"worldedit.generation.cylinder"})
   @Logging(Logging.LogMode.PLACEMENT)
   @SynchronousSettingExpected
   public int hcyl(
      Actor actor,
      LocalSession session,
      EditSession editSession,
      @Arg(desc = "The pattern of blocks to generate") Pattern pattern,
      @Radii(2) @Arg(desc = "The radii of the cylinder. 1st is N/S, 2nd is E/W") List<Double> radii,
      @Arg(desc = "The height of the cylinder",def = {"1"}) int height,
      @Arg(desc = "Thickness of the cyclinder. 0 creates a normal //hcyl.",def = {"0"}) double thickness
   ) throws WorldEditException {
      double radiusX;
      double radiusZ;
      switch (radii.size()) {
         case 1:
            radiusX = radiusZ = Math.max(1.0, radii.get(0));
            break;
         case 2:
            radiusX = Math.max(1.0, radii.get(0));
            radiusZ = Math.max(1.0, radii.get(1));
            break;
         default:
            actor.print(Caption.of("worldedit.cyl.invalid-radius"));
            return 0;
      }

      this.worldEdit.checkMaxRadius(radiusX, actor);
      this.worldEdit.checkMaxRadius(radiusZ, actor);
      this.worldEdit.checkMaxRadius(height, actor);
      if (!(thickness > radiusX) && !(thickness > radiusZ)) {
         BlockVector3 pos = session.getPlacementPosition(actor);
         int affected = editSession.makeCylinder(pos, pattern, radiusX, radiusZ, height, thickness, false);
         actor.print(Caption.of("worldedit.cyl.created", TextComponent.of(affected)));
         return affected;
      } else {
         actor.print(Caption.of("worldedit.hcyl.thickness-too-large"));
         return 0;
      }
   }

   @Command(
      name = "/cyl",
      desc = "Generates a cylinder."
   )
   @CommandPermissions({"worldedit.generation.cylinder"})
   @Logging(Logging.LogMode.PLACEMENT)
   @SynchronousSettingExpected
   public int cyl(
      Actor actor,
      LocalSession session,
      EditSession editSession,
      @Arg(desc = "The pattern of blocks to generate") Pattern pattern,
      @Radii(2) @Arg(desc = "The radii of the cylinder. 1st is N/S, 2nd is E/W") List<Double> radii,
      @Arg(desc = "The height of the cylinder",def = {"1"}) int height,
      @Switch(name = 'h',desc = "Make a hollow cylinder") boolean hollow
   ) throws WorldEditException {
      double radiusX;
      double radiusZ;
      switch (radii.size()) {
         case 1:
            radiusX = radiusZ = Math.max(1.0, radii.get(0));
            break;
         case 2:
            radiusX = Math.max(1.0, radii.get(0));
            radiusZ = Math.max(1.0, radii.get(1));
            break;
         default:
            actor.print(Caption.of("worldedit.cyl.invalid-radius"));
            return 0;
      }

      this.worldEdit.checkMaxRadius(radiusX, actor);
      this.worldEdit.checkMaxRadius(radiusZ, actor);
      this.worldEdit.checkMaxRadius(height, actor);
      BlockVector3 pos = session.getPlacementPosition(actor);
      int affected = editSession.makeCylinder(pos, pattern, radiusX, radiusZ, height, !hollow);
      if (actor instanceof Player && Settings.settings().GENERAL.UNSTUCK_ON_GENERATE) {
         ((Player)actor).findFreePosition();
      }

      actor.print(Caption.of("worldedit.cyl.created", TextComponent.of(affected)));
      return affected;
   }

   @Command(
      name = "/cone",
      desc = "Generates a cone."
   )
   @CommandPermissions({"worldedit.generation.cone"})
   @Logging(Logging.LogMode.PLACEMENT)
   @SynchronousSettingExpected
   public int cone(
      Actor actor,
      LocalSession session,
      EditSession editSession,
      @Arg(desc = "The pattern of blocks to generate") Pattern pattern,
      @Radii(2) @Arg(desc = "The radii of the cone. 1st is N/S, 2nd is E/W") List<Double> radii,
      @Arg(desc = "The height of the cone",def = {"1"}) int height,
      @Switch(name = 'h',desc = "Make a hollow cone") boolean hollow,
      @Arg(desc = "Thickness of the hollow cone",def = {"1"}) double thickness
   ) throws WorldEditException {
      double radiusX;
      double radiusZ;
      switch (radii.size()) {
         case 1:
            radiusX = radiusZ = Math.max(1.0, radii.get(0));
            break;
         case 2:
            radiusX = Math.max(1.0, radii.get(0));
            radiusZ = Math.max(1.0, radii.get(1));
            break;
         default:
            actor.printError(Caption.of("worldedit.cone.invalid-radius"));
            return 0;
      }

      this.worldEdit.checkMaxRadius(radiusX, actor);
      this.worldEdit.checkMaxRadius(radiusZ, actor);
      this.worldEdit.checkMaxRadius(height, actor);
      BlockVector3 pos = session.getPlacementPosition(actor);
      int affected = editSession.makeCone(pos, pattern, radiusX, radiusZ, height, !hollow, thickness);
      if (actor instanceof Player && Settings.settings().GENERAL.UNSTUCK_ON_GENERATE) {
         ((Player)actor).findFreePosition();
      }

      actor.printInfo(Caption.of("worldedit.cone.created", TextComponent.of(affected)));
      return affected;
   }

   @Command(
      name = "/hsphere",
      desc = "Generates a hollow sphere."
   )
   @CommandPermissions({"worldedit.generation.sphere"})
   @Logging(Logging.LogMode.PLACEMENT)
   @SynchronousSettingExpected
   public int hsphere(
      Actor actor,
      LocalSession session,
      EditSession editSession,
      @Arg(desc = "The pattern of blocks to generate") Pattern pattern,
      @Radii(3) @Arg(desc = "The radii of the sphere. Order is N/S, U/D, E/W") List<Double> radii,
      @Switch(name = 'r',desc = "Raise the bottom of the sphere to the placement position") boolean raised
   ) throws WorldEditException {
      return this.sphere(actor, session, editSession, pattern, radii, raised, true);
   }

   @Command(
      name = "/sphere",
      desc = "Generates a filled sphere."
   )
   @CommandPermissions({"worldedit.generation.sphere"})
   @Logging(Logging.LogMode.PLACEMENT)
   @SynchronousSettingExpected
   public int sphere(
      Actor actor,
      LocalSession session,
      EditSession editSession,
      @Arg(desc = "The pattern of blocks to generate") Pattern pattern,
      @Radii(3) @Arg(desc = "The radii of the sphere. Order is N/S, U/D, E/W") List<Double> radii,
      @Switch(name = 'r',desc = "Raise the bottom of the sphere to the placement position") boolean raised,
      @Switch(name = 'h',desc = "Make a hollow sphere") boolean hollow
   ) throws WorldEditException {
      double radiusX;
      double radiusY;
      double radiusZ;
      switch (radii.size()) {
         case 1:
            radiusX = radiusY = radiusZ = Math.max(0.0, radii.get(0));
            break;
         case 3:
            radiusX = Math.max(0.0, radii.get(0));
            radiusY = Math.max(0.0, radii.get(1));
            radiusZ = Math.max(0.0, radii.get(2));
            break;
         default:
            actor.print(Caption.of("worldedit.sphere.invalid-radius"));
            return 0;
      }

      this.worldEdit.checkMaxRadius(radiusX, actor);
      this.worldEdit.checkMaxRadius(radiusY, actor);
      this.worldEdit.checkMaxRadius(radiusZ, actor);
      BlockVector3 pos = session.getPlacementPosition(actor);
      if (raised) {
         pos = pos.add(0, (int)radiusY, 0);
      }

      int affected = editSession.makeSphere(pos, pattern, radiusX, radiusY, radiusZ, !hollow);
      if (actor instanceof Player && Settings.settings().GENERAL.UNSTUCK_ON_GENERATE) {
         ((Player)actor).findFreePosition();
      }

      actor.print(Caption.of("worldedit.sphere.created", TextComponent.of(affected)));
      return affected;
   }

   @Command(
      name = "forestgen",
      aliases = {"/forestgen"},
      desc = "Generate a forest"
   )
   @CommandPermissions({"worldedit.generation.forest"})
   @Logging(Logging.LogMode.POSITION)
   @SynchronousSettingExpected
   public int forestGen(
      Actor actor,
      LocalSession session,
      EditSession editSession,
      @Arg(desc = "The size of the forest, in blocks",def = {"10"}) int size,
      @Arg(desc = "The type of forest",def = {"tree"}) TreeType type,
      @Arg(desc = "The density of the forest, between 0 and 100",def = {"5"}) double density
   ) throws WorldEditException {
      CommandUtil.checkCommandArgument(0.0 <= density && density <= 100.0, "Density must be between 0 and 100");
      this.worldEdit.checkMaxRadius(size, actor);
      density /= 100.0;
      int affected = editSession.makeForest(session.getPlacementPosition(actor), size, density, type);
      actor.print(Caption.of("worldedit.forestgen.created", TextComponent.of(affected)));
      return affected;
   }

   @Command(
      name = "pumpkins",
      aliases = {"/pumpkins"},
      desc = "Generate pumpkin patches"
   )
   @CommandPermissions({"worldedit.generation.pumpkins"})
   @Logging(Logging.LogMode.POSITION)
   @SynchronousSettingExpected
   public int pumpkins(
      Actor actor,
      LocalSession session,
      EditSession editSession,
      @Arg(desc = "The size of the patch",def = {"10"}) int size,
      @Arg(desc = "//TODO ",def = {"0.02"}) double density
   ) throws WorldEditException {
      CommandUtil.checkCommandArgument(0.0 <= density && density <= 100.0, "Density must be between 0 and 100");
      this.worldEdit.checkMaxRadius(size, actor);
      int affected = editSession.makePumpkinPatches(session.getPlacementPosition(actor), size, density);
      actor.print(Caption.of("worldedit.pumpkins.created", TextComponent.of(affected)));
      return affected;
   }

   @Command(
      name = "/feature",
      aliases = {"/placefeature"},
      desc = "Generate Minecraft features"
   )
   @Logging(Logging.LogMode.PLACEMENT)
   @CommandPermissions({"worldedit.generation.feature"})
   public int feature(Actor actor, LocalSession session, EditSession editSession, @Arg(desc = "Type of feature to place") ConfiguredFeatureType feature) throws WorldEditException {
      int affected = editSession.generateFeature(feature, session.getPlacementPosition(actor));
      if (affected == 0) {
         actor.print(Caption.of("worldedit.generate.feature.failed"));
      } else {
         actor.print(Caption.of("worldedit.feature.created", TextComponent.of(affected)));
      }

      return affected;
   }

   @Command(
      name = "/structure",
      desc = "Generate Minecraft structures"
   )
   @CommandPermissions({"worldedit.generation.structure"})
   @Logging(Logging.LogMode.POSITION)
   public int structure(Actor actor, LocalSession session, EditSession editSession, @Arg(desc = "The structure") StructureType feature) throws WorldEditException {
      int affected = editSession.generateStructure(feature, session.getPlacementPosition(actor));
      if (affected > 0) {
         actor.printInfo(Caption.of("worldedit.structure.created", TextComponent.of(affected)));
      } else {
         actor.printError(Caption.of("worldedit.generate.structure.failed"));
      }

      return affected;
   }

   @Command(
      name = "/hpyramid",
      desc = "Generate a hollow pyramid"
   )
   @CommandPermissions({"worldedit.generation.pyramid"})
   @Logging(Logging.LogMode.PLACEMENT)
   @SynchronousSettingExpected
   public int hollowPyramid(
      Actor actor,
      LocalSession session,
      EditSession editSession,
      @Arg(desc = "The pattern of blocks to set") Pattern pattern,
      @Confirm(Confirm.Processor.RADIUS) @Arg(desc = "The size of the pyramid") int size
   ) throws WorldEditException {
      return this.pyramid(actor, session, editSession, pattern, size, true);
   }

   @Command(
      name = "/pyramid",
      desc = "Generate a filled pyramid"
   )
   @CommandPermissions({"worldedit.generation.pyramid"})
   @Logging(Logging.LogMode.PLACEMENT)
   @SynchronousSettingExpected
   public int pyramid(
      Actor actor,
      LocalSession session,
      EditSession editSession,
      @Arg(desc = "The pattern of blocks to set") Pattern pattern,
      @Confirm(Confirm.Processor.RADIUS) @Arg(desc = "The size of the pyramid") int size,
      @Switch(name = 'h',desc = "Make a hollow pyramid") boolean hollow
   ) throws WorldEditException {
      this.worldEdit.checkMaxRadius(size, actor);
      BlockVector3 pos = session.getPlacementPosition(actor);
      int affected = editSession.makePyramid(pos, pattern, size, !hollow);
      if (actor instanceof Player && Settings.settings().GENERAL.UNSTUCK_ON_GENERATE) {
         ((Player)actor).findFreePosition();
      }

      actor.print(Caption.of("worldedit.pyramid.created", TextComponent.of(affected)));
      return affected;
   }

   @Command(
      name = "/generate",
      aliases = {"/gen", "/g"},
      desc = "Generates a shape according to a formula.",
      descFooter = "For details, see https://ehub.to/we/expr"
   )
   @CommandPermissions({"worldedit.generation.shape"})
   @Logging(Logging.LogMode.ALL)
   @SynchronousSettingExpected
   @Confirm(Confirm.Processor.REGION)
   public int generate(
      Actor actor,
      LocalSession session,
      EditSession editSession,
      @Selection Region region,
      @Arg(desc = "The pattern of blocks to set") Pattern pattern,
      @Arg(desc = "Expression to test block placement locations and set block type",variable = true) List<String> expression,
      @Switch(name = 'h',desc = "Generate a hollow shape") boolean hollow,
      @Switch(name = 'r',desc = "Use the game's coordinate origin") boolean useRawCoords,
      @Switch(name = 'o',desc = "Use the placement's coordinate origin") boolean offset,
      @Switch(name = 'c',desc = "Use the selection's center as origin") boolean offsetCenter
   ) throws WorldEditException {
      Vector3 zero;
      Vector3 unit;
      if (useRawCoords) {
         zero = Vector3.ZERO;
         unit = Vector3.ONE;
      } else if (offset) {
         zero = session.getPlacementPosition(actor).toVector3();
         unit = Vector3.ONE;
      } else if (offsetCenter) {
         Vector3 min = region.getMinimumPoint().toVector3();
         Vector3 max = region.getMaximumPoint().toVector3();
         zero = max.add(min).multiply(0.5);
         unit = Vector3.ONE;
      } else {
         Vector3 min = region.getMinimumPoint().toVector3();
         Vector3 max = region.getMaximumPoint().toVector3();
         zero = max.add(min).multiply(0.5);
         unit = max.subtract(zero);
         if (unit.x() == 0.0) {
            unit = unit.withX(1.0);
         }

         if (unit.y() == 0.0) {
            unit = unit.withY(1.0);
         }

         if (unit.z() == 0.0) {
            unit = unit.withZ(1.0);
         }
      }

      Vector3 unit1 = unit;

      try {
         int affected = editSession.makeShape(region, zero, unit1, pattern, String.join(" ", expression), hollow, session.getTimeout());
         if (actor instanceof Player && Settings.settings().GENERAL.UNSTUCK_ON_GENERATE) {
            ((Player)actor).findFreePosition();
         }

         actor.print(Caption.of("worldedit.generate.created", TextComponent.of(affected)));
         return affected;
      } catch (ExpressionException var15) {
         actor.printError(TextComponent.of(var15.getMessage()));
         return 0;
      }
   }

   @Command(
      name = "/generatebiome",
      aliases = {"/genbiome", "/gb"},
      desc = "Sets biome according to a formula.",
      descFooter = "Formula must return positive numbers (true) if the point is inside the shape\nSets the biome of blocks in that shape.\nFor details, see https://ehub.to/we/expr"
   )
   @CommandPermissions({"worldedit.generation.shape.biome"})
   @Logging(Logging.LogMode.ALL)
   @Preload(Preload.PreloadCheck.PRELOAD)
   @SynchronousSettingExpected
   @Confirm(Confirm.Processor.REGION)
   public int generateBiome(
      Actor actor,
      LocalSession session,
      EditSession editSession,
      @Selection Region region,
      @Arg(desc = "The biome type to set") BiomeType target,
      @Arg(desc = "Expression to test block placement locations and set biome type. Requires x,y,z variables be present",variable = true) List<String> expression,
      @Switch(name = 'h',desc = "Generate a hollow shape") boolean hollow,
      @Switch(name = 'r',desc = "Use the game's coordinate origin") boolean useRawCoords,
      @Switch(name = 'o',desc = "Use the placement's coordinate origin") boolean offset,
      @Switch(name = 'c',desc = "Use the selection's center as origin") boolean offsetCenter
   ) throws WorldEditException {
      Vector3 zero;
      Vector3 unit;
      if (useRawCoords) {
         zero = Vector3.ZERO;
         unit = Vector3.ONE;
      } else if (offset) {
         zero = session.getPlacementPosition(actor).toVector3();
         unit = Vector3.ONE;
      } else if (offsetCenter) {
         Vector3 min = region.getMinimumPoint().toVector3();
         Vector3 max = region.getMaximumPoint().toVector3();
         zero = max.add(min).multiply(0.5);
         unit = Vector3.ONE;
      } else {
         Vector3 min = region.getMinimumPoint().toVector3();
         Vector3 max = region.getMaximumPoint().toVector3();
         zero = max.add(min).multiply(0.5);
         unit = max.subtract(zero);
         if (unit.x() == 0.0) {
            unit = unit.withX(1.0);
         }

         if (unit.y() == 0.0) {
            unit = unit.withY(1.0);
         }

         if (unit.z() == 0.0) {
            unit = unit.withZ(1.0);
         }
      }

      Vector3 unit1 = unit;

      try {
         int affected = editSession.makeBiomeShape(region, zero, unit1, target, String.join(" ", expression), hollow, session.getTimeout());
         actor.print(Caption.of("worldedit.generatebiome.changed", TextComponent.of(affected)));
         return affected;
      } catch (ExpressionException var15) {
         actor.printError(TextComponent.of(var15.getMessage()));
         return 0;
      }
   }

   @Command(
      name = "/caves",
      desc = "Generates a cave network"
   )
   @CommandPermissions({"worldedit.generation.caves"})
   @Logging(Logging.LogMode.PLACEMENT)
   @Preload(Preload.PreloadCheck.PRELOAD)
   @SynchronousSettingExpected
   @Confirm(Confirm.Processor.REGION)
   public void caves(
      Actor actor,
      LocalSession session,
      EditSession editSession,
      @Selection Region region,
      @Arg(name = "size",desc = "TODO",def = {"8"}) int sizeOpt,
      @Arg(name = "frequency",desc = "TODO",def = {"40"}) int frequencyOpt,
      @Arg(name = "rarity",desc = "TODO",def = {"7"}) int rarityOpt,
      @Arg(name = "minY",desc = "TODO",def = {"8"}) int minYOpt,
      @Arg(name = "maxY",desc = "TODO",def = {"127"}) int maxYOpt,
      @Arg(name = "systemFrequency",desc = "TODO",def = {"1"}) int systemFrequencyOpt,
      @Arg(name = "individualRarity",desc = "TODO",def = {"25"}) int individualRarityOpt,
      @Arg(name = "pocketChance",desc = "TODO",def = {"0"}) int pocketChanceOpt,
      @Arg(name = "pocketMin",desc = "TODO",def = {"0"}) int pocketMinOpt,
      @Arg(name = "pocketMax",desc = "TODO",def = {"3"}) int pocketMaxOpt
   ) throws WorldEditException {
      CavesGen gen = new CavesGen(
         sizeOpt, frequencyOpt, rarityOpt, minYOpt, maxYOpt, systemFrequencyOpt, individualRarityOpt, pocketChanceOpt, pocketMinOpt, pocketMaxOpt
      );
      editSession.generate(region, gen);
      actor.print(Caption.of("fawe.worldedit.visitor.visitor.block", editSession.getBlockChangeCount()));
   }

   @Command(
      name = "/ores",
      desc = "Generates ores"
   )
   @CommandPermissions({"worldedit.generation.ore"})
   @Logging(Logging.LogMode.PLACEMENT)
   @Preload(Preload.PreloadCheck.PRELOAD)
   @SynchronousSettingExpected
   @Confirm(Confirm.Processor.REGION)
   public void ores(
      Actor actor,
      LocalSession session,
      EditSession editSession,
      @Selection Region region,
      @Arg(desc = "Mask") Mask mask,
      @Switch(name = 'b',desc = "Make all ores deepslate equivalent for y<0") boolean deepslateBelowZero,
      @Switch(name = 'd',desc = "Make all ores deepslate equivalent when placed into deepslate, tuff, etc.") boolean deepslateWhereDeepslate
   ) throws WorldEditException {
      new MaskTraverser(mask).setNewExtent(editSession);
      editSession.addOres(region, mask, deepslateBelowZero, deepslateWhereDeepslate);
      actor.print(Caption.of("fawe.worldedit.visitor.visitor.block", editSession.getBlockChangeCount()));
   }

   @Command(
      name = "/img",
      aliases = {"/image", "image"},
      desc = "Generate an image"
   )
   @CommandPermissions({"worldedit.generation.image"})
   @SynchronousSettingExpected
   @Logging(Logging.LogMode.PLACEMENT)
   public void image(
      Actor actor,
      LocalSession session,
      EditSession editSession,
      @Arg(desc = "Image URL (imgur only)") String imageURL,
      @Arg(desc = "boolean",def = {"true"}) boolean randomize,
      @Arg(desc = "TODO",def = {"100"}) int threshold,
      @Arg(desc = "BlockVector2",def = {""}) BlockVector2 dimensions
   ) throws WorldEditException, IOException, URISyntaxException {
      TextureUtil tu = Fawe.instance().getCachedTextureUtil(randomize, 0, threshold);
      URL url = new URL(imageURL);
      MainUtil.checkImageHost(url.toURI());
      if (dimensions != null) {
         CommandUtil.checkCommandArgument(
            (long)dimensions.x() * dimensions.z() <= Settings.settings().WEB.MAX_IMAGE_SIZE,
            Caption.of("fawe.error.image-dimensions", Settings.settings().WEB.MAX_IMAGE_SIZE)
         );
      }

      ExecutorService executor = Executors.newSingleThreadExecutor();
      Future<BufferedImage> future = executor.submit(() -> {
         BufferedImage imagex = MainUtil.readImage(url);
         if (dimensions != null) {
            imagex = ImageUtil.getScaledInstance(imagex, dimensions.x(), dimensions.z(), RenderingHints.VALUE_INTERPOLATION_BILINEAR, false);
         }

         return imagex;
      });

      BufferedImage image;
      try {
         image = future.get(Settings.settings().WEB.MAX_IMAGE_LOAD_TIME, TimeUnit.SECONDS);
      } catch (TimeoutException | InterruptedException var18) {
         actor.printError(Caption.of("fawe.web.image.load.timeout", Settings.settings().WEB.MAX_IMAGE_LOAD_TIME));
         return;
      } catch (Throwable var19) {
         if (var19.getCause() instanceof FaweException faweException) {
            throw faweException;
         }

         throw new IOException(var19.getCause());
      }

      BlockVector3 pos1 = session.getPlacementPosition(actor);
      BlockVector3 pos2 = pos1.add(image.getWidth() - 1, 0, image.getHeight() - 1);
      CuboidRegion region = new CuboidRegion(pos1, pos2);
      RegionVisitor visitor = new RegionVisitor(region, pos -> {
         int x = pos.x() - pos1.x();
         int z = pos.z() - pos1.z();
         int color = image.getRGB(x, z);
         BlockType block = tu.getNearestBlock(color);
         return block != null ? editSession.setBlock(pos, block.getDefaultState()) : false;
      }, editSession);
      Operations.completeBlindly(visitor);
      actor.print(Caption.of("fawe.worldedit.visitor.visitor.block", editSession.getBlockChangeCount()));
   }

   @Command(
      name = "/ore",
      desc = "Generates ores"
   )
   @CommandPermissions({"worldedit.generation.ore"})
   @Logging(Logging.LogMode.PLACEMENT)
   @Preload(Preload.PreloadCheck.PRELOAD)
   @SynchronousSettingExpected
   @Confirm(Confirm.Processor.REGION)
   public void ore(
      Actor actor,
      LocalSession session,
      EditSession editSession,
      @Selection Region region,
      @Arg(desc = "Mask") Mask mask,
      @Arg(desc = "Pattern") Pattern material,
      @Arg(desc = "Ore vein size") @Range(from = 0L,to = 2147483647L) int size,
      @Arg(desc = "Ore vein frequency (number of times to attempt to place ore)",def = {"10"}) @Range(from = 0L,to = 2147483647L) int freq,
      @Arg(desc = "Ore vein rarity (% chance each attempt is placed)",def = {"100"}) @Range(from = 0L,to = 100L) int rarity,
      @Arg(desc = "Ore vein min y",def = {"0"}) int minY,
      @Arg(desc = "Ore vein max y",def = {"63"}) int maxY
   ) throws WorldEditException {
      new MaskTraverser(mask).setNewExtent(editSession);
      CommandUtil.checkCommandArgument(minY >= editSession.getMinY(), Caption.of("fawe.error.outside-range-lower", "miny", editSession.getMinY()));
      CommandUtil.checkCommandArgument(maxY <= editSession.getMaxY(), Caption.of("fawe.error.outside-range-upper", "maxy", editSession.getMaxY()));
      CommandUtil.checkCommandArgument(minY < maxY, Caption.of("fawe.error.argument-size-mismatch", "miny", "maxy"));
      editSession.addOre(region, mask, material, size, freq, rarity, minY, maxY);
      actor.print(Caption.of("fawe.worldedit.visitor.visitor.block", editSession.getBlockChangeCount()));
   }

   @Command(
      name = "/blob",
      aliases = {"/rock"},
      desc = "Creates a distorted sphere"
   )
   @Logging(Logging.LogMode.PLACEMENT)
   @SynchronousSettingExpected
   @CommandPermissions({"worldedit.generation.blob"})
   public int blob(
      Actor actor,
      LocalSession session,
      EditSession editSession,
      @Arg(desc = "Pattern") Pattern pattern,
      @Arg(desc = "size",def = {"5"}) double size,
      @Arg(desc = "radius",def = {"5"}) Vector3 radius,
      @Arg(name = "roundness",desc = "roundness",def = {"100"}) double sphericity,
      @Arg(desc = "double",def = {"30"}) double frequency,
      @Arg(desc = "double",def = {"50"}) double amplitude
   ) throws WorldEditException {
      double max = MathMan.max(radius.x(), radius.y(), radius.z());
      this.worldEdit.checkMaxRadius(max, actor);
      BlockVector3 pos = session.getPlacementPosition(actor);
      int affected = editSession.makeBlob(pos, pattern, size, frequency / 100.0, amplitude / 100.0, radius.divide(max), sphericity / 100.0);
      if (actor instanceof Player && Settings.settings().GENERAL.UNSTUCK_ON_GENERATE) {
         ((Player)actor).findFreePosition();
      }

      actor.print(Caption.of("worldedit.blob.created", TextComponent.of(affected)));
      return affected;
   }
}
