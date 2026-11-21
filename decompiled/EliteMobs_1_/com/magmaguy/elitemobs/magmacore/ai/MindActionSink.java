package com.magmaguy.elitemobs.magmacore.ai;

@FunctionalInterface
public interface MindActionSink {
   MindActionResult request(MindActionContext var1, MindActionRequest var2);

   static MindActionSink rejecting() {
      return (context, request) -> MindActionResult.REJECTED;
   }
}
