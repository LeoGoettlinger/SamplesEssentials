package org.sethomegui.SetHomeGUI.libs.pool2.impl;

import org.sethomegui.SetHomeGUI.libs.pool2.PooledObject;

public class DefaultEvictionPolicy<T> implements EvictionPolicy<T> {
   @Override
   public boolean evict(EvictionConfig config, PooledObject<T> underTest, int idleCount) {
      return config.getIdleSoftEvictDuration().compareTo(underTest.getIdleDuration()) < 0 && config.getMinIdle() < idleCount
         || config.getIdleEvictDuration().compareTo(underTest.getIdleDuration()) < 0;
   }
}
