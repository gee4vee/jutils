/**
 * 
 */
package com.valencia.jutils.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Networking-related utilities.
 * 
 * @author Gabriel Valencia, gee4vee@me.com
 */
public class NetworkUtils {
    
    public static final String[] SITE_LOCAL_IP_ADDR_STARTS = new String[] {"192.", "10.", "172."};
    
    public static String getLocalHostName() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }
    
    /**
     * Returns the IPv4 address of the network interface that is used to connect this machine to a local network, i.e. the site local IP address. 
     * Typically this is the IP address that begins with 192., 10., or 172.
     * 
     * @throws SocketException
     */
    public static String getLocalNetworkIPv4Address() throws SocketException {
        Map<NetworkInterface, String> ni = getLocalNetworkInterface();
        if (ni == null || ni.isEmpty()) {
            return null;
        }
        
        return ni.values().iterator().next();
    }
    
    public static String getLocalNetworkIPv4Address(NetworkInterface ni) {
        Enumeration<InetAddress> niAddresses = ni.getInetAddresses();
        while (niAddresses.hasMoreElements()) {
            InetAddress niAddress = niAddresses.nextElement();
            String niHostAddr = niAddress.getHostAddress();
            for (String start : SITE_LOCAL_IP_ADDR_STARTS) {
                if (niHostAddr.startsWith(start)) {
                    return niHostAddr;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Returns the network interface that is used to connect this machine to a local network, i.e. the site local network interface. Typically 
     * this network interface has an IP address that begins with 192., 10., or 172.
     * 
     * @return A <code>Map</code> containing the network interface as the key whose value is the local network IPv4 address. The returned map 
     * will be empty if the local network interface could not be found.
     * 
     * @throws SocketException
     */
    public static Map<NetworkInterface, String> getLocalNetworkInterface() throws SocketException {
        Map<NetworkInterface, String> result = new LinkedHashMap<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            Enumeration<InetAddress> niAddresses = ni.getInetAddresses();
            while (niAddresses.hasMoreElements()) {
                InetAddress niAddress = niAddresses.nextElement();
                String niHostAddr = niAddress.getHostAddress();
                for (String start : SITE_LOCAL_IP_ADDR_STARTS) {
                    if (niHostAddr.startsWith(start)) {
                        result.put(ni, niHostAddr);
                    }
                }
            }
        }
        
        return result;
    }
    
    public static void main(String[] args) throws SocketException {
        System.out.println("ip = " + getLocalNetworkIPv4Address());
    }
}
