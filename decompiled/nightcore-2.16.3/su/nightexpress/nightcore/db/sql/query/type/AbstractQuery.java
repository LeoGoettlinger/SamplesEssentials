package su.nightexpress.nightcore.db.sql.query.type;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.db.sql.util.SQLUtils;

@Deprecated
public abstract class AbstractQuery<T> {
   @NonNull
   public String createSQL(@NonNull String table) {
      return this.buildSQL(SQLUtils.escape(table));
   }

   public abstract boolean isEmpty();

   public abstract void onExecute(@NonNull PreparedStatement var1, @NonNull T var2) throws SQLException;

   @NonNull
   protected abstract String buildSQL(@NonNull String var1);
}
