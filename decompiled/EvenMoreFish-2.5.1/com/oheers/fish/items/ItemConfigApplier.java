package com.oheers.fish.items;

import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.items.config.DisplayNameItemConfig;
import com.oheers.fish.items.config.ItemConfig;
import com.oheers.fish.items.config.LoreItemConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public class ItemConfigApplier {
   public static void apply(
      @NonNull ItemStack item, @Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements, @NonNull ItemConfig<?> config, boolean usingAddon
   ) {
      if (!usingAddon) {
         config.apply(item, player, replacements);
      } else if (config instanceof DisplayNameItemConfig display) {
         ItemConfigApplier.AddonBehavior behavior = ItemConfigApplier.AddonBehavior.getOrDefault(
            MainConfig.getInstance().getDisplayNameAddonBehavior(), ItemConfigApplier.AddonBehavior.REPLACE
         );
         behavior.applyDisplay(item, player, replacements, display);
      } else if (config instanceof LoreItemConfig lore) {
         ItemConfigApplier.AddonBehavior behavior = ItemConfigApplier.AddonBehavior.getOrDefault(
            MainConfig.getInstance().getLoreAddonBehavior(), ItemConfigApplier.AddonBehavior.REPLACE
         );
         behavior.applyLore(item, player, replacements, lore);
      } else {
         config.apply(item, player, replacements);
      }
   }

   public static enum AddonBehavior {
      NOTHING,
      APPEND,
      PREPEND,
      REPLACE;

      @NonNull
      public static ItemConfigApplier.AddonBehavior getOrDefault(@Nullable String behavior, @NonNull ItemConfigApplier.AddonBehavior def) {
         return FishUtils.getEnumValue(ItemConfigApplier.AddonBehavior.class, behavior, def);
      }

      public void applyDisplay(
         @NonNull ItemStack item, @Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements, @NonNull DisplayNameItemConfig display
      ) {
         Logging.debug("AddonBehavior for the Display Name is set to: " + this);
         if (this.equals(REPLACE)) {
            display.apply(item, player, replacements);
         }
      }

      public void applyLore(@NonNull ItemStack item, @Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements, @NonNull LoreItemConfig lore) {
         Logging.debug("AddonBehavior for the Lore is set to: " + this);
         switch (this) {
            case NOTHING:
            default:
               break;
            case APPEND:
               List<Component> beforex = this.fetchLoreOrEmpty(item);
               System.out.println(beforex);
               lore.apply(item, player, replacements);
               List<Component> afterx = item.lore();
               System.out.println(afterx);
               if (afterx != null) {
                  beforex.addAll(afterx);
                  System.out.println(beforex);
                  item.editMeta(meta -> meta.lore(before));
               }
               break;
            case PREPEND:
               List<Component> before = item.lore();
               lore.apply(item, player, replacements);
               List<Component> after = this.fetchLoreOrEmpty(item);
               if (before != null) {
                  after.addAll(before);
                  item.editMeta(meta -> meta.lore(after));
               }
               break;
            case REPLACE:
               lore.apply(item, player, replacements);
         }
      }

      private List<Component> fetchLoreOrEmpty(@NonNull ItemStack item) {
         return (List<Component>)Optional.ofNullable(item.lore()).map(ArrayList::new).orElseGet(ArrayList::new);
      }
   }
}
