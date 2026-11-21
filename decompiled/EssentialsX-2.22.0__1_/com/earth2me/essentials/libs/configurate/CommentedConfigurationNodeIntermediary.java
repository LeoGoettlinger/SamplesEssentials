package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;

public interface CommentedConfigurationNodeIntermediary<N extends CommentedConfigurationNodeIntermediary<N>> extends ScopedConfigurationNode<N> {
   @Nullable
   String comment();

   N comment(@Nullable String comment);

   N commentIfAbsent(String comment);
}
