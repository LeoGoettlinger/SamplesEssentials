package de.theredend2000.advancedhunt.migration.legacy;

import de.theredend2000.advancedhunt.managers.CollectionManager;
import java.util.Locale;
import java.util.Objects;

public final class LegacyLocationKey {
   private final String collectionName;
   private final String world;
   private final int x;
   private final int y;
   private final int z;

   public LegacyLocationKey(String collectionName, String world, int x, int y, int z) {
      this.collectionName = normalizeCollectionName(collectionName);
      this.world = normalize(world);
      this.x = x;
      this.y = y;
      this.z = z;
   }

   private static String normalizeCollectionName(String s) {
      return CollectionManager.normalizeCollectionName(s);
   }

   private static String normalize(String s) {
      return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
   }

   public String collectionName() {
      return this.collectionName;
   }

   public String world() {
      return this.world;
   }

   public int x() {
      return this.x;
   }

   public int y() {
      return this.y;
   }

   public int z() {
      return this.z;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof LegacyLocationKey)) {
         return false;
      } else {
         LegacyLocationKey that = (LegacyLocationKey)o;
         return this.x == that.x
            && this.y == that.y
            && this.z == that.z
            && Objects.equals(this.collectionName, that.collectionName)
            && Objects.equals(this.world, that.world);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.collectionName, this.world, this.x, this.y, this.z);
   }

   @Override
   public String toString() {
      return "LegacyLocationKey{collection='"
         + this.collectionName
         + '\''
         + ", world='"
         + this.world
         + '\''
         + ", x="
         + this.x
         + ", y="
         + this.y
         + ", z="
         + this.z
         + '}';
   }
}
