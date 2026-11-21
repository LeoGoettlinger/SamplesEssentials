package com.magmaguy.elitemobs.shaded.reflections;

import com.magmaguy.elitemobs.shaded.reflections.scanners.Scanner;
import java.net.URL;
import java.util.Set;
import java.util.function.Predicate;

public interface Configuration {
   Set<Scanner> getScanners();

   Set<URL> getUrls();

   Predicate<String> getInputsFilter();

   boolean isParallel();

   ClassLoader[] getClassLoaders();

   boolean shouldExpandSuperTypes();
}
