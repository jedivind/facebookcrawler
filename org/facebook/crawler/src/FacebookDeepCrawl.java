/* Facebook Deep Crawler.
 *
 * @description Login to facebook, navigate to webpage, retrieve and parse the page,
 * 				insert into database.
 * @author Vinay Bharadwaj (vbharadwaj6@cc.gatech.edu)
 */

package org.facebook.crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
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

public class FacebookDeepCrawl {

	private FacebookDatabase db;
	private String loginURL = "http://www.facebook.com/login.php";
	private String [] Fields; //"id","music","books","athletes","movies","activities","interested_in"
	private DefaultHttpClient httpclient;
	private List<Cookie> cookies;
	private HttpGet httpget;
	private HttpPost httppost;
	private HttpResponse httpresponse;
	private HttpEntity httpentity;
	private List<NameValuePair> credentials;
	private ResultSet resultSet;

	public FacebookDeepCrawl( FacebookDatabase db, String UserEmail, String Password, String [] Fields ) {
		this.db = db;
		this.Fields = Fields;
		this.httpclient = new DefaultHttpClient();
		this.credentials = new ArrayList <NameValuePair>();
        this.credentials.add( new BasicNameValuePair( "email", UserEmail ) );
        this.credentials.add( new BasicNameValuePair( "pass", Password ) );
	}

