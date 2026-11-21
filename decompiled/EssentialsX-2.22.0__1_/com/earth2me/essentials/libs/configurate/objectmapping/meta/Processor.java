package com.earth2me.essentials.libs.configurate.objectmapping.meta;

import com.earth2me.essentials.libs.configurate.CommentedConfigurationNodeIntermediary;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ResourceBundle;

@FunctionalInterface
public interface Processor<V> {
   void process(V value, ConfigurationNode destination);

   static Processor.Factory<Comment, Object> comments() {
      return (data, fieldType) -> (value, destination) -> {
         if (destination instanceof CommentedConfigurationNodeIntermediary) {
            CommentedConfigurationNodeIntermediary<?> commented = (CommentedConfigurationNodeIntermediary<?>)destination;
            if (data.override()) {
               commented.comment(data.value());
            } else {
               commented.commentIfAbsent(data.value());
            }
         }
      };
   }

   static Processor.Factory<Comment, Object> localizedComments(final ResourceBundle source) {
      return (data, fieldType) -> {
         String translated = Localization.key(source, data.value());
         return (value, destination) -> {
            if (destination instanceof CommentedConfigurationNodeIntermediary) {
               CommentedConfigurationNodeIntermediary<?> commented = (CommentedConfigurationNodeIntermediary<?>)destination;
               if (data.override()) {
                  commented.comment(translated);
               } else {
                  commented.commentIfAbsent(translated);
               }
            }
         };
      };
   }

   @FunctionalInterface
   public interface Factory<A extends Annotation, T> {
      Processor<T> make(A data, Type value);
   }
}
