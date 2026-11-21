package com.oheers.fish.items.nbt.abstracted;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.Logging;
import com.oheers.fish.utils.MinecraftVersionHelper;
import java.lang.reflect.Field;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class ItemStackNBTHolderBase extends NBTHolder<ItemStack> {
   private static final Field CRAFT_DELEGATE;

   public ItemStackNBTHolderBase(@NonNull ItemStack obj) {
      super(obj);
   }

   public ItemStackNBTHolderBase(@NonNull String raw) {
      super(createRaw(raw));
   }

   @NonNull
   private static ItemStack createRaw(@NonNull String raw) {
      ItemStack rawItem = EvenMoreFish.getInstance().getVersionProvider().deserializeItemStack(raw);
      if (rawItem == null) {
         throw new RuntimeException("Invalid raw NBT");
      } else {
         return rawItem;
      }
   }

   @Nullable
   protected static ItemStack fetchCraftDelegate(@NonNull ItemStack item) {
      if (CRAFT_DELEGATE == null) {
         if (MinecraftVersionHelper.isAtLeastVersion("1.21.1")) {
            Logging.error("Could not fetch craftDelegate method.");
         }

         return item;
      } else {
         try {
            return (ItemStack)CRAFT_DELEGATE.get(item);
         } catch (IllegalArgumentException | ClassCastException | IllegalAccessException var2) {
            Logging.error("Failed to fetch craftDelegate.", var2);
            return item;
         }
      }
   }

   static {
      Field field;
      try {
         field = ItemStack.class.getDeclaredField("craftDelegate");
         field.setAccessible(true);
      } catch (NoSuchFieldException var2) {
         throw new IllegalStateException("Could not find ItemStack's craftDelegate field, but it should exist.");
      }

      CRAFT_DELEGATE = field;
   }
}
