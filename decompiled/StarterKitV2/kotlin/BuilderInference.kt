package kotlin

import java.lang.annotation.ElementType
import java.lang.annotation.RetentionPolicy
import kotlin.experimental.ExperimentalTypeInference

@Target(allowedTargets = [AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY])
@Retention(AnnotationRetention.BINARY)
@Deprecated(message = "BuilderInference annotation must not be used anymore. Builder inference is enabled automatically for builder calls if needed.")
@DeprecatedSinceKotlin(warningSince = "2.0", errorSince = "2.5", hiddenSince = "2.6")
@java.lang.annotation.Retention(RetentionPolicy.CLASS)
@java.lang.annotation.Target([ElementType.METHOD, ElementType.PARAMETER])
@SinceKotlin(version = "1.3")
@ExperimentalTypeInference
annotation class BuilderInference(

)
