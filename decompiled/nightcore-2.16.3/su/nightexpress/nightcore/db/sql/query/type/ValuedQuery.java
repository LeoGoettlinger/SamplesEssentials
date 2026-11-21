package su.nightexpress.nightcore.db.sql.query.type;

import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.db.sql.column.Column;

@Deprecated
public interface ValuedQuery<Q extends ValuedQuery<Q, T>, T> {
   @NonNull
   Q setValue(@NonNull Column var1, @NonNull Function<T, String> var2);
}
