package com.gao.im.common.utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;




/**
 *
 */
public class OrgJosnUtils {
	
	/**
	 * json string convert to xml string
	 */
	public static String json2xml(String json){
		 
		return null; 
	}
	
	/**
	 * xml string convert to json string
	 */
	public static String xml2json(String xml){
		try {
			return XML.toJSONObject(xml).toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null; 
	}
	 
}


