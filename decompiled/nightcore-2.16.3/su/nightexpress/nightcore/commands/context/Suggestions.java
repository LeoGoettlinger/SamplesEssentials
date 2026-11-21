package su.nightexpress.nightcore.commands.context;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;

public class Suggestions {
   public List<String> suggestions = new ArrayList<>();

   @NonNull
   public List<String> getSuggestions() {
      return this.suggestions;
   }

   public void setSuggestions(@NonNull List<String> suggestions) {
      this.suggestions = suggestions;
   }
}
