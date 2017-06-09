package com.gmail.xibalbazedd.zhorse.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.AbstractHorse;

import com.gmail.xibalbazedd.zhorse.ZHorse;
import com.gmail.xibalbazedd.zhorse.enums.CommandSettingsEnum;
import com.gmail.xibalbazedd.zhorse.enums.KeyWordEnum;
import com.gmail.xibalbazedd.zhorse.enums.LocaleEnum;
import com.gmail.xibalbazedd.zhorse.utils.MessageConfig;

import net.md_5.bungee.api.ChatColor;

public class CommandSettings extends AbstractCommand {
	
	private static final String EXACT_DISPLAY_MODE = "exact";
	private static final String ROUNDED_DISPLAY_MODE = "rounded";
	private static final String[] STATS_DISPLAY_MODE_ARRAY = {EXACT_DISPLAY_MODE, ROUNDED_DISPLAY_MODE};
	
	private String fullCommand;
	private String subCommand;

	public CommandSettings(ZHorse zh, CommandSender s, String[] a) {
		super(zh, s, a);
		if (isPlayer() && analyseArguments() && hasPermission() && isWorldEnabled()) {			
			if (!idMode) {
				if (isOnHorse(true)) { // select the horse w/ or w/o target
					horse = (AbstractHorse) p.getVehicle();
					if (isOwner(targetUUID, false, true, true)) {
						idMode = true;
						Integer horseIDInt = zh.getDM().getHorseID(horse.getUniqueId());
						horseID = horseIDInt != null ? horseIDInt.toString() : null;
					}
				}
				execute();
			}
			else if (isRegistered(targetUUID, horseID)) {
				execute();
			}
		}
	}

