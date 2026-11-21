package me.chancesd.pvpmanager.sdutils.command;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ArgumentType {
   public static final ArgumentType STRING = new ArgumentType("STRING");
   public static final ArgumentType PLAYER = new ArgumentType("PLAYER");
   public static final ArgumentType INTEGER = new ArgumentType("INTEGER");
   public static final ArgumentType DOUBLE = new ArgumentType("DOUBLE");
   public static final ArgumentType BOOLEAN = new ArgumentType("BOOLEAN");
   public static final ArgumentType PLAYER_OR_ALL = new ArgumentType("PLAYER_OR_ALL");
   public static final ArgumentType STRING_ARRAY = new ArgumentType("STRING_ARRAY");
   public static final ArgumentType WORLD = new ArgumentType("WORLD");
   public static final ArgumentType MATERIAL = new ArgumentType("MATERIAL");
   public static final ArgumentType DURATION = new ArgumentType("DURATION");
   public static final ArgumentType OFFLINE_PLAYER = new ArgumentType("OFFLINE_PLAYER");
   private final String name;
   private final Supplier<List<String>> tabCompletions;
   private final Class<? extends Enum<?>> enumClass;

   private ArgumentType(String name, Supplier<List<String>> tabCompletions, Class<? extends Enum<?>> enumClass) {
      this.name = name;
      this.tabCompletions = tabCompletions;
      this.enumClass = enumClass;
   }

   private ArgumentType(String name) {
      this(name, null, null);
   }

   public static ArgumentType custom(String name, Supplier<List<String>> tabCompletions) {
      return new ArgumentType(name, tabCompletions, null);
   }

   public static <E extends Enum<E>> ArgumentType enumType(Class<E> enumClass) {
      return new ArgumentType("ENUM_" + enumClass.getSimpleName(), () -> Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toList(), enumClass);
   }

   public String getName() {
      return this.name;
   }

   public Supplier<List<String>> getTabCompletions() {
      return this.tabCompletions;
   }

   public boolean hasTabCompletions() {
      return this.tabCompletions != null;
   }

   public Class<? extends Enum<?>> getEnumClass() {
      return this.enumClass;
   }

   public boolean isEnumType() {
      return this.enumClass != null;
   }

   @Override
   public String toString() {
      return this.name;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         ArgumentType that = (ArgumentType)obj;
         return this.name.equals(that.name);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.name.hashCode();
   }
}
