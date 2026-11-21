package org.enginehub.linbus.tree;

public abstract sealed class LinNumberTag<T extends Number> extends LinTag<T> permits LinByteTag, LinDoubleTag, LinFloatTag, LinIntTag, LinLongTag, LinShortTag {
   protected LinNumberTag() {
   }
}
