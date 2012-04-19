

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.sql.*;
import java.sql.Connection;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;


public class voterscraper {
	
	public static void main(String args[]) throws Exception{

	
	Connection conn = null;
	
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
	
	
	Statement s;
	
	s = conn.createStatement();
	int id=1;
	ResultSet rs = s.executeQuery("SELECT first_name, last_name from user where hometown like '%washington' or location like '%washington'");
	int x=0,y=0;
	String farray [] = new String [100000];
	String larray [] = new String [100000];
	while(rs.next()){
	farray [x] = rs.getString(1);
	larray [x++] = rs.getString(2);
	}
	
	
	for(y=0; y<x; y++){
	Document doc = Jsoup.connect("http://usefulwork.com/cgi-bin/wavoterdb.cgi?county=All+Counties&fulldisplay=1&statewide=1&block=&predir=&street=&type=&postdir=&last="+larray[y]+"&first="+farray[y]+"&sortby=prename&submitName=Search+by+Name").get();
	Elements rows2 = doc.getElementsByTag("body");
	Elements rows3 = doc.getElementsByTag("font");
	/*if(doc.getElementsByTag("tr").hasAttr("bgcolor")){
	Elements last = doc.getElementsByTag("tr");
	for(Element lt : last){
		System.out.println(lt);
	}
	}*/
	int k=0,i=0;
	String array_of_names[] = new String[50];
	String array_of_values[] = new String[50];
	for(Element row : rows3){
		if(k<15){
		array_of_names[k] = row.ownText();
		k++;}
		else{
			//System.out.println("row is");
			//System.out.println(i);
			//System.out.println(row.ownText());
			k++;
			if(i==2)
			{
					//System.out.println("HERE");
				String names[] = row.ownText().split(" ");
				///System.out.println("names are after split");
				//System.out.println(names[0]);
				if(names.length>1){
					int o = i;
					//System.out.println(names[1]);
			array_of_values[i] = names[0];
			i = i +1;
			array_of_values[i] = names[1];
			//System.out.println("Starts from here");
			//System.out.println(array_of_values[o]);
			//System.out.println(array_of_values[o++]);
			//System.out.println("endds here");
			}
				else{
					array_of_values[i] = names[0];
					i = i + 1;
					array_of_values[i] = " ";
				}
			i++;
			}
			else{
			array_of_values[i] = row.ownText();
			//System.out.println(array_of_values[i]);
			i++;}
		}
		if((k%15==0 || i%16==0) && i>1){
			int m;
			for(m=0;m<=15;m++)
			{
				System.out.println(array_of_values[m]);
			}
			i=0;
			//System.out.println("NEW");
			//write update query here;
		
		s.executeUpdate("INSERT into voter_db_wa VALUES('"+id+"','"+array_of_values[0]+"','"+array_of_values[1]+"','"+array_of_values[2]+"','"+array_of_values[3]+"','"+array_of_values[4]+"','"+array_of_values[5]+"','"+array_of_values[6]+"','"+array_of_values[7]+"','"+array_of_values[8]+"','"+array_of_values[9]+"','"+array_of_values[10]+"','"+array_of_values[11]+"','"+array_of_values[12]+"','"+array_of_values[13]+"','"+array_of_values[14]+"','"+array_of_values[15]+"')");
		id++;}
	}
	}
	}
		//System.out.println(row);
	//Elements rows = doc.getElementsByTag("tr");
	/*for(Element row : rows2){
		Elements values = row.getElementsByTag("tr");
		for(Element val : values){
			Elements values2 = val.getElementsByTag("td");
			for(Element t : values2){
				System.out.println(t);
				System.out.println("XXXXXXXXXXXXXXXXXXXXX");
				break;
			}
			}
		}*/
	//System.out.println(row);
	}
