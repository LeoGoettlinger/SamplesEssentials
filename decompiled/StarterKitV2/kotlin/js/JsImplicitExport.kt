package kotlin.js

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.internal.UsedFromCompilerGeneratedCode

@Target(allowedTargets = [AnnotationTarget.CLASS])
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target([ElementType.TYPE])
@UsedFromCompilerGeneratedCode
annotation class JsImplicitExport(
   val couldBeConvertedToExplicitExport: Boolean
)
