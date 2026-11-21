package com.github.retrooper.packetevents.protocol.world.states.type;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.MaterialType;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StateType {
   private final StateType.Mapped mapped;
   private final float blastResistance;
   private final float hardness;
   private final boolean isSolid;
   private final boolean isBlocking;
   private final boolean isAir;
   private final boolean requiresCorrectTool;
   private final boolean exceedsCube;
   private final MaterialType materialType;

   @Internal
   public StateType(
      TypesBuilderData typeData,
      float blastResistance,
      float hardness,
      boolean isSolid,
      boolean isBlocking,
      boolean isAir,
      boolean requiresCorrectTool,
      boolean isShapeExceedsCube,
      MaterialType materialType
   ) {
      this.mapped = new StateType.Mapped(typeData);
      this.blastResistance = blastResistance;
      this.hardness = hardness;
      this.isSolid = isSolid;
      this.isBlocking = isBlocking;
      this.isAir = isAir;
      this.requiresCorrectTool = requiresCorrectTool;
      this.exceedsCube = isShapeExceedsCube;
      this.materialType = materialType;
   }

   public StateType.Mapped getMapped() {
      return this.mapped;
   }

   public WrappedBlockState createBlockState() {
      return WrappedBlockState.getDefaultState(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(), this);
   }

   public WrappedBlockState createBlockState(ClientVersion version) {
      return WrappedBlockState.getDefaultState(version, this);
   }

   public String getName() {
      return this.mapped.getName().getKey();
   }

   public float getBlastResistance() {
      return this.blastResistance;
   }

   public float getHardness() {
      return this.hardness;
   }

   public boolean isSolid() {
      return this.isSolid;
   }

   public boolean isBlocking() {
      return this.isBlocking;
   }

   public boolean isAir() {
      return this.isAir;
   }

   public boolean isRequiresCorrectTool() {
      return this.requiresCorrectTool;
   }

   public boolean isReplaceable() {
      switch (this.getMaterialType()) {
         case AIR:
         case STRUCTURAL_AIR:
         case REPLACEABLE_PLANT:
         case REPLACEABLE_FIREPROOF_PLANT:
         case REPLACEABLE_WATER_PLANT:
         case WATER:
         case BUBBLE_COLUMN:
         case LAVA:
         case TOP_SNOW:
         case FIRE:
            return true;
         default:
            return false;
      }
   }

   public boolean exceedsCube() {
      return this.exceedsCube;
   }

   public MaterialType getMaterialType() {
      return this.materialType;
   }

   @Override
   public String toString() {
      return this.getName();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         StateType stateType = (StateType)o;
         return Float.compare(this.blastResistance, stateType.blastResistance) == 0
            && Float.compare(this.hardness, stateType.hardness) == 0
            && this.isSolid == stateType.isSolid
            && this.isBlocking == stateType.isBlocking
            && this.isAir == stateType.isAir
            && this.requiresCorrectTool == stateType.requiresCorrectTool
            && this.exceedsCube == stateType.exceedsCube
            && Objects.equals(this.getName(), stateType.getName())
            && this.materialType == stateType.materialType;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.getName(),
         this.blastResistance,
         this.hardness,
         this.isSolid,
         this.isBlocking,
         this.isAir,
         this.requiresCorrectTool,
         this.exceedsCube,
         this.materialType
      );
   }

   public final class Mapped extends AbstractMappedEntity {
      @Internal
      public Mapped(@Nullable TypesBuilderData data) {
         super(data);
      }

      public StateType getStateType() {
         return StateType.this;
      }
   }
}
