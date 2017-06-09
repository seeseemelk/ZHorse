package com.gmail.xibalbazedd.zhorse.commands;

import org.bukkit.command.CommandSender;

import com.gmail.xibalbazedd.zhorse.ZHorse;
import com.gmail.xibalbazedd.zhorse.enums.LocaleEnum;
import com.gmail.xibalbazedd.zhorse.utils.MessageConfig;

public class CommandTp extends AbstractCommand {

	public CommandTp(ZHorse zh, CommandSender s, String[] a) {
		super(zh, s, a);
		if (isPlayer() && analyseArguments() && hasPermission() && isWorldEnabled() && applyArgument(true)) {
			if (!idMode) {
				if (!targetMode) {
					horseID = zh.getDM().getPlayerFavoriteHorseID(p.getUniqueId()).toString();
					if (isRegistered(p.getUniqueId(), horseID)) {
						horse = zh.getHM().getFavoriteHorse(p.getUniqueId());
						if (isHorseLoaded(true)) {
							execute();
						}
					}
				}
				else {
					sendCommandUsage();
				}
			}
			else {
				if (isRegistered(targetUUID, horseID)) {
					horse = zh.getHM().getHorse(targetUUID, Integer.parseInt(horseID));
					if (isHorseLoaded(true)) {
						execute();
					}
				}
			}
		}
	}
	
	private void execute() {
		if (isOwner(true) && isWorldCrossable(p.getWorld()) && isWorldCrossable(horse.getWorld()) && isNotOnHorse() && isHorseInRangeTp() && zh.getEM().canAffordCommand(p, command)) {
			p.teleport(horse);
			zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.TELEPORTED_TO_HORSE) {{ setHorseName(horseName); }});
			zh.getEM().payCommand(p, command);
		}
	}
}