	private void handleCookies() throws ClientProtocolException, IOException{

		this.httpget = new HttpGet( this.loginURL );
        this.httpresponse = this.httpclient.execute(httpget);
        this.httpentity = this.httpresponse.getEntity();

        System.out.println("Login form get: " + this.httpresponse.getStatusLine());

        if (this.httpentity != null) {
        	this.httpentity.consumeContent();
        }

        System.out.println("Initial set of cookies:");

        this.cookies = this.httpclient.getCookieStore().getCookies();
        if (this.cookies.isEmpty()) {
            System.out.println("None");
        } else {
            for (int i = 0; i < this.cookies.size(); i++) {
            System.out.println("- " + this.cookies.get(i).toString());
            }
        }

        this.httppost = new HttpPost( loginURL );
        this.httppost.setEntity(new UrlEncodedFormEntity( this.credentials, HTTP.UTF_8 ) );
        this.httpresponse = httpclient.execute( this.httppost );
        this.httpentity = this.httpresponse.getEntity();

        System.out.println("Double check we've got right page " + EntityUtils.toString(this.httpentity));
        System.out.println("Login form get: " + this.httpresponse.getStatusLine());

        if (this.httpentity != null) {
   	     	this.httpentity.consumeContent();
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
	}

	public void deepCrawl() throws ClientProtocolException, IOException, SQLException{

		this.handleCookies();
		this.db.initDatabaseConnection();
		this.db.createStatement();

		String arry [] = new String [7];

		this.resultSet = this.db.executeSelectQuery("SELECT id from user where id in (SELECT userid from gruid where groupid in (SELECT gid from groups))");
		int idCount=0;
		String idArray [] = null;

		while( this.resultSet.next() ){
			idArray[idCount++] = new String();
			idArray[idCount] = this.resultSet.getString(1);
		}

		for( int idIndex = 0; idIndex <= idCount; idIndex++ ){
			FileWriter ofile = new FileWriter( idArray[idIndex] + ".html" );
			PrintWriter pw = new PrintWriter(ofile);

			this.httpentity.consumeContent();
			this.httpget = new HttpGet( "https://facebook.com/" + idArray[idIndex] + "?sk=info" );
			this.httpresponse = httpclient.execute( this.httpget );
			this.httpentity = this.httpresponse.getEntity();
			String PageContents = EntityUtils.toString( httpentity );
			pw.println( "Page check - list" + PageContents );

			Document doc = Jsoup.parse( PageContents );
			String dob = "", location1 = "" , work = "" ,study= "" , workplace = "";

			Elements links = doc.getElementsByClass("hidden_elem");
			for (Element link : links){
				String par = link.html().toString();
				par.replaceFirst( "<!--", "" );
				String result = "";
				if(par.length() > 15){
					result = par.substring( 5, par.length()-4 );
					Document doc1 = Jsoup.parse(result);
					Elements location = doc1.getElementsByClass("fbProfileBylineLabel");
					Elements athletes,quotes,musicall,booksall,moviesall,activitiesall;

					Elements otherdata = doc1.getElementsByClass("label");
					String temp;
					arry[0] = idArray[idIndex];

					for (Element data : otherdata){
						switch( data.ownText() ){
							case "Favorite Athletes":  athletes = data.siblingElements();
										   for( Element newathlete : athletes ){
										   	temp = newathlete.getElementsByClass("mediaPageName").text();
										   	temp = temp.replace('\'', ' ');
										   	temp = temp.replace('\\',' ');
										   	arry[3] = temp;
										   	System.out.println(temp);
										   }
										   break;

							case "Favorite Quotations": quotes = data.siblingElements();
										    for( Element newquote : quotes ){
											temp = newquote.getElementsByClass("data").text();
											temp = temp.replace('\'', ' ');
											temp = temp.replace('\\',' ');
											System.out.println(temp);
											this.db.executeUpdateQuery("UPDATE user SET quotes='"+temp+"' WHERE id='" + idArray[idIndex] + "'");
										    }
										    break;			    

							case "Music":		    musicall = data.siblingElements();
										    for( Element newmusic : musicall ){
											temp = newmusic.getElementsByClass( "mediaPageName" ).text();
											temp = temp.replace('\'', ' ');
											temp = temp.replace('\\',' ');
											arry[1]=temp;
											System.out.println(temp);
										    }
										    break;

							case "Books":		    booksall = data.siblingElements();
										    for( Element newbook : booksall ){
											temp = newbook.getElementsByClass("mediaPageName").text();
											temp = temp.replace('\'', ' ');
											temp = temp.replace('\\',' ');
											arry[2]=temp;
											System.out.println(temp);
										    }
										    break;

							case "Movies":		    moviesall = data.siblingElements();
										    for( Element newmovie : moviesall ){
											temp = newmovie.getElementsByClass("mediaPageName").text();
											temp = temp.replace('\'', ' ');
											temp = temp.replace('\\',' ');
											arry[4]=temp;
											System.out.println(temp);
										    }
										    break;

							case "Activities":	    activitiesall = data.siblingElements();
										    for( Element newactivity : activitiesall ){
											temp =newactivity.getElementsByClass("fwb").text();
											temp = temp.replace('\'', ' ');
											temp = temp.replace('\\',' ');
											arry[5]=temp;
											System.out.println(temp);
										    }
										    break;

							case "About":		    activitiesall = data.siblingElements();
										    for(Element newactivity : activitiesall){
											temp = newactivity.getElementsByClass("data").text();
											temp = temp.replace('\'', ' ');
											temp = temp.replace('\\',' ');
											System.out.println(temp);
											this.db.executeUpdateQuery("UPDATE user SET bio='" + temp + "' WHERE id='" + idArray[idIndex] + "'");
										    }
										    break;

							case "Interested In":       activitiesall = data.siblingElements();
										    for( Element newactivity : activitiesall ){
											temp = newactivity.getElementsByClass("data").text();
											temp = temp.replace('\'', ' ');
											temp = temp.replace('\\',' ');
											arry[6] = temp;
											System.out.println(temp);
										    }
										    break;

							case "Relationship Status": activitiesall = data.siblingElements();
										    for(Element newactivity : activitiesall){
											temp = newactivity.getElementsByClass("data").text();
											temp = temp.replace('\'', ' ');
											temp = temp.replace('\\',' ');
											System.out.println(temp);
											this.db.executeUpdateQuery("UPDATE user SET relationship_status='"+ temp +"' WHERE id='" + idArray[idIndex] + "'");
										    }
										    break;
						}
						this.db.executeUpdateQuery("INSERT into user_misc_data (id,music,books,athletes,movies,activities,interested_in) VALUES " +
								"('"+arry[0]+"','"+arry[1]+"','"+arry[2]+"','"+arry[3]+"','"+arry[4]+"','"+arry[5]+"','"+arry[6]+"')");
			}

			for( Element loc : location ){
				if(loc.text().startsWith("Born")){
					dob = loc.select("a").text();
					dob = dob.replace('\'', ' ');
					this.db.executeUpdateQuery("UPDATE user SET birthday='" + dob + "' WHERE id='" + idArray[idIndex] + "'");
				}
				if(loc.text().startsWith("Worked") || loc.text().startsWith("Works")){
					work = loc.select("a").text();
					work = work.replace('\'', ' ');
					this.db.executeUpdateQuery("UPDATE user SET work='" + work + "' WHERE id='" + idArray[idIndex] + "'");
				}
				if(loc.text().startsWith("Studies") || loc.text().startsWith("Studied")){
					study = loc.select("a").text();
					study = study.replace('\'', ' ');
					this.db.executeUpdateQuery("UPDATE user SET education='" + study +"' WHERE id='" + idArray[idIndex] + "'");
				}
				if(loc.text().startsWith("Lives")){
					workplace = loc.select("a").text();
					workplace = workplace.replace('\'', ' ');
					this.db.executeUpdateQuery("UPDATE user set location='" + workplace + "' WHERE id='" + idArray[idIndex] + "'");
				}
				if(loc.text().startsWith("From")){
					location1 = loc.select("a").text();
					location1 = location1.replace('\'', ' ');
					this.db.executeUpdateQuery("UPDATE user set hometown='" + location1 + "' WHERE id='" + idArray[idIndex] + "'");
				}
				System.out.println(dob);
				String dobirth[] = dob.split("' '");
				String date = dob.trim();
				date = FacebookDatabase.trimQuery(date);
				System.out.println(date);
				System.out.println(location1);
				System.out.println(workplace);
			}
			}

		}
		}
		this.httpclient.getConnectionManager().shutdown();
	}
}

