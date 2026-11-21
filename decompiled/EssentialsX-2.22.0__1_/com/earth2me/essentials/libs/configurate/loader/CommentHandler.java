package com.earth2me.essentials.libs.configurate.loader;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.google.errorprone.annotations.Immutable;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Stream;

@Immutable
public interface CommentHandler {
   @Nullable
   String extractHeader(BufferedReader reader) throws IOException;

   Stream<String> toComment(Stream<String> lines);
}
