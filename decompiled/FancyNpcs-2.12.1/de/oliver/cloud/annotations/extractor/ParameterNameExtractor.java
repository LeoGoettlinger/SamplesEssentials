package de.oliver.cloud.annotations.extractor;

import java.lang.reflect.Parameter;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public interface ParameterNameExtractor {
   @NonNull
   static ParameterNameExtractor simple() {
      return Parameter::getName;
   }

   @NonNull
   static ParameterNameExtractor withTransformation(@NonNull Function<String, String> transformation) {
      return parameter -> transformation.apply(parameter.getName());
   }

   @NonNull
   String extract(@NonNull Parameter parameter);
}
