package su.nightexpress.nightcore.integration.item.adapter.impl;

import com.ssomar.score.api.executableitems.ExecutableItemsAPI;
import com.ssomar.score.api.executableitems.config.ExecutableItemInterface;
import com.ssomar.score.sobject.SObjectInterface;
import java.util.Optional;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.integration.item.adapter.IdentifiableItemAdapter;
import su.nightexpress.nightcore.integration.item.data.ItemIdData;

public class ExecutableItemsAdapter extends IdentifiableItemAdapter {
   public ExecutableItemsAdapter() {
      super("executableitems");
   }

   @Nullable
   @Override
   public ItemStack createItem(@NonNull String itemId) {
      ExecutableItemInterface itemInterface = (ExecutableItemInterface)ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(itemId).orElse(null);
      return itemInterface == null ? null : itemInterface.buildItem(1, Optional.empty());
   }

   @Override
   public boolean canHandle(@NonNull ItemStack itemStack) {
      return ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(itemStack).isPresent();
   }

   public boolean canHandle(@NonNull ItemIdData data) {
      return ExecutableItemsAPI.getExecutableItemsManager().isValidID(data.getItemId());
   }

   @Nullable
   @Override
   public String getItemId(@NonNull ItemStack itemStack) {
      return ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(itemStack).<String>map(SObjectInterface::getId).orElse(null);
   }
}
