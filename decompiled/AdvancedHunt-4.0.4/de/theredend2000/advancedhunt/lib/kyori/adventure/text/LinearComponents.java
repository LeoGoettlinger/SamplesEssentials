package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.Style;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.StyleBuilderApplicable;

public final class LinearComponents {
   private LinearComponents() {
   }

   public static Component linear(final ComponentBuilderApplicable... applicables) {
      int length = applicables.length;
      if (length == 0) {
         return Component.empty();
      } else if (length == 1) {
         if (applicables[0] instanceof ComponentLike cl) {
            return cl.asComponent();
         } else {
            throw nothingComponentLike();
         }
      } else {
         TextComponentImpl.BuilderImpl builder = new TextComponentImpl.BuilderImpl();
         Style.Builder style = null;

         for (ComponentBuilderApplicable applicable : applicables) {
            if (applicable instanceof StyleBuilderApplicable sba) {
               if (style == null) {
                  style = Style.style();
               }

               style.apply(sba);
            } else if (style != null && applicable instanceof ComponentLike cl) {
               builder.applicableApply(cl.asComponent().style(style));
            } else {
               builder.applicableApply(applicable);
            }
         }

         int size = builder.children.size();
         if (size == 0) {
            throw nothingComponentLike();
         } else {
            return (Component)(size == 1 && !builder.hasStyle() ? builder.children.getFirst() : builder.build());
         }
      }
   }

   private static IllegalStateException nothingComponentLike() {
      return new IllegalStateException("Cannot build component linearly - nothing component-like was given");
   }
}
