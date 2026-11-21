package org.jsoup.select;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.NodeIterator;

abstract class StructuralEvaluator extends Evaluator {
   final Evaluator evaluator;
   final ThreadLocal<IdentityHashMap<Element, IdentityHashMap<Element, Boolean>>> threadMemo = ThreadLocal.withInitial(IdentityHashMap::new);

   public StructuralEvaluator(Evaluator evaluator) {
      this.evaluator = evaluator;
   }

   boolean memoMatches(Element root, Element element) {
      IdentityHashMap<Element, IdentityHashMap<Element, Boolean>> rootMemo = this.threadMemo.get();
      IdentityHashMap<Element, Boolean> memo = rootMemo.get(root);
      if (memo == null) {
         memo = new IdentityHashMap<>();
         rootMemo.put(root, memo);
      }

      Boolean matches = memo.get(element);
      if (matches == null) {
         matches = this.evaluator.matches(root, element);
         memo.put(element, matches);
      }

      return matches;
   }

   @Override
   protected void reset() {
      this.threadMemo.get().clear();
      super.reset();
   }

   static class Has extends StructuralEvaluator {
      static final ThreadLocal<NodeIterator<Element>> ThreadElementIter = ThreadLocal.withInitial(() -> new NodeIterator<>(new Element("html"), Element.class));

      public Has(Evaluator evaluator) {
         super(evaluator);
      }

      @Override
      public boolean matches(Element root, Element element) {
         NodeIterator<Element> it = ThreadElementIter.get();
         it.restart(element);

         while (it.hasNext()) {
            Element el = it.next();
            if (el != element && this.evaluator.matches(element, el)) {
               return true;
            }
         }

         return false;
      }

      @Override
      protected int cost() {
         return 10 * this.evaluator.cost();
      }

      @Override
      public String toString() {
         return String.format(":has(%s)", this.evaluator);
      }
   }

   @Deprecated
   static class ImmediateParent extends StructuralEvaluator {
      public ImmediateParent(Evaluator evaluator) {
         super(evaluator);
      }

      @Override
      public boolean matches(Element root, Element element) {
         if (root == element) {
            return false;
         } else {
            Element parent = element.parent();
            return parent != null && this.memoMatches(root, parent);
         }
      }

      @Override
      protected int cost() {
         return 1 + this.evaluator.cost();
      }

      @Override
      public String toString() {
         return String.format("%s > ", this.evaluator);
      }
   }

   static class ImmediateParentRun extends Evaluator {
      final ArrayList<Evaluator> evaluators = new ArrayList<>();
      int cost = 2;

      public ImmediateParentRun(Evaluator evaluator) {
         this.evaluators.add(evaluator);
         this.cost = this.cost + evaluator.cost();
      }

      void add(Evaluator evaluator) {
         this.evaluators.add(evaluator);
         this.cost = this.cost + evaluator.cost();
      }

      @Override
      public boolean matches(Element root, Element element) {
         if (element == root) {
            return false;
         } else {
            for (int i = this.evaluators.size() - 1; i >= 0; i--) {
               if (element == null) {
                  return false;
               }

               Evaluator eval = this.evaluators.get(i);
               if (!eval.matches(root, element)) {
                  return false;
               }

               element = element.parent();
            }

            return true;
         }
      }

      @Override
      protected int cost() {
         return this.cost;
      }

      @Override
      public String toString() {
         return StringUtil.join(this.evaluators, " > ");
      }
   }

   static class ImmediatePreviousSibling extends StructuralEvaluator {
      public ImmediatePreviousSibling(Evaluator evaluator) {
         super(evaluator);
      }

      @Override
      public boolean matches(Element root, Element element) {
         if (root == element) {
            return false;
         } else {
            Element prev = element.previousElementSibling();
            return prev != null && this.memoMatches(root, prev);
         }
      }

      @Override
      protected int cost() {
         return 2 + this.evaluator.cost();
      }

      @Override
      public String toString() {
         return String.format("%s + ", this.evaluator);
      }
   }

   static class Is extends StructuralEvaluator {
      public Is(Evaluator evaluator) {
         super(evaluator);
      }

      @Override
      public boolean matches(Element root, Element element) {
         return this.evaluator.matches(root, element);
      }

      @Override
      protected int cost() {
         return 2 + this.evaluator.cost();
      }

      @Override
      public String toString() {
         return String.format(":is(%s)", this.evaluator);
      }
   }

   static class Not extends StructuralEvaluator {
      public Not(Evaluator evaluator) {
         super(evaluator);
      }

      @Override
      public boolean matches(Element root, Element element) {
         return !this.memoMatches(root, element);
      }

      @Override
      protected int cost() {
         return 2 + this.evaluator.cost();
      }

      @Override
      public String toString() {
         return String.format(":not(%s)", this.evaluator);
      }
   }

   static class Parent extends StructuralEvaluator {
      public Parent(Evaluator evaluator) {
         super(evaluator);
      }

      @Override
      public boolean matches(Element root, Element element) {
         if (root == element) {
            return false;
         } else {
            for (Element parent = element.parent(); parent != null; parent = parent.parent()) {
               if (this.memoMatches(root, parent)) {
                  return true;
               }

               if (parent == root) {
                  break;
               }
            }

            return false;
         }
      }

      @Override
      protected int cost() {
         return 2 * this.evaluator.cost();
      }

      @Override
      public String toString() {
         return String.format("%s ", this.evaluator);
      }
   }

   static class PreviousSibling extends StructuralEvaluator {
      public PreviousSibling(Evaluator evaluator) {
         super(evaluator);
      }

      @Override
      public boolean matches(Element root, Element element) {
         if (root == element) {
            return false;
         } else {
            for (Element sibling = element.firstElementSibling(); sibling != null && sibling != element; sibling = sibling.nextElementSibling()) {
               if (this.memoMatches(root, sibling)) {
                  return true;
               }
            }

            return false;
         }
      }

      @Override
      protected int cost() {
         return 3 * this.evaluator.cost();
      }

      @Override
      public String toString() {
         return String.format("%s ~ ", this.evaluator);
      }
   }

   static class Root extends Evaluator {
      @Override
      public boolean matches(Element root, Element element) {
         return root == element;
      }

      @Override
      protected int cost() {
         return 1;
      }

      @Override
      public String toString() {
         return "";
      }
   }
}
