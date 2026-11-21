package net.luckperms.api.cacheddata;

import net.luckperms.api.query.QueryOptions;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface CachedData {
   @NonNull
   QueryOptions getQueryOptions();
}
