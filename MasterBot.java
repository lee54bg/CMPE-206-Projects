/*
 * Author: Brandon Lee Gaerlan
 * Program: Create a program that connects a SlaveBot to a MasterBot
 * The master must have the following specified commands as noted in the Programming Project requirements
 * */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class MasterBot extends Thread {
	
	// Initialize a single ServerSocket
	private ServerSocket masterSocket;
	private Socket clients;
	
	private InetAddress ipAddr;
	private int hostPortNum;
	private int targetPortNum, numOfConnections;
	
	// Slave sockets and their associated dates
	private List<Socket> slaveSockets = new ArrayList<Socket>();
	private List<String> ipAdds = new ArrayList<>();
	private List<LocalDate> dates = new ArrayList<LocalDate>();
	
	//Constructor
	public MasterBot(int hostPortNum) throws IOException {
		this.hostPortNum = hostPortNum;
		masterSocket = new ServerSocket(hostPortNum);		
	}
	
	/*
	 * Main Program
	 * */
	public static void main(String args[]) {
		
		try {
			int portNum;
			String cmdPrmpt = null;
			Scanner in = new Scanner(System.in);
			String[] cmdArgs;
			
			if(args.length != 2) {
				
				System.out.println(">Terminating.  Not enough arguments");
				System.exit(0);
				
			} else {
				
				portNum = Integer.parseInt(args[1]);
				
				System.out.println("Initiating Server");
				
				MasterBot master = new MasterBot(portNum);
				master.start();
				
				// Execute arguments in the CLI
				while(true) {
					
					System.out.print(">");
					cmdPrmpt = in.nextLine();
					
					cmdArgs = cmdPrmpt.split(" ");
					
					if(cmdArgs[0].toLowerCase().equals("exit")) {
	    				break;
	    			}				
					
					switch(cmdArgs[0].toLowerCase()) {
						case "connect":
							master.connect(cmdPrmpt);
							break;
						case "disconnect":
							master.disconnect(cmdPrmpt);
							break;
						case "list":
							master.list();
							break;
						case "ipscan":
							final String cmdPrmptIp = cmdPrmpt;
							// System.out.println(commandPrompt);
							Thread ipScan = new Thread(new Runnable() {
								@Override
								public void run() {
									master.ipscan(cmdPrmptIp);
								}
							});
							ipScan.start();
							ipScan.sleep(1000);
							break;
						case "tcpscan":
							final String commandPrompt = cmdPrmpt;
							Thread tcpScan = new Thread(new Runnable() {
								@Override
								public void run() {
									master.tcpscan(commandPrompt);
								}
							});
							tcpScan.start();
							tcpScan.sleep(1000);
							break;
						case "geoipscan":
							final String cmdPrmptGeo = cmdPrmpt;
							Thread geoIPScan = new Thread(new Runnable() {
								@Override
								public void run() {
									master.tcpscan(cmdPrmptGeo);
								}
							});
							geoIPScan.start();
							geoIPScan.sleep(1000);
							break;
						default:
							continue;						
					}
				} // End of while loop
				
				PrintWriter output;
				
				for(Socket sockets : master.getSlaveSockets() ) {
					output	= new PrintWriter(sockets.getOutputStream(), true);
					System.out.println(sockets.getInetAddress().getHostName()
						+ "\t"
						+ sockets.getInetAddress().getHostName());
					output.println("exit");					
				}
				
				System.out.println("Exiting MasterBot...");
			}			
		} catch(Exception e) {
			System.out.println(e.toString());
		}
		
		System.exit(0);
	} // End of Main
	
	// Getters and Setters
	
	public ServerSocket getMasterSocket() {
		return masterSocket;
	}
	
	public List<Socket> getSlaveSockets() {
		return slaveSockets;
	}

	public List<LocalDate> getDates() {
		return dates;
	}	
	
	public void initCmdLine() {
		
	}
	
	// Connect, Disconnect, and List methods
	
	public void connect(String cmdPrmpt) {
		try {
			String[] cmdArgs = cmdPrmpt.split(" ");
			
			ArrayList<Socket> sockets = (ArrayList<Socket>) getSlaveSockets();
			PrintWriter output;
			
			if(cmdArgs[1].equals("all")) {
				for(Socket socket : sockets) {
					output	= new PrintWriter(socket.getOutputStream(), true);
					output.println(cmdPrmpt);
					break;
				}
			} else if(checkIP(cmdArgs[1])) {
				String toCompare = "/" + cmdArgs[1];
				
				for(Socket socket : sockets) {
					if(socket.getInetAddress().toString().equals(toCompare)) {
						output	= new PrintWriter(socket.getOutputStream(), true);
						output.println(cmdPrmpt);
						break;
					}
				}
			} else {
				for(Socket socket : sockets) {
					if(socket.getInetAddress().getHostName().toString().equals(cmdArgs[1])) {
						output	= new PrintWriter(socket.getOutputStream(), true);
						output.println(cmdPrmpt);
						break;
					}
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void disconnect(String cmdPrmpt) {
		try {
			String[] cmdArgs = cmdPrmpt.split(" ");
			
			ArrayList<Socket> sockets = (ArrayList<Socket>) getSlaveSockets();
			PrintWriter output;			
			
			if(checkIP(cmdArgs[1])) {
				String toCompare = "/" + cmdArgs[1];
				
				for(Socket socket : sockets) {
					if(socket.getInetAddress().toString().equals(toCompare)) {
						output	= new PrintWriter(socket.getOutputStream(), true);
						output.println(cmdPrmpt);
						output.flush();
						break;
					}
				}
			} else {
				for(Socket socket : sockets) {
					if(socket.getInetAddress().getHostName().toString().equals(cmdArgs)) {
						System.out.println("Socket exists");
						output	= new PrintWriter(socket.getOutputStream(), true);
						output.flush();
						output.println(cmdPrmpt);						
						break;
					}
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void ipscan(String cmdPrmpt) {
		System.out.println("ipscan executed");
		try {
			String[] cmdArgs = cmdPrmpt.split(" ");
			
			ArrayList<Socket> sockets = (ArrayList<Socket>) getSlaveSockets();
			PrintWriter output;
			BufferedReader input;
			String[] listOfIps;
			int count = 0;
			
			if(cmdArgs[1].equals("all")) {
				for(Socket socket : sockets) {
					output	= new PrintWriter(socket.getOutputStream(), true);
					output.println(cmdPrmpt);
					input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String parseIps = input.readLine();
					listOfIps = parseIps.split(", ");
					for(String ip : listOfIps) {
						if(count % 5 == 0)
							System.out.println();
						System.out.print(ip + ", ");
						ipAdds.add(ip);
						count++;
					}
					break;
				}
			} else if(checkIP(cmdArgs[1])) {
				String toCompare = "/" + cmdArgs[1];
				
				for(Socket socket : sockets) {
					if(socket.getInetAddress().toString().equals(toCompare)) {
						output	= new PrintWriter(socket.getOutputStream(), true);
						output.println(cmdPrmpt);
						input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						String parseIps = input.readLine();
						listOfIps = parseIps.split(", ");
						for(String ip : listOfIps) {
							if(count % 5 == 0)
								System.out.println();
							System.out.print(ip + ", ");
							ipAdds.add(ip);
							count++;
						}
						break;
					}
				}
			} else {
				for(Socket socket : sockets) {
					if(socket.getInetAddress().getHostName().toString().equals(cmdArgs)) {
						output	= new PrintWriter(socket.getOutputStream(), true);
						output.println(cmdPrmpt);
						input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						String parseIps = input.readLine();
						listOfIps = parseIps.split(", ");
						for(String ip : listOfIps) {
							if(count % 5 == 0)
								System.out.println();
							System.out.print(ip + ", ");
							ipAdds.add(ip);
							count++;
						}
						break;
					}
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // End of ipscan method
	
	/*
	 * Geo IP Scan method
	 * */
	
	public void geoIPScan(String cmdPrmpt) {
		System.out.println("ipscan executed");
		try {
			String[] cmdArgs = cmdPrmpt.split(" ");
			
			ArrayList<Socket> sockets = (ArrayList<Socket>) getSlaveSockets();
			PrintWriter output;
			BufferedReader input;
			String[] listOfIps;
			int count = 0;
			
			if(cmdArgs[1].equals("all")) {
				for(Socket socket : sockets) {
					output	= new PrintWriter(socket.getOutputStream(), true);
					output.println(cmdPrmpt);
					input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String parseIps = input.readLine();
					listOfIps = parseIps.split(", ");
					for(String ip : listOfIps) {
						if(count % 5 == 0)
							System.out.println();
						System.out.print(ip + ", ");
						ipAdds.add(ip);
						count++;
					}
					break;
				}
			} else if(checkIP(cmdArgs[1])) {
				String toCompare = "/" + cmdArgs[1];
				
				for(Socket socket : sockets) {
					if(socket.getInetAddress().toString().equals(toCompare)) {
						output	= new PrintWriter(socket.getOutputStream(), true);
						output.println(cmdPrmpt);
						input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						String parseIps = input.readLine();
						listOfIps = parseIps.split(", ");
						for(String ip : listOfIps) {
							if(count % 5 == 0)
								System.out.println();
							System.out.print(ip + ", ");
							ipAdds.add(ip);
							count++;
						}
						break;
					}
				}
			} else {
				for(Socket socket : sockets) {
					if(socket.getInetAddress().getHostName().toString().equals(cmdArgs)) {
						output	= new PrintWriter(socket.getOutputStream(), true);
						output.println(cmdPrmpt);
						input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						String parseIps = input.readLine();
						listOfIps = parseIps.split(", ");
						for(String ip : listOfIps) {
							if(count % 5 == 0)
								System.out.println();
							System.out.print(ip + ", ");
							ipAdds.add(ip);
							count++;
						}
						break;
					}
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // End of geoIPScan method
	
	private void tcpscan(String cmdPrmpt) {
		try {
			String[] cmdArgs = cmdPrmpt.split(" ");
			
			ArrayList<Socket> sockets = (ArrayList<Socket>) getSlaveSockets();
			PrintWriter output;
			BufferedReader input;
			String[] listOfIps;
			int count = 0;
			
			if(cmdArgs[1].equals("all")) {
				for(Socket socket : sockets) {
					output	= new PrintWriter(socket.getOutputStream(), true);
					output.println(cmdPrmpt);
					input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String ipAddress = input.readLine();
					System.out.println(ipAddress);
					String parsePorts = input.readLine();
					listOfIps = parsePorts.split(", ");
					for(String ip : listOfIps) {
						if(count % 5 == 0)
							System.out.println();
						System.out.print(ip + ", ");
						ipAdds.add(ip);
						count++;
					}
					break;
				}
			} else if(checkIP(cmdArgs[1])) {
				String toCompare = "/" + cmdArgs[1];
				
				for(Socket socket : sockets) {
					if(socket.getInetAddress().toString().equals(toCompare)) {
						output	= new PrintWriter(socket.getOutputStream(), true);
						output.println(cmdPrmpt);
						input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						String ipAddress = input.readLine();
						System.out.println(ipAddress);
						String parsePorts = input.readLine();
						listOfIps = parsePorts.split(", ");
						for(String ip : listOfIps) {
							if(count % 5 == 0)
								System.out.println();
							System.out.print(ip + ", ");
							ipAdds.add(ip);
							count++;
						}
						break;
					}
				}
			} else {
				for(Socket socket : sockets) {
					if(socket.getInetAddress().getHostName().toString().equals(cmdArgs)) {
						output	= new PrintWriter(socket.getOutputStream(), true);
						output.println(cmdPrmpt);
						input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						String ipAddress = input.readLine();
						System.out.println(ipAddress);
						String parsePorts = input.readLine();
						listOfIps = parsePorts.split(", ");
						for(String ip : listOfIps) {
							if(count % 5 == 0)
								System.out.println();
							System.out.print(ip + ", ");
							ipAdds.add(ip);
							count++;
						}
						break;
					}
				}
			}
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void list() {
		
		//Initialize iterator
		Iterator<LocalDate> dateIterator = dates.iterator();
		
		//Iterate through each slave and display the following information
		for(Socket slave : slaveSockets)
			
			//Print out the list of slaves along with their IP address and their socket information
			System.out.println(
				slave.getInetAddress().getHostName() + "\t"
				+ slave.getRemoteSocketAddress() + "\t"
				+ slave.getPort() + "\t"
				+ dateIterator.next()
			);
		
	} // End of list method
	
	// Checks and validates IP address
	private static boolean checkIP(String ipAddr) {

		String[] num = ipAddr.split("\\.");

		if (num.length != 4)
			return false;

		for (String str : num) {
			int i = Integer.parseInt(str);
			if ((i < 0) || (i > 255))
				return false;
		}

		return true;
	} // End of checkIP method

	// Multithreaded server to accept clients (SlaveBots)
	public void run() {
		ServerSocket serverSocket = getMasterSocket();
		
		while(true) {
			try {
				clients = serverSocket.accept();
				slaveSockets.add(clients);
				dates.add(LocalDate.now());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
} // End of MasterBot class