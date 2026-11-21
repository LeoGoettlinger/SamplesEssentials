package com.artillexstudios.axafkzone.libs.axapi.utils;

public enum EquipmentSlot {
   MAIN_HAND(EquipmentSlot.Type.HAND, 0),
   OFF_HAND(EquipmentSlot.Type.HAND, 1),
   BOOTS(EquipmentSlot.Type.ARMOR, 0),
   LEGGINGS(EquipmentSlot.Type.ARMOR, 1),
   CHEST_PLATE(EquipmentSlot.Type.ARMOR, 2),
   HELMET(EquipmentSlot.Type.ARMOR, 3),
   BODY(EquipmentSlot.Type.ANIMAL_ARMOR, 6),
   SADDLE(EquipmentSlot.Type.SADDLE, 7);

   private final EquipmentSlot.Type type;
   private final int index;

   private EquipmentSlot(EquipmentSlot.Type type, int index) {
      this.type = type;
      this.index = index;
   }

   public EquipmentSlot.Type getType() {
      return this.type;
   }

   public int getIndex() {
      return this.index;
   }

   public static enum Type {
      HAND,
      ARMOR,
      ANIMAL_ARMOR,
      SADDLE;
   }
}
