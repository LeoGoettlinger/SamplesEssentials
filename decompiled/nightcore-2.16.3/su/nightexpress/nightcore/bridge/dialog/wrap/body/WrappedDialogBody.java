package su.nightexpress.nightcore.bridge.dialog.wrap.body;

import java.util.function.UnaryOperator;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.dialog.adapter.DialogBodyAdapter;

public interface WrappedDialogBody {
   @NonNull
   <D> D adapt(@NonNull DialogBodyAdapter<D> var1);

   @Deprecated
   @NonNull
   WrappedDialogBody replace(@NonNull UnaryOperator<String> var1);
}
