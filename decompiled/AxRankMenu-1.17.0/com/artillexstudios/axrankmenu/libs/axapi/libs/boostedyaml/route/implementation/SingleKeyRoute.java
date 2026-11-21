package com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.route.implementation;

import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.route.Route;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class SingleKeyRoute implements Route {
   private final Object key;

   public SingleKeyRoute(@NotNull Object key) {
      this.key = Objects.requireNonNull(key, "Route cannot contain null keys!");
   }

   @NotNull
   @Override
   public String join(char separator) {
      return this.key.toString();
   }

   @Override
   public int length() {
      return 1;
   }

   @NotNull
   @Override
   public Object get(int i) {
      if (i != 0) {
         throw new ArrayIndexOutOfBoundsException("Index " + i + " for single key route!");
      } else {
         return this.key;
      }
   }

   @NotNull
   @Override
   public Route parent() {
      throw new IllegalArgumentException("Empty routes are not allowed!");
   }

   @NotNull
   @Override
   public Route add(@NotNull Object key) {
      return Route.from(this.key, Objects.requireNonNull(key, "Route cannot contain null keys!"));
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Route)) {
         return false;
      } else {
         Route that = (Route)o;
         return that.length() != 1 ? false : Objects.equals(this.key, that.get(0));
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.key);
   }

   @Override
   public String toString() {
      return "SingleKeyRoute{key=" + this.key + '}';
   }
}
