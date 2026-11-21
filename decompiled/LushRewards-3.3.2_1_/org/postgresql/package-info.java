@DefaultQualifier.List({@DefaultQualifier(
      value = NonNull.class,
      locations = {TypeUseLocation.FIELD}
   ), @DefaultQualifier(
      value = NonNull.class,
      locations = {TypeUseLocation.PARAMETER}
   ), @DefaultQualifier(
      value = NonNull.class,
      locations = {TypeUseLocation.RETURN}
   )})
package org.postgresql;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.checkerframework.framework.qual.TypeUseLocation;
