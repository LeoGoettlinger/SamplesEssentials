package com.sk89q.worldedit.util.eventbus;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Set;

class HierarchyCache {
   private final LoadingCache<Class<?>, Set<Class<?>>> cache = CacheBuilder.newBuilder().weakKeys().build(CacheLoader.from(this::build));

   public Set<Class<?>> get(Class<?> concreteClass) {
      return (Set<Class<?>>)this.cache.getUnchecked(concreteClass);
   }

   protected Set<Class<?>> build(Class<?> concreteClass) {
      List<Class<?>> parents = Lists.newLinkedList();
      Set<Class<?>> classes = Sets.newHashSet();
      parents.add(concreteClass);

      while (!parents.isEmpty()) {
         Class<?> clazz = parents.remove(0);
         classes.add(clazz);
         Class<?> parent = clazz.getSuperclass();
         if (parent != null) {
            parents.add(parent);
         }

         Collections.addAll(parents, clazz.getInterfaces());
      }

      return classes;
   }
}
