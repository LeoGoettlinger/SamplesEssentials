package org.sethomegui.SetHomeGUI.libs.utils.supplier;

import java.util.Set;
import org.jetbrains.annotations.NotNull;

public interface SetSupplier {
   @NotNull
   <T> Set<T> supply(int var1);
}
