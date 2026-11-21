package su.nightexpress.nightcore.dialog;

import org.jspecify.annotations.NonNull;

@Deprecated
public interface DialogHandler {
   boolean onInput(@NonNull Dialog var1, @NonNull WrappedInput var2);
}
