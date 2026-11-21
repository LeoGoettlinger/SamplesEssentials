package com.sk89q.worldedit.util.formatting.text;

import com.sk89q.worldedit.util.formatting.text.format.Style;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class TextComponentImpl extends AbstractComponent implements TextComponent {
   static final TextComponent EMPTY = createDirect("");
   static final TextComponent NEWLINE = createDirect("\n");
   static final TextComponent SPACE = createDirect(" ");
   private final String content;

   @NonNull
   private static TextComponent createDirect(@NonNull final String content) {
      return new TextComponentImpl(EMPTY_COMPONENT_LIST, Style.empty(), content);
   }

   TextComponentImpl(@NonNull final List<Component> children, @NonNull final Style style, @NonNull final String content) {
      super(children, style);
      this.content = content;
   }

   @NonNull
   @Override
   public String content() {
      return this.content;
   }

   @NonNull
   @Override
   public TextComponent content(@NonNull final String content) {
      return Objects.equals(this.content, content) ? this : new TextComponentImpl(this.children, this.style, Objects.requireNonNull(content, "content"));
   }

   @Override
   public boolean isEmpty() {
      return this == EMPTY;
   }

   @NonNull
   public TextComponent children(@NonNull final List<Component> children) {
      return new TextComponentImpl(children, this.style, this.content);
   }

   @NonNull
   public TextComponent style(@NonNull final Style style) {
      return Objects.equals(this.style, style) ? this : new TextComponentImpl(this.children, style, this.content);
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof TextComponentImpl)) {
         return false;
      } else if (!super.equals(other)) {
         return false;
      } else {
         TextComponentImpl that = (TextComponentImpl)other;
         return Objects.equals(this.content, that.content);
      }
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      return 31 * result + this.content.hashCode();
   }

   @Override
   protected void populateToString(@NonNull final Map<String, Object> builder) {
      builder.put("content", this.content);
   }

   @NonNull
   public TextComponent.Builder toBuilder() {
      return new TextComponentImpl.BuilderImpl(this);
   }

   static final class BuilderImpl extends AbstractComponentBuilder<TextComponent, TextComponent.Builder> implements TextComponent.Builder {
      private String content = "";

      BuilderImpl() {
      }

      BuilderImpl(@NonNull final TextComponent component) {
         super(component);
         this.content = component.content();
      }

      @NonNull
      @Override
      public TextComponent.Builder content(@NonNull final String content) {
         this.content = Objects.requireNonNull(content, "content");
         return this;
      }

      @NonNull
      public TextComponent build() {
         return (TextComponent)(this.isEmpty() ? TextComponentImpl.EMPTY : new TextComponentImpl(this.children, this.buildStyle(), this.content));
      }

      private boolean isEmpty() {
         return this.content.isEmpty() && this.children.isEmpty() && !this.hasStyle();
      }
   }
}
