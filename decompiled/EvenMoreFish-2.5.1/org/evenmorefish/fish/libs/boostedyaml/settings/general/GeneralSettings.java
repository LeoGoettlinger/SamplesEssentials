package org.evenmorefish.fish.libs.boostedyaml.settings.general;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.evenmorefish.fish.libs.boostedyaml.serialization.YamlSerializer;
import org.evenmorefish.fish.libs.boostedyaml.serialization.standard.StandardSerializer;
import org.evenmorefish.fish.libs.boostedyaml.settings.Settings;
import org.evenmorefish.fish.libs.boostedyaml.utils.supplier.ListSupplier;
import org.evenmorefish.fish.libs.boostedyaml.utils.supplier.MapSupplier;
import org.evenmorefish.fish.libs.boostedyaml.utils.supplier.SetSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GeneralSettings implements Settings {
   public static final char DEFAULT_ROUTE_SEPARATOR = '.';
   public static final String DEFAULT_ESCAPED_SEPARATOR = Pattern.quote(String.valueOf('.'));
   public static final GeneralSettings.KeyFormat DEFAULT_KEY_FORMATTING = GeneralSettings.KeyFormat.STRING;
   public static final YamlSerializer DEFAULT_SERIALIZER = StandardSerializer.getDefault();
   public static final boolean DEFAULT_USE_DEFAULTS = true;
   public static final Object DEFAULT_OBJECT = null;
   public static final Number DEFAULT_NUMBER = 0;
   public static final String DEFAULT_STRING = null;
   public static final Character DEFAULT_CHAR = ' ';
   public static final Boolean DEFAULT_BOOLEAN = false;
   public static final ListSupplier DEFAULT_LIST = ArrayList::new;
   public static final SetSupplier DEFAULT_SET = LinkedHashSet::new;
   public static final MapSupplier DEFAULT_MAP = LinkedHashMap::new;
   public static final GeneralSettings DEFAULT = builder().build();
   private final GeneralSettings.KeyFormat keyFormat;
   private final char separator;
   private final String escapedSeparator;
   private final YamlSerializer serializer;
   private final boolean useDefaults;
   private final Object defaultObject;
   private final Number defaultNumber;
   private final String defaultString;
   private final Character defaultChar;
   private final Boolean defaultBoolean;
   private final ListSupplier defaultList;
   private final SetSupplier defaultSet;
   private final MapSupplier defaultMap;

   private GeneralSettings(GeneralSettings.Builder builder) {
      this.keyFormat = builder.keyFormat;
      this.separator = builder.routeSeparator;
      this.escapedSeparator = Pattern.quote(String.valueOf(this.separator));
      this.serializer = builder.serializer;
      this.defaultObject = builder.defaultObject;
      this.defaultNumber = builder.defaultNumber;
      this.defaultString = builder.defaultString;
      this.defaultChar = builder.defaultChar;
      this.defaultBoolean = builder.defaultBoolean;
      this.defaultList = builder.defaultList;
      this.defaultSet = builder.defaultSet;
      this.defaultMap = builder.defaultMap;
      this.useDefaults = builder.useDefaults;
   }

   public GeneralSettings.KeyFormat getKeyFormat() {
      return this.keyFormat;
   }

   public char getRouteSeparator() {
      return this.separator;
   }

   public String getEscapedSeparator() {
      return this.escapedSeparator;
   }

   public YamlSerializer getSerializer() {
      return this.serializer;
   }

   public boolean isUseDefaults() {
      return this.useDefaults;
   }

   public Object getDefaultObject() {
      return this.defaultObject;
   }

   public String getDefaultString() {
      return this.defaultString;
   }

   public Character getDefaultChar() {
      return this.defaultChar;
   }

   public Number getDefaultNumber() {
      return this.defaultNumber;
   }

   public Boolean getDefaultBoolean() {
      return this.defaultBoolean;
   }

   public <T> List<T> getDefaultList(int size) {
      return this.defaultList.supply(size);
   }

   public <T> List<T> getDefaultList() {
      return this.getDefaultList(0);
   }

   public <T> Set<T> getDefaultSet(int size) {
      return this.defaultSet.supply(size);
   }

   public <T> Set<T> getDefaultSet() {
      return this.getDefaultSet(0);
   }

   public <K, V> Map<K, V> getDefaultMap(int size) {
      return this.defaultMap.supply(size);
   }

   public <K, V> Map<K, V> getDefaultMap() {
      return this.getDefaultMap(0);
   }

   public MapSupplier getDefaultMapSupplier() {
      return this.defaultMap;
   }

   public static GeneralSettings.Builder builder() {
      return new GeneralSettings.Builder();
   }

   public static GeneralSettings.Builder builder(GeneralSettings settings) {
      return builder()
         .setKeyFormat(settings.keyFormat)
         .setRouteSeparator(settings.separator)
         .setSerializer(settings.serializer)
         .setUseDefaults(settings.useDefaults)
         .setDefaultObject(settings.defaultObject)
         .setDefaultNumber(settings.defaultNumber)
         .setDefaultString(settings.defaultString)
         .setDefaultChar(settings.defaultChar)
         .setDefaultBoolean(settings.defaultBoolean)
         .setDefaultList(settings.defaultList)
         .setDefaultSet(settings.defaultSet)
         .setDefaultMap(settings.defaultMap);
   }

   public static class Builder {
      private GeneralSettings.KeyFormat keyFormat = GeneralSettings.DEFAULT_KEY_FORMATTING;
      private char routeSeparator = '.';
      private YamlSerializer serializer = GeneralSettings.DEFAULT_SERIALIZER;
      private boolean useDefaults = true;
      private Object defaultObject = GeneralSettings.DEFAULT_OBJECT;
      private Number defaultNumber = GeneralSettings.DEFAULT_NUMBER;
      private String defaultString = GeneralSettings.DEFAULT_STRING;
      private Character defaultChar = GeneralSettings.DEFAULT_CHAR;
      private Boolean defaultBoolean = GeneralSettings.DEFAULT_BOOLEAN;
      private ListSupplier defaultList = GeneralSettings.DEFAULT_LIST;
      private SetSupplier defaultSet = GeneralSettings.DEFAULT_SET;
      private MapSupplier defaultMap = GeneralSettings.DEFAULT_MAP;

      private Builder() {
      }

      public GeneralSettings.Builder setKeyFormat(@NotNull GeneralSettings.KeyFormat keyFormat) {
         this.keyFormat = keyFormat;
         return this;
      }

      public GeneralSettings.Builder setRouteSeparator(char separator) {
         this.routeSeparator = separator;
         return this;
      }

      public GeneralSettings.Builder setSerializer(@NotNull YamlSerializer serializer) {
         this.serializer = serializer;
         return this;
      }

      public GeneralSettings.Builder setUseDefaults(boolean useDefaults) {
         this.useDefaults = useDefaults;
         return this;
      }

      public GeneralSettings.Builder setDefaultObject(@Nullable Object defaultObject) {
         this.defaultObject = defaultObject;
         return this;
      }

      public GeneralSettings.Builder setDefaultNumber(@NotNull Number defaultNumber) {
         this.defaultNumber = defaultNumber;
         return this;
      }

      public GeneralSettings.Builder setDefaultString(@Nullable String defaultString) {
         this.defaultString = defaultString;
         return this;
      }

      public GeneralSettings.Builder setDefaultChar(@Nullable Character defaultChar) {
         this.defaultChar = defaultChar;
         return this;
      }

      public GeneralSettings.Builder setDefaultBoolean(@Nullable Boolean defaultBoolean) {
         this.defaultBoolean = defaultBoolean;
         return this;
      }

      public GeneralSettings.Builder setDefaultList(@NotNull ListSupplier defaultList) {
         this.defaultList = defaultList;
         return this;
      }

      public GeneralSettings.Builder setDefaultSet(@NotNull SetSupplier defaultSet) {
         this.defaultSet = defaultSet;
         return this;
      }

      public GeneralSettings.Builder setDefaultMap(@NotNull MapSupplier defaultMap) {
         this.defaultMap = defaultMap;
         return this;
      }

      public GeneralSettings build() {
         return new GeneralSettings(this);
      }
   }

   public static enum KeyFormat {
      STRING,
      OBJECT;
   }
}
