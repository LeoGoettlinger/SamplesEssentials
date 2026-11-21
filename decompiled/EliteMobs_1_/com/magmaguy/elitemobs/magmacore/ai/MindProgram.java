package com.magmaguy.elitemobs.magmacore.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public final class MindProgram {
   private static final Pattern IDENTIFIER = Pattern.compile("[a-z0-9._-]+:[a-z0-9/._-]+");
   private final String identifier;
   private final long revision;
   private final MindSchema schema;
   private final List<MindSensor> sensors;
   private final List<MindBehavior> behaviors;
   private final MindExecutionPolicy executionPolicy;
   private final AtomicBoolean claimed = new AtomicBoolean();

   private MindProgram(MindProgram.Builder builder) {
      if (builder.identifier == null || !IDENTIFIER.matcher(builder.identifier).matches()) {
         throw new IllegalStateException("Mind program identifier must be a namespaced lower-case key");
      } else if (builder.revision < 1L) {
         throw new IllegalStateException("Mind program revision must be positive");
      } else {
         this.identifier = builder.identifier;
         this.revision = builder.revision;
         this.schema = builder.schema;
         this.sensors = immutableUnique(builder.sensors, "sensor");
         List<MindBehavior> orderedBehaviors = new ArrayList<>(builder.behaviors);
         orderedBehaviors.sort(Comparator.comparingInt(MindBehavior::priority).thenComparing(MindBehavior::identifier));
         this.behaviors = immutableUnique(orderedBehaviors, "behavior");
         this.executionPolicy = builder.executionPolicy;
      }
   }

   public static MindProgram.Builder builder(String identifier, long revision) {
      return new MindProgram.Builder(identifier, revision);
   }

   public String identifier() {
      return this.identifier;
   }

   public long revision() {
      return this.revision;
   }

   public MindSchema schema() {
      return this.schema;
   }

   public List<MindSensor> sensors() {
      return this.sensors;
   }

   public List<MindBehavior> behaviors() {
      return this.behaviors;
   }

   public MindExecutionPolicy executionPolicy() {
      return this.executionPolicy;
   }

   public void claimForBinding() {
      if (!this.claimed.compareAndSet(false, true)) {
         throw new IllegalStateException("Mind program instances cannot be shared across entity bindings: " + this.identifier);
      }
   }

   private static <T> List<T> immutableUnique(List<T> values, String kind) {
      Set<String> identifiers = new HashSet<>();

      for (T value : values) {
         String identifier = value instanceof MindSensor sensor ? sensor.identifier() : ((MindBehavior)value).identifier();
         if (identifier == null || identifier.isBlank()) {
            throw new IllegalStateException("Mind " + kind + " identifier cannot be blank");
         }

         if (!identifiers.add(identifier)) {
            throw new IllegalStateException("Duplicate mind " + kind + ": " + identifier);
         }

         if (value instanceof MindSensor sensorx && sensorx.intervalTicks() <= 0) {
            throw new IllegalStateException("Mind sensor interval must be positive: " + identifier);
         }

         if (value instanceof MindBehavior behavior) {
            Set<MindControl> controls = Objects.requireNonNull(behavior.controls(), "Mind behavior controls");
            if (controls.stream().anyMatch(Objects::isNull)) {
               throw new IllegalStateException("Mind behavior controls cannot contain null: " + identifier);
            }
         }
      }

      return Collections.unmodifiableList(new ArrayList<>(values));
   }

   public static final class Builder {
      private final String identifier;
      private final long revision;
      private MindSchema schema = MindSchema.empty();
      private final List<MindSensor> sensors = new ArrayList<>();
      private final List<MindBehavior> behaviors = new ArrayList<>();
      private MindExecutionPolicy executionPolicy = MindExecutionPolicy.DEFAULT;

      private Builder(String identifier, long revision) {
         this.identifier = identifier;
         this.revision = revision;
      }

      public MindProgram.Builder schema(MindSchema schema) {
         this.schema = Objects.requireNonNull(schema, "schema");
         return this;
      }

      public MindProgram.Builder sensor(MindSensor sensor) {
         this.sensors.add(Objects.requireNonNull(sensor, "sensor"));
         return this;
      }

      public MindProgram.Builder behavior(MindBehavior behavior) {
         this.behaviors.add(Objects.requireNonNull(behavior, "behavior"));
         return this;
      }

      public MindProgram.Builder executionPolicy(MindExecutionPolicy executionPolicy) {
         this.executionPolicy = Objects.requireNonNull(executionPolicy, "executionPolicy");
         return this;
      }

      public MindProgram build() {
         return new MindProgram(this);
      }
   }
}
