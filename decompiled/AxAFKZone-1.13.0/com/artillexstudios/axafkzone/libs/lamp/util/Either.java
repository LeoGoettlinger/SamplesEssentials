package com.artillexstudios.axafkzone.libs.lamp.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Either<A, B> {
   public static <A, B> Either<A, B> first(A value) {
      return new Either.First<>(value);
   }

   public static <A, B> Either<A, B> second(B value) {
      return new Either.Second<>(value);
   }

   public abstract Optional<A> first();

   public abstract Optional<B> second();

   public boolean isFirst() {
      return this instanceof Either.First;
   }

   public boolean isSecond() {
      return this instanceof Either.Second;
   }

   public abstract Either<A, B> ifFirst(Consumer<A> var1);

   public abstract Either<A, B> ifSecond(Consumer<B> var1);

   public Either<B, A> swap() {
      return this.map(Either::second, Either::first);
   }

   public abstract <T> T map(Function<? super A, ? extends T> var1, Function<? super B, ? extends T> var2);

   public <T> Either<T, B> mapFirst(Function<? super A, ? extends T> function) {
      return this.map(t -> (Either<T, B>)first(function.apply(t)), Either::second);
   }

   public <T> Either<A, T> mapSecond(Function<? super B, ? extends T> function) {
      return this.map(Either::first, t -> (Either<A, T>)second(function.apply(t)));
   }

   private static class First<A, B> extends Either<A, B> {
      private final A value;
      private Optional<A> valueOptional;

      First(A value) {
         this.value = value;
      }

      @Override
      public Optional<A> first() {
         return this.valueOptional == null ? (this.valueOptional = Optional.of(this.value)) : this.valueOptional;
      }

      @Override
      public Optional<B> second() {
         return Optional.empty();
      }

      @Override
      public Either<A, B> ifFirst(Consumer<A> action) {
         action.accept(this.value);
         return this;
      }

      @Override
      public Either<A, B> ifSecond(Consumer<B> action) {
         return this;
      }

      @Override
      public <T> T map(Function<? super A, ? extends T> mapFirst, Function<? super B, ? extends T> mapSecond) {
         return (T)mapFirst.apply(this.value);
      }

      @Override
      public String toString() {
         return "First{value=" + this.value + "}";
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o instanceof Either.Second) {
            Either.Second<?, ?> first = (Either.Second<?, ?>)o;
            return this.value.equals(first.value);
         } else if (o instanceof Either.First) {
            Either.First<?, ?> second = (Either.First<?, ?>)o;
            return this.value.equals(second.value);
         } else {
            return this.value.equals(o);
         }
      }

      @Override
      public int hashCode() {
         return this.value.hashCode();
      }
   }

   private static class Second<A, B> extends Either<A, B> {
      private final B value;
      private Optional<B> valueOptional;

      Second(B value) {
         this.value = value;
      }

      @Override
      public Optional<A> first() {
         return Optional.empty();
      }

      @Override
      public Optional<B> second() {
         return this.valueOptional == null ? (this.valueOptional = Optional.of(this.value)) : this.valueOptional;
      }

      @Override
      public Either<A, B> ifFirst(Consumer<A> action) {
         return this;
      }

      @Override
      public Either<A, B> ifSecond(Consumer<B> action) {
         action.accept(this.value);
         return this;
      }

      @Override
      public <T> T map(Function<? super A, ? extends T> mapFirst, Function<? super B, ? extends T> mapSecond) {
         return (T)mapSecond.apply(this.value);
      }

      @Override
      public String toString() {
         return "Second{value=" + this.value + "}";
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o instanceof Either.Second) {
            Either.Second<?, ?> first = (Either.Second<?, ?>)o;
            return this.value.equals(first.value);
         } else if (o instanceof Either.First) {
            Either.First<?, ?> second = (Either.First<?, ?>)o;
            return this.value.equals(second.value);
         } else {
            return this.value.equals(o);
         }
      }

      @Override
      public int hashCode() {
         return this.value.hashCode();
      }
   }
}
