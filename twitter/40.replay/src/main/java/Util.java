import java.net.NetworkInterface;
import java.net.InetAddress;
import java.util.Enumeration;

class Util {
	static String GetHostname() throws java.net.UnknownHostException {
		java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
		String hn = addr.getHostName();
		//System.out.println(hn);
		return hn;
	}

	static String GetEth0IP() throws java.net.SocketException {
		for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
			NetworkInterface intf = en.nextElement();
			if (intf.getName().equals("eth0")) {
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
					String ip = enumIpAddr.nextElement().toString();
					if (ip.charAt(0) == '/')
						ip = ip.substring(1);
					//System.out.println(ip);
					return ip;
				}
			}
		}
		return new String();
	}
}
