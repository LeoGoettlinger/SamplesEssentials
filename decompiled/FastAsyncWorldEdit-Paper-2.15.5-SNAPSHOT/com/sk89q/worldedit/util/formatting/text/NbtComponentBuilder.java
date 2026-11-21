package com.sk89q.worldedit.util.formatting.text;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface NbtComponentBuilder<C extends NbtComponent<C, B>, B extends NbtComponentBuilder<C, B>> extends ComponentBuilder<C, B> {
   @NonNull
   B nbtPath(@NonNull final String nbtPath);

   @NonNull
   B interpret(final boolean interpret);
}
