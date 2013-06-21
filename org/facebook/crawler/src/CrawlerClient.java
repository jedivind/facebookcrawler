package org.facebook.crawler;

public class CrawlerClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		FacebookDBParams dbParams = new FacebookDBParams("127.0.0.1" , "root" , "", "facebook"
									, "jdbc" , "mysql" , "3306");
		FacebookDatabase DB = new FacebookDatabase( dbParams );
		String accessToken = "CAACEdEose0cBAJQmX7VwQajxecQ7EcEcqnUuVUZAlw6g1NVM4ZBCPJ3ZCHMahArRtfmqy7kscx6OOhTG0PSGNbihikN8hjufo52t3n6JmJ1gAKeNOqxRNRfoPQqhjD8B0Og72CTftXa4YksSPGF482xub6gIuUZD";
		String [] fields = {"id","name","first_name","last_name","middle_name","link","username","birthday","hometown",
		    "location","bio","quotes","education","work","gender","relationship_status","languages","email","religion"}; //Fields to fetch.
		    ;
		String [] groupIDs = { "2452805792" };
		FacebookJsonParser crawlFB = new FacebookJsonParser( DB , accessToken, fields , groupIDs);

		crawlFB.crawlJson(CrawlOptions.GROUP);
	}

}
