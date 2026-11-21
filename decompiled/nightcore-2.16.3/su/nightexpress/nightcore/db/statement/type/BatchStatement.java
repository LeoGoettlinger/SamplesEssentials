package su.nightexpress.nightcore.db.statement.type;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.db.config.DatabaseType;
import su.nightexpress.nightcore.db.statement.condition.Wheres;

public interface BatchStatement<T> {
   @NonNull
   String toSql(@NonNull DatabaseType var1, @NonNull String var2, @Nullable Wheres<T> var3);

   void prepare(@NonNull PreparedStatement var1, @NonNull T var2, @Nullable Wheres<T> var3) throws SQLException;
}
