//Facebook scraper. Login to facebook, navigate to webpage, retrieve and parse the page,
//insert into database.
//Vinay Bharadwaj (vbharadwaj6@cc.gatech.edu)
//WORKING COPY DO NOT MODIFY!
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class scraping{
	  public static String trim( String stringToTrim )
		{
		    String answer = stringToTrim.replace(',', ' ');
		 	
		    System.out.println(answer);
		    return answer;
		}
	public static void main(String[] args) throws Exception {
		
		String arry [] = new String [7];
		String fields [] = {"id","music","books","athletes","movies","activities","interested_in"}; 
		
		
		Connection conn = null;
		
		
		DefaultHttpClient httpclient = new DefaultHttpClient();

   	 HttpGet httpget = new HttpGet("http://www.facebook.com/login.php");

   	 HttpResponse response = httpclient.execute(httpget);
   	 HttpEntity entity = response.getEntity();

   	 System.out.println("Login form get: " + response.getStatusLine());
   	 if (entity != null) {
   	     entity.consumeContent();
   	 }
   	 System.out.println("Initial set of cookies:");
   	 List<Cookie> cookies = httpclient.getCookieStore().getCookies();
   	 if (cookies.isEmpty()) {
   	     System.out.println("None");
   	 } else {
   	     for (int i = 0; i < cookies.size(); i++) {
   	         System.out.println("- " + cookies.get(i).toString());
   	     }
   	 }

   	 HttpPost httpost = new HttpPost("http://www.facebook.com/login.php");

   	 List <NameValuePair> nvps = new ArrayList <NameValuePair>();
   	 nvps.add(new BasicNameValuePair("email", "email@someplace.com"));
   	 nvps.add(new BasicNameValuePair("pass", "password"));
   	 
   	 httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

   	 response = httpclient.execute(httpost);
   	 entity = response.getEntity();
   	 System.out.println("Double check we've got right page " + EntityUtils.toString(entity));

   	 System.out.println("Login form get: " + response.getStatusLine());
   	 if (entity != null) {
   	     entity.consumeContent();
   	 }

   	 System.out.println("Post logon cookies:");
   	 cookies = httpclient.getCookieStore().getCookies();
   	 if (cookies.isEmpty()) {
   	     System.out.println("None");
   	 } else {
   	     for (int i = 0; i < cookies.size(); i++) {
   	         System.out.println("- " + cookies.get(i).toString());
   	     }
   	 }

		
		try{
			//open db conn
			String userName = "root";
			String password = "root";
			String url = "jdbc:mysql://127.0.0.1:3306/facebook";
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				conn = DriverManager.getConnection(url,userName,password);
				System.out.println("DB conn established");
		}
		
		catch (Exception e){
			System.err.println("Cannot connect to DB");
		}
		
		Statement s = conn.createStatement();
		
	
		/*HttpGet nw = new HttpGet("http://facebook.com/56013190");
		response = httpclient.execute(nw);
		entity = response.getEntity();
		System.out.println(EntityUtils.toString(entity));*/
		
	
		ResultSet rs = s.executeQuery("SELECT id from user where id in (SELECT userid from gruid where groupid in (SELECT gid from groups))");
		int x=0;
		String idarray [] = new String [100000];
		while(rs.next())
		idarray [x++] = rs.getString(1);
		
		for(int k=0; k<=x; k++){
		FileWriter ofile = new FileWriter(idarray[k]+".html");
		PrintWriter pw = new PrintWriter(ofile);
		entity.consumeContent();
		HttpGet nw = new HttpGet("https://facebook.com/"+idarray[k]+"?sk=info");
	//	httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
		response = httpclient.execute(nw);
		entity = response.getEntity();
		pw.println("Page check - list" +EntityUtils.toString(entity));
		
		//System.out.println(EntityUtils.toString(entity));
		
		
		File input = new File(idarray[k]+".html");
		Document doc = Jsoup.parse(input, "UTF-8", "");
		String dob="", location1="" , work="" ,study="" , workplace="";
		Elements link = doc.getElementsByClass("hidden_elem");
		for (Element links : link){
			String par = links.html().toString();
			par.replaceFirst("<!--","");
			String result="";
			if(par.length() > 15){
			result = par.substring(5,par.length()-4);
			Document doc1 = Jsoup.parse(result);
			Elements location = doc1.getElementsByClass("fbProfileBylineLabel");
			//System.out.println(email);;
			Elements athletes,quotes,musicall,booksall,moviesall,activitiesall;
			
			Elements otherdata = doc1.getElementsByClass("label");
			String temp;
			arry[0] = idarray[k];
			
			for (Element data : otherdata){
				if(data.ownText().equals("Favorite Athletes"))
				{
					athletes = data.siblingElements();
				for(Element newathlete : athletes){
					temp = newathlete.getElementsByClass("mediaPageName").text();
					temp = temp.replace('\'', ' ');
					temp = temp.replace('\\',' ');
					arry[3]=temp;
					System.out.println(temp);
				}
				}
				if(data.ownText().equals("Favorite Quotations"))
				{
					quotes = data.siblingElements();
				for(Element newquote : quotes){
					temp = newquote.getElementsByClass("data").text();
					temp = temp.replace('\'', ' ');
					temp = temp.replace('\\',' ');
					System.out.println(temp);
					s.executeUpdate("UPDATE user SET quotes='"+temp+"' WHERE id='"+idarray[k]+"'");
					
				}
				}
				if(data.ownText().equals("Music"))
				{
					musicall = data.siblingElements();
				for(Element newmusic : musicall){
					temp = newmusic.getElementsByClass("mediaPageName").text();
					temp = temp.replace('\'', ' ');
					temp = temp.replace('\\',' ');
					arry[1]=temp;
					System.out.println(temp);
				}
				}
				
				if(data.ownText().equals("Books"))
				{
					booksall = data.siblingElements();
				for(Element newbook : booksall){
					temp = newbook.getElementsByClass("mediaPageName").text();
					temp = temp.replace('\'', ' ');
					temp = temp.replace('\\',' ');
					arry[2]=temp;
					System.out.println(temp);
				}
				}
				if(data.ownText().equals("Movies"))
				{
					moviesall = data.siblingElements();
				for(Element newmovie : moviesall){
					temp = newmovie.getElementsByClass("mediaPageName").text();
					temp = temp.replace('\'', ' ');
					temp = temp.replace('\\',' ');
					arry[4]=temp;
					System.out.println(temp);
				}
				}
				
				if(data.ownText().equals("Activities"))
				{
					activitiesall = data.siblingElements();
				for(Element newactivity : activitiesall){
					temp =newactivity.getElementsByClass("fwb").text();
					temp = temp.replace('\'', ' ');
					temp = temp.replace('\\',' ');
					arry[5]=temp;
					System.out.println(temp);
				}
				}
				
				if(data.ownText().startsWith("About"))
				{
					activitiesall = data.siblingElements();
				for(Element newactivity : activitiesall){
					temp = newactivity.getElementsByClass("data").text();
					temp = temp.replace('\'', ' ');
					temp = temp.replace('\\',' ');
					System.out.println(temp);
					s.executeUpdate("UPDATE user SET bio='"+temp+"' WHERE id='"+idarray[k]+"'");
					
				}
				}
				
				if(data.ownText().equals("Interested In"))
				{
					activitiesall = data.siblingElements();
				for(Element newactivity : activitiesall){
					temp = newactivity.getElementsByClass("data").text();
					temp = temp.replace('\'', ' ');
					temp = temp.replace('\\',' ');
					arry[6] = temp;
					System.out.println(temp);
				}
				}
				
				if(data.ownText().equals("Relationship Status"))
				{
					activitiesall = data.siblingElements();
				for(Element newactivity : activitiesall){
					temp = newactivity.getElementsByClass("data").text();
					temp = temp.replace('\'', ' ');
					temp = temp.replace('\\',' ');
					System.out.println(temp);
					s.executeUpdate("UPDATE user SET relationship_status='"+temp+"' WHERE id='"+idarray[k]+"'");
				
				}
				s.executeUpdate("INSERT into user_misc_data (id,music,books,athletes,movies,activities,interested_in) VALUES ('"+arry[0]+"','"+arry[1]+"','"+arry[2]+"','"+arry[3]+"','"+arry[4]+"','"+arry[5]+"','"+arry[6]+"')");
				}
				
			}
			
			for(Element loc : location){
				//System.out.println(loc.text().substring(0, 4));
				if(loc.text().startsWith("Born")){
					dob = loc.select("a").text();
					dob = dob.replace('\'', ' ');
					s.executeUpdate("UPDATE user SET birthday='"+dob+"' WHERE id='"+idarray[k]+"'");
				}
				if(loc.text().startsWith("Worked") || loc.text().startsWith("Works")){
					work = loc.select("a").text();
					work = work.replace('\'', ' ');
					s.executeUpdate("UPDATE user SET work='"+work+"' WHERE id='"+idarray[k]+"'");
				}
				if(loc.text().startsWith("Studies") || loc.text().startsWith("Studied")){
					study = loc.select("a").text();
					study = study.replace('\'', ' ');
					s.executeUpdate("UPDATE user SET education='"+study+"' WHERE id='"+idarray[k]+"'");
				}
				if(loc.text().startsWith("Lives")){
					workplace = loc.select("a").text();
					workplace = workplace.replace('\'', ' ');
					s.executeUpdate("UPDATE user set location='"+workplace+"' WHERE id='"+idarray[k]+"'");
				}
				if(loc.text().startsWith("From")){
					location1 = loc.select("a").text();
					location1 = location1.replace('\'', ' ');
					s.executeUpdate("UPDATE user set hometown='"+location1+"' WHERE id='"+idarray[k]+"'");
				}
				System.out.println(dob);
				String dobirth[] = dob.split("' '");
				String date = dob.trim();
				date = trim(date);
				System.out.println(date);
				System.out.println(location1);
				System.out.println(workplace);
			}
			}
			//System.out.println(result);
		}
		}
		try
		{
			conn.close();
			System.out.println("DB conn closed");
		}
		catch(Exception e){}
		httpclient.getConnectionManager().shutdown();
		
	}
}

