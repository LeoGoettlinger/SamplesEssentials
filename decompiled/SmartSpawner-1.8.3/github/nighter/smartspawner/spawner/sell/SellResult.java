package github.nighter.smartspawner.spawner.sell;

import github.nighter.smartspawner.spawner.properties.ItemSignature;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;

public class SellResult {
   private final double totalValue;
   private final long itemsSold;
   private final Map<ItemSignature, Long> itemsToRemove;
   private final long timestamp;
   private final boolean successful;

   public SellResult(double totalValue, long itemsSold, Map<ItemSignature, Long> itemsToRemove) {
      this.totalValue = totalValue;
      this.itemsSold = itemsSold;
      this.itemsToRemove = new HashMap<>(itemsToRemove);
      this.timestamp = System.currentTimeMillis();
      this.successful = totalValue > 0.0 && !itemsToRemove.isEmpty();
   }

   public static SellResult empty() {
      return new SellResult(0.0, 0L, Collections.emptyMap());
   }

   public boolean hasItems() {
      return !this.itemsToRemove.isEmpty();
   }

   @Generated
   public double getTotalValue() {
      return this.totalValue;
   }

   @Generated
   public long getItemsSold() {
      return this.itemsSold;
   }

   @Generated
   public Map<ItemSignature, Long> getItemsToRemove() {
      return this.itemsToRemove;
   }

   @Generated
   public long getTimestamp() {
      return this.timestamp;
   }

   @Generated
   public boolean isSuccessful() {
      return this.successful;
   }
}
