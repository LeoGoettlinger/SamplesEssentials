package kotlin

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Target(allowedTargets = [AnnotationTarget.VALUE_PARAMETER])
@MustBeDocumented
@Documented
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target([ElementType.PARAMETER])
@SinceKotlin(version = "2.3")
@ExperimentalVersionOverloading
annotation class IntroducedAt(
   val version: String
)
