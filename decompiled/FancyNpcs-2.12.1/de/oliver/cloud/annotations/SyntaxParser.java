package de.oliver.cloud.annotations;

import java.lang.reflect.Method;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(
   status = Status.STABLE
)
public interface SyntaxParser {
   @NonNull
   List<SyntaxFragment> parseSyntax(@Nullable Method method, @NonNull String string);
}
