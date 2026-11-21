package kotlin

import kotlin.internal.InlineOnly
import kotlin.internal.IntrinsicConstEvaluation

@SinceKotlin(version = "1.5")
@InlineOnly
@IntrinsicConstEvaluation
public inline fun Byte.toUShort(): UShort {
   return UShort.constructor-impl((short)`$this$toUShort`);
}

@SinceKotlin(version = "1.5")
@InlineOnly
@IntrinsicConstEvaluation
public inline fun Short.toUShort(): UShort {
   return UShort.constructor-impl(`$this$toUShort`);
}

@SinceKotlin(version = "1.5")
@InlineOnly
@IntrinsicConstEvaluation
public inline fun Int.toUShort(): UShort {
   return UShort.constructor-impl((short)`$this$toUShort`);
}

@SinceKotlin(version = "1.5")
@InlineOnly
@IntrinsicConstEvaluation
public inline fun Long.toUShort(): UShort {
   return UShort.constructor-impl((short)((int)`$this$toUShort`));
}
