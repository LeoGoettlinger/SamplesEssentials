package ac.grim.grimac.shaded.com.github.retrooper.packetevents.util;

import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.function.Function;

public class Either<L, R> {
   @Nullable
   private final L left;
   @Nullable
   private final R right;

   private Either(@Nullable L left, @Nullable R right) {
      this.left = left;
      this.right = right;
   }

   public static <L, R> Either<L, R> createLeft(L left) {
      return new Either<>(left, null);
   }

   public static <L, R> Either<L, R> createRight(R right) {
      return new Either<>(null, right);
   }

   public static <T> T unwrap(Either<T, ? extends T> either) {
      return either.left != null ? either.left : either.right;
   }

   public Object get() {
      return this.left != null ? this.left : this.right;
   }

   public <T> T map(Function<L, T> leftFn, Function<R, T> rightFn) {
      return this.left != null ? leftFn.apply(this.left) : rightFn.apply(this.right);
   }

   public boolean isLeft() {
      return this.left != null;
   }

   @Nullable
   public L getLeft() {
      return this.left;
   }

   public boolean isRight() {
      return this.right != null;
   }

   @Nullable
   public R getRight() {
      return this.right;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof Either)) {
         return false;
      } else {
         Either<?, ?> either = (Either<?, ?>)obj;
         return !Objects.equals(this.left, either.left) ? false : Objects.equals(this.right, either.right);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.left, this.right);
   }
}
