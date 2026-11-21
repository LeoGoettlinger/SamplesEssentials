package de.theredend2000.advancedhunt.lib.cloud.component.preprocessor;

import java.util.Collection;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
public interface PreprocessorHolder<C> {
   @NonNull
   Collection<ComponentPreprocessor<C>> preprocessors();
}
