package su.nightexpress.nightcore.bridge.dialog.wrap.input;

import java.util.function.UnaryOperator;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.dialog.adapter.DialogInputAdapter;

public interface WrappedDialogInput {
   @NonNull
   String key();

   @NonNull
   <I> I adapt(@NonNull DialogInputAdapter<I> var1);

   @Deprecated
   @NonNull
   WrappedDialogInput replace(@NonNull UnaryOperator<String> var1);
}
