package com.magmaguy.elitemobs.magmacore.config;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.UnaryOperator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ContentFileSelector {
   private ContentFileSelector() {
   }

   public static List<File> select(Collection<File> candidates) {
      return select(candidates, UnaryOperator.identity());
   }

   public static List<File> select(Collection<File> candidates, UnaryOperator<String> filenameKey) {
      Comparator<File> order = Comparator.comparing(ContentFileSelector::absolutePath, String.CASE_INSENSITIVE_ORDER)
         .thenComparing(ContentFileSelector::absolutePath);
      Map<String, File> selected = new LinkedHashMap<>();

      for (File file : candidates.stream().sorted(order).toList()) {
         if (!OutdatedConfigurationArchive.isArchivePath(file.toPath())) {
            File previous = selected.putIfAbsent(filenameKey.apply(file.getName()), file);
            if (previous != null && !absolutePath(previous).equals(absolutePath(file)) && !sameContents(previous, file)) {
               Logger.warn(
                  "Duplicate content filename '"
                     + file.getName()
                     + "': selected "
                     + absolutePath(previous)
                     + "; skipped "
                     + absolutePath(file)
                     + ". Only the selected file will be loaded. Resolve the duplicate filenames."
               );
            }
         }
      }

      return List.copyOf(selected.values());
   }

   private static boolean sameContents(File selected, File skipped) {
      try {
         if (Files.mismatch(selected.toPath(), skipped.toPath()) == -1L) {
            return true;
         }
      } catch (IOException var8) {
         return false;
      }

      String name = skipped.getName().toLowerCase(Locale.ROOT);
      if (!name.endsWith(".yml") && !name.endsWith(".yaml")) {
         return false;
      } else {
         try {
            YamlConfiguration selectedValues = YamlConfiguration.loadConfiguration(selected);
            Map<String, Object> skippedValues = YamlConfiguration.loadConfiguration(skipped).getValues(true);
            if (skippedValues.isEmpty()) {
               return false;
            } else {
               for (Entry<String, Object> entry : skippedValues.entrySet()) {
                  if (!(entry.getValue() instanceof ConfigurationSection) && !Objects.equals(selectedValues.get(entry.getKey()), entry.getValue())) {
                     return false;
                  }
               }

               return true;
            }
         } catch (RuntimeException var7) {
            return false;
         }
      }
   }

   private static String absolutePath(File file) {
      return file.toPath().toAbsolutePath().normalize().toString();
   }
}
