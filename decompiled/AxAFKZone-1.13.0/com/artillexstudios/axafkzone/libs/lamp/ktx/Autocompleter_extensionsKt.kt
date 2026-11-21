package com.artillexstudios.axafkzone.libs.lamp.ktx

import com.artillexstudios.axafkzone.libs.lamp.CommandHandler
import com.artillexstudios.axafkzone.libs.lamp.autocomplete.AutoCompleter
import com.artillexstudios.axafkzone.libs.lamp.autocomplete.SuggestionProvider
import com.artillexstudios.axafkzone.libs.lamp.autocomplete.SuggestionProviderFactory
import kotlin.jvm.internal.Intrinsics

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
