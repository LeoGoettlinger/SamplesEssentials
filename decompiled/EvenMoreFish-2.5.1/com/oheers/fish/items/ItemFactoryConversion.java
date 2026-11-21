package com.oheers.fish.items;

import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;

public class ItemFactoryConversion {
   public void performConversions(@NonNull Section section) {
      this.copyIfPresent(section, "glowing", "item.glowing");
      this.copyIfPresent(section, "dye-colour", "item.dye-colour");
      this.copyIfPresent(section, "durability", "item.durability");
      this.copyIfPresent(section, "lore", "item.lore");
      this.copyIfPresent(section, "displayname", "item.displayname");
   }

   private void copyIfPresent(@NonNull Section section, @NonNull String from, @NonNull String to) {
      if (section.contains(from) && !section.contains(to)) {
         section.set(to, section.get(from));
      }
   }
}
