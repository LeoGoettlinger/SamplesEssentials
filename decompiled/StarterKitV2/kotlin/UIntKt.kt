package kotlin

import kotlin.internal.InlineOnly
import kotlin.internal.IntrinsicConstEvaluation

@SinceKotlin(version = "1.5")
@InlineOnly
@IntrinsicConstEvaluation
public inline fun Byte.toUInt(): UInt {
   return UInt.constructor-impl(`$this$toUInt`);
}

@SinceKotlin(version = "1.5")
@InlineOnly
@IntrinsicConstEvaluation
public inline fun Short.toUInt(): UInt {
   return UInt.constructor-impl(`$this$toUInt`);
}

@SinceKotlin(version = "1.5")
@InlineOnly
@IntrinsicConstEvaluation
public inline fun Int.toUInt(): UInt {
   return UInt.constructor-impl(`$this$toUInt`);
}

@SinceKotlin(version = "1.5")
@InlineOnly
@IntrinsicConstEvaluation
public inline fun Long.toUInt(): UInt {
   return UInt.constructor-impl((int)`$this$toUInt`);
}

@SinceKotlin(version = "1.5")
@InlineOnly
@IntrinsicConstEvaluation
public inline fun Float.toUInt(): UInt {
   return UnsignedKt.doubleToUInt((double)`$this$toUInt`);
}

@SinceKotlin(version = "1.5")
@InlineOnly
@IntrinsicConstEvaluation
public inline fun Double.toUInt(): UInt {
   return UnsignedKt.doubleToUInt(`$this$toUInt`);
}
