package org.facebook.crawler;

public class CrawlerClient {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		FacebookDBParams dbParams = new FacebookDBParams("127.0.0.1" , "root" , "AlphaCanisMajoris", "facebook"
									, "jdbc" , "mysql" , "3306");
		FacebookDatabase DB = new FacebookDatabase( dbParams );
		String accessToken = "CAACEdEose0cBAHgJEaf4dLuHAZBe00UZBSZCz2kmlQDlyFL17M637zeQwPlZAzm6R6Kljqjb3ctywejoMeI3CUoRzyYkxBMUukA38gANQXxkbjPsZAiRNTAdDtinCq6PeUGDxBwpBF7bpvMbmLZBTeZB9nvTZCSySzcZD";
		String [] fields = {"id","name","first_name","last_name","middle_name","link","username","birthday","hometown",
		    "location","bio","quotes","education","work","gender","relationship_status","languages","email","religion"}; //Fields to fetch.
		    ;
		String [] groupIDs = { "282714248411245" };
		FacebookJsonParser crawlFB = new FacebookJsonParser( DB , accessToken, fields , groupIDs);
		
		crawlFB.crawlJson(CrawlOptions.GROUP);
	}
	
}
