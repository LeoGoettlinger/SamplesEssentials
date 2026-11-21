package su.nightexpress.nightcore.db.statement.type;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.db.statement.condition.Wheres;

public interface QueryStatement<R> {
   @NonNull
   String toSql(@NonNull String var1, @Nullable Wheres var2, @Nullable Integer var3);

   @Nullable
   R map(@NonNull ResultSet var1) throws SQLException;
}
