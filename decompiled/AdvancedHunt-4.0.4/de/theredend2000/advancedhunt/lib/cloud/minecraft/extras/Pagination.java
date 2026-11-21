package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import org.checkerframework.checker.nullness.qual.NonNull;

final class Pagination<T> {
   private final BiFunction<Integer, Integer, List<Component>> headerRenderer;
   private final BiFunction<T, Boolean, Component> rowRenderer;
   private final BiFunction<Integer, Integer, Component> footerRenderer;
   private final BiFunction<Integer, Integer, Component> outOfRangeRenderer;

   Pagination(
      @NonNull final BiFunction<Integer, Integer, List<Component>> headerRenderer,
      @NonNull final BiFunction<T, Boolean, Component> rowRenderer,
      @NonNull final BiFunction<Integer, Integer, Component> footerRenderer,
      @NonNull final BiFunction<Integer, Integer, Component> outOfRangeRenderer
   ) {
      this.headerRenderer = headerRenderer;
      this.rowRenderer = rowRenderer;
      this.footerRenderer = footerRenderer;
      this.outOfRangeRenderer = outOfRangeRenderer;
   }

   @NonNull
   List<Component> render(@NonNull final List<T> content, final int page, final int itemsPerPage) {
      int pages = (int)Math.ceil(content.size() / (itemsPerPage * 1.0));
      if (page >= 1 && page <= pages) {
         List<Component> renderedContent = new ArrayList<>(this.headerRenderer.apply(page, pages));
         int start = itemsPerPage * (page - 1);
         int maxIndex = start + itemsPerPage;

         for (int index = start; index < maxIndex && index <= content.size() - 1; index++) {
            renderedContent.add(this.rowRenderer.apply(content.get(index), index == maxIndex - 1));
         }

         renderedContent.add(this.footerRenderer.apply(page, pages));
         return Collections.unmodifiableList(renderedContent);
      } else {
         return Collections.singletonList(this.outOfRangeRenderer.apply(page, pages));
      }
   }
}
