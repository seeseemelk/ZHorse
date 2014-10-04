package eu.reborn_minecraft.zhorse.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

import eu.reborn_minecraft.zhorse.ZHorse;

public class Command {
	protected ZHorse zh;
	protected String[] a;
	protected CommandSender s;
	protected String command;
	protected boolean displayConsole;
	protected boolean adminMode;
	protected boolean idMode;
	protected boolean targetMode;
	protected boolean idAllow;
	protected boolean targetAllow;
	protected String userID;
	protected String targetName;
	protected UUID targetUUID;
	protected Player p;
	protected Horse horse;
	protected String horseName;
	protected boolean samePlayer;
	
	public Command(ZHorse zh, String[] a, CommandSender s) {
		this.zh = zh;
		this.a = a;
		this.s = s;
		if (a.length != 0) {
			this.command = a[0];
		}
		else {
			this.command = zh.getLM().help;
		}
		this.displayConsole = !(zh.getCM().isConsoleMuted());
		this.idAllow = false;
		this.targetAllow = false;
	}
	
	protected boolean analyseArguments() {
		int adminModeCount = 0;
		int idModeCount = 0;
		int targetModeCount = 0;
		for (int i=0; i<a.length; i++) {
			boolean checkSuccess = true;
			if (a[i].equals("-a")) {
				checkSuccess = (adminModeCount == 0);
				if (checkSuccess) {
					adminMode = true;
					adminModeCount += 1;
				}
			}
			else if (a[i].equals("-i")) {
				checkSuccess = (idModeCount == 0 && i != a.length-1 && !(a[i+1].equals("-a") || a[i+1].equals("-i") || a[i+1].equals("-t")));
				if (checkSuccess) {
					idMode = true;
					userID = a[i+1];
					idModeCount += 1;
				}
			}
			else if (a[i].equals("-t")) {
				checkSuccess = (targetModeCount == 0 && i != a.length-1 && !(a[i+1].equals("-a") || a[i+1].equals("-i") || a[i+1].equals("-t")));
				if (checkSuccess) {
					targetMode = true;
					targetName = a[i+1];
					targetModeCount += 1;
				}
			}
			if (!checkSuccess) {
				if (displayConsole) {
					sendCommandUsage();
				}
				return false;
			}
		}
		if (targetName == null) {
			targetName = p.getName();
			targetUUID = p.getUniqueId();
			samePlayer = true;
		}
		else {
			targetName = zh.getUM().getPlayerName(targetName);
			targetUUID = getPlayerUUID(targetName);
			samePlayer = p.getUniqueId().equals(targetUUID);
		}
		if (targetUUID == null) {
			s.sendMessage(String.format(zh.getLM().getCommandAnswer(zh.getLM().unknownPlayer), targetName));
			return false;
		}
		cleanArgs();
		return true;
	}

	protected void cleanArgs() {
		List<String> b = new ArrayList<String>();
		for (int i=1; i<a.length; i++) {
			if (!(a[i].equals("-a") || a[i].equals("-i") || a[i].equals("-t"))) {
				b.add(a[i]);
			}
			else if (a[i].equals("-i") || a[i].equals("-t")) {
				i++;
			}
		}
		String[] c = new String[b.size()];
		for (int i=0; i<b.size(); i++) {
			c[i] = b.get(i);
		}
		a = c;
	}
	
