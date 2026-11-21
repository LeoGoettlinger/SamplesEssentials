package com.artillexstudios.axtrade.libs.axapi.config;

import com.artillexstudios.axtrade.libs.axapi.config.annotation.Comment;
import com.artillexstudios.axtrade.libs.axapi.config.annotation.ConfigurationPart;
import com.artillexstudios.axtrade.libs.axapi.config.annotation.Header;
import com.artillexstudios.axtrade.libs.axapi.config.annotation.Hidden;
import com.artillexstudios.axtrade.libs.axapi.config.annotation.Named;
import com.artillexstudios.axtrade.libs.axapi.config.annotation.Serializable;
import java.nio.file.Path;
import java.util.Map;

@Header({"This is a configuration file!"})
public class ConfigTest implements ConfigurationPart {
   @Named("setting")
   @Comment("oreo")
   public static double value = 0.0;
   public static ConfigTest.CustomClass bigOreo = new ConfigTest.CustomClass(69420, 10);
   public static Map<String, ConfigTest.CustomClass> oreos = Map.of(
      "world", new ConfigTest.CustomClass(2, 4), "world_the_end", new ConfigTest.CustomClass(), "spawn", new ConfigTest.CustomClass(10, 30)
   );

   public static void main(String[] args) {
      YamlConfiguration<ConfigTest> conf = YamlConfiguration.of(Path.of("D:\\EcoSkills\\rivals\\AxConfig\\asd\\bruh.yml"), ConfigTest.class)
         .withDumperOptions(dumperOptions -> {})
         .withLoaderOptions(loaderOptions -> loaderOptions.setAllowDuplicateKeys(false))
         .build();
      conf.load();
      conf.save();
   }

   @Serializable
   public static class CustomClass {
      @Comment("This is the max")
      public int max;
      @Comment("this is the min")
      public int min;
      @Hidden
      public String secret = null;

      public CustomClass() {
         this(1, 2);
      }

      public CustomClass(int min, int max) {
         this.min = min;
         this.max = max;
      }

      @Override
      public String toString() {
         return "CustomClass{max=" + this.max + ", min=" + this.min + "}";
      }
   }
}
