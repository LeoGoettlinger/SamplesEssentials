package org.evenmorefish.fish.libs.boostedyaml.route;

import java.util.Objects;
import java.util.regex.Pattern;
import org.evenmorefish.fish.libs.boostedyaml.route.implementation.MultiKeyRoute;
import org.evenmorefish.fish.libs.boostedyaml.route.implementation.SingleKeyRoute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Route {
   @NotNull
   static Route from(@NotNull Object... route) {
      if (Objects.requireNonNull(route, "Route array cannot be null!").length == 0) {
         throw new IllegalArgumentException("Empty routes are not allowed!");
      } else {
         return (Route)(route.length == 1 ? new SingleKeyRoute(route[0]) : new MultiKeyRoute(route));
      }
   }

   @NotNull
   static Route from(@NotNull Object key) {
      return new SingleKeyRoute(key);
   }

   @NotNull
   static Route fromSingleKey(@NotNull Object key) {
      return new SingleKeyRoute(key);
   }

   @NotNull
   static Route fromString(@NotNull String route) {
      return fromString(route, '.');
   }

   @NotNull
   static Route fromString(@NotNull String route, char separator) {
      return (Route)(route.indexOf(separator) != -1 ? new MultiKeyRoute(route.split(Pattern.quote(String.valueOf(separator)))) : new SingleKeyRoute(route));
   }

   @NotNull
   static Route fromString(@NotNull String route, @NotNull RouteFactory routeFactory) {
      return (Route)(route.indexOf(routeFactory.getSeparator()) != -1
         ? new MultiKeyRoute(route.split(routeFactory.getEscapedSeparator()))
         : new SingleKeyRoute(route));
   }

   @NotNull
   static Route addTo(@Nullable Route route, @NotNull Object key) {
      return route == null ? fromSingleKey(key) : route.add(key);
   }

   @NotNull
   String join(char var1);

   int length();

   @NotNull
   Object get(int var1);

   @NotNull
   Route add(@NotNull Object var1);

   @NotNull
   Route parent();

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();
}
