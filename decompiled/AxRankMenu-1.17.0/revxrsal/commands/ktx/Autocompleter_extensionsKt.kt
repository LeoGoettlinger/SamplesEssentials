package revxrsal.commands.ktx

import kotlin.jvm.internal.Intrinsics
import revxrsal.commands.CommandHandler
import revxrsal.commands.autocomplete.AutoCompleter
import revxrsal.commands.autocomplete.SuggestionProvider
import revxrsal.commands.autocomplete.SuggestionProviderFactory

@JvmSynthetic
public inline fun <reified T> AutoCompleter.parameterSuggestions(provider: SuggestionProvider): AutoCompleter {
   val `$this$parameterSuggestions_u24lambda_u2d0`: AutoCompleter = `$this$parameterSuggestions`;
   Intrinsics.reifiedOperationMarker(4, "T");
   `$this$parameterSuggestions_u24lambda_u2d0`.registerParameterSuggestions(Object::class.java, provider);
   return `$this$parameterSuggestions`;
}

@JvmSynthetic
public inline fun <reified T> AutoCompleter.parameterSuggestions(provider: String): AutoCompleter {
   val `$this$parameterSuggestions_u24lambda_u2d1`: AutoCompleter = `$this$parameterSuggestions`;
   Intrinsics.reifiedOperationMarker(4, "T");
   `$this$parameterSuggestions_u24lambda_u2d1`.registerParameterSuggestions(Object::class.java, provider);
   return `$this$parameterSuggestions`;
}

@JvmSynthetic
public inline fun <reified T> AutoCompleter.parameterSuggestions(values: Collection<String>): AutoCompleter {
   val `$this$parameterSuggestions_u24lambda_u2d2`: AutoCompleter = `$this$parameterSuggestions`;
   Intrinsics.reifiedOperationMarker(4, "T");
   `$this$parameterSuggestions_u24lambda_u2d2`.registerParameterSuggestions(Object::class.java, SuggestionProvider.of(values));
   return `$this$parameterSuggestions`;
}

public fun AutoCompleter.suggestionFactory(factory: SuggestionProviderFactory): AutoCompleter {
   `$this$suggestionFactory`.registerSuggestionFactory(factory);
   return `$this$suggestionFactory`;
}

public fun CommandHandler.autoCompleter(dsl: (AutoCompleter) -> Unit) {
   val var10001: AutoCompleter = `$this$autoCompleter`.getAutoCompleter();
   dsl.invoke(var10001);
}
