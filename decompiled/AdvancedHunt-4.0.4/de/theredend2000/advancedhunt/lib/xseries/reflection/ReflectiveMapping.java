package de.theredend2000.advancedhunt.lib.xseries.reflection;

import de.theredend2000.advancedhunt.lib.xseries.reflection.jvm.NamedReflectiveHandle;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public interface ReflectiveMapping {
   boolean shouldBeChecked();

   String category();

   String name();

   String process(NamedReflectiveHandle var1, String var2);
}
