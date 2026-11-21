package me.clip.placeholderapi.configuration;

import java.util.Comparator;
import me.clip.placeholderapi.expansion.cloud.CloudExpansion;
import org.jetbrains.annotations.NotNull;

public enum ExpansionSort implements Comparator<CloudExpansion> {
   NAME(Comparator.comparing(CloudExpansion::getName)),
   AUTHOR(Comparator.comparing(CloudExpansion::getAuthor)),
   LATEST(Comparator.comparing(CloudExpansion::getLastUpdate).reversed());

   @NotNull
   private final Comparator<CloudExpansion> comparator;

   private ExpansionSort(@NotNull final Comparator<CloudExpansion> comparator) {
      this.comparator = comparator;
   }

   public final int compare(CloudExpansion expansion1, CloudExpansion expansion2) {
      return this.comparator.compare(expansion1, expansion2);
   }
}
