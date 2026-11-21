package com.earth2me.essentials.libs.kyori.adventure.text;

import com.earth2me.essentials.libs.kyori.adventure.util.Buildable;
import org.jetbrains.annotations.NotNull;

@Deprecated
public interface BuildableComponent<C extends BuildableComponent<C, B>, B extends ComponentBuilder<C, B>> extends Buildable<C, B>, Component {
   /** @deprecated */
   @NotNull
   @Override
   B toBuilder();
}
