package com.oheers.fish.api.fishing.rods;

import com.oheers.fish.api.AbstractFileBasedManager;
import com.oheers.fish.api.baits.AbstractBaitManager;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class AbstractRodManager<T extends ICustomRod> extends AbstractFileBasedManager<T> {
   private static AbstractRodManager<? extends ICustomRod> instance;

   protected AbstractRodManager() {
      super(AbstractBaitManager.getInstance());
      if (instance != null) {
         throw new IllegalStateException("RodManager has already been initialized!");
      } else {
         instance = this;
      }
   }

   @NonNull
   public static AbstractRodManager<? extends ICustomRod> getInstance() {
      if (instance == null) {
         throw new IllegalStateException("RodManager has not been initialized yet!");
      } else {
         return instance;
      }
   }

   @Nullable
   public abstract ICustomRod getRod(@NonNull ItemStack var1);

   @Nullable
   public abstract ICustomRod getRod(@NonNull String var1);
}
