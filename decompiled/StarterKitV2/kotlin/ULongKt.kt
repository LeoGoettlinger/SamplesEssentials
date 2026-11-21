package kotlin

import kotlin.internal.InlineOnly
import kotlin.internal.IntrinsicConstEvaluation

@SinceKotlin(version = "1.5")
@InlineOnly
@IntrinsicConstEvaluation
public inline fun Byte.toULong(): ULong {
   return ULong.constructor-impl((long)`$this$toULong`);
}

@SinceKotlin(version = "1.5")
@InlineOnly
@IntrinsicConstEvaluation
public inline fun Short.toULong(): ULong {
   return ULong.constructor-impl((long)`$this$toULong`);
}

@SinceKotlin(version = "1.5")
@InlineOnly
@IntrinsicConstEvaluation
public inline fun Int.toULong(): ULong {
   return ULong.constructor-impl((long)`$this$toULong`);
}

@SinceKotlin(version = "1.5")
@InlineOnly
@IntrinsicConstEvaluation
public inline fun Long.toULong(): ULong {
   return ULong.constructor-impl(`$this$toULong`);
}

@SinceKotlin(version = "1.5")
@InlineOnly
@IntrinsicConstEvaluation
public inline fun Float.toULong(): ULong {
   return UnsignedKt.doubleToULong((double)`$this$toULong`);
}

@SinceKotlin(version = "1.5")
@InlineOnly
@IntrinsicConstEvaluation
public inline fun Double.toULong(): ULong {
   return UnsignedKt.doubleToULong(`$this$toULong`);
}
