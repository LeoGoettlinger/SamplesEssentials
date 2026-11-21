package com.earth2me.essentials.libs.checkerframework.checker.nullness.qual;

import com.earth2me.essentials.libs.checkerframework.framework.qual.DefaultFor;
import com.earth2me.essentials.libs.checkerframework.framework.qual.LiteralKind;
import com.earth2me.essentials.libs.checkerframework.framework.qual.QualifierForLiterals;
import com.earth2me.essentials.libs.checkerframework.framework.qual.SubtypeOf;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf({})
@QualifierForLiterals({LiteralKind.NULL})
@DefaultFor(
   types = {Void.class}
)
public @interface Nullable {
}
