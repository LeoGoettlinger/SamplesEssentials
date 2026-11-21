package com.earth2me.essentials.libs.checkerframework.checker.nullness.qual;

import com.earth2me.essentials.libs.checkerframework.framework.qual.MonotonicQualifier;
import com.earth2me.essentials.libs.checkerframework.framework.qual.SubtypeOf;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE})
@SubtypeOf({Nullable.class})
@MonotonicQualifier(NonNull.class)
public @interface MonotonicNonNull {
}
