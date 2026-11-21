package ac.grim.grimac.shaded.maps.weak;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

class DescriptionDeferringAbsence extends AbstractAbsence<DynamicChild> {
   DescriptionDeferringAbsence(DescriptionDeferringAbsence parent, Object key) {
      super(parent, key);
   }

   DescriptionDeferringAbsence(IssueDescribingChild parent, Object key) {
      super(parent, key);
   }

   @Override
   public Dynamic get(Object key) {
      return new DescriptionDeferringAbsence(this, key);
   }

   @Override
   public Object asObject() {
      LinkedList<DynamicChild> chainFromDescriber = DynamicChildLogic.using(this).getAscendingChainAllWith(DescriptionDeferringAbsence.class::isInstance);
      throw new NoSuchElementException(
         ((IssueDescribingChild)chainFromDescriber.getFirst().parent())
            .describeIssue(chainFromDescriber.stream().map(child -> child.key().asObject()).collect(Collectors.toList()))
      );
   }
}
