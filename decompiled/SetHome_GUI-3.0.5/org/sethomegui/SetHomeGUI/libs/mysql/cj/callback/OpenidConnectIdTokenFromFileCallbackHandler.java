package org.sethomegui.SetHomeGUI.libs.mysql.cj.callback;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.Messages;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertyKey;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.WrongArgumentException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.util.StringUtils;

public class OpenidConnectIdTokenFromFileCallbackHandler implements MysqlCallbackHandler {
   private static final int ID_TOKEN_SIZE_LIMIT = 10240;

   @Override
   public void handle(MysqlCallback cb) {
      if (OpenidConnectAuthenticationCallback.class.isAssignableFrom(cb.getClass())) {
         OpenidConnectAuthenticationCallback openidConnectAuthCallback = (OpenidConnectAuthenticationCallback)cb;
         String idTokenFileName = openidConnectAuthCallback.getConnProperty(PropertyKey.idTokenFile);
         if (StringUtils.isNullOrEmpty(idTokenFileName)) {
            throw (WrongArgumentException)ExceptionFactory.createException(
               WrongArgumentException.class, Messages.getString("AuthenticationOpenidConnect.MissingIdTokenFileOption")
            );
         } else {
            File idTokenFile = new File(idTokenFileName);
            if (idTokenFile.exists() && idTokenFile.isFile()) {
               if (idTokenFile.length() > 10240L) {
                  throw (WrongArgumentException)ExceptionFactory.createException(
                     WrongArgumentException.class, Messages.getString("AuthenticationOpenidConnect.InvalidIdTokenFile")
                  );
               } else {
                  try {
                     byte[] idToken = Files.readAllBytes(idTokenFile.toPath());
                     openidConnectAuthCallback.setIdentityToken(idToken);
                  } catch (IOException var6) {
                     throw (WrongArgumentException)ExceptionFactory.createException(
                        WrongArgumentException.class, Messages.getString("AuthenticationOpenidConnect.FailReadIdTokenFile")
                     );
                  }
               }
            } else {
               throw (WrongArgumentException)ExceptionFactory.createException(
                  WrongArgumentException.class, Messages.getString("AuthenticationOpenidConnect.FailReadIdTokenFile")
               );
            }
         }
      }
   }
}
