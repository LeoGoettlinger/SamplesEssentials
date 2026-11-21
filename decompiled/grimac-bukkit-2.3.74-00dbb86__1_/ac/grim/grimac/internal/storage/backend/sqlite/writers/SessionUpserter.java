package ac.grim.grimac.internal.storage.backend.sqlite.writers;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.sql.SQLException;
import java.util.UUID;

@ApiStatus.Internal
public interface SessionUpserter extends AutoCloseable {
   void addBatch(UUID var1, UUID var2, String var3, long var4, long var6, long var8, String var10, String var11, int var12, String var13, String var14) throws SQLException;

   void executeBatch() throws SQLException;

   @Override
   void close() throws SQLException;
}
