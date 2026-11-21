package com.oheers.fish.items.config;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentMessage;
import org.evenmorefish.fish.libs.daisylib.messages.replacer.Replacer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class DisplayNameItemConfig extends ItemConfig<String> {
   public DisplayNameItemConfig(@NonNull Section section) {
      super(section);
   }

   public DisplayNameItemConfig(@NonNull DisplayNameItemConfig base) {
      super(base);
   }

   public String getConfiguredValue() {
      return this.section.getString("displayname");
   }

   @Override
   protected BiConsumer<ItemStack, String> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
      return (item, value) -> {
         if (value != null && !value.isEmpty()) {
            String name = Optional.ofNullable(player).<String>map(OfflinePlayer::getName).orElse("N/A");
            Replacer replacer = Replacer.replacer().addReplacement("{player}", name);
            if (replacements != null) {
               replacer.addReplacements(replacements);
            }

            item.editMeta(meta -> {
               Component display = ComponentMessage.componentMessage(value).replace(replacer).parsePlaceholderAPI(player).get();
               meta.displayName(display);
            });
         } else {
            item.editMeta(meta -> meta.displayName(Component.empty()));
         }
      };
   }

   @NonNull
   public DisplayNameItemConfig createCopy() {
      return new DisplayNameItemConfig(this);
   }
}