	private void execute() {
		if ((!targetMode || samePlayer || hasPermissionAdmin(false)) && zh.getEM().canAffordCommand(p, command)) {
			if (!argument.isEmpty()) {
				subCommand = argument.contains(" ") ? argument.split(" ")[0] : argument;
				if (subCommand.equalsIgnoreCase((CommandSettingsEnum.FAVORITE.getName()))) {
					fullCommand = command + KeyWordEnum.DOT.getValue() + CommandSettingsEnum.LANGUAGE.getName();
					setFavorite();
				}
				else if (subCommand.equalsIgnoreCase(CommandSettingsEnum.LANGUAGE.getName())) {
					fullCommand = command + KeyWordEnum.DOT.getValue() + CommandSettingsEnum.FAVORITE.getName().toLowerCase();
					setLanguage();
				}
				else if (subCommand.equalsIgnoreCase((CommandSettingsEnum.STATS.getName()))) {
					fullCommand = command + KeyWordEnum.DOT.getValue() + CommandSettingsEnum.STATS.getName();
					setStatsDisplay();
				}
				else if (subCommand.equalsIgnoreCase((CommandSettingsEnum.SWAP.getName()))) {
					fullCommand = command + KeyWordEnum.DOT.getValue() + CommandSettingsEnum.SWAP.getName();
					swapIDs();
				}
				else {
					zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.UNKNOWN_SETTINGS_COMMAND) {{ setValue(subCommand); }});
					sendCommandSettingsDescriptionList();
				}
			}
			else {
				sendCommandSettingsDescriptionList();
			}
		}
	}
	
	private void setFavorite() {
		if (hasPermission(s, fullCommand , true, false)) {
			if (argument.split(" ").length >= 2) {
				idMode = true;
				horseID = argument.substring(argument.indexOf(" ") + 1);
			}
			if (idMode) {
				if (isRegistered(targetUUID, horseID)) {
					if (!zh.getDM().getPlayerFavoriteHorseID(targetUUID).toString().equals(horseID)) {
						zh.getDM().updatePlayerFavorite(targetUUID, Integer.parseInt(horseID));
						if (samePlayer) {
							zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.FAVORITE_EDITED) {{ setHorseName(horseName); }});
						}
						else {
							zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.FAVORITE_EDITED_OTHER) {{ setHorseName(horseName); setPlayerName(targetName); }});
						}
						zh.getEM().payCommand(p, command);
					}
					else {
						if (samePlayer) {
							zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.FAVORITE_ALREADY_SET) {{ setHorseName(horseName); }});
						}
						else {
							zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.FAVORITE_ALREADY_SET_OTHER) {{ setHorseName(horseName); setPlayerName(targetName); }});
						}
					}
				}
			}
			else {
				zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.MISSING_HORSE_ID));
				sendCommandUsage(subCommand, true, true);
			}
		}
	}

	private void setLanguage() {
		if (hasPermission(s, fullCommand , true, false)) {
			if (argument.split(" ").length >= 2) {
				String language = argument.substring(argument.indexOf(" ") + 1).toUpperCase();
				if (zh.getCM().isLanguageAvailable(language)) {
					if (!zh.getDM().getPlayerLanguage(targetUUID).equals(language)) {
						zh.getDM().updatePlayerLanguage(targetUUID, language);
						if (samePlayer) {
							zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.LANGUAGE_EDITED) {{ setLanguage(language); }});
						}
						else {
							zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.LANGUAGE_EDITED_OTHER) {{ setLanguage(language); setPlayerName(targetName); }});
						}
						zh.getEM().payCommand(p, command);
					}
					else {
						if (samePlayer) {
							zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.LANGUAGE_ALREADY_USED) {{ setLanguage(language); }});
						}
						else {
							zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.LANGUAGE_ALREADY_USED_OTHER) {{ setLanguage(language); setPlayerName(targetName); }});
						}
					}
				}
				else {
					displayAvailableLanguages(LocaleEnum.UNKNOWN_LANGUAGE, language);
				}
			}
			else {
				displayAvailableLanguages(LocaleEnum.MISSING_LANGUAGE);
				sendCommandUsage(subCommand, true, true);
			}
		}
	}
	
	private void setStatsDisplay() {
		if (hasPermission(s, fullCommand , true, false)) {
			if (argument.split(" ").length == 2) {
				String displayMode = argument.substring(argument.indexOf(" ") + 1);
				boolean validDisplayMode = false;
				Boolean shouldDisplayExactStats = null;
				if (displayMode.equalsIgnoreCase(EXACT_DISPLAY_MODE)) {
					validDisplayMode = true;
					shouldDisplayExactStats = true;
				}
				else if (displayMode.equalsIgnoreCase(ROUNDED_DISPLAY_MODE)) {
					validDisplayMode = true;
					shouldDisplayExactStats = false;
				}
				if (validDisplayMode) {
					if (shouldDisplayExactStats ^ zh.getDM().isPlayerDisplayingExactStats(targetUUID)) { // XOR
						zh.getDM().updatePlayerDisplayExactStats(targetUUID, shouldDisplayExactStats);
						if (samePlayer) {
							zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.STATS_DISPLAY_MODE_EDITED) {{ setValue(displayMode); }});
						}
						else {
							zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.STATS_DISPLAY_MODE_EDITED_OTHER) {{ setValue(displayMode); setPlayerName(targetName); }});
						}
						zh.getEM().payCommand(p, command);
					}
					else {
						if (samePlayer) {
							zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.STATS_DISPLAY_MODE_ALREADY_USED) {{ setValue(displayMode); }});
						}
						else {
							zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.STATS_DISPLAY_MODE_ALREADY_USED_OTHER) {{ setValue(displayMode); setPlayerName(targetName); }});
						}
					}
				}
				else {
					displayAvailableStatsDisplayMode(LocaleEnum.UNKNOWN_STATS_DISPLAY_MODE);
				}
				
			}
			else {
				displayAvailableStatsDisplayMode(LocaleEnum.MISSING_STATS_DISPLAY_MODE);
				sendCommandUsage(subCommand, true, true);
			}
		}
	}
	
	private void swapIDs() {
		if (hasPermission(s, fullCommand , true, false)) {
			if (argument.split(" ").length == 3) {
				String horseID1 = argument.split(" ")[1];
				String horseID2 = argument.split(" ")[2];
				if (isRegistered(targetUUID, horseID1) && isRegistered(targetUUID, horseID2)) {
					int favoriteHorseID = zh.getDM().getPlayerFavoriteHorseID(targetUUID);
					if (favoriteHorseID == Integer.parseInt(horseID1)) {
						zh.getDM().updatePlayerFavorite(targetUUID, Integer.parseInt(horseID2));
					}
					else if (favoriteHorseID == Integer.parseInt(horseID2)) {
						zh.getDM().updatePlayerFavorite(targetUUID, Integer.parseInt(horseID1));
					}
					UUID horseUUID1 = zh.getDM().getHorseUUID(targetUUID, Integer.parseInt(horseID1));
					UUID horseUUID2 = zh.getDM().getHorseUUID(targetUUID, Integer.parseInt(horseID2));
					zh.getDM().updateHorseID(horseUUID1, Integer.parseInt(horseID2));
					zh.getDM().updateHorseID(horseUUID2, Integer.parseInt(horseID1));
					zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.IDS_SWAPPED));
					zh.getEM().payCommand(p, command);
				}
			}
			else {
				zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.MISSING_HORSE_IDS));
				sendCommandUsage(subCommand, true, true);
			}
		}
	}
	
	private void displayAvailableLanguages(LocaleEnum index) {
		displayAvailableLanguages(index, null);
	}
	
	private void displayAvailableLanguages(LocaleEnum index, String language) {
		List<String> availableLanguages = zh.getCM().getAvailableLanguages();
		String availableLanguagesMessage = "";
		for (int i = 0; i < availableLanguages.size(); ++i) {
			final String availableLanguage = availableLanguages.get(i);
			availableLanguagesMessage += zh.getMM().getMessage(s, new MessageConfig(LocaleEnum.AVAILABLE_OPTION_FORMAT) {{ setValue(availableLanguage); }}, true);
			if (i < availableLanguages.size() - 1) {
				availableLanguagesMessage += ", ";
			}
		}
		availableLanguagesMessage += ChatColor.RESET;
		final String message = availableLanguagesMessage;
		if (language != null) {
			zh.getMM().sendMessage(s, new MessageConfig(index) {{ setLanguage(language); setValue(message); }});
		}
		else {
			zh.getMM().sendMessage(s, new MessageConfig(index) {{ setValue(message); }});
		}
	}
	
	private void displayAvailableStatsDisplayMode(LocaleEnum index) {
		String availableStatsDisplayModesMessage = "";
		for (int i = 0; i < STATS_DISPLAY_MODE_ARRAY.length; ++i) {
			final String availableDisplayMode = STATS_DISPLAY_MODE_ARRAY[i];
			availableStatsDisplayModesMessage += zh.getMM().getMessage(s, new MessageConfig(LocaleEnum.AVAILABLE_OPTION_FORMAT) {{ setValue(availableDisplayMode); }}, true);
			if (i < STATS_DISPLAY_MODE_ARRAY.length - 1) {
				availableStatsDisplayModesMessage += ", ";
			}
		}
		availableStatsDisplayModesMessage += ChatColor.RESET;
		final String message = availableStatsDisplayModesMessage;
		zh.getMM().sendMessage(s, new MessageConfig(index) {{ setValue(message); }});
	}

}