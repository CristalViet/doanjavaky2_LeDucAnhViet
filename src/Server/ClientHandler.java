package Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

import Client.Room;

public class ClientHandler extends Thread {
	Client client;
	public static List<ClientHandler> clientHandlers=new ArrayList<>();
	public static int RoomCurrentNumber=0;
	List<RoomServerSide>RoomList=new ArrayList<>();
	public ClientHandler(Socket socket) throws IOException {
		super();
		this.client = new Client();
		client.socket=socket;
		OutputStream os=socket.getOutputStream();
		client.sender=new BufferedWriter(new OutputStreamWriter(os,StandardCharsets.UTF_8));
		InputStream is=socket.getInputStream();
		client.receiver=new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8));
		client.port=socket.getPort();
		
	}
	
	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		
		
			
			try {
				while (true) {
					
					String CommandLine=client.receiver.readLine();
				
				
						System.out.println("Received request from user");
						System.out.println(CommandLine);
						switch (CommandLine) {
						case "new signup":{
							String UserName=client.receiver.readLine();
							String password=client.receiver.readLine();
							String avatarLink=client.receiver.readLine();
							String des=client.receiver.readLine();
							System.out.println(UserName+" "+password+" "+avatarLink);
							String Checker=CheckAccount(UserName, password);
							
							if(Checker.equals("khong ton tai")&&!CheckClientHandler(UserName))
							{
								
								client.userName=UserName;
								clientHandlers.add(this);
								ServerPanel.userlist2.add(UserName);
								
								//add vào bảng client dang hoat dong
								DefaultListModel<String>list=new DefaultListModel<>();
								for (String client : ServerPanel.userlist2) {
									list.addElement(client);
								}
								ServerPanel.list.setModel(list);;
								
						
								System.out.print("Count client: "+ServerPanel.CountClient());
								client.sender.write("signup success");
								
								client.sender.newLine();
								client.sender.flush();
								CreateAccount(UserName, password, avatarLink, des);
								System.out.println("Server: signup success");
							}
							break;
						}
					
						case "new login": {
							
							String UserName=client.receiver.readLine();
							String password=client.receiver.readLine();
							System.out.println("Ten la"+UserName);
							String Checker=CheckAccount(UserName, password);
							System.out.println("chay toi day");
							System.out.println(Checker);
							if(Checker.equals("thanh cong")&&!CheckClientHandler(UserName)) {
								client.userName=UserName;
								clientHandlers.add(this);
								
								
								
								
								
								//list hien thi ben server
								ServerPanel.userlist2.add(UserName);
//								UpdateListUserOnline();
								//add vào bảng client dang hoat dong
								DefaultListModel<String>list=new DefaultListModel<>();
								for (String client : ServerPanel.userlist2) {
									list.addElement(client);
								}
								ServerPanel.list.setModel(list);;
								
						
								System.out.print("Count client: "+ServerPanel.CountClient());
								
								client.sender.write("login success");
								client.sender.newLine();
								client.sender.flush();
								//gui thong tin user moi tham gia cho cac client khac
								for (ClientHandler ClientHandler :ClientHandler.clientHandlers) {
									if(client.userName.equals(ClientHandler.client.userName))
											continue;
									ClientHandler.client.sender.write("new user online");
									ClientHandler.client.sender.newLine();
									ClientHandler.client.sender.flush();
								
								}
								
								
							}
							else if(Checker.equals("sai mat khau")) {
								System.out.println("Dang nhap thap bai");
								client.sender.write("login failed");
								client.sender.newLine();
								client.sender.flush();
							}
							else if(CheckClientHandler(UserName))
							{
								client.sender.write("exist login");
								client.sender.newLine();
								client.sender.flush();
							}
							else System.out.println("Khong ton tai");
							break;
						}
						case "user exit":{
							RemoveClientHandler(this);
							UpdateClientHandlerListToServer();
							ServerPanel.UpdateJlistUserOnline();
							break;
						}
						case "request create room":{
							String RoomName=client.receiver.readLine();
							String RoomType=client.receiver.readLine();
							int NumberOfUsers=Integer.parseInt(client.receiver.readLine());
							List<String>listUser=new ArrayList<>();
							for(int i=0;i<NumberOfUsers;i++) {
								listUser.add(client.receiver.readLine());
							}
							
							//tang so luong room
//							RoomCurrentNumber++;
//							RoomServerSide room=new RoomServerSide(RoomCurrentNumber, RoomName, listUser);
//							RoomList.add(room);
							break;
						}
						case "request sendtext":{
							
							String msg=client.receiver.readLine();
							System.out.println("Thong diep cua client: "+msg);
							for (ClientHandler clientHandler : clientHandlers) {
								if(!clientHandler.equals(this))
								{	
									clientHandler.client.sender.write("textToUser");
									clientHandler.client.sender.newLine();
									clientHandler.client.sender.write(msg);
									clientHandler.client.sender.newLine();
									clientHandler.client.sender.flush();
								}
							}
						}
						
					}
				}
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
	}
