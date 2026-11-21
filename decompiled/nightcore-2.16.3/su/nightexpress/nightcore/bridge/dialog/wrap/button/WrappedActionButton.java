package su.nightexpress.nightcore.bridge.dialog.wrap.button;

import java.util.function.UnaryOperator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.dialog.DialogDefaults;
import su.nightexpress.nightcore.bridge.dialog.adapter.DialogButtonAdapter;
import su.nightexpress.nightcore.bridge.dialog.wrap.action.WrappedDialogAction;
import su.nightexpress.nightcore.bridge.placeholder.PlaceholderReplacer;

@NullMarked
public record WrappedActionButton(String label, @Nullable String tooltip, int width, @Nullable WrappedDialogAction action) {
   public <B> B adapt(DialogButtonAdapter<B> adapter) {
      return adapter.adaptButton(this);
   }

   @Deprecated
   public WrappedActionButton replace(UnaryOperator<String> operator) {
      return new WrappedActionButton(operator.apply(this.label), this.tooltip == null ? null : operator.apply(this.tooltip), this.width, this.action);
   }

   public static final class Builder {
      private final String label;
      private int width = 150;
      @Nullable
      private String tooltip;
      @Nullable
      private WrappedDialogAction action;
      @Nullable
      private PlaceholderReplacer replacer;

      public Builder(String label) {
         this(label, null);
      }

      public Builder(String label, @Nullable String tooltip) {
         this.label = label;
         this.tooltip(tooltip);
      }

      public WrappedActionButton.Builder tooltip(@Nullable String tooltip) {
         this.tooltip = tooltip;
         return this;
      }

      public WrappedActionButton.Builder width(int width) {
         this.width = DialogDefaults.clampWidth(width);
         return this;
      }

      public WrappedActionButton.Builder action(@Nullable WrappedDialogAction action) {
         this.action = action;
         return this;
      }

      public WrappedActionButton.Builder placeholders(@Nullable PlaceholderReplacer replacer) {
         this.replacer = replacer;
         return this;
      }

      public WrappedActionButton build() {
         PlaceholderReplacer replacer = this.replacer;
         String label = this.label;
         String tooltip = this.tooltip;
         if (replacer != null) {
            label = replacer.apply(label);
            if (tooltip != null) {
               tooltip = replacer.apply(tooltip);
            }
         }

         return new WrappedActionButton(label, tooltip, this.width, this.action);
      }
   }
}
