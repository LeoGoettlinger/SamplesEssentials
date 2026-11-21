package kotlin

import kotlin.contracts.InvocationKind
import kotlin.internal.InlineOnly

internal class ContextParametersKt__ContextKt {
   @InlineOnly
   @SinceKotlin(version = "2.2")
   @JvmStatic
   public inline fun <T, R> context(with: T, block: (T) -> R): R {
      contract {
         callsInPlace(block, InvocationKind.EXACTLY_ONCE)
      }

      return (R)block.invoke(with);
   }

   @InlineOnly
   @SinceKotlin(version = "2.2")
   @JvmStatic
   public inline fun <A, B, R> context(a: A, b: B, block: (A, B) -> R): R {
      contract {
         callsInPlace(block, InvocationKind.EXACTLY_ONCE)
      }

      return (R)block.invoke(a, b);
   }

   @InlineOnly
   @SinceKotlin(version = "2.2")
   @JvmStatic
   public inline fun <A, B, C, R> context(a: A, b: B, c: C, block: (A, B, C) -> R): R {
      contract {
         callsInPlace(block, InvocationKind.EXACTLY_ONCE)
      }

      return (R)block.invoke(a, b, c);
   }

   @InlineOnly
   @SinceKotlin(version = "2.2")
   @JvmStatic
   public inline fun <A, B, C, D, R> context(a: A, b: B, c: C, d: D, block: (A, B, C, D) -> R): R {
      contract {
         callsInPlace(block, InvocationKind.EXACTLY_ONCE)
      }

      return (R)block.invoke(a, b, c, d);
   }

   @InlineOnly
   @SinceKotlin(version = "2.2")
   @JvmStatic
   public inline fun <A, B, C, D, E, R> context(a: A, b: B, c: C, d: D, e: E, block: (A, B, C, D, E) -> R): R {
      contract {
         callsInPlace(block, InvocationKind.EXACTLY_ONCE)
      }

      return (R)block.invoke(a, b, c, d, e);
   }

   @InlineOnly
   @SinceKotlin(version = "2.2")
   @JvmStatic
   public inline fun <A, B, C, D, E, F, R> context(a: A, b: B, c: C, d: D, e: E, f: F, block: (A, B, C, D, E, F) -> R): R {
      contract {
         callsInPlace(block, InvocationKind.EXACTLY_ONCE)
      }

      return (R)block.invoke(a, b, c, d, e, f);
   }

   open fun ContextParametersKt__ContextKt() {
   }
}
