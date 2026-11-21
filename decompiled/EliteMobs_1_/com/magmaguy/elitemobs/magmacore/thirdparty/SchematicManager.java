package com.magmaguy.elitemobs.magmacore.thirdparty;

import com.magmaguy.elitemobs.magmacore.MagmaCore;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.WorkloadRunnable;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

public class SchematicManager {
   private static final Queue<SchematicManager.PasteBlockOperation> pasteQueue = new ConcurrentLinkedQueue<>();
   private static boolean erroredOnce = false;
   private static boolean isDistributedPasting = false;

   private SchematicManager() {
   }

   public static Clipboard load(File schematicFile) {
      ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);

      try {
         ClipboardReader reader = format.getReader(new FileInputStream(schematicFile));

         Clipboard clipboard;
         try {
            clipboard = reader.read();
         } catch (Throwable var7) {
            if (reader != null) {
               try {
                  reader.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (reader != null) {
            reader.close();
         }

         return clipboard;
      } catch (IOException var8) {
         var8.printStackTrace();
         return null;
      } catch (NoSuchElementException var9) {
         Logger.warn("Failed to get element from schematic " + schematicFile.getName());
         var9.printStackTrace();
         return null;
      } catch (Exception var10) {
         Logger.warn(
            "Failed to load schematic "
               + schematicFile.getName()
               + " ! 99% of the time, this is because you are not using the correct WorldEdit version for your Minecraft server. You should be downloading WorldEdit from here https://dev.bukkit.org/projects/worldedit . You can check which versions the download links are compatible with by hovering over them."
         );
         if (!erroredOnce) {
            var10.printStackTrace();
         } else {
            Logger.warn("Hiding stacktrace for this error, as it has already been printed once");
         }

         erroredOnce = true;
         return null;
      }
   }

   public static void paste(Clipboard clipboard, Location location) {
      World world = BukkitAdapter.adapt(location.getWorld());

      try {
         EditSession editSession = WorldEdit.getInstance().newEditSession(world);

         try {
            Operation operation = new ClipboardHolder(clipboard)
               .createPaste(editSession)
               .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
               .build();
            Operations.complete(operation);
         } catch (Throwable var7) {
            if (editSession != null) {
               try {
                  editSession.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (editSession != null) {
            editSession.close();
         }
      } catch (WorldEditException var8) {
         throw new RuntimeException(var8);
      }
   }

   private static List<SchematicManager.PasteBlock> createPasteBlocks(
      Clipboard schematicClipboard, Location location, Vector schematicOffset, Function<Boolean, Material> pedestalMaterialProvider, boolean pasteAir
   ) {
      List<SchematicManager.PasteBlock> pasteBlocks = new ArrayList<>();
      Location adjustedLocation = location.clone().add(schematicOffset);

      for (int x = 0; x < schematicClipboard.getDimensions().x(); x++) {
         for (int y = 0; y < schematicClipboard.getDimensions().y(); y++) {
            for (int z = 0; z < schematicClipboard.getDimensions().z(); z++) {
               BlockVector3 adjustedClipboardLocation = BlockVector3.at(
                  x + schematicClipboard.getMinimumPoint().x(), y + schematicClipboard.getMinimumPoint().y(), z + schematicClipboard.getMinimumPoint().z()
               );
               BaseBlock baseBlock = schematicClipboard.getFullBlock(adjustedClipboardLocation);
               BlockState blockState = baseBlock.toImmutableState();
               BlockData blockData = Bukkit.createBlockData(baseBlock.toImmutableState().getAsString());
               Material material = BukkitAdapter.adapt(baseBlock.getBlockType());
               Block worldBlock = adjustedLocation.clone().add(new Vector(x, y, z)).getBlock();
               String materialString = material.toString().toUpperCase(Locale.ROOT);
               boolean isGround = !BukkitAdapter.adapt(
                     schematicClipboard.getBlock(
                           BlockVector3.at(adjustedClipboardLocation.x(), adjustedClipboardLocation.y() + 1, adjustedClipboardLocation.z())
                        )
                        .getBlockType()
                  )
                  .isSolid();
               if (material != Material.BARRIER && (material != Material.AIR || pasteAir)) {
                  if (materialString.endsWith("SIGN")
                     || materialString.endsWith("STAIRS")
                     || materialString.endsWith("BOX")
                     || materialString.endsWith("CHEST_BOAT")
                     || materialString.equals("BEACON")
                     || materialString.endsWith("FURNACE")
                     || materialString.equals("CALIBRATED_SCULK_SENSOR")
                     || materialString.equals("CAMPFIRE")
                     || materialString.equals("CARTOGRAPHY_TABLE")
                     || materialString.equals("CAULDRON")
                     || materialString.contains("COMMAND_BLOCK")
                     || materialString.endsWith("ANVIL")
                     || materialString.equals("CRAFTER")
                     || materialString.equals("ITEM_FRAME")
                     || materialString.equals("DISPENSER")
                     || materialString.equals("DROPPER")
                     || materialString.equals("ENCHANTING_TABLE")
                     || materialString.equals("BARREL")
                     || materialString.equals("CHEST")
                     || materialString.equals("ENDER_CHEST")
                     || materialString.equals("TRAPPED_CHEST")
                     || materialString.equals("FLETCHING_TABLE")
                     || materialString.equals("FURNACE_MINECART")
                     || materialString.equals("GRINDSTONE")
                     || materialString.equals("HOPPER")
                     || materialString.equals("HOPPER_MINECART")
                     || materialString.equals("JUKEBOX")
                     || materialString.equals("LEVER")
                     || materialString.equals("LOOM")
                     || materialString.equals("LODESTONE")
                     || materialString.startsWith("POTTED")
                     || materialString.startsWith("SCULK")
                     || materialString.equals("POWERED_RAIL")
                     || materialString.equals("SMOKER")
                     || materialString.equals("STONECUTTER")
                     || materialString.equals("SOUL_CAMPFIRE")
                     || materialString.contains("SPAWNER")) {
                     pasteBlocks.add(new SchematicManager.PasteBlock(worldBlock, null, createSingleBlockClipboard(adjustedLocation, baseBlock, blockState)));
                  } else if (material == Material.BEDROCK) {
                     if (!worldBlock.getType().isSolid()) {
                        Material pedestalMaterial = pedestalMaterialProvider.apply(isGround);
                        worldBlock.setType(pedestalMaterial);
                        pasteBlocks.add(new SchematicManager.PasteBlock(worldBlock, pedestalMaterial.createBlockData(), null));
                     }
                  } else {
                     pasteBlocks.add(new SchematicManager.PasteBlock(worldBlock, blockData, null));
                  }
               }
            }
         }
      }

      return pasteBlocks;
   }

   private static Clipboard createSingleBlockClipboard(Location location, final BaseBlock baseBlock, final BlockState blockState) {
      return new Clipboard() {
         public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) throws WorldEditException {
            return false;
         }

         public Operation commit() {
            return null;
         }

         public BlockState getBlock(BlockVector3 position) {
            return blockState;
         }

         public BaseBlock getFullBlock(BlockVector3 position) {
            return baseBlock;
         }

         public BlockVector3 getMinimumPoint() {
            return BlockVector3.at(0, 0, 0);
         }

         public BlockVector3 getMaximumPoint() {
            return BlockVector3.at(0, 0, 0);
         }

         public List<? extends Entity> getEntities(Region region) {
            return new ArrayList<>();
         }

         public List<? extends Entity> getEntities() {
            return new ArrayList<>();
         }

         public Entity createEntity(com.sk89q.worldedit.util.Location location, BaseEntity entity) {
            return null;
         }

         public Region getRegion() {
            return new CuboidRegion(BlockVector3.at(0, 0, 0), BlockVector3.at(0, 0, 0));
         }

         public BlockVector3 getDimensions() {
            return BlockVector3.at(1, 1, 1);
         }

         public BlockVector3 getOrigin() {
            return BlockVector3.at(0, 0, 0);
         }

         public void setOrigin(BlockVector3 origin) {
         }
      };
   }

   public static void pasteFileDistributedWithPedestal(
      File schematicFile,
      Location location,
      Vector schematicOffset,
      Function<Boolean, Material> pedestalMaterialProvider,
      Runnable onComplete,
      double percentageOfTickUsedForPasting,
      boolean randomizeRotation,
      boolean pasteAir
   ) {
      Clipboard clipboard = load(schematicFile);
      if (clipboard == null) {
         throw new IllegalStateException("Could not load schematic " + schematicFile);
      } else {
         if (randomizeRotation) {
            double rotateY = new Random().nextInt(4) * 90 - 90;
            if (rotateY < 0.0) {
               rotateY = 270.0;
            }

            try {
               clipboard = clipboard.transform(new AffineTransform().rotateY(rotateY));
            } catch (WorldEditException var13) {
               throw new RuntimeException(var13);
            }
         }

         List<SchematicManager.PasteBlock> pasteBlocks = createPasteBlocks(clipboard, location, schematicOffset, pedestalMaterialProvider, pasteAir);
         pasteDistributed(pasteBlocks, location, onComplete, percentageOfTickUsedForPasting);
      }
   }

   public static void pasteDistributedWithPedestal(
      Clipboard schematicClipboard,
      Location location,
      Vector schematicOffset,
      Function<Boolean, Material> pedestalMaterialProvider,
      Runnable onComplete,
      double percentageOfTickUsedForPasting,
      boolean pasteAir
   ) {
      List<SchematicManager.PasteBlock> pasteBlocks = createPasteBlocks(schematicClipboard, location, schematicOffset, pedestalMaterialProvider, pasteAir);
      pasteDistributed(pasteBlocks, location, onComplete, percentageOfTickUsedForPasting);
   }

   public static void pasteDistributed(
      List<SchematicManager.PasteBlock> pasteBlocks, Location location, Runnable onComplete, double percentageOfTickUsedForPasting
   ) {
      pasteQueue.add(new SchematicManager.PasteBlockOperation(pasteBlocks, location, onComplete));
      if (!isDistributedPasting) {
         processNextPaste(percentageOfTickUsedForPasting);
      }
   }

   private static void processNextPaste(double percentageOfTickUsedForPasting) {
      if (pasteQueue.isEmpty()) {
         isDistributedPasting = false;
      } else {
         isDistributedPasting = true;
         SchematicManager.PasteBlockOperation operation = pasteQueue.poll();
         WorkloadRunnable workload = new WorkloadRunnable(percentageOfTickUsedForPasting, () -> {
            if (operation.onComplete != null) {
               operation.onComplete.run();
            }

            processNextPaste(percentageOfTickUsedForPasting);
         });

         for (SchematicManager.PasteBlock pasteBlock : operation.blocks) {
            workload.addWorkload(
               () -> {
                  if (pasteBlock.blockData() != null) {
                     pasteBlock.block().setBlockData(pasteBlock.blockData());
                  } else if (pasteBlock.clipboard() != null) {
                     try {
                        EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(pasteBlock.block().getLocation().getWorld()));

                        try {
                           Operation worldeditPaste = new ClipboardHolder(pasteBlock.clipboard())
                              .createPaste(editSession)
                              .to(BlockVector3.at(pasteBlock.block().getX(), pasteBlock.block().getY(), pasteBlock.block().getZ()))
                              .build();
                           Operations.complete(worldeditPaste);
                        } catch (Throwable var5x) {
                           if (editSession != null) {
                              try {
                                 editSession.close();
                              } catch (Throwable var4) {
                                 var5x.addSuppressed(var4);
                              }
                           }

                           throw var5x;
                        }

                        if (editSession != null) {
                           editSession.close();
                        }
                     } catch (WorldEditException var6) {
                        throw new RuntimeException(var6);
                     }
                  }
               }
            );
         }

         workload.runTaskTimer(MagmaCore.getInstance().getRequestingPlugin(), 0L, 1L);
      }
   }

   public record PasteBlock(Block block, BlockData blockData, Clipboard clipboard) {
   }

   private record PasteBlockOperation(List<SchematicManager.PasteBlock> blocks, Location location, Runnable onComplete) {
   }
}
