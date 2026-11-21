package com.sk89q.jnbt;

import com.google.common.base.Preconditions;
import org.enginehub.linbus.tree.LinStringTag;

@Deprecated
public final class StringTag extends Tag<String, LinStringTag> {
   public StringTag(String value) {
      super(LinStringTag.of((String)Preconditions.checkNotNull(value)));
   }

   public StringTag(LinStringTag tag) {
      super(tag);
   }

   public String getValue() {
      return this.linTag.value();
   }

   @Override
   public int getTypeCode() {
      return 8;
   }
}
