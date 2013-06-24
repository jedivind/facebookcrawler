//Facebook Graph API JSON parser.
//Navigate to facebook graph pages, parse JSON objects and insert into database.
//Vinay Bharadwaj (vbharadwaj6@cc.gatech.edu)

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import org.JSONException;
//import org.JSONObject;
class JsonReader {
		
  private static String readAll(Reader rd) throws IOException {
    StringBuilder sb = new StringBuilder();
    int cp;
    while ((cp = rd.read()) != -1) {
      sb.append((char) cp);
    }
    return sb.toString();
    
  }
  
  public static String trim( String stringToTrim )
	{
	    String answer = stringToTrim.replace('\'', ' ');
	 	
	    System.out.println(answer);
	    return answer;
	}

  public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
    InputStream is = new URL(url).openStream();
    try {
      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
      String jsonText = readAll(rd);
      JSONObject json = new JSONObject(jsonText);
      return json;
    } finally {
      is.close();
    }
  }

  public static void main(String[] args) throws IOException, JSONException, SQLException {
	  ResultSet rs;
	  String org = "https://graph.facebook.com/";
	  String memappend = "?access_token=CAACEdEose0cBAEhx6YgZA7spxZCgPknLP2YfBZBLI11G3oqyqp5JftCGDE1iUJ2JFAIsDKHVvAnzZCvAmIyYrmsBjTInPkTDRPIzKT5EPggBjmgCM16AakNl5qAfZA9v21MSiCjC3uvLQCRnlS6Ui2qTXnPFZBoOYZD";
	  String memlistappend = "/members?access_token=CAACEdEose0cBAFKHoKTaQXZCXZBlmGZCfBpWRhpW0rb1ZCdmFxxiotbbJlsNNzgcQ6zlLEHTChy7gxEgrKkn4ZBvUrBKj1EMFBuxGlyLXpk2EbAREKcIA8EE5ZB3uVEeq7bb2XbA9BndbsMHJAvbS8RQTAtDp2M50ZD";
	 // String GroupID [] = {"119463501490124","239158266136572" , "227089650680923","2214852731","267875486564043","233058263409131","183935261686010","140093246089847","140093246089847","181534605262558","5470017690","67279303232","128324667274339","41084647681"};
	  //String GroupID []= {"212186883948","110657075652944","114427718571227","128331470602550","146358868726854"};
	  //"2254756782"//,"2452805792","18733117205",
	  //"18771832390","22687005489","26075687027","58663571961","60742831043",
	  //"175010588812","194247758860",
	  String GroupID [] = {"282714248411245"};
	  String fields [] = {"id","name","first_name","last_name","middle_name","link","username","birthday","hometown","location","bio","quotes","education","work","gender","relationship_status","languages","email","religion"};
	  String remove ="\'";
	  Connection conn = null;

		int count=0;
		
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
		
		for(int i=0 ; i<GroupID.length ; i++)
		{
			String new_org = org;
			new_org = new_org.concat(GroupID[i]);
			new_org = new_org.concat(memlistappend); //Group members list & id
			System.out.println(new_org);  
			String mem_org;
			
	  
    JSONObject jsongrp = readJsonFromUrl(new_org);
    //JSONObject jsonusr = readJsonFromUrl()
    //System.out.println(json.toString());
    //System.out.println(jsongrp.get("id"));
   // System.out.println(json.get("birthday"));
   // System.out.println(json.get("location"));
    
    //String array[] = JSONObject.getNames(json.getJSONObject("location"));
    JSONArray array = jsongrp.getJSONArray("data");
    JSONObject jsongrpmemb;
    JSONObject jsongrpmembinfo;
    for(int k=0; k<array.length(); k++){
    jsongrpmemb = (array.getJSONObject(k));
    mem_org = org;
    mem_org = mem_org.concat(jsongrpmemb.getString("id"));
    mem_org = mem_org.concat(memappend);
    jsongrpmembinfo = readJsonFromUrl(mem_org); // Member info
    //System.out.println(jsongrpmembinfo.get("name"));
    String arry[] = new String[20];
    String temp = "";
    JSONArray temparry = jsongrpmembinfo.names();
    for(int l=0; l<temparry.length(); l++){
   // System.out.println(jsongrpmembinfo.get(temparry.getString(l)));
    	String check = temparry.getString(l);
    	if(check.equals("id") || check.equals("name") || check.equals("first_name") || check.equals("middle_name") || check.equals("last_name") || check.equals("link") ||check.equals("username") ||check.equals("birthday") ||check.equals("gender") ||check.equals("relationship_status") ||check.equals("bio") ||check.equals("quotes") ||check.equals("email") ||check.equals("religion"))
    	temp = jsongrpmembinfo.getString(temparry.getString(l));
    	
    if(temparry.getString(l).equals("location"))
    {
    	
    	String locarr[] = JSONObject.getNames(jsongrpmembinfo.getJSONObject("location"));
    	
    	for(int z=0; z<locarr.length; z++)
    	if(locarr[z].equals("name"))
    		temp = jsongrpmembinfo.getString(locarr[z]);
        	
    }
    
    if(temparry.getString(l).equals("hometown"))
    {
    	String homearr[] = JSONObject.getNames(jsongrpmembinfo.getJSONObject("hometown"));
    	
    	for(int z=0; z<homearr.length; z++)
    		if(homearr[z].equals("name"))
    			temp = jsongrpmembinfo.getString(homearr[z]);
    }
    	
    if(temparry.getString(l).equals("languages"))
    {	String langs = "";
    	JSONArray langarry = jsongrpmembinfo.getJSONArray("languages");
    	JSONObject langnames;
    	for(int y=0; y<langarry.length(); y++){
    		langnames = langarry.getJSONObject(y);
    		langs = langs.concat(" "+langnames.getString("name")+" ");
    	}
    	temp = langs;
    }
    
    if(temparry.getString(l).equals("education"))
    {	String education = "";
    	JSONArray eduarry = jsongrpmembinfo.getJSONArray("education");
    	JSONObject eduname;
    	String scharry[] = JSONObject.getNames(jsongrpmembinfo.getJSONObject("education"));;
    	
    	for(int x=0; x<eduarry.length(); x++){
    		if(scharry[x].equals("school"))
    		{
    			eduname = eduarry.getJSONObject(x);
    			education = education.concat(" "+eduname.getString("name")+" ");
    		}
    		
    	}
    	temp = education;
    }
    
    if(temparry.getString(l).equals("work"))
    {	String work = "";
    	JSONArray workarry = jsongrpmembinfo.getJSONArray("work");
    	JSONObject workname;
    	String wrkarry[] = JSONObject.getNames(jsongrpmembinfo.getJSONObject("work"));;
    	
    	for(int w=0; w<workarry.length(); w++){
    		if(wrkarry[w].equals("employer"))
    		{
    			workname = workarry.getJSONObject(w);
    			work = work.concat(" "+workname.getString("name")+" ");
    		}
    		
    	}
    	temp = work;
    }
    
    String tempcheck = temparry.getString(l);
    //System.out.println(temp);
    for(int x=0; x<fields.length; x++)
    if(tempcheck.equals(fields[x])){
    	//System.out.println(fields[x]);
        //System.out.println(temp);
    	 temp = trim(temp);
    	arry[x] = temp;
        //count = s.executeUpdate("INSERT into facebook.user ("+fields[x]+") VALUES ("+temp+")");
    	
    }
    
    temp = jsongrpmembinfo.getString("id");
    //if(temparry.getString(l).equals("id"));
      //count = s.executeUpdate("INSERT into gruid (userid,groupid) VALUES ("+temp+","+GroupID[i]+")");	
    
    
    }
    try{
    rs = s.executeQuery("SELECT DISTINCT id from user where id="+temp);
    count=0;
    while(rs.next())
    count = count + 1;
    //System.out.println(count);
    if(count == 0)
    count = s.executeUpdate("INSERT into user VALUES ('"+arry[0]+"','"+arry[1]+"','"+arry[2]+"','"+arry[3]+"','"+arry[4]+"','"+arry[5]+"','"+arry[6]+"','"+arry[7]+"','"+arry[8]+"','"+arry[9]+"','"+arry[10]+"','"+arry[11]+"','"+arry[12]+"','"+arry[13]+"','"+arry[14]+"','"+arry[15]+"','"+arry[16]+"','"+arry[17]+"','"+arry[18]+"')");
    count = s.executeUpdate("INSERT into gruid (userid,groupid) VALUES ('"+temp+"','"+GroupID[i]+"')");	
    //}
    }
    catch(Exception e){
    }
    }
    //for(int k=0;k<array.length;k++)
    //	System.out.println(array[k]);
    //System.out.println(a);
    //System.out.println(namearray);
    //System.out.println(jsongrp.get("data"));
}
		try
		{
			conn.close();
			System.out.println("DB conn closed");
		}
		catch(Exception e){}
  }
}
