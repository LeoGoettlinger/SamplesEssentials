package ac.grim.grimac.platform.bukkit.events;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.platform.bukkit.utils.convert.BukkitConversionUtils;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.PistonTemplate;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

public class PistonEvent implements Listener {
   private final Material SLIME_BLOCK = Material.getMaterial("SLIME_BLOCK");
   private final Material HONEY_BLOCK = Material.getMaterial("HONEY_BLOCK");
   private static final double MAX_HORIZONTAL_DISTANCE = 24.0;
   private static final double MAX_VERTICAL_DISTANCE = 64.0;

   private static boolean isCloseEnough(int x, int y, int z, double posX, double posY, double posZ) {
      return Math.abs(x - posX) <= 24.0 && Math.abs(y - posY) <= 64.0 && Math.abs(z - posZ) <= 24.0;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPistonPushEvent(BlockPistonExtendEvent event) {
      boolean hasSlimeBlock = false;
      boolean hasHoneyBlock = false;
      List<SimpleCollisionBox> boxes = new ArrayList<>();

      for (Block block : event.getBlocks()) {
         boxes.add(new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true).offset((double)block.getX(), (double)block.getY(), (double)block.getZ()));
         boxes.add(
            new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true)
               .offset(
                  (double)(block.getX() + event.getDirection().getModX()),
                  (double)(block.getY() + event.getDirection().getModY()),
                  (double)(block.getZ() + event.getDirection().getModZ())
               )
         );
         if (block.getType() == this.SLIME_BLOCK) {
            hasSlimeBlock = true;
         }

         if (block.getType() == this.HONEY_BLOCK) {
            hasHoneyBlock = true;
         }
      }

      Block piston = event.getBlock();
      boxes.add(
         new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true)
            .offset(
               (double)(piston.getX() + event.getDirection().getModX()),
               (double)(piston.getY() + event.getDirection().getModY()),
               (double)(piston.getZ() + event.getDirection().getModZ())
            )
      );
      PistonTemplate data = new PistonTemplate(BukkitConversionUtils.fromBukkitFace(event.getDirection()), boxes, true, hasSlimeBlock, hasHoneyBlock);
      this.addPistonData(data, piston.getX(), piston.getY(), piston.getZ());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPistonRetractEvent(BlockPistonRetractEvent event) {
      boolean hasSlimeBlock = false;
      boolean hasHoneyBlock = false;
      List<SimpleCollisionBox> boxes = new ArrayList<>();
      BlockFace face = BukkitConversionUtils.fromBukkitFace(event.getDirection());
      Block piston = event.getBlock();
      if (event.getBlocks().isEmpty()) {
         boxes.add(
            new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true)
               .offset((double)(piston.getX() + face.getModX()), (double)(piston.getY() + face.getModY()), (double)(piston.getZ() + face.getModZ()))
         );
      }

      for (Block block : event.getBlocks()) {
         boxes.add(new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true).offset((double)block.getX(), (double)block.getY(), (double)block.getZ()));
         boxes.add(
            new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true)
               .offset((double)(block.getX() + face.getModX()), (double)(block.getY() + face.getModY()), (double)(block.getZ() + face.getModZ()))
         );
         if (block.getType() == this.SLIME_BLOCK) {
            hasSlimeBlock = true;
         }

         if (block.getType() == this.HONEY_BLOCK) {
            hasHoneyBlock = true;
         }
      }

      PistonTemplate data = new PistonTemplate(face, boxes, false, hasSlimeBlock, hasHoneyBlock);
      this.addPistonData(data, piston.getX(), piston.getY(), piston.getZ());
   }

   private void addPistonData(PistonTemplate pistonTemplate, int blockX, int blockY, int blockZ) {
      for (GrimPlayer player : GrimAPI.INSTANCE.getPlayerDataManager().getEntries()) {
         if (player.compensatedWorld.isChunkLoaded(blockX >> 4, blockZ >> 4) && isCloseEnough(blockX, blockY, blockZ, player.x, player.y, player.z)) {
            int lastTrans = player.lastTransactionSent.get();
            player.latencyUtils.addRealTimeTaskAsync(lastTrans, () -> player.compensatedWorld.addPiston(pistonTemplate, lastTrans));
         }
      }
   }
}
