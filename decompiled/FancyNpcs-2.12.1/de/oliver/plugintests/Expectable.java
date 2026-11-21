package de.oliver.plugintests;

public class Expectable<T> {
   private final T t;

   private Expectable(T t) {
      this.t = t;
   }

   public static <T> Expectable<T> expect(T t) {
      return new Expectable<>(t);
   }

   public void toBeDefined() {
      if (this.t == null) {
         throw new AssertionError("Expected not null but got null");
      }
   }

   public void toBeNull() {
      if (this.t != null) {
         throw new AssertionError("Expected null but got not null");
      }
   }

   public void toBe(T expected) {
      if (this.t != expected) {
         throw new AssertionError("Expected " + expected + " but got " + this.t);
      }
   }

   public void toEqual(T expected) {
      if (!this.t.equals(expected)) {
         throw new AssertionError("Expected " + expected + " but got " + this.t);
      }
   }

   public void toBeGreaterThan(T expected) {
      if (this.t instanceof Integer) {
         if ((Integer)this.t <= (Integer)expected) {
            throw new AssertionError("Expected " + this.t + " to be greater than " + expected);
         }
      } else if (this.t instanceof Long) {
         if ((Long)this.t <= (Long)expected) {
            throw new AssertionError("Expected " + this.t + " to be greater than " + expected);
         }
      } else if (this.t instanceof Float) {
         if ((Float)this.t <= (Float)expected) {
            throw new AssertionError("Expected " + this.t + " to be greater than " + expected);
         }
      } else if (this.t instanceof Double) {
         if ((Double)this.t <= (Double)expected) {
            throw new AssertionError("Expected " + this.t + " to be greater than " + expected);
         }
      } else {
         throw new AssertionError("toBeGreaterThan can only be used on Integers, Longs, Floats, and Doubles");
      }
   }

   public void toBeLessThan(T expected) {
      if (this.t instanceof Integer) {
         if ((Integer)this.t >= (Integer)expected) {
            throw new AssertionError("Expected " + this.t + " to be less than " + expected);
         }
      } else if (this.t instanceof Long) {
         if ((Long)this.t >= (Long)expected) {
            throw new AssertionError("Expected " + this.t + " to be less than " + expected);
         }
      } else if (this.t instanceof Float) {
         if ((Float)this.t >= (Float)expected) {
            throw new AssertionError("Expected " + this.t + " to be less than " + expected);
         }
      } else if (this.t instanceof Double) {
         if ((Double)this.t >= (Double)expected) {
            throw new AssertionError("Expected " + this.t + " to be less than " + expected);
         }
      } else {
         throw new AssertionError("toBeLessThan can only be used on Integers, Longs, Floats, and Doubles");
      }
   }

   public void toBeInstanceOf(Class<?> expected) {
      if (!expected.isInstance(this.t)) {
         throw new AssertionError("Expected " + this.t + " to be an instance of " + expected);
      }
   }

   public void toContain(Object expected) {
      if (this.t instanceof String) {
         if (!((String)this.t).contains((String)expected)) {
            throw new AssertionError("Expected " + expected + " to be contained in " + this.t);
         }
      } else if (this.t instanceof Iterable) {
         if (!((Iterable)this.t).spliterator().tryAdvance(ox -> {
            if (!ox.equals(expected)) {
               throw new AssertionError("Expected " + expected + " to be contained in " + this.t);
            }
         })) {
            throw new AssertionError("Expected " + expected + " to be contained in " + this.t);
         }
      } else if (this.t instanceof Object[]) {
         for (Object o : (Object[])this.t) {
            if (o.equals(expected)) {
               return;
            }
         }

         throw new AssertionError("Expected " + expected + " to be contained in " + this.t);
      } else {
         throw new AssertionError("toContain can only be used on Strings, Iterables and Arrays");
      }
   }

   public void toHaveLength(int expected) {
      if (this.t instanceof String) {
         if (((String)this.t).length() != expected) {
            throw new AssertionError("Expected " + expected + " but got " + ((String)this.t).length());
         }
      } else if (this.t instanceof Iterable) {
         if (((Iterable)this.t).spliterator().getExactSizeIfKnown() != expected) {
            throw new AssertionError("Expected " + expected + " but got " + ((Iterable)this.t).spliterator().getExactSizeIfKnown());
         }
      } else if (this.t instanceof Object[]) {
         if (this.t.length != expected) {
            throw new AssertionError("Expected " + expected + " but got " + this.t.length);
         }
      } else {
         throw new AssertionError("toHaveLength can only be used on Strings");
      }
   }

   public <E extends Throwable> E toThrow(Class<E> expected) {
      try {
         ((Runnable)this.t).run();
      } catch (Throwable var3) {
         if (expected.isInstance(var3)) {
            return (E)var3;
         }

         throw new AssertionError("Expected " + expected.getSimpleName() + " but got " + var3.getClass().getSimpleName());
      }

      throw new AssertionError("Expected " + expected.getSimpleName() + " but got nothing");
   }
}
