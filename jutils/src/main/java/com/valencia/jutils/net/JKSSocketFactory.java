/**
 * 
 */
package com.valencia.jutils.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>A factory for producing sockets and server sockets including support for SSL with TLS v1.2. For SSL support, a trust store, server 
 * key store, and client key store in JKS format are required. The key store and underlying key are expected to have the same password, 
 * if any.
 * 
 * <p>Based on code from https://github.com/jawi/ssl-socket-demo.
 * 
 * @author Gabriel Valencia, gee4vee@me.com
 */
public class JKSSocketFactory implements SslContextProvider {
    
    /**
     * This class' logger can be changed by applications to redirect logging.
     */
    public static Logger logger = LogManager.getLogger(JKSSocketFactory.class);
	
	public static final String PROTOCOL_TLS_12 = "TLSv1.2";
	
    public static final int DEFAULT_SOCKET_SO_TIMEOUT = 1000*60;
	
    protected boolean useSSL = true;
    protected final String truststoreName;
    protected final String truststorePass;
    
	protected final String serverKeystoreName;
	protected final String serverKeystorePass;
	
    protected final String clientKeystoreName;
    protected final String clientKeystorePass;

    /**
     * Creates a new socket factory.
     * 
     * @param useSSL If <code>true</code>, the sockets returned by the create methods will be SSL sockets requiring client authentication 
     * and all the other parameters must not be <code>null</code>. Otherwise, the sockets will not support SSL and the rest of the 
     * parameters in this constructor can be <code>null</code>.
     * @param truststorePath The path to the trust store JKS file. This trust store will be set at the JVM level, thereby affecting 
     * all SSL socket usage in the current JVM.
     * @param truststorePass The trust store's password. Must match the underlying key's password.
     * @param serverKeystorePath The path to the key store JKS file to be used for server sockets.
     * @param serverKeystorePass The password for the server key store. Must match the underlying key's password.
     * @param clientKeystorePath The path to the key store JKS file to be used for client sockets.
     * @param clientKeystorePass The password for the client key store. Must match the underlying key's password.
     */
	public JKSSocketFactory(boolean useSSL, String truststorePath, String truststorePass, String serverKeystorePath, String serverKeystorePass, 
	        String clientKeystorePath, String clientKeystorePass) {
		this.useSSL = useSSL;
		this.truststoreName = truststorePath;
		this.truststorePass = truststorePass;
		
		this.serverKeystoreName = serverKeystorePath;
		this.serverKeystorePass = serverKeystorePass;
        
        this.clientKeystoreName = clientKeystorePath;
        this.clientKeystorePass = clientKeystorePass;
		
		if (this.useSSL) {
            System.setProperty("javax.net.ssl.trustStore", this.truststoreName);
		} else {
            System.setProperty("javax.net.ssl.trustStore", "");
		}
	}
    
    public ServerSocket createServerSocket(int port) throws IOException, GeneralSecurityException {
        ServerSocket socket;
        if (this.useSSL) {
            if (logger.isTraceEnabled()) {
                logger.trace("Creating SSL server socket for port " + port);
            }
            socket = SslUtils.createSSLServerSocket(port, this);
            ((SSLServerSocket)socket).setNeedClientAuth(true);
            
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Creating server socket for port " + port);
            }
            socket = new ServerSocket(port);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Returning server socket impl " + socket.getClass().getName());
        }
        return socket;
    }

    public Socket createSocket(String host, int port) throws IOException, GeneralSecurityException {
        return this.createSocket(host, port, DEFAULT_SOCKET_SO_TIMEOUT);
    }
	
	public Socket createSocket(String host, int port, Integer timeout) throws IOException, GeneralSecurityException {
		Socket socket;
		if (this.useSSL) {
	        if (logger.isTraceEnabled()) {
	            logger.trace("Creating SSL socket for " + host + ":" + port);
	        }
            socket = SslUtils.createSSLSocket(host, port, this);
            if (timeout != null) {
                socket.setSoTimeout(timeout);
            }
			
		} else {
            if (logger.isTraceEnabled()) {
                logger.trace("Creating socket for " + host + ":" + port);
            }
            socket = new Socket(host, port);
		}

        if (logger.isTraceEnabled()) {
            logger.trace("Returning socket impl " + socket.getClass().getName());
        }
		return socket;
	}
    
    @Override
    public KeyManager[] getServerKeyManagers() throws GeneralSecurityException, IOException {
        return SslUtils.createKeyManagers(this.serverKeystoreName, this.serverKeystorePass.toCharArray());
    }
    
    @Override
    public KeyManager[] getClientKeyManagers() throws GeneralSecurityException, IOException {
        return SslUtils.createKeyManagers(this.clientKeystoreName, this.clientKeystorePass.toCharArray());
    }

    @Override
    public TrustManager[] getTrustManagers() throws GeneralSecurityException, IOException {
        return SslUtils.createTrustManagers(this.truststoreName, this.truststorePass.toCharArray());
    }

    @Override
    public String getProtocol() {
        return PROTOCOL_TLS_12;
    }

}
