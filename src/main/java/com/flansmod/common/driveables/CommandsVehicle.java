package com.flansmod.common.driveables;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import java.util.HashMap;
import java.util.List;

public class CommandsVehicle extends CommandBase {
	//public static HashMap<String, List<String>> vehicleOwners = new HashMap<>();

	@Override
	public String getName() {
		return "vehicle";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "(/vehicle) + (add | remove | del | list) + (playerName)";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 0) {
			sender.sendMessage(new TextComponentString(getUsage(sender)));
			return;
		}
		String action = args[0].toLowerCase();
		if (action.equals("list")) {
			List<String> list = VehicleOwnerManager.getPlayersForOwner(sender.getName());
			sender.sendMessage(new TextComponentString("\u00a78\u00bb \u00a77List of all players who got access to the vehicle"));
			if (!list.isEmpty()) {
				StringBuilder message = new StringBuilder("\u00a77- \u00a7f");
				for (String passenger : list) {
					message.append(passenger).append(", ");
				}
				message.setLength(message.length() - 2);
				sender.sendMessage(new TextComponentString(message.toString()));
			} else {
				sender.sendMessage(new TextComponentString("\u00a7f No Player."));
			}
		} else if (args.length >= 2) {
			String playerName = args[1];
			if (action.equals("add")) {
				if (!playerName.equalsIgnoreCase(sender.getName())) {
					VehicleOwnerManager.addPlayerToOwner(sender.getName(), playerName);
					sender.sendMessage(new TextComponentString("\u00a78\u00bb \u00a7b" + playerName + " \u00a77added successfully to the vehicle owners list."));
				} else {
					sender.sendMessage(new TextComponentString("\u00a78\u00bb \u00a7cYou can't add yourself to the vehicle owner list."));
				}
			} else if (action.equals("remove") || action.equals("del")) {
				String result = VehicleOwnerManager.removePlayerFromOwner(sender.getName(), playerName);
				sender.sendMessage(new TextComponentString(result));
			}
		}
	}

	public List addTabCompletionOptions(ICommandSender sender, String[] prm) {
		if (prm.length <= 1) {
			return getListOfStringsMatchingLastWord(prm, "add",
					"remove",
					"del",
					"list"
			);
		}

		return null;
	}

	//private void addPlayerToOwner(String ownerName, String playerName) {
	//	List<String> players = vehicleOwners.getOrDefault(ownerName, new ArrayList<>());
	//	players.add(playerName);
	//	vehicleOwners.put(ownerName, players);
	//}
//
//
	//private void removePlayerFromOwner(String ownerName, String playerName) {
	//	List<String> players = vehicleOwners.get(ownerName);
	//	if (players != null) {
	//		players.remove(playerName);
	//		if (players.isEmpty()) {
	//			vehicleOwners.remove(ownerName);
	//		} else {
	//			vehicleOwners.put(ownerName, players);
	//		}
	//	}
	//}

	//public static List<String> getPlayersForOwner(String ownerName) {
	//	return vehicleOwners.getOrDefault(ownerName, new ArrayList<>());
	//}
}
