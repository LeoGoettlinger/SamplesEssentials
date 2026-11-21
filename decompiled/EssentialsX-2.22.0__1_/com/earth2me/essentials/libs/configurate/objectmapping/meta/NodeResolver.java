package com.earth2me.essentials.libs.configurate.objectmapping.meta;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.BasicConfigurationNode;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public interface NodeResolver {
   NodeResolver SKIP_FIELD = parent -> null;

   @Nullable
   ConfigurationNode resolve(ConfigurationNode parent);

   static NodeResolver.Factory nodeKey() {
      return (name, element) -> element.isAnnotationPresent(NodeKey.class) ? node -> BasicConfigurationNode.root(node.options()).raw(node.key()) : null;
   }

   static NodeResolver.Factory keyFromSetting() {
      return (name, element) -> {
         if (element.isAnnotationPresent(Setting.class)) {
            String key = element.getAnnotation(Setting.class).value();
            if (!key.isEmpty()) {
               return node -> node.node(key);
            }
         }

         return null;
      };
   }

   static NodeResolver.Factory onlyWithAnnotation(final Class<? extends Annotation> annotation) {
      return (name, element) -> !element.isAnnotationPresent(annotation) ? SKIP_FIELD : null;
   }

   static NodeResolver.Factory onlyWithSetting() {
      return onlyWithAnnotation(Setting.class);
   }

   static NodeResolver.Factory nodeFromParent() {
      return (name, element) -> {
         Setting setting = element.getAnnotation(Setting.class);
         return setting != null && setting.nodeFromParent() ? node -> node : null;
      };
   }

   public interface Factory {
      @Nullable
      NodeResolver make(String name, AnnotatedElement element);
   }
}
