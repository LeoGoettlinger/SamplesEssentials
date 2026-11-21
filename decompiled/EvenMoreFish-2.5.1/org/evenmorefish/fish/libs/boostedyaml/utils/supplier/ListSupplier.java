package org.evenmorefish.fish.libs.boostedyaml.utils.supplier;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface ListSupplier {
   @NotNull
   <T> List<T> supply(int var1);
}
