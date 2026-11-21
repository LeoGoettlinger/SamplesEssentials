package xyz.lychee.lagfixer.libs.kyori.adventure.text;

import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.util.Buildable;

@Deprecated
public interface BuildableComponent<C extends BuildableComponent<C, B>, B extends ComponentBuilder<C, B>> extends Buildable<C, B>, Component {
   /** @deprecated */
   @NotNull
   @Override
   B toBuilder();
}
