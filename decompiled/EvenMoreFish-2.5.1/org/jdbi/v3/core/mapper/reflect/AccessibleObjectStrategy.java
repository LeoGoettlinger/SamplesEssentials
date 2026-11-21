package org.jdbi.v3.core.mapper.reflect;

import java.lang.reflect.AccessibleObject;
import java.util.function.Consumer;
import org.jdbi.v3.meta.Alpha;

@Alpha
public enum AccessibleObjectStrategy implements Consumer<AccessibleObject> {
   FORCE_MAKE_ACCESSIBLE {
      public void accept(AccessibleObject accessibleObject) {
         accessibleObject.setAccessible(true);
      }
   },
   DO_NOT_MAKE_ACCESSIBLE {
      public void accept(AccessibleObject accessibleObject) {
      }
   };
}
