package de.oliver.cloud.annotations.extractor;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import de.oliver.cloud.annotations.AnnotationParser;
import de.oliver.cloud.annotations.DescriptionMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Generated;

@API(
   status = Status.STABLE,
   consumers = {"de.oliver.cloud.*"}
)
@Generated(
   from = "StandardArgumentExtractor",
   generator = "Immutables"
)
public final class ImmutableStandardArgumentExtractor extends StandardArgumentExtractor {
   @NonNull
   private final AnnotationParser<?> annotationParser;
   @NonNull
   private final ParameterNameExtractor parameterNameExtractor;
   @NonNull
   private final DescriptionMapper descriptionMapper;
   private static final byte STAGE_INITIALIZING = -1;
   private static final byte STAGE_UNINITIALIZED = 0;
   private static final byte STAGE_INITIALIZED = 1;
   private transient volatile ImmutableStandardArgumentExtractor.InitShim initShim = new ImmutableStandardArgumentExtractor.InitShim();

   private ImmutableStandardArgumentExtractor(
      @NonNull AnnotationParser<?> annotationParser, @NonNull ParameterNameExtractor parameterNameExtractor, @NonNull DescriptionMapper descriptionMapper
   ) {
      this.annotationParser = Objects.requireNonNull(annotationParser, "annotationParser");
      this.parameterNameExtractor = Objects.requireNonNull(parameterNameExtractor, "parameterNameExtractor");
      this.descriptionMapper = Objects.requireNonNull(descriptionMapper, "descriptionMapper");
      this.initShim = null;
   }

   private ImmutableStandardArgumentExtractor(ImmutableStandardArgumentExtractor.Builder builder) {
      this.annotationParser = builder.annotationParser;
      if (builder.parameterNameExtractor != null) {
         this.initShim.parameterNameExtractor(builder.parameterNameExtractor);
      }

      if (builder.descriptionMapper != null) {
         this.initShim.descriptionMapper(builder.descriptionMapper);
      }

      this.parameterNameExtractor = this.initShim.parameterNameExtractor();
      this.descriptionMapper = this.initShim.descriptionMapper();
      this.initShim = null;
   }

   private ImmutableStandardArgumentExtractor(
      ImmutableStandardArgumentExtractor original,
      @NonNull AnnotationParser<?> annotationParser,
      @NonNull ParameterNameExtractor parameterNameExtractor,
      @NonNull DescriptionMapper descriptionMapper
   ) {
      this.annotationParser = annotationParser;
      this.parameterNameExtractor = parameterNameExtractor;
      this.descriptionMapper = descriptionMapper;
      this.initShim = null;
   }

   @NonNull
   @Override
   public AnnotationParser<?> annotationParser() {
      return this.annotationParser;
   }

   @NonNull
   @Override
   public ParameterNameExtractor parameterNameExtractor() {
      ImmutableStandardArgumentExtractor.InitShim shim = this.initShim;
      return shim != null ? shim.parameterNameExtractor() : this.parameterNameExtractor;
   }

   @NonNull
   @Override
   public DescriptionMapper descriptionMapper() {
      ImmutableStandardArgumentExtractor.InitShim shim = this.initShim;
      return shim != null ? shim.descriptionMapper() : this.descriptionMapper;
   }

   public final ImmutableStandardArgumentExtractor withAnnotationParser(AnnotationParser<?> value) {
      if (this.annotationParser == value) {
         return this;
      } else {
         AnnotationParser<?> newValue = Objects.requireNonNull(value, "annotationParser");
         return new ImmutableStandardArgumentExtractor(this, newValue, this.parameterNameExtractor, this.descriptionMapper);
      }
   }

   public final ImmutableStandardArgumentExtractor withParameterNameExtractor(ParameterNameExtractor value) {
      if (this.parameterNameExtractor == value) {
         return this;
      } else {
         ParameterNameExtractor newValue = Objects.requireNonNull(value, "parameterNameExtractor");
         return new ImmutableStandardArgumentExtractor(this, this.annotationParser, newValue, this.descriptionMapper);
      }
   }

   public final ImmutableStandardArgumentExtractor withDescriptionMapper(DescriptionMapper value) {
      if (this.descriptionMapper == value) {
         return this;
      } else {
         DescriptionMapper newValue = Objects.requireNonNull(value, "descriptionMapper");
         return new ImmutableStandardArgumentExtractor(this, this.annotationParser, this.parameterNameExtractor, newValue);
      }
   }

   @Override
   public boolean equals(Object another) {
      return this == another ? true : another instanceof ImmutableStandardArgumentExtractor && this.equalsByValue((ImmutableStandardArgumentExtractor)another);
   }

   private boolean equalsByValue(ImmutableStandardArgumentExtractor another) {
      return this.annotationParser.equals(another.annotationParser)
         && this.parameterNameExtractor.equals(another.parameterNameExtractor)
         && this.descriptionMapper.equals(another.descriptionMapper);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.annotationParser.hashCode();
      h += (h << 5) + this.parameterNameExtractor.hashCode();
      return h + (h << 5) + this.descriptionMapper.hashCode();
   }

   @Override
   public String toString() {
      return "StandardArgumentExtractor{annotationParser="
         + this.annotationParser
         + ", parameterNameExtractor="
         + this.parameterNameExtractor
         + ", descriptionMapper="
         + this.descriptionMapper
         + "}";
   }

