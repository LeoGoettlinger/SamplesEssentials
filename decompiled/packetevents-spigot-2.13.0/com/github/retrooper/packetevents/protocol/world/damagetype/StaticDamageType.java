package com.github.retrooper.packetevents.protocol.world.damagetype;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticDamageType extends AbstractMappedEntity implements DamageType {
   private final String messageId;
   private final DamageScaling scaling;
   private final float exhaustion;
   private final DamageEffects effects;
   private final DeathMessageType deathMessageType;

   @Internal
   public StaticDamageType(
      @Nullable TypesBuilderData data, String messageId, DamageScaling scaling, float exhaustion, DamageEffects effects, DeathMessageType deathMessageType
   ) {
      super(data);
      this.messageId = messageId;
      this.scaling = scaling;
      this.exhaustion = exhaustion;
      this.effects = effects;
      this.deathMessageType = deathMessageType;
   }

   public DamageType copy(@Nullable TypesBuilderData newData) {
      return new StaticDamageType(newData, this.messageId, this.scaling, this.exhaustion, this.effects, this.deathMessageType);
   }

   @Override
   public String getMessageId() {
      return this.messageId;
   }

   @Override
   public DamageScaling getScaling() {
      return this.scaling;
   }

   @Override
   public float getExhaustion() {
      return this.exhaustion;
   }

   @Override
   public DamageEffects getEffects() {
      return this.effects;
   }

   @Override
   public DeathMessageType getDeathMessageType() {
      return this.deathMessageType;
   }

   @Override
   public boolean deepEquals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof StaticDamageType)) {
         return false;
      } else {
         StaticDamageType that = (StaticDamageType)obj;
         if (Float.compare(that.exhaustion, this.exhaustion) != 0) {
            return false;
         } else if (!this.messageId.equals(that.messageId)) {
            return false;
         } else if (this.scaling != that.scaling) {
            return false;
         } else {
            return this.effects != that.effects ? false : this.deathMessageType == that.deathMessageType;
         }
      }
   }

   @Override
   public int deepHashCode() {
      return Objects.hash(this.messageId, this.scaling, this.exhaustion, this.effects, this.deathMessageType);
   }

   @Override
   public String toString() {
      return "StaticDamageType{messageId='"
         + this.messageId
         + '\''
         + ", scaling="
         + this.scaling
         + ", exhaustion="
         + this.exhaustion
         + ", effects="
         + this.effects
         + ", deathMessageType="
         + this.deathMessageType
         + '}';
   }
}
