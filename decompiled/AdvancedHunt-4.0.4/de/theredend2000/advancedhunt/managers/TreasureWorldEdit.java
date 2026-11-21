package de.theredend2000.advancedhunt.managers;

public class TreasureWorldEdit {
   private final String worldName;
   private final int x;
   private final int y;
   private final int z;
   private final String material;
   private final String blockState;
   private final String nbtData;
   private final TreasureWorldEdit.Action action;

   public TreasureWorldEdit(String worldName, int x, int y, int z, String material, String blockState, String nbtData, TreasureWorldEdit.Action action) {
      this.worldName = worldName;
      this.x = x;
      this.y = y;
      this.z = z;
      this.material = material;
      this.blockState = blockState;
      this.nbtData = nbtData;
      this.action = action;
   }

   public String getWorldName() {
      return this.worldName;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getZ() {
      return this.z;
   }

   public String getMaterial() {
      return this.material;
   }

   public String getBlockState() {
      return this.blockState;
   }

   public String getNbtData() {
      return this.nbtData;
   }

   public TreasureWorldEdit.Action getAction() {
      return this.action;
   }

   public int getChunkX() {
      return this.x >> 4;
   }

   public int getChunkZ() {
      return this.z >> 4;
   }

   public static enum Action {
      RESTORE,
      REMOVE;
   }
}
