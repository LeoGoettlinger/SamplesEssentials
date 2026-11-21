package net.kyori.adventure.text;

import net.kyori.adventure.util.Buildable;
import org.jetbrains.annotations.NotNull;

@Deprecated
public interface BuildableComponent<C extends BuildableComponent<C, B>, B extends ComponentBuilder<C, B>> extends Buildable<C, B>, Component {
   /** @deprecated */
   @NotNull
   @Override
   B toBuilder();
}
