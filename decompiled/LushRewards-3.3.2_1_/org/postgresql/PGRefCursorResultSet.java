package org.postgresql;

import org.checkerframework.checker.nullness.qual.Nullable;

@Deprecated
public interface PGRefCursorResultSet {
   @Deprecated
   @Nullable
   String getRefCursor();
}
