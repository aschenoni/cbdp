import java.net.NetworkInterface;
import java.net.InetAddress;
import java.util.Enumeration;

class Util {
	static private String _hostname = null;
	static String GetHostname() throws java.net.UnknownHostException {
		if (_hostname != null)
			return _hostname;
		java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
		_hostname = addr.getHostName();
		return _hostname;
	}

	static private String _eth0_ip = null;
	static String GetEth0IP() throws java.net.SocketException {
		if (_eth0_ip != null)
			return _eth0_ip;
		for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
			NetworkInterface intf = en.nextElement();
			if (intf.getName().equals("eth0")) {
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
					String ip = enumIpAddr.nextElement().toString();
					if (ip.charAt(0) == '/')
						ip = ip.substring(1);
					//System.out.println(ip);
					_eth0_ip = ip;
					return _eth0_ip;
				}
			}
		}
		if (true) throw new RuntimeException("Unable to get the IPv4 address");
		return "";
	}
}
