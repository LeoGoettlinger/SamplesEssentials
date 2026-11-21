package ac.grim.grimac.checks.impl.scaffolding;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.verbose.VerboseCodecs;
import ac.grim.grimac.checks.type.BlockPlaceCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3f;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3i;
import ac.grim.grimac.utils.anticheat.update.BlockPlace;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;
import java.util.ArrayList;
import java.util.List;

@CheckData(
   name = "MultiPlace",
   stableKey = "grim.scaffolding.multi_place",
   description = "Placed multiple blocks in a tick",
   experimental = true
)
public class MultiPlace extends BlockPlaceCheck {
   private static final Verbose V = Verbose.of("face={face}, lastFace={face}, cursor={cursor}, lastCursor={cursor}, pos={mcpos}, lastPos={mcpos}");
   private final List<MultiPlace.FlagData> flags = new ArrayList<>();
   private boolean hasPlaced;
   private BlockFace lastFace;
   private Vector3f lastCursor;
   private Vector3i lastPos;

   public MultiPlace(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onBlockPlace(BlockPlace place) {
      BlockFace face = place.getFace();
      Vector3f cursor = place.cursor;
      Vector3i pos = place.position;
      if (this.hasPlaced && (face != this.lastFace || !cursor.equals(this.lastCursor) || !pos.equals(this.lastPos))) {
         int faceId = VerboseCodecs.enumId(face);
         int lastFaceId = VerboseCodecs.enumId(this.lastFace);
         if (!this.player.canSkipTicks()) {
            Verbose.Writer buf = V.write(this.verbose())
               .uint(faceId)
               .uint(lastFaceId)
               .cursor(cursor.x, cursor.y, cursor.z)
               .cursor(this.lastCursor.x, this.lastCursor.y, this.lastCursor.z)
               .mcPos(pos.x, pos.y, pos.z)
               .mcPos(this.lastPos.x, this.lastPos.y, this.lastPos.z);
            if (this.flag(buf) && this.shouldModifyPackets() && this.shouldCancel()) {
               place.resync();
            }
         } else {
            this.flags.add(new MultiPlace.FlagData(faceId, lastFaceId, cursor, this.lastCursor, pos, this.lastPos));
         }
      }

      this.lastFace = face;
      this.lastCursor = cursor;
      this.lastPos = pos;
      this.hasPlaced = true;
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (!this.player.cameraEntity.isSelf() || this.isTickPacket(event.getPacketType())) {
         this.hasPlaced = false;
      }
   }

   @Override
   public void onPredictionComplete(PredictionComplete predictionComplete) {
      if (this.player.canSkipTicks()) {
         if (this.player.isTickingReliablyFor(3)) {
            for (MultiPlace.FlagData data : this.flags) {
               this.flag(
                  V.write(this.verbose())
                     .uint(data.face())
                     .uint(data.lastFace())
                     .cursor(data.cursor().x, data.cursor().y, data.cursor().z)
                     .cursor(data.lastCursor().x, data.lastCursor().y, data.lastCursor().z)
                     .mcPos(data.pos().x, data.pos().y, data.pos().z)
                     .mcPos(data.lastPos().x, data.lastPos().y, data.lastPos().z)
               );
            }
         }

         this.flags.clear();
      }
   }

   private record FlagData(int face, int lastFace, Vector3f cursor, Vector3f lastCursor, Vector3i pos, Vector3i lastPos) {
   }
}
