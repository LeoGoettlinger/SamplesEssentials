package com.oheers.fish.api.baits;

import com.oheers.fish.api.AbstractFileBasedManager;
import com.oheers.fish.api.fishing.items.AbstractFishManager;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class AbstractBaitManager<T extends IBait> extends AbstractFileBasedManager<T> {
   private static AbstractBaitManager<? extends IBait> instance;

   protected AbstractBaitManager() {
      super(AbstractFishManager.getInstance());
      if (instance != null) {
         throw new IllegalStateException("BaitManager has already been initialized!");
      } else {
         instance = this;
      }
   }

   @NonNull
   public static AbstractBaitManager<? extends IBait> getInstance() {
      if (instance == null) {
         throw new IllegalStateException("BaitManager has not been initialized yet!");
      } else {
         return instance;
      }
   }

   @Nullable
   public abstract IBait getBait(@Nullable String var1);

   @Nullable
   public abstract IBait getBait(@Nullable ItemStack var1);

   @Nullable
   public abstract IBait getBait(@Nullable Entity var1);

   public abstract boolean isBait(@Nullable ItemStack var1);

   public abstract boolean isBait(@Nullable Entity var1);
}
