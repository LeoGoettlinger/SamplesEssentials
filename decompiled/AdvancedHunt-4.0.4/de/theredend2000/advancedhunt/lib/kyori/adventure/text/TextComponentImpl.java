package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

import de.theredend2000.advancedhunt.lib.kyori.adventure.internal.properties.AdventureProperties;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.Style;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.Nag;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;

sealed class TextComponentImpl implements TextComponent permits VirtualComponentImpl {
   private static final boolean WARN_WHEN_LEGACY_FORMATTING_DETECTED = Boolean.TRUE
      .equals(AdventureProperties.TEXT_WARN_WHEN_LEGACY_FORMATTING_DETECTED.value());
   @VisibleForTesting
   static final char SECTION_CHAR = '§';
   static final TextComponent EMPTY = createDirect("");
   static final TextComponent NEWLINE = createDirect("\n");
   static final TextComponent SPACE = createDirect(" ");
   private final String content;
   private final List<Component> children;
   private final Style style;

   static TextComponent create(final List<? extends ComponentLike> children, final Style style, final String content) {
      List<Component> filteredChildren = ComponentLike.asComponents(children, IS_NOT_EMPTY);
      return (TextComponent)(filteredChildren.isEmpty() && style.isEmpty() && content.isEmpty()
         ? Component.empty()
         : new TextComponentImpl(filteredChildren, Objects.requireNonNull(style, "style"), Objects.requireNonNull(content, "content")));
   }

   TextComponent create0(final List<? extends ComponentLike> children, final Style style, final String content) {
      return create(children, style, content);
   }

   private static TextComponent createDirect(final String content) {
      return new TextComponentImpl(List.of(), Style.empty(), content);
   }

   TextComponentImpl(final List<Component> children, final Style style, final String content) {
      this.content = content;
      this.children = children;
      this.style = style;
      if (WARN_WHEN_LEGACY_FORMATTING_DETECTED) {
         LegacyFormattingDetected nag = this.warnWhenLegacyFormattingDetected();
         if (nag != null) {
            Nag.print(nag);
         }
      }
   }

   @VisibleForTesting
   @Nullable
   final LegacyFormattingDetected warnWhenLegacyFormattingDetected() {
      return this.content.indexOf(167) != -1 ? new LegacyFormattingDetected(this) : null;
   }

   @Unmodifiable
   @Override
   public List<Component> children() {
      return this.children;
   }

   @Override
   public Style style() {
      return this.style;
   }

   @Override
   public String content() {
      return this.content;
   }

   @Override
   public TextComponent content(final String content) {
      return (TextComponent)(Objects.equals(this.content, content) ? this : this.create0(this.children, this.style, content));
   }

   public TextComponent children(final List<? extends ComponentLike> children) {
      return this.create0(children, this.style, this.content);
   }

   public TextComponent style(final Style style) {
      return this.create0(this.children, style, this.content);
   }

   @Override
   public boolean equals(final Object o) {
      return !(o instanceof TextComponentImpl that)
         ? false
         : Objects.equals(this.content, that.content) && Objects.equals(this.children, that.children) && Objects.equals(this.style, that.style);
   }

   @Override
   public int hashCode() {
      int result = 0;
      result = 31 * result + this.content.hashCode();
      result = 31 * result + this.children.hashCode();
      return 31 * result + this.style.hashCode();
   }

   @Override
   public String toString() {
      return "TextComponentImpl{content='" + this.content + "', children=" + this.children + ", style=" + this.style + "}";
   }

   @Override
   public TextComponent.Builder toBuilder() {
      return new TextComponentImpl.BuilderImpl(this);
   }

   static sealed class BuilderImpl
      extends AbstractComponentBuilder<TextComponent, TextComponent.Builder>
      implements TextComponent.Builder
      permits VirtualComponentImpl.BuilderImpl {
      private String content = "";

      BuilderImpl() {
      }

      BuilderImpl(final TextComponent component) {
         super(component);
         this.content = component.content();
      }

      @Override
      public TextComponent.Builder content(final String content) {
         this.content = Objects.requireNonNull(content, "content");
         return this;
      }

      @Override
      public String content() {
         return this.content;
      }

      public TextComponent build() {
         return this.isEmpty() ? Component.empty() : TextComponentImpl.create(this.children, this.buildStyle(), this.content);
      }

      private boolean isEmpty() {
         return this.content.isEmpty() && this.children.isEmpty() && !this.hasStyle();
      }
   }
}
