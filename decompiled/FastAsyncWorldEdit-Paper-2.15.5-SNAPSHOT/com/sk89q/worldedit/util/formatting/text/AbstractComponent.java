package com.sk89q.worldedit.util.formatting.text;

import com.sk89q.worldedit.util.formatting.text.format.Style;
import com.sk89q.worldedit.util.formatting.text.util.ShadyPines;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractComponent implements Component {
   protected static final List<Component> EMPTY_COMPONENT_LIST = Collections.emptyList();
   protected final List<Component> children;
   protected final Style style;

   static List<Component> unmodifiableCopy(final List<? extends Component> list) {
      return list.isEmpty() ? EMPTY_COMPONENT_LIST : Collections.unmodifiableList(new ArrayList<>(list));
   }

   protected AbstractComponent(@NonNull final List<Component> children, @NonNull final Style style) {
      this.children = unmodifiableCopy(children);
      this.style = style;
   }

   @NonNull
   @Override
   public List<Component> children() {
      return this.children;
   }

   @NonNull
   @Override
   public Style style() {
      return this.style;
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else {
         return !(other instanceof AbstractComponent) ? false : this.equals((AbstractComponent)other);
      }
   }

   protected boolean equals(@NonNull final AbstractComponent that) {
      return Objects.equals(this.children, that.children) && Objects.equals(this.style, that.style);
   }

   @Override
   public int hashCode() {
      int result = this.children.hashCode();
      return 31 * result + this.style.hashCode();
   }

   @Override
   public String toString() {
      return ShadyPines.toString(this, map -> {
         this.populateToString(map);
         map.put("children", this.children);
         map.put("style", this.style);
      });
   }

   protected void populateToString(@NonNull final Map<String, Object> builder) {
   }
}
