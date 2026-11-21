package org.jsoup.select;

import java.util.Optional;
import java.util.stream.Collectors;
import org.jsoup.nodes.Element;
import org.jspecify.annotations.Nullable;

public class Collector {
   private Collector() {
   }

   public static Elements collect(Evaluator eval, Element root) {
      eval.reset();
      return root.stream().filter(eval.asPredicate(root)).collect(Collectors.toCollection(Elements::new));
   }

   @Nullable
   public static Element findFirst(Evaluator eval, Element root) {
      eval.reset();
      Optional<Element> first = root.stream().filter(eval.asPredicate(root)).findFirst();
      return first.orElse(null);
   }
}
