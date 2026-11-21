package com.sk89q.worldedit.util.formatting.text;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface BuildableComponent<C extends BuildableComponent<C, B>, B extends ComponentBuilder<C, B>> extends Component {
   @NonNull
   B toBuilder();
}
