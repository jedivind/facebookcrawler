import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


class dbinsert
{
	public static void main(String[] args) throws Exception
	{
		Connection conn = null;
		Statement s;
		
		try{
			String userName = "root";
			String password = "root";
			String url = "jdbc:mysql://127.0.0.1:3306/facebook";
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				conn = DriverManager.getConnection(url,userName,password);
				System.out.println("DB conn established");
		}
		
		catch(Exception e)
		{
			System.err.println("Cannot connect to DB");
		}
		s = conn.createStatement();
		ResultSet rs;
		int idcount=0;
		String id[] = new String[2000]; 
		rs = s.executeQuery("select id from user where location like '%washington' or hometown like '%washington'");
		while(rs.next())
			id[idcount++]=rs.getString(1);
		//String dob[]=  (select birthdate from voter_db_wa);
		//String sex[] = (select gender from user where location like %washington or hometown like %washington);
		//String 
		for(int len=0;len<id.length;len++)
		{   String hometown[] = new String[2];
		    String location[] = new String[2];
			String details[] = new String[10]; 
			rs = s.executeQuery("select name,gender,hometown,location,relationship_status, first_name,middle_name,last_name from user where id='"+id[len]+"'");
			while(rs.next()){
				details[0] = rs.getString(1);
				details[1] = rs.getString(2);
				details[2] = rs.getString(3); //ht
				details[3] = rs.getString(4); //loc
				hometown  = details[2].split(",");
				location  = details[3].split(",");
				details[4] = rs.getString(5);
				details[5] = rs.getString(6); //first
				details[6] = rs.getString(7); //mid
				details[7] = rs.getString(8); //last
			}
			String disease = null;
			rs = s.executeQuery("select disease from groups where gid in (select groupid from gruid where userid ='"+id[len]+"')");
			while(rs.next())
				disease = rs.getString(1);
			
			String dob = null;
			rs = s.executeQuery("select birthdate from voter_db_wa where first_name ='"+details[6]+"' and last_name = '"+details[8]+"' and city like '"+hometown[0]+"%' or city like '"+location[0]+"%'");
			while(rs.next())
				dob = rs.getString(1);
			
			s.executeUpdate("insert into anonymized_ds(dob,sex,hometown,location,disease,relationship_status) values('"+dob+"','"+details[1]+"','"+details[2]+"','"+details[3]+"','"+disease+"','"+details[4]+"')");
		}
		
		
			
				try
				{
					conn.close();
					System.out.println("DB conn closed");
				}
				catch(Exception e){}
			
		
	}
}

