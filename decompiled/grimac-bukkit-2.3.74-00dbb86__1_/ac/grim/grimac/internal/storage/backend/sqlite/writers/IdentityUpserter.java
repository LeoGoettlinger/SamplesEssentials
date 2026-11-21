package ac.grim.grimac.internal.storage.backend.sqlite.writers;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.sql.SQLException;
import java.util.UUID;

@ApiStatus.Internal
public interface IdentityUpserter extends AutoCloseable {
   void addBatch(UUID var1, String var2, long var3, long var5) throws SQLException;

   void executeBatch() throws SQLException;

   @Override
   void close() throws SQLException;
}
