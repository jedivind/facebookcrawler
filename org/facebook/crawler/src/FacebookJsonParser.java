/*  Facebook Graph API JSON parser.
*   
*   @description Navigate to Facebook graph pages, parse JSON objects and insert into database.
*   @author Vinay Bharadwaj (vbharadwaj6@cc.gatech.edu)
*/
package org.facebook.crawler;

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

import org.apache.commons.lang.StringUtils;


enum CrawlOptions{
	GROUP, USER;
}

class FacebookJsonParser {

	private String accessToken; // Your FB access token here.
    private String GraphURL = "https://graph.facebook.com/"; // Facebook Graph API URL

    private String GroupID [] = {};
    private String UserID [] = {};
    private String Fields [] = {};
    //{"id","name","first_name","last_name","middle_name","link","username","birthday","hometown",
    //"location","bio","quotes","education","work","gender","relationship_status","languages","email","religion"}; //Fields to fetch.

	private FacebookDatabase db;


	public FacebookJsonParser( FacebookDatabase db , String accessToken , String[] Fields , String[] GroupIDs){
		this.db = db;
		this.accessToken = accessToken;
		this.Fields = Fields;
		this.GroupID = GroupIDs;
	}

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;

        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }

        return sb.toString();
    }

    private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {

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

    private void crawlGroup() throws IOException, JSONException, SQLException { // Crawl entire group info, including it's members.


        String memberListAppend = "/members?access_token=" + this.accessToken;

    	for( int GroupIDIndex = 0 ; GroupIDIndex < GroupID.length ; GroupIDIndex++ ) {

            String GroupURL = this.GraphURL;
			GroupURL = GroupURL.concat( GroupID[GroupIDIndex] );
			GroupURL = GroupURL.concat( memberListAppend ); //Group members list & id
			System.out.println(GroupURL);

            JSONObject GroupJSONObject = readJsonFromUrl(GroupURL);
            JSONArray GroupMemberListJSONArray = GroupJSONObject.getJSONArray("data");

            for( int MemberCount = 0; MemberCount < GroupMemberListJSONArray.length(); MemberCount++) {
                this.crawlUser( GroupMemberListJSONArray.getJSONObject( MemberCount ) , GroupIDIndex );

            }
		}
    }

    private void crawlUsers() throws IOException, JSONException, SQLException {

    	String accessTokenAppend = "?access_token=" + accessToken;

    	if( UserID.length > 0 )
    	for( int UserIDIndex = 0; UserIDIndex < UserID.length; UserIDIndex++ ){

    		String UserURL = this.GraphURL;
    		UserURL = UserURL.concat( UserID[ UserIDIndex ]);
    		UserURL = UserURL.concat( accessTokenAppend );

    		JSONObject UserObject = new JSONObject();
    		UserObject.put("id" , UserID[UserIDIndex]);
    		this.crawlUser( UserObject , -1 );

    	}
    }

    private void crawlUser( JSONObject GroupMemberJSONObject , int IDIndex ) throws IOException, JSONException, SQLException { // Crawl individual user info. Pass list of userIDs to crawl.

    	String accessTokenAppend = "?access_token=" + accessToken;
    	String MemberURL;
    	String ID = "";
    	JSONObject GroupMemberDataJSONObject;

        MemberURL = GraphURL;
        MemberURL = MemberURL.concat(GroupMemberJSONObject.getString("id"));
        MemberURL = MemberURL.concat(accessTokenAppend);

        GroupMemberDataJSONObject = readJsonFromUrl(MemberURL); // Member info
        //System.out.println(GroupMemberDataJSONObject.get("name"));

        String MemberDataFieldsArray[] = new String[19];
        String currentField = "";
        JSONArray MemberDataFieldNames = GroupMemberDataJSONObject.names();

            for( int fieldIndex = 0; fieldIndex < MemberDataFieldNames.length(); fieldIndex++ ) {
                String check = MemberDataFieldNames.getString(fieldIndex);
                String [] fieldsWithCommonStructure = {"id", "name", "first_name", "middle_name", "last_name", "link", "username", "birthday", "gender",
                										"relationship_status", "bio", "quotes", "email", "religion"	};
                //TODO Need to clean & compact this code section. Crawler will expand support for every field in the User object, but
                // database schema must be updated first to include all the fields.
                if( StringUtils.startsWithAny( check, fieldsWithCommonStructure ) ) {
                	currentField = GroupMemberDataJSONObject.getString( MemberDataFieldNames.getString(fieldIndex) );
                }

                switch( MemberDataFieldNames.getString(fieldIndex)  ){

                	case "location":  String locarr[] = JSONObject.getNames(GroupMemberDataJSONObject.getJSONObject("location"));
                					  for(int z=0; z<locarr.length; z++)
                					   	 if(locarr[z].equals("name"))
                					 	  	  currentField = GroupMemberDataJSONObject.getString(locarr[z]);
                					  break;

                	case "hometown":  String homearr[] = JSONObject.getNames(GroupMemberDataJSONObject.getJSONObject("hometown"));
                					  for( int z = 0; z < homearr.length; z++ )
                						  if( homearr[z].equals("name") )
                							  currentField = GroupMemberDataJSONObject.getString( homearr[z] );
                					  break;

                	case "languages": String langs = "";
                    				  JSONArray langarry = GroupMemberDataJSONObject.getJSONArray("languages");
                    				  JSONObject langnames;

                    				  for(int y=0; y<langarry.length(); y++){
                    					  langnames = langarry.getJSONObject(y);
                    					  langs = langs.concat( " " + langnames.getString("name") + " " );
                    				  }
                    				  currentField = langs;
                    				  break;

                	case "education": String education = "";
                    				  JSONArray eduarry = GroupMemberDataJSONObject.getJSONArray("education");
                    				  JSONObject eduname;
                    				  String scharry[] = JSONObject.getNames( GroupMemberDataJSONObject.getJSONObject("education") );

                    				  for( int x = 0; x < eduarry.length(); x++ ){
                    					  if( scharry[x].equals("school") )
                    					  {
                    						  eduname = eduarry.getJSONObject(x);
                    						  education = education.concat( " " + eduname.getString("name") + " " );
                    					  }
                    				  }
                    				  currentField = education;
                    				  break;

                	case "work": 	  String work = "";
                    				  JSONArray workarry = GroupMemberDataJSONObject.getJSONArray("work");
                    				  JSONObject workname;
                    				  String wrkarry[] = JSONObject.getNames( GroupMemberDataJSONObject.getJSONObject("work") );

                    				  for( int w=0; w<workarry.length(); w++ ){
                    					  if( wrkarry[w].equals("employer") )
                    					  {
                    						  workname = workarry.getJSONObject(w);
                    						  work = work.concat( " " + workname.getString("name") + " " );
                    					  }

                    				  }
                    				  currentField = work;
                    				  break;
                }

                String tempcheck = MemberDataFieldNames.getString(fieldIndex);

                for( int x = 0; x < Fields.length; x++ ){
                    if( tempcheck.equals(Fields[x]) ){

                        currentField = FacebookDatabase.trimQuery(currentField);
                        if( currentField != null )
                        MemberDataFieldsArray[x] = currentField;

                        else
                        	MemberDataFieldsArray[x] = "null";
                    }
                }
                ID = GroupMemberDataJSONObject.getString("id");

            }

        this.db.executeInsertQuery(ID , MemberDataFieldsArray , GroupID , IDIndex );
    }

	public void crawlJson( CrawlOptions op ) throws IOException, JSONException, SQLException {

        this.db.initDatabaseConnection();
		this.db.createStatement();

		//If Group Crawl
		if( op.toString().equals( "GROUP" ) && GroupID.length > 0 )
			this.crawlGroup();

		//If Users Crawl
		else if( op.toString().equalsIgnoreCase( "USER" ) ){
			this.crawlUsers();
		}

		else {
			System.err.println("Parameters missing.");
		}
		this.db.destroyDatabaseConnection();
    }
}
