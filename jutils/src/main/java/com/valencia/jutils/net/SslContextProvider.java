/**
 * 
 */
package com.valencia.jutils.net;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

/**
 * Provides context for components that communicate using SSL. Based on code from https://github.com/jawi/ssl-socket-demo.
 * 
 * @author Gabriel Valencia, <gee4vee@me.com>
 */
public interface SslContextProvider {
    
    /**
     * Returns the trust managers available to this provider.
     * 
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public TrustManager[] getTrustManagers() throws GeneralSecurityException, IOException;
    
    /**
     * Returns the key managers available to this provider to be used for creating server sockets.
     * 
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public KeyManager[] getServerKeyManagers() throws GeneralSecurityException, IOException;
    
    /**
     * Returns the key managers available to this provider to be used for creating client sockets.
     * 
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public KeyManager[] getClientKeyManagers() throws GeneralSecurityException, IOException;

    /**
     * Returns the SSL protocol used by this provider.
     */
    public String getProtocol();
}
