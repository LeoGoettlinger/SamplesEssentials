package kotlin.contracts

import kotlin.internal.ContractsDsl

@ContractsDsl
@ExperimentalContracts
@SinceKotlin(version = "1.3")
public interface SimpleEffect : Effect {
   @IgnorableReturnValue
   @ContractsDsl
   @ExperimentalContracts
   public abstract infix fun implies(booleanExpression: Boolean): ConditionalEffect {
   }
}
