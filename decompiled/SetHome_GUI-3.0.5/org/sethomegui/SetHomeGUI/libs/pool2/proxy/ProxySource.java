package org.sethomegui.SetHomeGUI.libs.pool2.proxy;

import org.sethomegui.SetHomeGUI.libs.pool2.UsageTracking;

interface ProxySource<T> {
   T createProxy(T var1, UsageTracking<T> var2);

   T resolveProxy(T var1);
}
