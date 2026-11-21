package su.nightexpress.nightcore.bridge.dialog.wrap.type;

import java.util.List;
import java.util.function.UnaryOperator;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.dialog.adapter.DialogTypeAdapter;
import su.nightexpress.nightcore.bridge.dialog.wrap.button.WrappedActionButton;

public record WrappedMultiActionType(@NonNull List<WrappedActionButton> actions, @Nullable WrappedActionButton exitAction, int columns)
   implements WrappedDialogType {
   @NonNull
   @Override
   public <T> T adapt(@NonNull DialogTypeAdapter<T> factory) {
      return factory.adaptType(this);
   }

   @NonNull
   public WrappedMultiActionType replace(@NonNull UnaryOperator<String> operator) {
      List<WrappedActionButton> actions = this.actions.stream().map(other -> other.replace(operator)).toList();
      WrappedActionButton exitAction = this.exitAction == null ? null : this.exitAction.replace(operator);
      return new WrappedMultiActionType(actions, exitAction, this.columns);
   }

   public static final class Builder {
      private final List<WrappedActionButton> actions;
      private WrappedActionButton exitAction = null;
      private int columns = 2;

      public Builder(@NonNull List<WrappedActionButton> actions) {
         this.actions = actions;
      }

      @NonNull
      public WrappedMultiActionType.Builder exitAction(@Nullable WrappedActionButton exitAction) {
         this.exitAction = exitAction;
         return this;
      }

      @NonNull
      public WrappedMultiActionType.Builder columns(int columns) {
         this.columns = Math.max(1, columns);
         return this;
      }

      @NonNull
      public WrappedMultiActionType build() {
         return new WrappedMultiActionType(this.actions, this.exitAction, this.columns);
      }
   }
}
