package com.oheers.fish.messages;

import java.util.List;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.daisylib.messages.config.ConfigReader;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class EMFConfigReader implements ConfigReader<Section> {
   private final Section config;

   public EMFConfigReader(@NonNull Section section) {
      this.config = section;
   }

   @Nullable
   @Override
   public Object getObject(String path) {
      return this.config.get(path);
   }

   @Nullable
   @Override
   public String getString(String path) {
      return this.config.getString(path);
   }

   @NonNull
   @Override
   public List<String> getStringList(String path) {
      return this.config.getStringList(path);
   }

   @NonNull
   public Section getConfig() {
      return this.config;
   }

   @Nullable
   @Override
   public ConfigReader<Section> getSection(@NonNull String path) {
      Section section = this.config.getSection(path);
      return section == null ? null : new EMFConfigReader(section);
   }
}
