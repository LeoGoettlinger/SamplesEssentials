package net.luckperms.api.util;

import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Page<T> {
   @NonNull
   List<T> entries();

   int overallSize();
}
