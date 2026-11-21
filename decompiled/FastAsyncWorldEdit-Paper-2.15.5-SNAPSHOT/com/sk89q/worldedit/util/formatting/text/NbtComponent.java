package com.sk89q.worldedit.util.formatting.text;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface NbtComponent<C extends NbtComponent<C, B>, B extends NbtComponentBuilder<C, B>> extends BuildableComponent<C, B> {
   @NonNull
   String nbtPath();

   @NonNull
   C nbtPath(@NonNull final String nbtPath);

   boolean interpret();

   @NonNull
   C interpret(final boolean interpret);
}
