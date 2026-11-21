package ac.grim.grimac.internal.storage.backend.sqlite.writers;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.sql.SQLException;

@ApiStatus.Internal
public interface SettingsUpserter extends AutoCloseable {
   void addBatch(String var1, String var2, String var3, byte[] var4, long var5) throws SQLException;

   void executeBatch() throws SQLException;

   @Override
   void close() throws SQLException;
}