   public static ImmutableStandardArgumentExtractor of(
      @NonNull AnnotationParser<?> annotationParser, @NonNull ParameterNameExtractor parameterNameExtractor, @NonNull DescriptionMapper descriptionMapper
   ) {
      return new ImmutableStandardArgumentExtractor(annotationParser, parameterNameExtractor, descriptionMapper);
   }

   public static ImmutableStandardArgumentExtractor copyOf(StandardArgumentExtractor instance) {
      return instance instanceof ImmutableStandardArgumentExtractor ? (ImmutableStandardArgumentExtractor)instance : builder().from(instance).build();
   }

   public static ImmutableStandardArgumentExtractor.Builder builder() {
      return new ImmutableStandardArgumentExtractor.Builder();
   }

   @Generated(
      from = "StandardArgumentExtractor",
      generator = "Immutables"
   )
   public static final class Builder {
      private static final long INIT_BIT_ANNOTATION_PARSER = 1L;
      private long initBits = 1L;
      @NonNull
      private AnnotationParser<?> annotationParser;
      @NonNull
      private ParameterNameExtractor parameterNameExtractor;
      @NonNull
      private DescriptionMapper descriptionMapper;

      private Builder() {
      }

      @CanIgnoreReturnValue
      public final ImmutableStandardArgumentExtractor.Builder from(StandardArgumentExtractor instance) {
         Objects.requireNonNull(instance, "instance");
         this.annotationParser(instance.annotationParser());
         this.parameterNameExtractor(instance.parameterNameExtractor());
         this.descriptionMapper(instance.descriptionMapper());
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableStandardArgumentExtractor.Builder annotationParser(@NonNull AnnotationParser<?> annotationParser) {
         this.annotationParser = Objects.requireNonNull(annotationParser, "annotationParser");
         this.initBits &= -2L;
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableStandardArgumentExtractor.Builder parameterNameExtractor(@NonNull ParameterNameExtractor parameterNameExtractor) {
         this.parameterNameExtractor = Objects.requireNonNull(parameterNameExtractor, "parameterNameExtractor");
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableStandardArgumentExtractor.Builder descriptionMapper(@NonNull DescriptionMapper descriptionMapper) {
         this.descriptionMapper = Objects.requireNonNull(descriptionMapper, "descriptionMapper");
         return this;
      }

      public ImmutableStandardArgumentExtractor build() {
         if (this.initBits != 0L) {
            throw new IllegalStateException(this.formatRequiredAttributesMessage());
         } else {
            return new ImmutableStandardArgumentExtractor(this);
         }
      }

      private String formatRequiredAttributesMessage() {
         List<String> attributes = new ArrayList<>();
         if ((this.initBits & 1L) != 0L) {
            attributes.add("annotationParser");
         }

         return "Cannot build StandardArgumentExtractor, some of required attributes are not set " + attributes;
      }
   }

   @Generated(
      from = "StandardArgumentExtractor",
      generator = "Immutables"
   )
   private final class InitShim {
      private byte parameterNameExtractorBuildStage = 0;
      @NonNull
      private ParameterNameExtractor parameterNameExtractor;
      private byte descriptionMapperBuildStage = 0;
      @NonNull
      private DescriptionMapper descriptionMapper;

      private InitShim() {
      }

      ParameterNameExtractor parameterNameExtractor() {
         if (this.parameterNameExtractorBuildStage == -1) {
            throw new IllegalStateException(this.formatInitCycleMessage());
         } else {
            if (this.parameterNameExtractorBuildStage == 0) {
               this.parameterNameExtractorBuildStage = -1;
               ParameterNameExtractor computedValue = ImmutableStandardArgumentExtractor.super.parameterNameExtractor();
               this.parameterNameExtractor = Objects.requireNonNull(computedValue, "parameterNameExtractor");
               this.parameterNameExtractorBuildStage = 1;
            }

            return this.parameterNameExtractor;
         }
      }

      void parameterNameExtractor(@NonNull ParameterNameExtractor parameterNameExtractor) {
         this.parameterNameExtractor = parameterNameExtractor;
         this.parameterNameExtractorBuildStage = 1;
      }

      DescriptionMapper descriptionMapper() {
         if (this.descriptionMapperBuildStage == -1) {
            throw new IllegalStateException(this.formatInitCycleMessage());
         } else {
            if (this.descriptionMapperBuildStage == 0) {
               this.descriptionMapperBuildStage = -1;
               DescriptionMapper computedValue = ImmutableStandardArgumentExtractor.super.descriptionMapper();
               this.descriptionMapper = Objects.requireNonNull(computedValue, "descriptionMapper");
               this.descriptionMapperBuildStage = 1;
            }

            return this.descriptionMapper;
         }
      }

      void descriptionMapper(@NonNull DescriptionMapper descriptionMapper) {
         this.descriptionMapper = descriptionMapper;
         this.descriptionMapperBuildStage = 1;
      }

      private String formatInitCycleMessage() {
         List<String> attributes = new ArrayList<>();
         if (this.parameterNameExtractorBuildStage == -1) {
            attributes.add("parameterNameExtractor");
         }

         if (this.descriptionMapperBuildStage == -1) {
            attributes.add("descriptionMapper");
         }

         return "Cannot build StandardArgumentExtractor, attribute initializers form cycle " + attributes;
      }
   }
}
