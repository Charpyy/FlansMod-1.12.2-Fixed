package com.flansmod.common.driveables;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import java.util.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.io.*;

public class VehicleOwnerManager {
	public static final String CSV_FILE_PATH = "vehicle_owners.csv";

	//public static void main(String[] args) {
	//	loadFromCSV();
	//	addPlayerToOwner("owner1", "player1");
	//	addPlayerToOwner("owner1", "player2");
	//	removePlayerFromOwner("owner1", "player1");
//
	//	saveToCSV();
	//}

	public static Map<String, List<String>> vehicleOwners = new HashMap<>();

	public static void addPlayerToOwner(String ownerName, String playerName) {
		List<String> players = vehicleOwners.computeIfAbsent(ownerName, k -> new ArrayList<>());
		players.add(playerName);
	}

	public static String removePlayerFromOwner(String ownerName, String playerName) {
		List<String> players = vehicleOwners.get(ownerName);
		if (players != null) {
			boolean removed = players.remove(playerName);
			if (removed) {
				if (players.isEmpty()) {
					vehicleOwners.remove(ownerName);
					return "\u00a78\u00bb \u00a7b" + playerName + " \u00a7fhas been removed";
				} else {
					return "\u00a78\u00bb \u00a7b" + playerName + " \u00a7fbeen removed";
				}
			} else {
				return "\u00a78\u00bb \u00a74" + playerName + " \u00a7cPlayer not found";
			}
		} else {
			return "\u00a78\u00bb \u00a74" + playerName + " \u00a7cPlayer not found";
		}
	}

	public static List<String> getPlayersForOwner(String ownerName) {
		return vehicleOwners.getOrDefault(ownerName, new ArrayList<>());
	}

	public static void loadFromCSV() {
		try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",");
				String owner = parts[0];
				List<String> players = new ArrayList<>(Arrays.asList(parts).subList(1, parts.length));
				vehicleOwners.put(owner, players);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveToCSV() {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(CSV_FILE_PATH))) {
			for (Map.Entry<String, List<String>> entry : vehicleOwners.entrySet()) {
				bw.write(entry.getKey() + "," + String.join(",", entry.getValue()));
				bw.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
