package com.sk89q.worldedit.util.formatting.text.renderer;

import com.sk89q.worldedit.util.formatting.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface ComponentRenderer<C> {
   @NonNull
   Component render(@NonNull final Component component, @NonNull final C context);
}
