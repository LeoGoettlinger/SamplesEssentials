package com.magmaguy.elitemobs.magmacore.ai;

@FunctionalInterface
public interface MindFailureListener {
   MindFailureListener IGNORE = failure -> {};

   void onFailure(MindFailure var1);
}
