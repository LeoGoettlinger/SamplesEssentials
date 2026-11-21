package com.oheers.fish.api.fishing.items;

import com.oheers.fish.api.AbstractFileBasedManager;
import java.util.TreeMap;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class AbstractFishManager<T extends IRarity> extends AbstractFileBasedManager<T> {
   private static AbstractFishManager<? extends IRarity> instance;

   protected AbstractFishManager() {
      if (instance != null) {
         throw new IllegalStateException("FishManager has already been initialized!");
      } else {
         instance = this;
      }
   }

   @NonNull
   public static AbstractFishManager<? extends IRarity> getInstance() {
      if (instance == null) {
         throw new IllegalStateException("FishManager has not been initialized yet!");
      } else {
         return instance;
      }
   }

   @Nullable
   public abstract IRarity getRarity(@NonNull String var1);

   @Nullable
   public abstract IFish getFish(@NonNull String var1, @NonNull String var2);

   @Nullable
   public abstract IFish getFish(@Nullable ItemStack var1);

   @Nullable
   public abstract IFish getFish(@Nullable Skull var1, @Nullable Player var2);

   @Nullable
   public abstract IFish getFish(@Nullable Entity var1);

   public abstract boolean isFish(@Nullable ItemStack var1);

   public abstract boolean isFish(@Nullable Skull var1);

   public abstract boolean isFish(@Nullable Entity var1);

   public abstract void setFishNbt(@NonNull ItemStack var1, @NonNull IFish var2);

   public abstract void setFishNbt(@NonNull Skull var1, @NonNull IFish var2);

   @NonNull
   public abstract TreeMap<String, ? extends IRarity> getRarityMap();
}