//	public static void UpdateListUserOnline() {
//		Client.list_username.clear();
//		ServerPanel.userlist2.clear();
//		for (Client client :Client.list_client) {
//				Client.list_username.add(client.getUserName());
//				ServerPanel.userlist2.add(client.getUserName());
//				
//		}
//	}
	public static void UpdateClientHandlerListToServer() {
		Client.list_username.clear();
		ServerPanel.userlist2.clear();
		for (ClientHandler clientHandler : clientHandlers) {
			Client.list_username.add(clientHandler.client.getUserName());
			ServerPanel.userlist2.add(clientHandler.client.getUserName());
			
		}
		
	}
	public boolean CheckClientHandler(String username) {
		for (ClientHandler client : clientHandlers) {
			if(username.equals(client.getClient().userName)) {
				return true;
			}
		}
		return false;
	}
	public ClientHandler findClientHandler(String name) {
		for (ClientHandler client : clientHandlers) {
			if(name.equals(client.getClient().userName)) {
				return client;
			}
		}
		return null;
	}
	public void RemoveClientHandler(ClientHandler clienthandler) {
		for (ClientHandler client : ClientHandler.clientHandlers) {
			   if(client.equals(clienthandler)) {
				   ClientHandler.clientHandlers.remove(clienthandler);
				   System.out.println("Da xoa");
				   return ;
			   }
		}
		System.out.println("Khong xoa");
	}
	
	public String CheckAccount(String username, String password) {
		for (Account account : Account.listAccount) {
			if(username.equals(account.getUsername())) {
				if(password.equals(account.getPassword())) {
				
					return "thanh cong";
				}
				else return "sai mat khau";
			}
			
		}
		return "khong ton tai";
	}
	
	public static void CreateAccount(String username, String password, String avatarlink, String Description) {
		String driverName="com.microsoft.sqlserver.jdbc.SQLServerDriver";
 		String url="jdbc:sqlserver://localhost:1433;databaseName=chat;integratedSecurity=false;trustServerCertificate=true";
 		String user="viet2";
 		String pass="123";
 		try {
 			Class.forName(driverName);
 			Connection con=DriverManager.getConnection(url, user, pass);
 			
 			String sql1="INSERT INTO account (username, password, avatarLink, description)"
 					+ " VALUES (?, ?, ?, ?);";
 			PreparedStatement preparedStatement=con.prepareStatement(sql1);
 			preparedStatement.setNString(1, username);
 			preparedStatement.setNString(2, password);
 			preparedStatement.setNString(3, avatarlink);
 			preparedStatement.setNString(4, Description);
           
           int result=preparedStatement.executeUpdate();
           Account ac=new Account(username, password, avatarlink, Description);
	    	Account.listAccount.add(ac);
        
 		         //duyet tung dong trong result
           
 		    
 		        
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 			
 		 catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
	}
}