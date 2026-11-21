package org.evenmorefish.fish.libs.boostedyaml.utils.supplier;

import java.util.Set;
import org.jetbrains.annotations.NotNull;

public interface SetSupplier {
   @NotNull
   <T> Set<T> supply(int var1);
}
