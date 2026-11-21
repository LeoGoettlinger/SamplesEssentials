package su.nightexpress.nightcore.bridge.item;

import java.util.Optional;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface ItemAdapter<T> {
   @NonNull
   String getName();

   boolean canHandle(@NonNull ItemStack var1);

   boolean canHandle(@NonNull T var1);

   @NonNull
   Optional<? extends AdaptedItem> adapt(@NonNull ItemStack var1);

   @Nullable
   ItemStack toItemStack(@NonNull T var1);

   @Nullable
   T fromItemStack(@NonNull ItemStack var1);

   boolean isVanilla();

   int getWeight();
}
