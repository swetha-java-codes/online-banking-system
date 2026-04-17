package com.MyjavaProject;
import java.util.Scanner;
import java.sql.*;


public class OnlineBankingSystem {

	public static void main(String[] args) {
		Connection con = null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/PROJECTDB","root","S03@Queen");
			
			Queries q = new Queries(con);
			q.createTable();
			q.register();
			q.loginMenu();
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if(con!=null && !con.isClosed()) {
					con.close();
				}
			}
			catch(SQLException e) {
				e.printStackTrace();
			}
		}
		

	}

}

class Queries{
	
	Connection con;
	int rowsEffected;
	Scanner sc = new Scanner(System.in);
	
	public Queries(Connection con) {
		this.con=con;
	}
	
	//CREATING TABLE
	public void createTable() {
		try {
		    Statement st = con.createStatement();
			rowsEffected = st.executeUpdate("CREATE TABLE USERS"
					+ "(ID INT AUTO_INCREMENT PRIMARY KEY,"
					+"NAME VARCHAR(50),"
					+"EMAIL VARCHAR(50) UNIQUE,"
					+"PASSWORD VARCHAR(50),"
					+"BALANCE DOUBLE DEFAULT 0"
					+")"
					);
			System.out.println("Table Created Successfully");
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	//MAIN MENU
	public void mainMenu() throws SQLException {
        while (true) {
            System.out.println("\n1.Register\n2.Login\n3.Exit");
            int choice = sc.nextInt();
            sc.nextLine();

            if (choice == 1)
                register();
            else if (choice == 2)
                loginMenu();
            else
                break;
        }
    }
	
	//REGISTER
	public void register() {
		try {
		System.out.print("NAME: ");
		String name = sc.nextLine();
		
		System.out.print("EMAIL: ");
		String email = sc.nextLine();
		
		System.out.println("PASSWORD:");
		String password = sc.nextLine();
		
		String query = "INSERT INTO USERS(NAME,EMAIL,PASSWORD) VALUES(?,?,?)";
		
			PreparedStatement pst = con.prepareStatement(query);
			
			pst.setString(1, name);
			pst.setString(2,email);
			pst.setString(3,password);
			
			rowsEffected = pst.executeUpdate();
			System.out.println("Registered Successfully!");
			pst.close();
		} 
		catch (Exception e) {
			System.out.println("email already exists!");
		}
	}
	
	//LOGIN MENU
	public void loginMenu() throws SQLException{
		System.out.println("Email: ");
		String email = sc.nextLine();
		
		System.out.println("Password: ");
		String password = sc.nextLine();
		
		String query = "SELECT * FROM USERS WHERE EMAIL=? AND PASSWORD=?";
		PreparedStatement pst = con.prepareStatement(query);
		
		pst.setString(1,email);
		pst.setString(2,password);
		
		ResultSet rs = pst.executeQuery();
		
		if(rs.next()) {
			int userId = rs.getInt("id");
			System.out.println("Login successfully");
			
			while(true) {
				System.out.println("\n1.Deposit\n2.Withdraw\n3.Balance\n4.Transfer\n5.Update Profile\n6.Delete Account\n7.Logout");
				int ch = sc.nextInt();
				sc.nextLine();
				
				if(ch==1) deposit(userId);
				else if (ch==2) withdraw(userId);
				else if (ch==3) checkBalance(userId);
				else if (ch==4) transfer(userId);
				else if (ch==5) updateUser(userId);
				else if (ch==6) deleteUser(userId);
				else break;
			}
		} else {
			System.out.println("Invalid Credentials!");
		}
		rs.close();
		pst.close();
	}
	
	//DEPOSIT
	public void deposit(int userId) throws SQLException{
		System.out.println("Amount: ");
		double amount = sc.nextDouble();
		
		String query = "UPDATE USERS SET BALANCE = BALANCE + ? WHERE id=?";
		PreparedStatement pst = con.prepareStatement(query);
		
		pst.setDouble(1,amount);
		pst.setInt(2, userId);
		
		pst.executeUpdate();
		System.out.println("Deposited!");
		pst.close();
	}
	
	
	//WITHDRAW
	public void withdraw(int userId) throws SQLException{
		System.out.println("Amount: ");
		double amount = sc.nextDouble();
		sc.nextLine();
		
		String check = "SELECT BALANCE FROM USERS WHERE id=?";
		PreparedStatement pst1 = con.prepareStatement(check);
		pst1.setInt(1, userId);
		
		ResultSet rs = pst1.executeQuery();
		
		if(rs.next() && rs.getDouble("BALANCE") >=amount) {
			
			String query = "UPDATE USERS SET BALANCE = BALANCE - ? WHERE id=?";
			PreparedStatement pst = con.prepareStatement(query);
			 
			pst.setDouble(1, amount);
			pst.setDouble(2, userId);
			
			pst.executeUpdate();
			System.out.println("Withdrawn Successfully!");
			pst.close();
			
		}else {
			System.out.println("Insufficient balance!");
		}
	}
	
	//CHECK BALANCE
	public void checkBalance(int userId) throws SQLException{
		String query = "SELECT BALANCE FROM USERS WHERE id=?";
		PreparedStatement pst = con.prepareStatement(query);
		
		pst.setInt(1, userId);
		
		ResultSet rs = pst.executeQuery();
		
		if(rs.next()) {
			System.out.println("Balance: "+ rs.getDouble("balance"));
		}
		rs.close();
		pst.close();
	}
	
	//TRANSFER
	public void transfer(int senderId) throws SQLException{
		
		System.out.println("Receiver Email: ");
		String email = sc.nextLine();
		
		System.out.println("Amount: ");
		double amount = sc.nextDouble();
		sc.nextLine();
		
		String find = "SELECT ID FROM USERS WHERE email=?";
		PreparedStatement pst1 = con.prepareStatement(find);
		pst1.setString(1,email);
		
		 ResultSet rs = pst1.executeQuery();

	        if (rs.next()) {
	            int receiverId = rs.getInt("id");

	            con.setAutoCommit(false);

	            try {
	                PreparedStatement ps2 = con.prepareStatement(
	                        "UPDATE USERS SET BALANCE = BALANCE - ? WHERE id=?");
	                ps2.setDouble(1, amount);
	                ps2.setInt(2, senderId);
	                ps2.executeUpdate();

	                PreparedStatement ps3 = con.prepareStatement(
	                        "UPDATE users SET balance = balance + ? WHERE id=?");
	                ps3.setDouble(1, amount);
	                ps3.setInt(2, receiverId);
	                ps3.executeUpdate();

	                con.commit();
	                System.out.println("Transfer Successful!");
	                ps2.close();
	                ps3.close();
	                
	            } catch (Exception e) {
	                con.rollback();
	                System.out.println("Transfer Failed!");
	            }

	        } else {
	            System.out.println("User not found!");
	        }
	        rs.close();
	        pst1.close();
	}
	
	//UPDATE USER DETAILS
	public void updateUser(int userId) throws SQLException{
		System.out.println("\n1.Update Name\n2.Update Email\n3.Update Password");
		int choice = sc.nextInt();
		sc.nextLine();
		
		String query = "";
		
		if(choice ==1) {
			System.out.println("Enter New Name: ");
			String name =  sc.nextLine();
			
			query = "UPDATE USERS SET NAME=? WHERE ID=?";
	        PreparedStatement pst = con.prepareStatement(query);

	        pst.setString(1, name);
	        pst.setInt(2, userId);

	        pst.executeUpdate();
	        System.out.println("Name Updated!");

	        pst.close();
		} else if (choice == 2) {
	        System.out.print("Enter New Email: ");
	        String email = sc.nextLine();

	        query = "UPDATE USERS SET EMAIL=? WHERE ID=?";
	        PreparedStatement pst = con.prepareStatement(query);

	        pst.setString(1, email);
	        pst.setInt(2, userId);

	        pst.executeUpdate();
	        System.out.println("Email Updated!");

	        pst.close();

	    } else if (choice == 3) {
	        System.out.print("Enter New Password: ");
	        String password = sc.nextLine();

	        query = "UPDATE USERS SET PASSWORD=? WHERE ID=?";
	        PreparedStatement pst = con.prepareStatement(query);

	        pst.setString(1, password);
	        pst.setInt(2, userId);

	        pst.executeUpdate();
	        System.out.println("Password Updated!");

	        pst.close();
	    }
	}
	
	//DELETE USER ACCOUNT
	public void deleteUser(int userId) throws SQLException{
		System.out.print("Are you sure you want to delete account? (yes/no): ");
	    String confirm = sc.nextLine();

	    if (confirm.equalsIgnoreCase("yes")) {

	        String query = "DELETE FROM USERS WHERE ID=?";
	        PreparedStatement pst = con.prepareStatement(query);

	        pst.setInt(1, userId);

	        int rows = pst.executeUpdate();

	        if (rows > 0) {
	            System.out.println("Account Deleted Successfully!");
	        } else {
	            System.out.println("Error deleting account!");
	        }

	        pst.close();

	    } else {
	        System.out.println("Deletion Cancelled.");
	    }
	}
}



















