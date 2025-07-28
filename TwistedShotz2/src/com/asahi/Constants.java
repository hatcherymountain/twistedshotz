package com.asahi;

public class Constants {

	public static final String DEFAULT_BG_COLOR="255,135,171";
	
	public static final String[] SHOT_TYPES= {"4 Pack","Party Pack"};
	
	public static String shotTypeAsString(int typeof)
	{
		String to = "";
		
		try {
			to = (String)SHOT_TYPES[typeof];
		} catch(Exception e) { 
			to = (String)SHOT_TYPES[0];
		}
		return to;
	}
	
}
