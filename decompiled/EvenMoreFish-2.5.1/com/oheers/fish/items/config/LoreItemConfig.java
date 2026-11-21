package com.oheers.fish.items.config;

import java.util.List;
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

public class LoreItemConfig extends ItemConfig<List<Component>> {
   public LoreItemConfig(@NonNull Section section) {
      super(section);
   }

   public LoreItemConfig(@NonNull LoreItemConfig base) {
      super(base);
   }

   public List<Component> getConfiguredValue() {
      List<String> lore = this.section.getStringList("lore");
      return lore.isEmpty() ? null : ComponentMessage.componentMessage(lore).get();
   }

   @Override
   protected BiConsumer<ItemStack, List<Component>> applyToItem(@Nullable OfflinePlayer player, @Nullable Map<String, ?> replacements) {
      return (item, value) -> {
         if (!value.isEmpty()) {
            String name = Optional.ofNullable(player).<String>map(OfflinePlayer::getName).orElse("N/A");
            Replacer replacer = Replacer.replacer().addReplacement("{player}", name);
            if (replacements != null) {
               replacer.addReplacements(replacements);
            }

            item.editMeta(meta -> {
               List<Component> lore = ComponentMessage.componentMessage(value).replace(replacer).parsePlaceholderAPI(player).get();
               meta.lore(lore);
            });
         }
      };
   }

   @NonNull
   public LoreItemConfig createCopy() {
      return new LoreItemConfig(this);
   }
}
