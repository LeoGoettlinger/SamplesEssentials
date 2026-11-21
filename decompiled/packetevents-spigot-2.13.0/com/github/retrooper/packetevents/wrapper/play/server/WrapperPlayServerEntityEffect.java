package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.potion.PotionType;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.BitSet;
import org.jetbrains.annotations.Nullable;

public class WrapperPlayServerEntityEffect extends PacketWrapper<WrapperPlayServerEntityEffect> {
   private static final int FLAG_AMBIENT = 1;
   private static final int FLAG_VISIBLE = 2;
   private static final int FLAG_SHOW_ICONS = 4;
   private int entityID;
   private PotionType potionType;
   private int effectAmplifier;
   private int effectDurationTicks;
   private byte flags;
   private NBTCompound factorData;

   public WrapperPlayServerEntityEffect(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerEntityEffect(int entityID, PotionType potionType, int amplifier, int duration, byte flags) {
      super(PacketType.Play.Server.ENTITY_EFFECT);
      this.entityID = entityID;
      this.potionType = potionType;
      this.effectAmplifier = amplifier;
      this.effectDurationTicks = duration;
      this.flags = flags;
   }

   @Override
   public void read() {
      this.entityID = this.readVarInt();
      int effectId;
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18_2)) {
         effectId = this.readVarInt();
      } else {
         effectId = this.readByte();
      }

      this.potionType = PotionTypes.getById(effectId, this.serverVersion);
      this.effectAmplifier = this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_20_5) ? this.readVarInt() : this.readByte();
      this.effectDurationTicks = this.readVarInt();
      if (this.serverVersion.isNewerThan(ServerVersion.V_1_7_10)) {
         this.flags = this.readByte();
      }

      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19) && this.serverVersion.isOlderThan(ServerVersion.V_1_20_5)) {
         this.factorData = this.readOptional(PacketWrapper::readNBT);
      }
   }

   @Override
   public void write() {
      this.writeVarInt(this.entityID);
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18_2)) {
         this.writeVarInt(this.potionType.getId(this.serverVersion.toClientVersion()));
      } else {
         this.writeByte(this.potionType.getId(this.serverVersion.toClientVersion()));
      }

      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_20_5)) {
         this.writeVarInt(this.effectAmplifier);
      } else {
         this.writeByte(this.effectAmplifier);
      }

      this.writeVarInt(this.effectDurationTicks);
      if (this.serverVersion.isNewerThan(ServerVersion.V_1_7_10)) {
         this.writeByte(this.flags);
      }

      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19) && this.serverVersion.isOlderThan(ServerVersion.V_1_20_5)) {
         this.writeOptional(this.factorData, PacketWrapper::writeNBT);
      }
   }

   public void copy(WrapperPlayServerEntityEffect wrapper) {
      this.entityID = wrapper.entityID;
      this.potionType = wrapper.potionType;
      this.effectAmplifier = wrapper.effectAmplifier;
      this.effectDurationTicks = wrapper.effectDurationTicks;
      this.flags = wrapper.flags;
      this.factorData = wrapper.factorData;
   }

   public PotionType getPotionType() {
      return this.potionType;
   }

   public void setPotionType(PotionType potionType) {
      this.potionType = potionType;
   }

   public int getEntityId() {
      return this.entityID;
   }

   public void setEntityId(int entityID) {
      this.entityID = entityID;
   }

   public int getEffectAmplifier() {
      return this.effectAmplifier;
   }

   public void setEffectAmplifier(int effectAmplifier) {
      this.effectAmplifier = effectAmplifier;
   }

   public int getEffectDurationTicks() {
      return this.effectDurationTicks;
   }

   public void setEffectDurationTicks(int effectDurationTicks) {
      this.effectDurationTicks = effectDurationTicks;
   }

   private byte getFlags() {
      return this.flags;
   }

   private void setFlags(byte flags) {
      this.flags = flags;
   }

   @Nullable
   public NBTCompound getFactorData() {
      return this.factorData;
   }

   public void setFactorData(@Nullable NBTCompound factorData) {
      this.factorData = factorData;
   }

   private byte constructFlags(boolean ambient, boolean visible, boolean icons) {
      BitSet bitSet = new BitSet(3);
      bitSet.set(0, ambient);
      bitSet.set(1, visible);
      bitSet.set(2, icons);
      return bitSet.toByteArray()[0];
   }

   public boolean isAmbient() {
      return this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_8_8) ? false : (this.getFlags() & 1) == 1;
   }

   public void setAmbient(boolean isAmbient) {
      if (this.serverVersion.isNewerThan(ServerVersion.V_1_8_8)) {
         this.setFlags(this.constructFlags(this.isVisible(), isAmbient, this.isShowIcon()));
      }
   }

   public boolean isVisible() {
      if (this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
         return false;
      } else {
         return this.serverVersion.isOlderThan(ServerVersion.V_1_10) ? this.getFlags() != 0 : (this.getFlags() & 2) == 2;
      }
   }

   public void setVisible(boolean isVisible) {
      if (!this.serverVersion.isOlderThan(ServerVersion.V_1_10)) {
         this.setFlags(this.constructFlags(isVisible, this.isAmbient(), this.isShowIcon()));
      }
   }

   public boolean isShowIcon() {
      return this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_13_2) ? false : (this.getFlags() & 4) == 4;
   }

   public void setShowIcon(boolean showIcon) {
      if (!this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_13_2)) {
         this.setFlags(this.constructFlags(this.isVisible(), this.isAmbient(), showIcon));
      }
   }
}
