package ch.jalu.configme.utils;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.configurationdata.ConfigurationDataBuilder;
import ch.jalu.configme.migration.MigrationService;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.resource.PropertyReader;
import ch.jalu.configme.resource.PropertyResource;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SettingsHolderClassValidator {
   @SafeVarargs
   public final void validate(@NotNull Class<? extends SettingsHolder>... settingHolders) {
      this.validate(Arrays.asList(settingHolders));
   }

   public void validate(@NotNull Iterable<Class<? extends SettingsHolder>> settingHolders) {
      this.validateAllPropertiesAreConstants(settingHolders);
      this.validateSettingsHolderClassesFinal(settingHolders);
      this.validateClassesHaveHiddenNoArgConstructor(settingHolders);
      ConfigurationData configurationData = this.createConfigurationData(settingHolders);
      this.validateHasCommentOnEveryProperty(configurationData, null);
      this.validateCommentLengthsAreWithinBounds(configurationData, null, 90);
      this.validateHasAllEnumEntriesInComment(configurationData, null);
   }

   public void validateConfigurationDataValidForMigrationService(
      @NotNull ConfigurationData configurationData, @NotNull PropertyResource resource, @NotNull MigrationService migrationService
   ) {
      resource.exportProperties(configurationData);
      PropertyReader reader = resource.createReader();
      if (migrationService.checkAndMigrate(reader, configurationData)) {
         throw new IllegalStateException("Migration service unexpectedly returned that a migration is required");
      }
   }

   public void validateAllPropertiesAreConstants(@NotNull Iterable<Class<? extends SettingsHolder>> settingHolders) {
      List<String> invalidFields = new ArrayList<>();

      for (Class<? extends SettingsHolder> clazz : settingHolders) {
         List<String> invalidFieldsForClazz = this.getAllFields(clazz)
            .filter(field -> Property.class.isAssignableFrom(field.getType()))
            .filter(field -> !this.isValidConstantField(field))
            .map(field -> field.getDeclaringClass().getSimpleName() + "#" + field.getName())
            .collect(Collectors.toList());
         invalidFields.addAll(invalidFieldsForClazz);
      }

      if (!invalidFields.isEmpty()) {
         throw new IllegalStateException("The following fields were found not to be public static final:\n- " + String.join("\n- ", invalidFields));
      }
   }

   public void validateSettingsHolderClassesFinal(@NotNull Iterable<Class<? extends SettingsHolder>> settingHolders) {
      List<String> invalidClasses = new ArrayList<>();

      for (Class<? extends SettingsHolder> clazz : settingHolders) {
         if (!Modifier.isFinal(clazz.getModifiers())) {
            invalidClasses.add(clazz.getCanonicalName());
         }
      }

      if (!invalidClasses.isEmpty()) {
         throw new IllegalStateException("The following classes are not final:\n- " + String.join("\n- ", invalidClasses));
      }
   }

   public void validateClassesHaveHiddenNoArgConstructor(@NotNull Iterable<Class<? extends SettingsHolder>> settingHolders) {
      List<String> invalidClasses = new ArrayList<>();

      for (Class<? extends SettingsHolder> clazz : settingHolders) {
         if (!this.hasValidConstructorSetup(clazz)) {
            invalidClasses.add(clazz.getCanonicalName());
         }
      }

      if (!invalidClasses.isEmpty()) {
         throw new IllegalStateException("The following classes do not have a single no-args private constructor:\n- " + String.join("\n- ", invalidClasses));
      }
   }

   public void validateHasCommentOnEveryProperty(@NotNull ConfigurationData configurationData, @Nullable Predicate<Property<?>> propertyFilter) {
      Predicate<Property<?>> filter = propertyFilter == null ? p -> true : propertyFilter;
      List<String> invalidProperties = new ArrayList<>();
      Map<String, List<String>> comments = configurationData.getAllComments();

      for (Property<?> property : configurationData.getProperties()) {
         if (filter.test(property)) {
            List<String> commentEntry = comments.get(property.getPath());
            if (!this.hasNonEmptyComment(commentEntry)) {
               invalidProperties.add(property.toString());
            }
         }
      }

      if (!invalidProperties.isEmpty()) {
         throw new IllegalStateException("The following properties do not have a comment:\n- " + String.join("\n- ", invalidProperties));
      }
   }

   public void validateCommentLengthsAreWithinBounds(@NotNull ConfigurationData configurationData, @Nullable Integer minLength, @Nullable Integer maxLength) {
      Predicate<String> hasInvalidLengthPredicate = this.createValidLengthPredicate(minLength, maxLength).negate();
      List<String> invalidPaths = new ArrayList<>();

      for (Entry<String, List<String>> entry : configurationData.getAllComments().entrySet()) {
         boolean hasInvalidLength = entry.getValue().stream().anyMatch(hasInvalidLengthPredicate);
         if (hasInvalidLength) {
            invalidPaths.add("Path '" + entry.getKey() + "'");
         }
      }

      if (!invalidPaths.isEmpty()) {
         String bound = minLength == null ? "" : "min length of " + minLength;
         if (maxLength != null) {
            bound = bound + (bound.isEmpty() ? "" : ", ") + "max length of " + maxLength;
         }

         throw new IllegalStateException(
            "The comments for the following paths are not within the bounds: " + bound + " characters:\n- " + String.join("\n- ", invalidPaths)
         );
      }
   }

   public void validateHasAllEnumEntriesInComment(@NotNull ConfigurationData configurationData, @Nullable Predicate<Property<?>> propertyFilter) {
      List<String> commentErrors = new ArrayList<>();

      for (Property<?> property : configurationData.getProperties()) {
         if (propertyFilter == null || propertyFilter.test(property)) {
            Class<? extends Enum<?>> enumType = this.getEnumTypeOfProperty(property);
            if (enumType != null) {
               List<String> expectedEnums = this.gatherExpectedEnumNames(enumType);
               String comments = String.join("\n", configurationData.getCommentsForSection(property.getPath()));
               List<String> missingEnumEntries = expectedEnums.stream().filter(e -> !comments.contains(e)).collect(Collectors.toList());
               if (!missingEnumEntries.isEmpty()) {
                  commentErrors.add("For " + property + ": missing " + String.join(", ", missingEnumEntries));
               }
            }
         }
      }

      if (!commentErrors.isEmpty()) {
         throw new IllegalStateException("The following enum properties do not list all enum values:\n- " + String.join("\n- ", commentErrors));
      }
   }

   protected boolean isValidConstantField(@NotNull Field field) {
      int modifiers = field.getModifiers();
      return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
   }

   @NotNull
   protected ConfigurationData createConfigurationData(@NotNull Iterable<Class<? extends SettingsHolder>> classes) {
      return ConfigurationDataBuilder.createConfiguration(classes);
   }

   protected boolean hasNonEmptyComment(@Nullable List<String> comments) {
      return comments != null && comments.stream().anyMatch(line -> !line.trim().isEmpty());
   }

   @NotNull
   protected Predicate<String> createValidLengthPredicate(@Nullable Integer minLength, @Nullable Integer maxLength) {
      if (minLength == null && maxLength == null) {
         throw new IllegalArgumentException("min length or max length must be not null");
      } else {
         return string -> (minLength == null || minLength <= string.length()) && (maxLength == null || maxLength >= string.length());
      }
   }

   @Nullable
   protected Class<? extends Enum<?>> getEnumTypeOfProperty(@NotNull Property<?> property) {
      Class<?> defaultValueType = property.getDefaultValue().getClass();
      if (defaultValueType.isAnonymousClass()) {
         defaultValueType = defaultValueType.getEnclosingClass();
      }

      return (Class<? extends Enum<?>>)(defaultValueType.isEnum() ? defaultValueType : null);
   }

   @NotNull
   protected List<String> gatherExpectedEnumNames(@NotNull Class<? extends Enum<?>> enumClass) {
      return Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).collect(Collectors.toList());
   }

   protected boolean hasValidConstructorSetup(@NotNull Class<? extends SettingsHolder> clazz) {
      Constructor<?>[] constructors = clazz.getDeclaredConstructors();
      return constructors.length == 1 && constructors[0].getParameterCount() == 0 && Modifier.isPrivate(constructors[0].getModifiers());
   }

   @NotNull
   protected Stream<Field> getAllFields(@NotNull Class<?> clazz) {
      if (Object.class.equals(clazz.getSuperclass())) {
         return Arrays.stream(clazz.getDeclaredFields());
      } else {
         Class<?> currentClass = clazz;

         List<Class<?>> classes;
         for (classes = new ArrayList<>(); currentClass != null && !currentClass.equals(Object.class); currentClass = currentClass.getSuperclass()) {
            classes.add(currentClass);
         }

         return classes.stream().flatMap(clz -> Arrays.stream(clz.getDeclaredFields()));
      }
   }
}
