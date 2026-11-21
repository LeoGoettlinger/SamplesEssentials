package org.evenmorefish.fish.libs.boostedyaml.route.implementation;

import java.util.Arrays;
import java.util.Objects;
import org.evenmorefish.fish.libs.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;

public class MultiKeyRoute implements Route {
   private final Object[] route;

   public MultiKeyRoute(@NotNull Object... route) {
      if (Objects.requireNonNull(route, "Route array cannot be null!").length == 0) {
         throw new IllegalArgumentException("Empty routes are not allowed!");
      } else {
         for (Object key : route) {
            Objects.requireNonNull(key, "Route cannot contain null keys!");
         }

         this.route = route;
      }
   }

   @NotNull
   @Override
   public String join(char separator) {
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < this.length(); i++) {
         builder.append(this.get(i)).append(i + 1 < this.length() ? separator : "");
      }

      return builder.toString();
   }

   @Override
   public int length() {
      return this.route.length;
   }

   @NotNull
   @Override
   public Object get(int i) {
      return this.route[i];
   }

   @NotNull
   @Override
   public Route add(@NotNull Object key) {
      Object[] route = Arrays.copyOf(this.route, this.route.length + 1);
      route[route.length - 1] = Objects.requireNonNull(key, "Route cannot contain null keys!");
      return new MultiKeyRoute(route);
   }

   @NotNull
   @Override
   public Route parent() {
      return this.route.length == 2 ? Route.from(this.route[0]) : Route.from(Arrays.copyOf(this.route, this.route.length - 1));
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Route)) {
         return false;
      } else {
         Route route1 = (Route)o;
         if (this.length() != route1.length()) {
            return false;
         } else if (this.length() == 1 && route1.length() == 1) {
            return Objects.equals(this.get(0), route1.get(0));
         } else {
            return !(route1 instanceof MultiKeyRoute) ? false : Arrays.equals(this.route, ((MultiKeyRoute)route1).route);
         }
      }
   }

   @Override
   public int hashCode() {
      return this.length() > 1 ? Arrays.hashCode(this.route) : Objects.hashCode(this.route[0]);
   }

   @Override
   public String toString() {
      return "MultiKeyRoute{route=" + Arrays.toString(this.route) + '}';
   }
}
