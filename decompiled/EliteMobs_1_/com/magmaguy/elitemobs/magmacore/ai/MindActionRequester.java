package com.magmaguy.elitemobs.magmacore.ai;

@FunctionalInterface
public interface MindActionRequester {
   MindActionResult request(MindActionRequest var1);

   static MindActionRequester rejecting() {
      return request -> MindActionResult.REJECTED;
   }
}
