package su.nightexpress.nightcore.bridge.dialog.wrap.body;

import java.util.function.UnaryOperator;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.dialog.adapter.DialogBodyAdapter;

public record WrappedItemDialogBody(
   @NonNull ItemStack item, @Nullable WrappedPlainMessageDialogBody description, boolean showDecorations, boolean showTooltip, int width, int height
) implements WrappedDialogBody {
   @NonNull
   @Override
   public <D> D adapt(@NonNull DialogBodyAdapter<D> adapter) {
      return adapter.adaptBody(this);
   }

   @NonNull
   public WrappedItemDialogBody replace(@NonNull UnaryOperator<String> operator) {
      return new WrappedItemDialogBody(
         new ItemStack(this.item),
         this.description == null ? null : this.description.replace(operator),
         this.showDecorations,
         this.showTooltip,
         this.width,
         this.height
      );
   }

   public static final class Builder {
      private final ItemStack item;
      private WrappedPlainMessageDialogBody description;
      private boolean showDecorations = true;
      private boolean showTooltip = true;
      private int width = 16;
      private int height = 16;

      public Builder(@NonNull ItemStack item) {
         this.item = item;
      }

      @NonNull
      public WrappedItemDialogBody.Builder description(@Nullable WrappedPlainMessageDialogBody description) {
         this.description = description;
         return this;
      }

      @NonNull
      public WrappedItemDialogBody.Builder showDecorations(boolean showDecorations) {
         this.showDecorations = showDecorations;
         return this;
      }

      @NonNull
      public WrappedItemDialogBody.Builder showTooltip(boolean showTooltip) {
         this.showTooltip = showTooltip;
         return this;
      }

      @NonNull
      public WrappedItemDialogBody.Builder width(int width) {
         this.width = Math.clamp((long)width, 1, 256);
         return this;
      }

      @NonNull
      public WrappedItemDialogBody.Builder height(int height) {
         this.height = Math.clamp((long)height, 1, 256);
         return this;
      }

      @NonNull
      public WrappedItemDialogBody build() {
         return new WrappedItemDialogBody(this.item, this.description, this.showDecorations, this.showTooltip, this.width, this.height);
      }
   }
}
