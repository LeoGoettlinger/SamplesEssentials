package com.sk89q.jnbt;

import java.util.Map;
import org.enginehub.linbus.tree.LinCompoundTag;

@Deprecated
public abstract class LazyCompoundTag extends CompoundTag {
   public LazyCompoundTag(Map<String, Tag<?, ?>> value) {
      super(value);
   }

   public LazyCompoundTag(LinCompoundTag adventureTag) {
      super(adventureTag);
   }

   public abstract LinCompoundTag toLinTag();
}
