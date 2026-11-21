package su.nightexpress.nightcore.db.statement;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface RowMapper<R> {
   @Nullable
   R map(@NonNull ResultSet var1) throws SQLException;
}
