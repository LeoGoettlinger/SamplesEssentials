package org.jdbi.v3.core.mapper;

import jakarta.annotation.Nonnull;
import org.jdbi.v3.meta.Alpha;

@Alpha
public interface PrefixedRowMapper<T> extends RowMapper<T> {
   @Nonnull
   String getPrefix();
}
