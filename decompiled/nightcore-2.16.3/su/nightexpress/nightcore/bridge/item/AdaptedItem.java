package su.nightexpress.nightcore.bridge.item;

import java.util.Optional;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface AdaptedItem {
   @NonNull
   ItemAdapter<?> getAdapter();

   @Nullable
   ItemStack getItemStack();

   @NonNull
   Optional<ItemStack> itemStack();

   boolean isSimilar(@NonNull ItemStack var1);

   boolean isValid();

   int getAmount();
}
