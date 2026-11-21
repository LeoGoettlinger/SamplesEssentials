package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.Style;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;

final class ComponentCompaction {
   @VisibleForTesting
   static final boolean SIMPLIFY_STYLE_FOR_BLANK_COMPONENTS = false;

   private ComponentCompaction() {
   }

   static Component compact(final Component self, @Nullable final Style parentStyle) {
      List<Component> children = self.children();
      Component optimized = self.children(List.of());
      if (parentStyle != null) {
         optimized = optimized.style(self.style().unmerge(parentStyle));
      }

      int childrenSize = children.size();
      if (childrenSize == 0) {
         if (isBlank(optimized)) {
            optimized = optimized.style(simplifyStyleForBlank(optimized.style(), parentStyle));
         }

         return optimized;
      } else {
         if (childrenSize == 1 && isText(optimized)) {
            TextComponent textComponent = (TextComponent)optimized;
            if (textComponent.content().isEmpty()) {
               Component child = children.getFirst();
               return child.style(child.style().merge(optimized.style(), Style.Merge.Strategy.IF_ABSENT_ON_TARGET)).compact();
            }
         }

         Style childParentStyle = optimized.style();
         if (parentStyle != null) {
            childParentStyle = childParentStyle.merge(parentStyle, Style.Merge.Strategy.IF_ABSENT_ON_TARGET);
         }

         List<Component> childrenToAppend = new ArrayList<>(children.size());

         for (Component child : children) {
            child = compact(child, childParentStyle);
            if (child.children().isEmpty() && isText(child)) {
               TextComponent textComponent = (TextComponent)child;
               if (textComponent.content().isEmpty()) {
                  continue;
               }
            }

            childrenToAppend.add(child);
         }

         if (isText(optimized)) {
            while (!childrenToAppend.isEmpty()) {
               Component child = childrenToAppend.getFirst();
               Style childStyle = child.style().merge(childParentStyle, Style.Merge.Strategy.IF_ABSENT_ON_TARGET);
               if (!isText(child) || !Objects.equals(childStyle, childParentStyle)) {
                  break;
               }

               optimized = joinText((TextComponent)optimized, (TextComponent)child);
               childrenToAppend.removeFirst();
               childrenToAppend.addAll(0, child.children());
            }
         }

         int i = 0;

         while (i + 1 < childrenToAppend.size()) {
            Component child = childrenToAppend.get(i);
            Component neighbor = childrenToAppend.get(i + 1);
            if (child.children().isEmpty() && isText(child) && isText(neighbor)) {
               Style childStyle = child.style().merge(childParentStyle, Style.Merge.Strategy.IF_ABSENT_ON_TARGET);
               Style neighborStyle = neighbor.style().merge(childParentStyle, Style.Merge.Strategy.IF_ABSENT_ON_TARGET);
               if (childStyle.equals(neighborStyle)) {
                  Component combined = joinText((TextComponent)child, (TextComponent)neighbor);
                  childrenToAppend.set(i, combined);
                  childrenToAppend.remove(i + 1);
                  continue;
               }
            }

            i++;
         }

         if (childrenToAppend.isEmpty() && isBlank(optimized)) {
            optimized = optimized.style(simplifyStyleForBlank(optimized.style(), parentStyle));
         }

         return optimized.children(childrenToAppend);
      }
   }

   private static boolean isBlank(final Component component) {
      if (isText(component)) {
         TextComponent textComponent = (TextComponent)component;
         String content = textComponent.content();

         for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c != ' ') {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static Style simplifyStyleForBlank(final Style style, @Nullable final Style parentStyle) {
      return style;
   }

   private static TextComponent joinText(final TextComponent one, final TextComponent two) {
      return TextComponentImpl.create(two.children(), one.style(), one.content() + two.content());
   }

   private static boolean isText(final Component component) {
      return component instanceof TextComponent && !(component instanceof VirtualComponent);
   }
}
