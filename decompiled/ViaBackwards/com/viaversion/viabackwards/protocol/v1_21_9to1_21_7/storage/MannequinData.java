package com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.storage;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.item.Item;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MannequinData implements StorableObject {
   private final UUID uuid;
   private final String name;
   private boolean hasTeam;
   private double x;
   private double y;
   private double z;
   private byte yaw;
   private byte pitch;
   private byte headYaw;
   private int[] passengers;
   private final List<EntityData> entityData = new ArrayList<>();
   private final Map<Byte, Item> itemMap = new HashMap<>();
   private Tag displayName;

   public MannequinData(UUID uuid, String name) {
      this.uuid = uuid;
      this.name = name;
   }

   public void setHasTeam(boolean hasTeam) {
      this.hasTeam = hasTeam;
   }

   public boolean hasTeam() {
      return this.hasTeam;
   }

   public UUID uuid() {
      return this.uuid;
   }

   public String name() {
      return this.name;
   }

   public void setPosition(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public void setRotation(byte yaw, byte pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }

   public void setPassengers(int[] passengers) {
      this.passengers = passengers;
   }

   public double x() {
      return this.x;
   }

   public double y() {
      return this.y;
   }

   public double z() {
      return this.z;
   }

   public byte yaw() {
      return this.yaw;
   }

   public byte pitch() {
      return this.pitch;
   }

   public List<EntityData> entityData() {
      return this.entityData;
   }

   public int[] passengers() {
      return this.passengers;
   }

   public void setDisplayName(Tag displayName) {
      this.displayName = displayName;
   }

   public Tag displayName() {
      return this.displayName;
   }

   public void setEquipment(byte slot, Item item) {
      this.itemMap.put(slot, item);
   }

   public Map<Byte, Item> itemMap() {
      return this.itemMap;
   }

   public byte headYaw() {
      return this.headYaw;
   }

   public void setHeadYaw(byte headYaw) {
      this.headYaw = headYaw;
   }
}
