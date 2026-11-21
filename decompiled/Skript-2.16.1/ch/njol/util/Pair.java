package ch.njol.util;

import java.io.Serializable;
import java.util.Objects;
import java.util.Map.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Deprecated(
   since = "2.10.0",
   forRemoval = true
)
public class Pair<T1, T2> implements Entry<T1, T2>, Cloneable, Serializable {
   private static final long serialVersionUID = 8296563685697678334L;
   @UnknownNullability
   protected T1 first;
   @UnknownNullability
   protected T2 second;

   public Pair() {
      this.first = null;
      this.second = null;
   }

   public Pair(@Nullable T1 first, @Nullable T2 second) {
      this.first = first;
      this.second = second;
   }

   public Pair(@NotNull Entry<T1, T2> entry) {
      this.first = entry.getKey();
      this.second = entry.getValue();
   }

   @UnknownNullability
   public T1 getFirst() {
      return this.first;
   }

   public void setFirst(@Nullable T1 first) {
      this.first = first;
   }

   @UnknownNullability
   public T2 getSecond() {
      return this.second;
   }

   public void setSecond(@Nullable T2 second) {
      this.second = second;
   }

   @Override
   public String toString() {
      return this.first + "," + this.second;
   }

   @Override
   public final boolean equals(@Nullable Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof Entry<?, ?> entry)) {
         return false;
      } else {
         Object first = this.first;
         Object second = this.second;
         return (first == null ? entry.getKey() == null : first.equals(entry.getKey()))
            && (second == null ? entry.getValue() == null : second.equals(entry.getValue()));
      }
   }

   @Override
   public final int hashCode() {
      return Objects.hash(this.first, this.second);
   }

   @UnknownNullability
   @Override
   public T1 getKey() {
      return this.first;
   }

   @UnknownNullability
   @Override
   public T2 getValue() {
      return this.second;
   }

   @UnknownNullability
   @Override
   public T2 setValue(@Nullable T2 value) {
      T2 old = this.second;
      this.second = value;
      return old;
   }

   public Pair<T1, T2> clone() {
      return new Pair<>(this);
   }
}
