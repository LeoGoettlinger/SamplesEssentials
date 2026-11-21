package de.oliver.cloud.annotations.method;

import de.oliver.cloud.annotations.descriptor.Descriptor;
import java.lang.reflect.Parameter;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Generated;

@API(
   status = Status.INTERNAL,
   consumers = {"de.oliver.cloud.*"}
)
@Generated(
   from = "ParameterValue",
   generator = "Immutables"
)
final class ParameterValueImpl implements ParameterValue {
   @NonNull
   private final Parameter parameter;
   @Nullable
   private final Object value;
   @Nullable
   private final Descriptor descriptor;

   private ParameterValueImpl(@NonNull Parameter parameter, @Nullable Object value, @Nullable Descriptor descriptor) {
      this.parameter = Objects.requireNonNull(parameter, "parameter");
      this.value = value;
      this.descriptor = descriptor;
   }

   private ParameterValueImpl(ParameterValueImpl original, @NonNull Parameter parameter, @Nullable Object value, @Nullable Descriptor descriptor) {
      this.parameter = parameter;
      this.value = value;
      this.descriptor = descriptor;
   }

   @NonNull
   @Override
   public Parameter parameter() {
      return this.parameter;
   }

   @Nullable
   @Override
   public Object value() {
      return this.value;
   }

   @Nullable
   @Override
   public Descriptor descriptor() {
      return this.descriptor;
   }

   public final ParameterValueImpl withParameter(Parameter value) {
      if (this.parameter == value) {
         return this;
      } else {
         Parameter newValue = Objects.requireNonNull(value, "parameter");
         return new ParameterValueImpl(this, newValue, this.value, this.descriptor);
      }
   }

   public final ParameterValueImpl withValue(@Nullable Object value) {
      return this.value == value ? this : new ParameterValueImpl(this, this.parameter, value, this.descriptor);
   }

   public final ParameterValueImpl withDescriptor(@Nullable Descriptor value) {
      return this.descriptor == value ? this : new ParameterValueImpl(this, this.parameter, this.value, value);
   }

   @Override
   public boolean equals(Object another) {
      return this == another ? true : another instanceof ParameterValueImpl && this.equalsByValue((ParameterValueImpl)another);
   }

   private boolean equalsByValue(ParameterValueImpl another) {
      return this.parameter.equals(another.parameter) && Objects.equals(this.value, another.value) && Objects.equals(this.descriptor, another.descriptor);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.parameter.hashCode();
      h += (h << 5) + Objects.hashCode(this.value);
      return h + (h << 5) + Objects.hashCode(this.descriptor);
   }

   @Override
   public String toString() {
      return "ParameterValue{parameter=" + this.parameter + ", value=" + this.value + ", descriptor=" + this.descriptor + "}";
   }

   public static ParameterValueImpl of(@NonNull Parameter parameter, @Nullable Object value, @Nullable Descriptor descriptor) {
      return new ParameterValueImpl(parameter, value, descriptor);
   }

   public static ParameterValueImpl copyOf(ParameterValue instance) {
      return instance instanceof ParameterValueImpl ? (ParameterValueImpl)instance : of(instance.parameter(), instance.value(), instance.descriptor());
   }
}
