package eu.reborn_minecraft.zhorse.database;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.AbstractHorse;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class HorseInventoryRecord {
	
	private String uuid;
	private List<InventoryItemRecord> itemRecordList;
	
	public HorseInventoryRecord(String uuid, List<InventoryItemRecord> itemRecordList) {
		this.uuid = uuid;
		this.itemRecordList = itemRecordList;
	}
	
	public HorseInventoryRecord(List<InventoryItemRecord> itemRecordList) {
		this(itemRecordList.get(0).getUUID(), itemRecordList);
	}
	
	public HorseInventoryRecord(AbstractHorse horse) {
		uuid = horse.getUniqueId().toString();
		itemRecordList = new ArrayList<>();
		Inventory horseInventory = horse.getInventory();
		for (int position = 0; position < horseInventory.getSize(); position++) {
			ItemStack item = horseInventory.getItem(position);
			if (item != null) {
				itemRecordList.add(new InventoryItemRecord(uuid, position, item));
			}
		}
	}
	
	public String getUUID() {
		return uuid;
	}
	
	public List<InventoryItemRecord> getItemRecordList() {
		return itemRecordList;
	}

}
