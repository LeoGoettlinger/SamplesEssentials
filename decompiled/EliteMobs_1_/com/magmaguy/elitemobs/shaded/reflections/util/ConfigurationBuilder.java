package com.magmaguy.elitemobs.shaded.reflections.util;

import com.magmaguy.elitemobs.shaded.reflections.Configuration;
import com.magmaguy.elitemobs.shaded.reflections.ReflectionsException;
import com.magmaguy.elitemobs.shaded.reflections.scanners.Scanner;
import com.magmaguy.elitemobs.shaded.reflections.scanners.Scanners;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ConfigurationBuilder implements Configuration {
   public static final Set<Scanner> DEFAULT_SCANNERS = new HashSet<>(Arrays.asList(Scanners.TypesAnnotated, Scanners.SubTypes));
   public static final Predicate<String> DEFAULT_INPUTS_FILTER = t -> true;
   private Set<Scanner> scanners;
   private Set<URL> urls;
   private Predicate<String> inputsFilter;
   private boolean isParallel = true;
   private ClassLoader[] classLoaders;
   private boolean expandSuperTypes = true;

   public ConfigurationBuilder() {
      this.urls = new HashSet<>();
   }

   public static ConfigurationBuilder build(Object... params) {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      List<Object> parameters = new ArrayList<>();

      for (Object param : params) {
         if (param.getClass().isArray()) {
            for (Object p : (Object[])param) {
               parameters.add(p);
            }
         } else if (param instanceof Iterable) {
            for (Object p : (Iterable)param) {
               parameters.add(p);
            }
         } else {
            parameters.add(param);
         }
      }

      ClassLoader[] loaders = Stream.of(params).filter(px -> px instanceof ClassLoader).distinct().toArray(ClassLoader[]::new);
      if (loaders.length != 0) {
         builder.addClassLoaders(loaders);
      }

      FilterBuilder inputsFilter = new FilterBuilder();
      builder.filterInputsBy(inputsFilter);

      for (Object paramx : parameters) {
         if (paramx instanceof String && !((String)paramx).isEmpty()) {
            builder.forPackage((String)paramx, loaders);
            inputsFilter.includePackage((String)paramx);
         } else if (paramx instanceof Class && !Scanner.class.isAssignableFrom((Class<?>)paramx)) {
            builder.addUrls(ClasspathHelper.forClass((Class<?>)paramx, loaders));
            inputsFilter.includePackage(((Class)paramx).getPackage().getName());
         } else if (paramx instanceof URL) {
            builder.addUrls((URL)paramx);
         } else if (paramx instanceof Scanner) {
            builder.addScanners((Scanner)paramx);
         } else if (paramx instanceof Class && Scanner.class.isAssignableFrom((Class<?>)paramx)) {
            try {
               builder.addScanners((Scanner)((Class)paramx).getDeclaredConstructor().newInstance());
            } catch (Exception var11) {
               throw new RuntimeException(var11);
            }
         } else {
            if (!(paramx instanceof Predicate)) {
               throw new ReflectionsException("could not use param '" + paramx + "'");
            }

            builder.filterInputsBy((Predicate<String>)paramx);
         }
      }

      if (builder.getUrls().isEmpty()) {
         builder.addUrls(ClasspathHelper.forClassLoader(loaders));
      }

      return builder;
   }

   public ConfigurationBuilder forPackage(String pkg, ClassLoader... classLoaders) {
      return this.addUrls(ClasspathHelper.forPackage(pkg, classLoaders));
   }

   public ConfigurationBuilder forPackages(String... packages) {
      for (String pkg : packages) {
         this.forPackage(pkg);
      }

      return this;
   }

   @Override
   public Set<Scanner> getScanners() {
      return this.scanners != null ? this.scanners : DEFAULT_SCANNERS;
   }

   public ConfigurationBuilder setScanners(Scanner... scanners) {
      this.scanners = new HashSet<>(Arrays.asList(scanners));
      return this;
   }

   public ConfigurationBuilder addScanners(Scanner... scanners) {
      if (this.scanners == null) {
         this.setScanners(scanners);
      } else {
         this.scanners.addAll(Arrays.asList(scanners));
      }

      return this;
   }

   @Override
   public Set<URL> getUrls() {
      return this.urls;
   }

   public ConfigurationBuilder setUrls(Collection<URL> urls) {
      this.urls = new HashSet<>(urls);
      return this;
   }

   public ConfigurationBuilder setUrls(URL... urls) {
      return this.setUrls(Arrays.asList(urls));
   }

   public ConfigurationBuilder addUrls(Collection<URL> urls) {
      this.urls.addAll(urls);
      return this;
   }

   public ConfigurationBuilder addUrls(URL... urls) {
      return this.addUrls(Arrays.asList(urls));
   }

   @Override
   public Predicate<String> getInputsFilter() {
      return this.inputsFilter != null ? this.inputsFilter : DEFAULT_INPUTS_FILTER;
   }

   public ConfigurationBuilder setInputsFilter(Predicate<String> inputsFilter) {
      this.inputsFilter = inputsFilter;
      return this;
   }

   public ConfigurationBuilder filterInputsBy(Predicate<String> inputsFilter) {
      return this.setInputsFilter(inputsFilter);
   }

   @Override
   public boolean isParallel() {
      return this.isParallel;
   }

   public ConfigurationBuilder setParallel(boolean parallel) {
      this.isParallel = parallel;
      return this;
   }

   @Override
   public ClassLoader[] getClassLoaders() {
      return this.classLoaders;
   }

   public ConfigurationBuilder setClassLoaders(ClassLoader[] classLoaders) {
      this.classLoaders = classLoaders;
      return this;
   }

   public ConfigurationBuilder addClassLoaders(ClassLoader... classLoaders) {
      this.classLoaders = this.classLoaders == null
         ? classLoaders
         : Stream.concat(Arrays.stream(this.classLoaders), Arrays.stream(classLoaders)).distinct().toArray(ClassLoader[]::new);
      return this;
   }

   @Override
   public boolean shouldExpandSuperTypes() {
      return this.expandSuperTypes;
   }

   public ConfigurationBuilder setExpandSuperTypes(boolean expandSuperTypes) {
      this.expandSuperTypes = expandSuperTypes;
      return this;
   }
}
