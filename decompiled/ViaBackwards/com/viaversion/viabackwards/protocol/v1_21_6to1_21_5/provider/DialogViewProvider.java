package com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.provider;

import com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data.Dialog;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.providers.Provider;

public interface DialogViewProvider extends Provider {
   void openDialog(UserConnection var1, Dialog var2);

   void closeDialog(UserConnection var1);
}
