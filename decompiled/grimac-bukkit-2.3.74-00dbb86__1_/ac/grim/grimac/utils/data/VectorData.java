package ac.grim.grimac.utils.data;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.math.Vector3dm;
import java.util.Objects;
import lombok.Generated;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

public class VectorData {
   public final VectorData.VectorType vectorType;
   public VectorData lastVector;
   public VectorData preUncertainty;
   public Vector3dm vector;
   @MonotonicNonNull
   public Vector3dm input;
   private boolean isKnockback;
   private boolean firstBreadKb;
   private boolean isExplosion;
   private boolean firstBreadExplosion;
   private boolean isTrident;
   private boolean isZeroPointZeroThree;
   private boolean isSwimHop;
   private boolean isFlipSneaking;
   private boolean isFlipItem;
   private boolean isJump;
   private boolean isAttackSlow = false;
   private boolean isWithInput = false;

   public VectorData(Vector3dm vector, VectorData lastVector, VectorData.VectorType vectorType) {
      this.vector = vector;
      this.lastVector = lastVector;
      this.vectorType = vectorType;
      if (lastVector != null) {
         this.isKnockback = lastVector.isKnockback;
         this.firstBreadKb = lastVector.firstBreadKb;
         this.isExplosion = lastVector.isExplosion;
         this.firstBreadExplosion = lastVector.firstBreadExplosion;
         this.isTrident = lastVector.isTrident;
         this.isZeroPointZeroThree = lastVector.isZeroPointZeroThree;
         this.isSwimHop = lastVector.isSwimHop;
         this.isFlipSneaking = lastVector.isFlipSneaking;
         this.isFlipItem = lastVector.isFlipItem;
         this.isJump = lastVector.isJump;
         this.preUncertainty = lastVector.preUncertainty;
         this.isAttackSlow = lastVector.isAttackSlow;
         this.input = lastVector.input;
         this.isWithInput = lastVector.isWithInput;
      }

      this.addVectorType(vectorType);
   }

   public VectorData(Vector3dm vector, VectorData.VectorType vectorType) {
      this.vector = vector;
      this.vectorType = vectorType;
      this.addVectorType(vectorType);
   }

   public VectorData returnNewModified(VectorData.VectorType type) {
      return new VectorData(this.vector, this, type);
   }

   public VectorData returnNewModified(Vector3dm newVec, VectorData.VectorType type) {
      return new VectorData(newVec, this, type);
   }

   public boolean isSetbackKb(GrimPlayer player) {
      if (!this.isKnockback) {
         return false;
      } else {
         VelocityData bread = this.firstBreadKb ? player.firstBreadKB : player.likelyKB;
         return bread != null && bread.isSetback;
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         VectorData that = (VectorData)o;
         return this.isKnockback == that.isKnockback
            && this.firstBreadKb == that.firstBreadKb
            && this.isExplosion == that.isExplosion
            && this.firstBreadExplosion == that.firstBreadExplosion
            && this.isTrident == that.isTrident
            && this.isZeroPointZeroThree == that.isZeroPointZeroThree
            && this.isSwimHop == that.isSwimHop
            && this.isFlipSneaking == that.isFlipSneaking
            && this.isFlipItem == that.isFlipItem
            && this.isJump == that.isJump
            && this.isAttackSlow == that.isAttackSlow
            && this.vectorType == that.vectorType
            && Objects.equals(this.lastVector, that.lastVector)
            && Objects.equals(this.preUncertainty, that.preUncertainty)
            && Objects.equals(this.vector, that.vector);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.vectorType,
         this.lastVector,
         this.preUncertainty,
         this.vector,
         this.isKnockback,
         this.firstBreadKb,
         this.isExplosion,
         this.firstBreadExplosion,
         this.isTrident,
         this.isZeroPointZeroThree,
         this.isSwimHop,
         this.isFlipSneaking,
         this.isFlipItem,
         this.isJump,
         this.isAttackSlow
      );
   }

