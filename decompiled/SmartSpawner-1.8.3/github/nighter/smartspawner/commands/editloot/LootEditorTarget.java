package github.nighter.smartspawner.commands.editloot;

import lombok.Generated;

public enum LootEditorTarget {
   SMART_SPAWNER("spawner_mobs.yml", "editloot.mob_loot_title"),
   ITEM_SPAWNER("spawner_items.yml", "editloot.item_loot_title");

   private final String fileName;
   private final String titleKey;

   private LootEditorTarget(String fileName, String titleKey) {
      this.fileName = fileName;
      this.titleKey = titleKey;
   }

   @Generated
   public String getFileName() {
      return this.fileName;
   }

   @Generated
   public String getTitleKey() {
      return this.titleKey;
   }
}
