package org.postgresql.plugin;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.PSQLException;

public interface AuthenticationPlugin {
   @Nullable
   char[] getPassword(AuthenticationRequestType var1) throws PSQLException;
}
