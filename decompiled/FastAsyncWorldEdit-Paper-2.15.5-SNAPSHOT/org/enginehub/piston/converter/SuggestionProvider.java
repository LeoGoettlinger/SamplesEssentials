package org.enginehub.piston.converter;

import java.util.List;
import org.enginehub.piston.inject.InjectedValueAccess;

@FunctionalInterface
public interface SuggestionProvider {
   List<String> getSuggestions(String var1, InjectedValueAccess var2);
}
