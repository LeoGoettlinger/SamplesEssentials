package com.magmaguy.elitemobs.magmacore.match;

import java.util.Objects;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public interface PlayerCustody {
   static PlayerCustody none() {
      return new PlayerCustody() {
         @Override
         public void capture(Player player) {
         }

         @Override
         public void restore(Player player) {
         }

         @Override
         public void recover(Player player) {
         }
      };
   }

   static PlayerCustody snapshot(NamespacedKey key) {
      return new SnapshotCustody(Objects.requireNonNull(key, "key"));
   }

   void capture(Player var1);

   void restore(Player var1);

   void recover(Player var1);
}
