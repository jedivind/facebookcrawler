package org.facebook.crawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class FacebookDatabase {

	private Connection dbConn;
	private String dbURL;
	private Statement statement;
	private ResultSet results;
	private FacebookDBParams dbParams;

	public FacebookDatabase( FacebookDBParams dbParams ){
		this.dbParams = dbParams;

	}
	public static String trimQuery( String Query ){
		String trim = Query.replace(',', ' ');
		trim = trim.replace('\'', ' ');
		return trim;
	}

	public boolean createStatement(){

			try {
				this.statement = this.dbConn.createStatement();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return true;
	}

	public ResultSet executeSelectQuery( String Query ) throws SQLException{
		return this.statement.executeQuery( Query );
	}
	
	public int executeUpdateQuery( String Query ) throws SQLException{
		return this.statement.executeUpdate( Query );
	}
	
	public int executeInsertQuery( String UserID , String [] arry , String [] GroupID , int Index ){

		int count = 0;
		try {

            this.results = this.statement.executeQuery( "SELECT DISTINCT id from user where id=" + UserID );

            while( results.next() )
                count = count + 1;

            if(count == 0){
                /*count = this.statement.executeUpdate("INSERT into user VALUES ('"+arry[0]+"','"+arry[1]+"','"+arry[2]+"','"+arry[3]+"','"+arry[4]+
                		"','"+arry[5]+"','"+arry[6]+"','"+arry[7]+"','"+arry[8]+"','"+arry[9]+"','"+arry[10]+"','"+arry[11]+"','"+arry[12]+"','"+
                		arry[13]+"','"+arry[14]+"','"+arry[15]+"','"+arry[16]+"','"+arry[17]+"','"+arry[18]+"')");
                */

                count = this.statement.executeUpdate( this.createInsertQueryString( "INSERT" , "user" , arry ) );
            }

            if( Index != -1 )
            count = this.statement.executeUpdate( "INSERT into gruid (userid,groupid) VALUES ('" + UserID + "','" + GroupID[Index] + "')" );

        } catch(Exception e){
        	e.printStackTrace();
        }

		return count;
	}

	private String createInsertQueryString( String SQLCommand , String tableName , String [] Values ){
		String queryString = "";
		String queryVals = "";
			if( tableName != null )
			if( SQLCommand.equalsIgnoreCase("INSERT") ){
				queryString = queryString.concat( "INSERT into " + tableName + " VALUES ('" );
				System.out.println(Values.length + " Values");
				for( int i = 0; i < Values.length - 1; i++ )
					queryVals = queryVals.concat( Values[i] + "','" );
				queryString = queryString.concat( queryVals + Values[Values.length - 1] + "')" );
				System.out.println(queryString);
			}

		return queryString;

	}

	public void initDatabaseConnection() {

    	try {

				this.dbURL = this.dbParams.getDBLink();
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				this.dbConn = DriverManager.getConnection( this.dbURL, this.dbParams.getDBUser(), this.dbParams.getDBPass());
				System.out.println("DB conn established");

		} catch(Exception e) {
			System.err.println("Cannot connect to DB");
			System.err.println("Connection URL " + this.dbParams.getDBLink());
			System.err.println(e.getMessage());
		}

    }

    public void destroyDatabaseConnection(){
    	try	{

            dbConn.close();
			System.out.println("DB conn closed");

        } catch(Exception e){}
    }
}
