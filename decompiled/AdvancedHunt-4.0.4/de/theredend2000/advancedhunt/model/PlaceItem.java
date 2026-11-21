package de.theredend2000.advancedhunt.model;

import java.util.Objects;
import java.util.UUID;

public class PlaceItem {
   private final UUID id;
   private String group;
   private String name;
   private String itemData;

   public PlaceItem(UUID id, String group, String name, String itemData) {
      this.id = Objects.requireNonNull(id, "id cannot be null");
      this.group = group;
      this.name = name;
      this.itemData = itemData;
   }

   public UUID getId() {
      return this.id;
   }

   public String getGroup() {
      return this.group;
   }

   public void setGroup(String group) {
      this.group = group;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getItemData() {
      return this.itemData;
   }

   public void setItemData(String itemData) {
      this.itemData = itemData;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof PlaceItem)) {
         return false;
      } else {
         PlaceItem that = (PlaceItem)o;
         return this.id.equals(that.id);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id);
   }
}
