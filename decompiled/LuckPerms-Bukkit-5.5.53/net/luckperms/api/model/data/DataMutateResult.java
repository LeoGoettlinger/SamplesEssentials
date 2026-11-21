package net.luckperms.api.model.data;

import net.luckperms.api.node.Node;
import net.luckperms.api.util.Result;
import org.checkerframework.checker.nullness.qual.NonNull;

public enum DataMutateResult implements Result {
   SUCCESS(true),
   FAIL(false),
   FAIL_ALREADY_HAS(false),
   FAIL_LACKS(false);

   private final boolean successful;

   private DataMutateResult(boolean successful) {
      this.successful = successful;
   }

   @Override
   public boolean wasSuccessful() {
      return this.successful;
   }

   public interface WithMergedNode {
      @NonNull
      DataMutateResult getResult();

      @NonNull
      Node getMergedNode();
   }
}