	protected boolean craftHorseName() {
		if (a.length != 0) {
			horseName = "";
			if (zh.getCM().isHorseNameAllowed() || adminMode) {
				for (int i=0; i<a.length; i++) {
					horseName += a[i];
					if (i+1 < a.length) {
						horseName += " ";
					}
				}
				int maxLength = zh.getCM().getMaximumHorseNameLength();
				int minLength = zh.getCM().getMinimumHorseNameLength();
				int l = horseName.length();
				if ((l >= minLength && (l <= maxLength || maxLength == -1)) || adminMode) {
					return true;
				}
				else if (displayConsole) {
					if (l < minLength) {
						s.sendMessage(String.format(zh.getLM().getCommandAnswer(zh.getLM().horseNameTooShort), minLength));
					}
					else if (l > maxLength) {
						s.sendMessage(String.format(zh.getLM().getCommandAnswer(zh.getLM().horseNameTooLong), maxLength));
					}
				}
			}
			else if (displayConsole) {
				s.sendMessage(zh.getLM().getCommandAnswer(zh.getLM().horseNameForbidden));
			}
		}
		else {
			if (!zh.getCM().isHorseNameRequired() || adminMode) {
				if (zh.getUM().isRegistered(horse)) {
					horseName = zh.getUM().getHorseName(horse);
					return true;
				}
				else {
					horseName = zh.getCM().getRandomName();
					if (horseName != null) {
						return true;
					}
					String errorMessage = "\"horsenames\" list in config is empty ! Please add one name in it or increment \"minimum-horsename-length\".";
					zh.getLogger().severe(errorMessage);
				}
			}
			else if (displayConsole) {
				s.sendMessage(zh.getLM().getCommandAnswer(zh.getLM().horseNameMandatory));
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	protected UUID getPlayerUUID(String playerName) {
		if (zh.getUM().isRegistered(playerName)) {
			return zh.getUM().getPlayerUUID(playerName);
		}
		else {
			if (zh.getServer().getOfflinePlayer(playerName).hasPlayedBefore()) {
				return zh.getServer().getOfflinePlayer(playerName).getUniqueId();
			}
			return null;
		}
	}
	
	protected boolean hasReachedMaxClaims(UUID playerUUID) {
		if (adminMode) {
			return false;
		}
		int claimsAmount;
		int maxClaims;
		claimsAmount = zh.getUM().getClaimsAmount(playerUUID);
		maxClaims = zh.getCM().getMaximumClaims(playerUUID);
		if (claimsAmount < maxClaims || maxClaims == -1) {
			return false;
		}
		else if (displayConsole) {
			if (p.getUniqueId().equals(playerUUID)) {
				s.sendMessage(zh.getLM().getCommandAnswer(zh.getLM().maximumClaimsReached));
			}
			else {
				s.sendMessage(String.format(zh.getLM().getCommandAnswer(zh.getLM().maximumClaimsReachedOther), targetName));
			}
		}
		return true;
	}
	
	protected boolean hasPermission() {
    	return (hasPermission(p, command, false, false));
	}
	
	protected boolean hasPermission(UUID playerUUID, String command, boolean ignoreModes, boolean hideConsole) {
		if (isPlayerOnline(playerUUID)) {
    		Player target = zh.getServer().getPlayer(playerUUID);
    		return hasPermission(target, command, ignoreModes, hideConsole);
    	}
    	else if (displayConsole) {
    		s.sendMessage(String.format(zh.getLM().getCommandAnswer(zh.getLM().playerOffline), zh.getUM().getPlayerName(playerUUID)));
    	}
    	return false;
	}
	
	protected boolean hasPermission(Player p, String command, boolean ignoreModes, boolean hideConsole) {
		String perm = "zh." + command;
    	if ((adminMode || (idMode && !idAllow) || (targetMode && !targetAllow)) && !ignoreModes) {
    		perm += zh.getLM().admin;
    	}
    	if (zh.getPerms().has(p, perm)) {
    		return true;
    	}
    	else if (displayConsole && !hideConsole) {
    		s.sendMessage(String.format(zh.getLM().getCommandAnswer(zh.getLM().missingPermission), perm));
    	}
    	return false;
	}
	
	protected boolean isClaimable() {
		if (horse != null) {
			if (adminMode) {
				return true;
			}
			if (horse.isTamed()) {
				if (!zh.getUM().isRegistered(horse)) {
					return true;
				}
				else if (displayConsole) {
					if (zh.getUM().isClaimedBy(p.getUniqueId(), horse)) {
						s.sendMessage(zh.getLM().getCommandAnswer(zh.getLM().horseAlreadyClaimed));
					}
					else {
						if (!targetMode) {
							targetName = zh.getUM().getPlayerName(horse);
						}
						s.sendMessage(String.format(zh.getLM().getCommandAnswer(zh.getLM().horseBelongsTo), targetName));
					}
				}
			}
			else if (displayConsole) {
				s.sendMessage(zh.getLM().getCommandAnswer(zh.getLM().horseNotTamed));
			}
		}
		else if (displayConsole) {
			if (idMode && !targetMode) {
				s.sendMessage(String.format(zh.getLM().getCommandAnswer(zh.getLM().unknownHorseId), userID));
			}
			else if (idMode && targetMode) {
				s.sendMessage(String.format(zh.getLM().getCommandAnswer(zh.getLM().unknownHorseIdOther), targetName, userID));
			}
		}
		return false;
	}
	
	protected boolean isHorseEmpty() {
		if (adminMode) {
			horse.eject();
			return true;
		}
		Entity passenger = horse.getPassenger();
		if (passenger == null) {
			return true;
		}
		else if (displayConsole) {
			String passengerName = ((Player)passenger).getName();
			s.sendMessage(String.format(zh.getLM().getCommandAnswer(zh.getLM().horseMountedBy), horseName, passengerName));
		}
		return false;
	}
	
	protected boolean isNotOnHorse() {
		if (adminMode) {
			return true;
		}
		if (p.getVehicle() != horse) {
			return true;
		}
		else if (displayConsole) {
			s.sendMessage(String.format(zh.getLM().getCommandAnswer(zh.getLM().horseMounted), horseName));
		}
		return false;
	}
	
	protected boolean isOnHorse() {
		if (p.isInsideVehicle() && p.getVehicle() instanceof Horse) {
			return true;
		}
		else if (displayConsole) {
			s.sendMessage(zh.getLM().getCommandAnswer(zh.getLM().notOnHorse));
		}
		return false;
	}
	
	protected boolean isOnSameWorld() {
		if (adminMode) {
			return true;
		}
		if (p.getWorld().equals(horse.getLocation().getWorld())) {
			return true;
		}
		else if (displayConsole) {
			s.sendMessage(String.format(zh.getLM().getCommandAnswer(zh.getLM().differentWorld), horseName));
		}
		return false;
	}
	
	protected boolean isOwner() {
		return isOwner(false);
	}
	
	protected boolean isOwner(boolean hideErrorMessage) {
		if (adminMode) {
			return true;
		}
		if (zh.getUM().isClaimedBy(p.getUniqueId(), horse)) {
			return true;
		}
		else if (displayConsole) {
			String ownerName = zh.getUM().getPlayerName(horse);
			s.sendMessage(String.format(zh.getLM().getCommandAnswer(zh.getLM().horseBelongsTo), ownerName));
		}
		return false;
	}
	
	protected boolean isPlayer() {
		if (s instanceof Player) {
			p = (Player)s;
			return true;
		}
		else if (displayConsole) {
			s.sendMessage(zh.getLM().getCommandAnswer(zh.getLM().playerCommand));
		}
		return false;
	}
	
	protected boolean isPlayerOnline(UUID playerUUID) {
		for (Player p : zh.getServer().getOnlinePlayers()) {
			if (p.getUniqueId().equals(playerUUID)) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean isRegistered() {
		if (zh.getUM().isRegistered(horse)) {
			horseName = zh.getUM().getHorseName(horse);
			return true;
		}
		else if (displayConsole) {
			s.sendMessage(zh.getLM().getCommandAnswer(zh.getLM().horseNotClaimed));
		}
		return false;
	}
	
	protected boolean isWorldEnabled() {
		if (adminMode) {
			return true;
		}
		if (zh.getCM().isWorldEnabled(p.getWorld())) {
			return true;
		}
		else if (displayConsole) {
			s.sendMessage(zh.getLM().getCommandAnswer(zh.getLM().worldDisabled));
		}
		return false;
	}
	
	protected void sendCommandUsage() {
		sendCommandUsage(true);
	}
	
	protected void sendCommandUsage(boolean sendErrorMessage) {
		if (sendErrorMessage) {
			s.sendMessage(zh.getLM().getCommandAnswer(zh.getLM().commandIncorrect));
		}
		s.sendMessage(" " + zh.getLM().getHeaderMessage(zh.getLM().commandUsageHeader));
		s.sendMessage(" " + String.format(zh.getLM().getCommandAnswer(zh.getLM().commandUsage, true), zh.getLM().getCommandUsage(command)));
	}
	
	protected void sendUnknownHorseMessage(String playerName) {
		sendUnknownHorseMessage(playerName, false);
	}
	
	protected void sendUnknownHorseMessage(String playerName, boolean playerHorse) {
		if (displayConsole) {
			if (samePlayer || playerHorse) {
				s.sendMessage(String.format(zh.getLM().getCommandAnswer(zh.getLM().unknownHorseId), userID));
			}
			else {
				s.sendMessage(String.format(zh.getLM().getCommandAnswer(zh.getLM().unknownHorseIdOther), playerName, userID));
			}
		}
	}
}