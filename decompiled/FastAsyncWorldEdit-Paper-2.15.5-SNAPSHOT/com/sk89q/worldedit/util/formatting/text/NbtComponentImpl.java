package com.sk89q.worldedit.util.formatting.text;

import com.sk89q.worldedit.util.formatting.text.format.Style;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class NbtComponentImpl<C extends NbtComponent<C, B>, B extends NbtComponentBuilder<C, B>> extends AbstractComponent implements NbtComponent<C, B> {
   final String nbtPath;
   final boolean interpret;

   NbtComponentImpl(@NonNull final List<Component> children, @NonNull final Style style, final String nbtPath, final boolean interpret) {
      super(children, style);
      this.nbtPath = nbtPath;
      this.interpret = interpret;
   }

   @NonNull
   @Override
   public String nbtPath() {
      return this.nbtPath;
   }

   @Override
   public boolean interpret() {
      return this.interpret;
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof NbtComponent)) {
         return false;
      } else if (!super.equals(other)) {
         return false;
      } else {
         NbtComponent<?, ?> that = (NbtComponent<?, ?>)other;
         return Objects.equals(this.nbtPath, that.nbtPath()) && this.interpret == that.interpret();
      }
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + this.nbtPath.hashCode();
      return 31 * result + Boolean.hashCode(this.interpret);
   }

   @Override
   protected void populateToString(@NonNull final Map<String, Object> builder) {
      builder.put("nbtPath", this.nbtPath);
      builder.put("interpret", this.interpret);
   }

   abstract static class BuilderImpl<C extends NbtComponent<C, B>, B extends NbtComponentBuilder<C, B>>
      extends AbstractComponentBuilder<C, B>
      implements NbtComponentBuilder<C, B> {
      @Nullable
      String nbtPath;
      boolean interpret;

      BuilderImpl() {
      }

      BuilderImpl(@NonNull final C component) {
         super(component);
         this.nbtPath = component.nbtPath();
         this.interpret = component.interpret();
      }

      @NonNull
      @Override
      public B nbtPath(@NonNull final String nbtPath) {
         this.nbtPath = nbtPath;
         return (B)this;
      }

      @NonNull
      @Override
      public B interpret(final boolean interpret) {
         this.interpret = interpret;
         return (B)this;
      }
   }
}
