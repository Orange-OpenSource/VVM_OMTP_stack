package com.orange.labs.uk.omtp.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.orange.labs.uk.omtp.logging.Logger;

public final class ConnectionUtils {

	private static final Logger logger = Logger.getLogger(ConnectionUtils.class);
	
	public static int lookupHost(String hostname) {
	    InetAddress inetAddress;
	    try {
	        inetAddress = InetAddress.getByName(hostname);
	    } catch (UnknownHostException e) {
	    	logger.w(String.format("Impossible to get the InetAddress: %s", e.getMessage()));
	    	// Nothing to do.
	        return -1;
	    }
	    logger.i(inetAddress.toString());
	    byte[] addrBytes;
	    int addr;
	    addrBytes = inetAddress.getAddress();
	    addr = ((addrBytes[3] & 0xff) << 24)
	            | ((addrBytes[2] & 0xff) << 16)
	            | ((addrBytes[1] & 0xff) << 8)
	            |  (addrBytes[0] & 0xff);
	    return addr;
	}

	
}
