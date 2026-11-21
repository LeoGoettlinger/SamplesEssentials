package su.nightexpress.nightcore.bridge.dialog.wrap.type;

import java.util.function.UnaryOperator;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.dialog.adapter.DialogTypeAdapter;

public interface WrappedDialogType {
   @NonNull
   <T> T adapt(@NonNull DialogTypeAdapter<T> var1);

   @Deprecated
   @NonNull
   WrappedDialogType replace(@NonNull UnaryOperator<String> var1);
}
