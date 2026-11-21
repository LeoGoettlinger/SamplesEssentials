package su.nightexpress.nightcore.bridge.dialog.wrap.type;

import java.util.function.UnaryOperator;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.dialog.adapter.DialogTypeAdapter;
import su.nightexpress.nightcore.bridge.dialog.wrap.button.WrappedActionButton;

public record WrappedNoticeType(@NonNull WrappedActionButton action) implements WrappedDialogType {
   @NonNull
   @Override
   public <T> T adapt(@NonNull DialogTypeAdapter<T> factory) {
      return factory.adaptType(this);
   }

   @NonNull
   public WrappedNoticeType replace(@NonNull UnaryOperator<String> operator) {
      return new WrappedNoticeType(this.action.replace(operator));
   }
}
