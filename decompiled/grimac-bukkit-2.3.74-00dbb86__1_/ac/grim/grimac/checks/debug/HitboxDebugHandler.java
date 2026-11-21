package ac.grim.grimac.checks.debug;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import ac.grim.grimac.utils.collisions.datatypes.CollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.NoCollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.Pair;
import ac.grim.grimac.utils.math.Vector3dm;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArraySet;

public class HitboxDebugHandler extends AbstractDebugHandler {
   private final Set<GrimPlayer> listeners = new CopyOnWriteArraySet<>(new HashSet<>());

   public HitboxDebugHandler(GrimPlayer grimPlayer) {
      super(grimPlayer);
   }

   @Override
   public boolean toggleListener(GrimPlayer player) {
      boolean wasPresent = this.listeners.remove(player);
      if (wasPresent) {
         return false;
      } else {
         this.listeners.add(player);
         return true;
      }
   }

   @Override
   public boolean toggleConsoleOutput() {
      throw new UnsupportedOperationException();
   }

   public void sendHitboxData(
      Map<Integer, CollisionBox> hitboxes,
      Set<Integer> targetEntities,
      List<Pair<Vector3dm, Double>> lookVecsAndEyeHeights,
      double baseX,
      double baseY,
      double baseZ,
      boolean isPrediction,
      double reachDistance
   ) {
      if (this.isEnabled()) {
         ByteBuf buffer = Unpooled.buffer();

         try {
            buffer.writeByte(1);
            byte global_flags = 0;
            global_flags = (byte)(global_flags | (isPrediction ? 1 : 0));
            buffer.writeByte(global_flags);
            buffer.writeDouble(reachDistance);
            this.writeVector(buffer, baseX, baseY, baseZ);
            ByteBufHelper.writeVarInt(buffer, lookVecsAndEyeHeights.size());

            for (Pair<Vector3dm, Double> pair : lookVecsAndEyeHeights) {
               Vector3dm lookVec = pair.first();
               double eyeHeight = pair.second();
               buffer.writeDouble(eyeHeight);
               this.writeVector(buffer, lookVec.getX(), lookVec.getY(), lookVec.getZ());
            }

            ByteBufHelper.writeVarInt(buffer, hitboxes.size());

            for (Entry<Integer, CollisionBox> entry : hitboxes.entrySet()) {
               int entityId = entry.getKey();
               CollisionBox box = entry.getValue();
               ByteBufHelper.writeVarInt(buffer, entityId);
               byte flags = 0;
               flags = (byte)(flags | (targetEntities.contains(entityId) ? 1 : 0));
               flags = (byte)(flags | (box == NoCollisionBox.INSTANCE ? 2 : 0));
               buffer.writeByte(flags);
               if ((flags & 2) == 0) {
                  SimpleCollisionBox simpleCollisionBox = (SimpleCollisionBox)box;
                  buffer.writeDouble(simpleCollisionBox.minX);
                  buffer.writeDouble(simpleCollisionBox.minY);
                  buffer.writeDouble(simpleCollisionBox.minZ);
                  buffer.writeDouble(simpleCollisionBox.maxX);
                  buffer.writeDouble(simpleCollisionBox.maxY);
                  buffer.writeDouble(simpleCollisionBox.maxZ);
               }
            }

            byte[] data = new byte[buffer.readableBytes()];
            buffer.readBytes(data);
            WrapperPlayServerPluginMessage packet = new WrapperPlayServerPluginMessage("grim:debug_hitbox", data);

            for (GrimPlayer listener : this.listeners) {
               if (listener != null) {
                  listener.user.sendPacket((PacketWrapper<?>)packet);
               }
            }
         } finally {
            buffer.release();
         }
      }
   }

   @Override
   public boolean isEnabled() {
      return !this.listeners.isEmpty();
   }

   private void writeVector(ByteBuf buffer, double x, double y, double z) {
      buffer.writeDouble(x);
      buffer.writeDouble(y);
      buffer.writeDouble(z);
   }
}
