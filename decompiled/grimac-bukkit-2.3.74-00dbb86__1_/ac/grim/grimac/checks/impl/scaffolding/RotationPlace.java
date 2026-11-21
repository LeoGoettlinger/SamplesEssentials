package ac.grim.grimac.checks.impl.scaffolding;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.BlockPlaceCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attributes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3d;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3f;
import ac.grim.grimac.utils.anticheat.update.BlockPlace;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.Pair;
import ac.grim.grimac.utils.nmsutil.Ray;
import ac.grim.grimac.utils.nmsutil.ReachUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CheckData(
   name = "RotationPlace",
   stableKey = "grim.scaffolding.rotation_place",
   description = "Placed a block while not looking at it"
)
public class RotationPlace extends BlockPlaceCheck {
   private static final Verbose V = Verbose.of("[pre-flying|post-flying]");
   private double flagBuffer = 0.0;
   private boolean ignorePost = false;

   public RotationPlace(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onBlockPlace(BlockPlace place) {
      if (place.material != StateTypes.SCAFFOLDING) {
         if (this.player.cameraEntity.isSelf()) {
            if (!this.player.inVehicle()) {
               if (this.flagBuffer > 0.0 && !this.didRayTraceHit(place)) {
                  this.ignorePost = true;
                  if (this.flag(V.write(this.verbose()).bool(true)) && this.shouldModifyPackets() && this.shouldCancel()) {
                     place.resync();
                  }
               }
            }
         }
      }
   }

   @Override
   public void onPostFlyingBlockPlace(BlockPlace place) {
      if (place.material != StateTypes.SCAFFOLDING) {
         if (this.player.cameraEntity.isSelf()) {
            if (!this.player.inVehicle()) {
               if (this.ignorePost) {
                  this.ignorePost = false;
               } else {
                  boolean hit = this.didRayTraceHit(place);
                  if (!hit) {
                     this.flagBuffer = 1.0;
                     this.flag(V.write(this.verbose()).bool(false));
                  } else {
                     this.flagBuffer = Math.max(0.0, this.flagBuffer - 0.1);
                  }
               }
            }
         }
      }
   }

   private boolean didRayTraceHit(BlockPlace place) {
      SimpleCollisionBox box = new SimpleCollisionBox(place.position);
      List<Vector3f> possibleLookDirs = new ArrayList<>(
         Arrays.asList(new Vector3f(this.player.yaw, this.player.pitch, 0.0F), new Vector3f(this.player.lastYaw, this.player.pitch, 0.0F))
      );
      double[] possibleEyeHeights = this.player.getPossibleEyeHeights();
      double minEyeHeight = Double.MAX_VALUE;
      double maxEyeHeight = Double.MIN_VALUE;

      for (double height : possibleEyeHeights) {
         minEyeHeight = Math.min(minEyeHeight, height);
         maxEyeHeight = Math.max(maxEyeHeight, height);
      }

      SimpleCollisionBox eyePositions = new SimpleCollisionBox(
         this.player.x, this.player.y + minEyeHeight, this.player.z, this.player.x, this.player.y + maxEyeHeight, this.player.z
      );
      eyePositions.expand(this.player.getMovementThreshold());
      if (eyePositions.isIntersected(box)) {
         return true;
      } else {
         if (this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
            possibleLookDirs.add(new Vector3f(this.player.lastYaw, this.player.lastPitch, 0.0F));
         }

         if (this.player.getClientVersion().isOlderThan(ClientVersion.V_1_8)) {
            possibleLookDirs = Collections.singletonList(new Vector3f(this.player.yaw, this.player.pitch, 0.0F));
         }

         double distance = this.player.compensatedEntities.self.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);

         for (double d : possibleEyeHeights) {
            for (Vector3f lookDir : possibleLookDirs) {
               Vector3d starting = new Vector3d(this.player.x, this.player.y + d, this.player.z);
               Ray trace = new Ray(this.player, starting.getX(), starting.getY(), starting.getZ(), lookDir.getX(), lookDir.getY());
               Pair<Vector3d, BlockFace> intercept = ReachUtils.calculateIntercept(box, trace.origin(), trace.getPointAtDistance(distance));
               if (intercept.first() != null) {
                  return true;
               }
            }
         }

         return false;
      }
   }
}
