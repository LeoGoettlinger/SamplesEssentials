package org.enginehub.piston.suggestion;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.enginehub.piston.CommandParseResult;

public interface SuggestionProvider {
   ImmutableSet<Suggestion> provideSuggestions(List<String> var1, CommandParseResult var2);
}
