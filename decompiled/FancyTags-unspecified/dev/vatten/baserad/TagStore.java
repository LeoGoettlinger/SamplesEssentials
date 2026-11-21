package dev.vatten.baserad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import net.kyori.adventure.text.Component;

public class TagStore {
   private final Plugin plugin;
   private final Map<String, Tag> tags;
   private List<String> tagNames;
   private Component tagAtlas;
   private int tagAtlasLength = 0;

   TagStore(Plugin plugin, Map<String, Tag> tags) {
      this.plugin = plugin;
      this.tags = tags;
      this.refresh();
   }

   public Tag getTag(String tagName) {
      return this.tags.get(tagName);
   }

   public boolean addTag(String tagName, Tag tag) {
      if (this.tags.containsKey(tagName)) {
         return false;
      } else {
         this.tags.put(tagName, tag);
         this.refresh();
         this.plugin.saveTags();
         return true;
      }
   }

   public boolean renameTag(String from, String to) {
      if (this.tags.containsKey(from) && !this.tags.containsKey(to)) {
         this.tags.put(to, this.tags.remove(from));
         this.refresh();
         this.plugin.saveTags();
         return true;
      } else {
         return false;
      }
   }

   public boolean removeTag(String tagName) {
      if (this.tags.containsKey(tagName)) {
         this.tags.remove(tagName);
         this.refresh();
         this.plugin.saveTags();
         return true;
      } else {
         return false;
      }
   }

   Map<String, Tag> getTags() {
      return this.tags;
   }

   int getTagAtlasLength() {
      return this.tagAtlasLength;
   }

   private void refresh() {
      List<String> names = new ArrayList<>();
      this.tagAtlasLength = 0;
      Component atlas = Component.empty();

      for (Entry<String, Tag> entry : this.tags.entrySet()) {
         Component serialized = entry.getValue().serialize(this.plugin.getMiniMessage());
         names.add(entry.getKey());
         atlas = atlas.append(serialized);
         this.tagAtlasLength = this.tagAtlasLength + entry.getValue().getLength();
      }

      this.tagNames = Collections.unmodifiableList(names);
      this.tagAtlas = atlas;
   }

   @Generated
   public List<String> getTagNames() {
      return this.tagNames;
   }

   @Generated
   public Component getTagAtlas() {
      return this.tagAtlas;
   }
}
