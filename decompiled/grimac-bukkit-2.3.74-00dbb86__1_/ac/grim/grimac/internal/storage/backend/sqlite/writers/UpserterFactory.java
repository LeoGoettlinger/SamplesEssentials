package ac.grim.grimac.internal.storage.backend.sqlite.writers;

import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.sql.Connection;
import java.sql.SQLException;

@ApiStatus.Internal
public interface UpserterFactory {
   UpserterFactory MODERN = new ModernUpserterFactory();
   UpserterFactory LEGACY = new LegacyUpserterFactory();

   SessionUpserter newSessionUpserter(Connection var1, TableNames var2) throws SQLException;

   IdentityUpserter newIdentityUpserter(Connection var1, TableNames var2) throws SQLException;

   SettingsUpserter newSettingsUpserter(Connection var1, TableNames var2) throws SQLException;
}
