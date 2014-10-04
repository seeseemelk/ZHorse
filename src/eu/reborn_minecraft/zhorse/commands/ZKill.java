package eu.reborn_minecraft.zhorse.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

import eu.reborn_minecraft.zhorse.ZHorse;

public class ZKill {

	public ZKill(CommandSender s, String[] a, ZHorse zh) {
		if (s instanceof Player) {
			Player p = (Player) s;
			if (zh.getCM().isWorldEnabled(p.getWorld())) {
				String perm = "zh." + a[0];
				if(zh.getPerms().has(p, perm)) {
					if (p.isInsideVehicle() && p.getVehicle() instanceof Horse) {
						Horse horse = (Horse)p.getVehicle();
						if (zh.getUM().isRegistered(horse)) {
							if (zh.getUM().isClaimedBy(p.getUniqueId(), horse)) {
								if (zh.getEM().isReadyToPay(p, a[0])) {
									Damageable d = horse;
									d.damage(d.getHealth());
									zh.getEM().payCommand(p, a[0]);
								}
							}
							else {
								String ownerName = zh.getUM().getPlayerName(horse);
								p.sendMessage(String.format(zh.getLM().getCommandAnswer(zh.getLM().horseBelongsTo), ownerName));
							}
						}
						else {
							p.sendMessage(zh.getLM().getCommandAnswer(zh.getLM().horseNotClaimed));
						}
					}
					else {
						p.sendMessage(zh.getLM().getCommandAnswer(zh.getLM().notOnHorse));
					}
				}
				else {
					p.sendMessage(String.format(zh.getLM().getCommandAnswer(zh.getLM().missingPermission), perm));
				}
			}
			else {
				p.sendMessage(zh.getLM().getCommandAnswer(zh.getLM().worldDisabled));
			}
		}
		else {
			s.sendMessage(zh.getLM().getCommandAnswer(zh.getLM().playerCommand));
		}
	}

}