   public void addVectorType(VectorData.VectorType type) {
      switch (type) {
         case Swimhop:
            this.isSwimHop = true;
         case Climbable:
         case HackyClimbable:
         case Teleport:
         case SkippedTicks:
         case InputResult:
         case StuckMultiplier:
         case Spectator:
         case Dead:
         case SurfaceSwimming:
         case SwimmingSpace:
         case BestVelPicked:
         case Firework:
         case Lenience:
         case TridentJump:
         case SlimePistonBounce:
         default:
            break;
         case Knockback:
            this.isKnockback = true;
            break;
         case FirstBreadKnockback:
            this.firstBreadKb = true;
            break;
         case Explosion:
            this.isExplosion = true;
            break;
         case FirstBreadExplosion:
            this.firstBreadExplosion = true;
            break;
         case WithInput:
            this.isWithInput = true;
            break;
         case Jump:
            this.isJump = true;
            break;
         case Trident:
            this.isTrident = true;
            break;
         case ZeroPointZeroThree:
            this.isZeroPointZeroThree = true;
            break;
         case AttackSlow:
            this.isAttackSlow = true;
            break;
         case Flip_Sneaking:
            this.isFlipSneaking = true;
            break;
         case Flip_Use_Item:
            this.isFlipItem = true;
      }
   }

   @Override
   public String toString() {
      return "VectorData{pointThree=" + this.isZeroPointZeroThree + ", vector=" + this.vector + "}";
   }

   @Generated
   public boolean isKnockback() {
      return this.isKnockback;
   }

   @Generated
   public boolean isFirstBreadKb() {
      return this.firstBreadKb;
   }

   @Generated
   public boolean isExplosion() {
      return this.isExplosion;
   }

   @Generated
   public boolean isFirstBreadExplosion() {
      return this.firstBreadExplosion;
   }

   @Generated
   public boolean isTrident() {
      return this.isTrident;
   }

   @Generated
   public boolean isZeroPointZeroThree() {
      return this.isZeroPointZeroThree;
   }

   @Generated
   public boolean isSwimHop() {
      return this.isSwimHop;
   }

   @Generated
   public boolean isFlipSneaking() {
      return this.isFlipSneaking;
   }

   @Generated
   public boolean isFlipItem() {
      return this.isFlipItem;
   }

   @Generated
   public boolean isJump() {
      return this.isJump;
   }

   @Generated
   public boolean isAttackSlow() {
      return this.isAttackSlow;
   }

   @Generated
   public boolean isWithInput() {
      return this.isWithInput;
   }

   public static final class MoveVectorData extends VectorData {
      public int x;
      public int z;

      public MoveVectorData(Vector3dm vector, VectorData lastVector, VectorData.VectorType vectorType, int x, int z) {
         super(vector, lastVector, vectorType);
         this.x = x;
         this.z = z;
         if (x != 0 || z != 0) {
            this.addVectorType(VectorData.VectorType.WithInput);
         }
      }

      public MoveVectorData(Vector3dm vector, VectorData.VectorType vectorType, int x, int z) {
         super(vector, vectorType);
         this.x = x;
         this.z = z;
         if (x != 0 || z != 0) {
            this.addVectorType(VectorData.VectorType.WithInput);
         }
      }
   }

   public static enum VectorType {
      Normal,
      Swimhop,
      Climbable,
      Knockback,
      FirstBreadKnockback,
      HackyClimbable,
      Teleport,
      SkippedTicks,
      Explosion,
      FirstBreadExplosion,
      InputResult,
      WithInput,
      StuckMultiplier,
      Spectator,
      Dead,
      Jump,
      SurfaceSwimming,
      SwimmingSpace,
      BestVelPicked,
      Firework,
      Lenience,
      TridentJump,
      Trident,
      SlimePistonBounce,
      ZeroPointZeroThree,
      AttackSlow,
      Flip_Sneaking,
      Flip_Use_Item,
      EntityPushing;
   }
}
