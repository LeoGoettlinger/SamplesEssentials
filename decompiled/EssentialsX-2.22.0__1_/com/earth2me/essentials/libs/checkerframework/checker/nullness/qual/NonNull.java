package com.earth2me.essentials.libs.checkerframework.checker.nullness.qual;

import com.earth2me.essentials.libs.checkerframework.framework.qual.DefaultFor;
import com.earth2me.essentials.libs.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import com.earth2me.essentials.libs.checkerframework.framework.qual.LiteralKind;
import com.earth2me.essentials.libs.checkerframework.framework.qual.QualifierForLiterals;
import com.earth2me.essentials.libs.checkerframework.framework.qual.SubtypeOf;
import com.earth2me.essentials.libs.checkerframework.framework.qual.TypeKind;
import com.earth2me.essentials.libs.checkerframework.framework.qual.TypeUseLocation;
import com.earth2me.essentials.libs.checkerframework.framework.qual.UpperBoundFor;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf({MonotonicNonNull.class})
@DefaultQualifierInHierarchy
@QualifierForLiterals({LiteralKind.STRING})
@DefaultFor({TypeUseLocation.EXCEPTION_PARAMETER})
@UpperBoundFor(
   typeKinds = {TypeKind.PACKAGE, TypeKind.INT, TypeKind.BOOLEAN, TypeKind.CHAR, TypeKind.DOUBLE, TypeKind.FLOAT, TypeKind.LONG, TypeKind.SHORT, TypeKind.BYTE}
)
public @interface NonNull {
}
