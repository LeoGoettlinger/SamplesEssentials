package com.viaversion.viaversion.libs.mcstructs.converter.model;

import java.util.function.Function;
import javax.annotation.Nonnull;
import lombok.Generated;

public interface Either<L, R> {
   static <L, R> Either<L, R> left(@Nonnull L left) {
      return new Either.Left<>(left);
   }

   static <L, R> Either<L, R> right(@Nonnull R right) {
      return new Either.Right<>(right);
   }

   static <T> T unwrap(Either<? extends T, ? extends T> either) {
      return (T)(either.isLeft() ? either.getLeft() : either.getRight());
   }

   L getLeft();

   R getRight();

   boolean isLeft();

   boolean isRight();

   <ML, MR> Either<ML, MR> map(Function<L, ML> var1, Function<R, MR> var2);

   <T> T xmap(Function<L, T> var1, Function<R, T> var2);

   Either<R, L> swap();

   @Override
   String toString();

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();

   public static class Left<L, R> implements Either<L, R> {
      private final L left;

      private Left(L left) {
         this.left = left;
      }

      @Override
      public L getLeft() {
         return this.left;
      }

      @Override
      public R getRight() {
         return null;
      }

      @Override
      public boolean isLeft() {
         return true;
      }

      @Override
      public boolean isRight() {
         return false;
      }

      @Override
      public <ML, MR> Either<ML, MR> map(Function<L, ML> leftMapper, Function<R, MR> rightMapper) {
         return (Either<ML, MR>)Either.left(leftMapper.apply(this.getLeft()));
      }

      @Override
      public <T> T xmap(Function<L, T> leftMapper, Function<R, T> rightMapper) {
         return leftMapper.apply(this.getLeft());
      }

      @Override
      public Either<R, L> swap() {
         return Either.right(this.left);
      }

      @Override
      public String toString() {
         return "Left{" + this.getLeft() + "}";
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof Either.Left)) {
            return false;
         } else {
            Either.Left<?, ?> other = (Either.Left<?, ?>)o;
            if (!other.canEqual(this)) {
               return false;
            } else {
               Object this$left = this.getLeft();
               Object other$left = other.getLeft();
               return this$left == null ? other$left == null : this$left.equals(other$left);
            }
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof Either.Left;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $left = this.getLeft();
         return result * 59 + ($left == null ? 43 : $left.hashCode());
      }
   }

   public static class Right<L, R> implements Either<L, R> {
      private final R right;

      private Right(R right) {
         this.right = right;
      }

      @Override
      public L getLeft() {
         return null;
      }

      @Override
      public R getRight() {
         return this.right;
      }

      @Override
      public boolean isLeft() {
         return false;
      }

      @Override
      public boolean isRight() {
         return true;
      }

      @Override
      public <ML, MR> Either<ML, MR> map(Function<L, ML> leftMapper, Function<R, MR> rightMapper) {
         return (Either<ML, MR>)Either.right(rightMapper.apply(this.right));
      }

      @Override
      public <T> T xmap(Function<L, T> leftMapper, Function<R, T> rightMapper) {
         return rightMapper.apply(this.right);
      }

      @Override
      public Either<R, L> swap() {
         return Either.left(this.right);
      }

      @Override
      public String toString() {
         return "Right{" + this.getRight() + "}";
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof Either.Right)) {
            return false;
         } else {
            Either.Right<?, ?> other = (Either.Right<?, ?>)o;
            if (!other.canEqual(this)) {
               return false;
            } else {
               Object this$right = this.getRight();
               Object other$right = other.getRight();
               return this$right == null ? other$right == null : this$right.equals(other$right);
            }
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof Either.Right;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $right = this.getRight();
         return result * 59 + ($right == null ? 43 : $right.hashCode());
      }
   }
}
