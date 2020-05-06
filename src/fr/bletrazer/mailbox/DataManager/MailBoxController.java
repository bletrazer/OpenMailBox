package fr.bletrazer.mailbox.DataManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import fr.bletrazer.mailbox.sql.DataSQL;
import fr.bletrazer.mailbox.sql.ItemDataSQL;
import fr.bletrazer.mailbox.sql.LetterDataSQL;
import fr.bletrazer.mailbox.utils.ItemStackBuilder;
import fr.bletrazer.mailbox.utils.LangManager;
import fr.bletrazer.mailbox.utils.MessageLevel;
import fr.bletrazer.mailbox.utils.MessageUtils;

public class MailBoxController {

	private static DataHolder getHolderFromDataBase(UUID uuid) {
		DataHolder res = new DataHolder(uuid, new ArrayList<>());
		List<Data> dataList = DataSQL.getInstance().getDataList(uuid);

		for (Data data : dataList) {
			ItemData itemData = ItemDataSQL.getInstance().find(data.getId());
			LetterData letterData = LetterDataSQL.getInstance().find(data.getId());

			if (itemData != null) {
				res.addData(itemData);

			} else if (letterData != null) {
				res.addData(letterData);
			}
		}

		return res;
	}

	public static void load(UUID uuid) {
		DataManager.getCache().put(uuid, getHolderFromDataBase(uuid));
	}

	public static void unload(UUID uuid) {
		DataManager.getCache().remove(uuid);
	}

	public static void initialize() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			load(player.getUniqueId());
		}
	}

	public static DataHolder getDataHolder(UUID uuid) {
		DataHolder res = DataManager.getDataHolder(uuid);

		if (res == null) {
			res = getHolderFromDataBase(uuid);
		}

		return res;
	}

	public static Boolean sendLetter(LetterData letterData) {
		Boolean res = false;
		LetterData temp = LetterDataSQL.getInstance().create(letterData);

		if (temp != null) {
			DataHolder holder = DataManager.getDataHolder(temp.getOwnerUuid());
			if (holder != null) {
				holder.addData(temp);
			}

			res = true;
		}

		return res;

	}

	public static Boolean sendLetters(List<LetterData> letters) {
		Boolean res = false;

		if (letters != null && !letters.isEmpty()) {
			if (letters.size() > 1) {
				List<LetterData> sent = LetterDataSQL.getInstance().createAll(letters);

				if (sent != null) {
					for (LetterData letter : sent) {
						DataHolder holder = DataManager.getDataHolder(letter.getOwnerUuid());

						if (holder != null) {
							holder.addData(letter);
						}

						// notification
						Player recipient = Bukkit.getPlayer(letter.getOwnerUuid());

						if (recipient != null) {
							MessageUtils.sendMessage(recipient, MessageLevel.NOTIFICATION, LangManager.getValue("string_receive_letter_notification", letter.getAuthor()) );
						}
					}

					res = true;

				}

			} else {
				LetterData toSend = letters.get(0);
				
				if (sendLetter(toSend)) {
					// notification
					Player recipient = Bukkit.getPlayer(toSend.getOwnerUuid());

					if (recipient != null) {
						MessageUtils.sendMessage(recipient, MessageLevel.NOTIFICATION, LangManager.getValue("string_receive_letter_notification", toSend.getAuthor()) );

					}
					res = true;
					
				} else {
					MessageUtils.sendMessage( Bukkit.getPlayer(toSend.getAuthor()), MessageLevel.ERROR, LangManager.getValue("string_error_player") );
					
				}
			}
		}

		return res;
	}

	public static Boolean sendItem(ItemData itemData) {
		Boolean res = false;
		ItemData temp = ItemDataSQL.getInstance().create(itemData);

		if (temp != null) {
			DataHolder holder = DataManager.getDataHolder(temp.getOwnerUuid());

			if (holder != null) {
				holder.addData(temp);
			}

			res = true;

		}

		return res;

	}

	public static Boolean sendItems(List<ItemData> items) {
		Boolean res = false;

		if (items != null && !items.isEmpty()) {
			if (items.size() > 1) {
				List<ItemData> sent = ItemDataSQL.getInstance().createAll(items);

				if (sent != null) {
					for (ItemData item : sent) {
						DataHolder holder = DataManager.getDataHolder(item.getOwnerUuid());

						if (holder != null) {
							holder.addData(item);
						}

						// notification
						Player recipient = Bukkit.getPlayer(item.getOwnerUuid());

						if (recipient != null) {
							MessageUtils.sendMessage(recipient, MessageLevel.NOTIFICATION, LangManager.getValue("string_receive_item_notification", item.getAuthor()) );
						}
					}
					res = true;

				}

			} else {
				ItemData toSend = items.get(0);
				if (sendItem(toSend)) {
					// notification
					Player recipient = Bukkit.getPlayer(toSend.getOwnerUuid());

					if (recipient != null) {
						MessageUtils.sendMessage(recipient, MessageLevel.NOTIFICATION, LangManager.getValue("string_receive_item_notification", toSend.getAuthor()) );

					}
					res = true;
					
				} else {
					MessageUtils.sendMessage( Bukkit.getPlayer(toSend.getAuthor()), MessageLevel.ERROR, LangManager.getValue("string_error_player") );
					
				}
			}
		}

		return res;
	}

	public static void respondToLetter(Player player, Long id, ItemStack book) {// TODO

	}

	private static ItemStack getBookView(LetterData letterData) {
		StringBuilder letterHead = new StringBuilder();
		letterHead.append(String.format("§l%s:§r %s\n", LangManager.getValue("string_author"), letterData.getAuthor()));

		SimpleDateFormat sdf = new SimpleDateFormat(LangManager.getValue("string_date_format"));

		letterHead.append(String.format("§l%s:§r %s\n", LangManager.getValue("string_reception_date"), sdf.format(letterData.getCreationDate())));
		letterHead.append(String.format("§l%s:§r %s\n", LangManager.getValue("string_object"), letterData.getObject()));

		ItemStack book = new ItemStackBuilder(Material.WRITTEN_BOOK).build();
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		bookMeta.setAuthor(letterData.getAuthor());
		List<String> pages = new ArrayList<>();
		pages.add(letterHead.toString());
		pages.addAll(letterData.getContent());

		bookMeta.setPages(pages);
		bookMeta.setTitle(letterData.getObject());

		book.setItemMeta(bookMeta);

		return book;
	}

	public static void readLetter(Player player, LetterData letterData) {
		player.openBook(getBookView(letterData));

		if (letterData.getOwnerUuid().equals(player.getUniqueId())) {
			letterData.setIsRead(true);
			LetterDataSQL.getInstance().update(letterData);

		}
	}

	public static void deleteLetter(DataHolder holder, LetterData letterData) {
		holder.removeData(letterData.getId() );
		LetterDataSQL.getInstance().delete(letterData);

	}

	public static void deleteData(DataHolder holder, Long id) {
		Data data = holder.getData(id);

		if (data instanceof ItemData) {
			deleteItem(holder, (ItemData) data);

		} else if (data instanceof LetterData) {
			deleteLetter(holder, (LetterData) data);
		}
	}

	public static Boolean recoverItem(Player player, DataHolder holder, ItemData itemData) {
		Boolean success = false;
		
		if (player.getInventory().firstEmpty() >= 0) {
			player.getInventory().addItem(itemData.getItem() );
			deleteItem(holder, itemData);
			success = true;

		}

		return success;
	}

	public static void deleteItem(DataHolder holder, ItemData itemData) {
		holder.removeData(itemData.getId() );
		ItemDataSQL.getInstance().delete(itemData);
	}
}
