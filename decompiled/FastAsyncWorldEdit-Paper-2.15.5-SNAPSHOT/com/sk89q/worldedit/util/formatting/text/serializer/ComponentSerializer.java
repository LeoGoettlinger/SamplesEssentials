package com.sk89q.worldedit.util.formatting.text.serializer;

import com.sk89q.worldedit.util.formatting.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface ComponentSerializer<I extends Component, O extends Component, R> {
   @NonNull
   O deserialize(@NonNull final R input);

   @NonNull
   R serialize(@NonNull final I component);
}
