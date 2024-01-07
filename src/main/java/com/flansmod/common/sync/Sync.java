package com.flansmod.common.sync;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

import com.flansmod.common.FlansMod;



public class Sync {
	public static TreeMap<String, String> hashes = new TreeMap<String, String>();

	public static String cachedHash = "";

	public static String getStringHash(String str) {
		String hash = "";
		try {
			MessageDigest digester = MessageDigest.getInstance("SHA-512");
			byte[] encodedhash = digester.digest(validateString(str).getBytes(StandardCharsets.US_ASCII));
			hash =  Hex.encodeHexString(encodedhash);
		} catch (Exception e) {
			FlansMod.log.warn("[Sync] Error has occured.");
			e.printStackTrace(); 
		}
		return hash;
	}

	public static String getUnifiedHash() {
		String str = "";
		for (Map.Entry<String, String> hash : hashes.entrySet()) {
			str += hash.getKey();
			FlansMod.log.debug(hash.getKey() + " " +hash.getValue());
		}

		cachedHash = getStringHash(str);
		return cachedHash;
	}

	public static void addHash(String str, String shortname) {
		hashes.put(getStringHash(str), shortname);
	}

	private static String validateString(String e) {
		String out = "";
		for (char c : e.toCharArray()) {
			if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ+=-()[]{}#%^&$£@?.,<>0123456789".contains("" + Character.toUpperCase(c))) {
				out += c;
			}
		}
		return out;

	}
}
