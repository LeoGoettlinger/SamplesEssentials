package su.nightexpress.nightcore.bridge.dialog.wrap.input.single;

import java.util.List;
import java.util.function.UnaryOperator;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.dialog.DialogDefaults;
import su.nightexpress.nightcore.bridge.dialog.adapter.DialogInputAdapter;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.WrappedDialogInput;
import su.nightexpress.nightcore.util.Strings;

public record WrappedSingleOptionDialogInput(
   @NonNull String key, @NonNull String label, @NonNull List<WrappedSingleOptionEntry> entries, int width, boolean labelVisible
) implements WrappedDialogInput {
   @NonNull
   @Override
   public <I> I adapt(@NonNull DialogInputAdapter<I> adapter) {
      return adapter.adaptInput(this);
   }

   @NonNull
   public WrappedSingleOptionDialogInput replace(@NonNull UnaryOperator<String> operator) {
      return new WrappedSingleOptionDialogInput(this.key, operator.apply(this.label), List.copyOf(this.entries), this.width, this.labelVisible);
   }

   public static final class Builder {
      private final String key;
      private final List<WrappedSingleOptionEntry> entries;
      private final String label;
      private int width = 200;
      private boolean labelVisible = true;

      public Builder(@NonNull String key, @NonNull String label, @NonNull List<WrappedSingleOptionEntry> entries) {
         this.key = Strings.filterForVariable(key);
         this.entries = entries;
         this.label = label;
      }

      @NonNull
      public WrappedSingleOptionDialogInput.Builder width(int width) {
         this.width = DialogDefaults.clampWidth(width);
         return this;
      }

      @NonNull
      public WrappedSingleOptionDialogInput.Builder labelVisible(boolean labelVisible) {
         this.labelVisible = labelVisible;
         return this;
      }

      @NonNull
      public WrappedSingleOptionDialogInput build() {
         return new WrappedSingleOptionDialogInput(this.key, this.label, this.entries, this.width, this.labelVisible);
      }
   }
}
