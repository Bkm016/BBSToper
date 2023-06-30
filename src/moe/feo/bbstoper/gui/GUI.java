package moe.feo.bbstoper.gui;

import java.util.ArrayList;
import java.util.List;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import moe.feo.bbstoper.BBSToper;
import moe.feo.bbstoper.Crawler;
import moe.feo.bbstoper.Message;
import moe.feo.bbstoper.Option;
import moe.feo.bbstoper.Poster;
import moe.feo.bbstoper.Util;
import moe.feo.bbstoper.sql.SQLer;

public class GUI {

	private static SQLer sql;
	private Inventory inv;
	
	public static String getTitle() {// 获取插件的gui标题必须用此方法，因为用户可能会修改gui标题
		String title = Message.GUI_TITLE.getString().replaceAll("%PREFIX%", Message.PREFIX.getString());
		return title;
	}

	public GUI(Player player) {
		createGui(player);
		Bukkit.getScheduler().runTask(BBSToper.getInstance(), () -> player.openInventory(inv));
	}
	
	class BBSToperGUIHolder implements InventoryHolder {// 定义一个Holder用于识别此插件的GUI
		@Override
		public Inventory getInventory() {
			return getGui();
		}
	}

	@SuppressWarnings("deprecation")
	public void createGui(Player player) {
		InventoryHolder holder = new BBSToperGUIHolder();
		setGui(Bukkit.createInventory(holder, InventoryType.CHEST, getTitle()));
//		for (int i = 0; i < inv.getSize(); i++) {// 设置边框
//			if (i > 9 && i < 17) {
//                continue;
//            }
//			inv.setItem(i, getRandomPane());
//		}

		ItemStack skull = new ItemStack(Material.PAPER);
//		try {
//			skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
//		} catch (NoSuchFieldError e) {// 某些高版本服务端不兼容旧版写法
//			skull = new ItemStack(Material.getMaterial("PLAYER_HEAD"), 1);
//		}
		SkullMeta skullmeta = (SkullMeta) skull.getItemMeta();// 玩家头颅
		if (Option.GUI_DISPLAYHEADSKIN.getBoolean()) {// 如果开启了头颅显示，才会设置头颅的所有者
			try {
				skullmeta.setOwningPlayer(player);
			} catch (NoSuchMethodError e) {// 这里为了照顾低版本
				skullmeta.setOwner(player.getName());
			}
		}
		skullmeta.setDisplayName(Message.GUI_SKULL.getString().replaceAll("%PLAYER%", player.getName()));
		List<String> skulllores = new ArrayList<String>();
		Poster poster = sql.getPoster(player.getUniqueId().toString());
		if (poster == null) {
			skulllores.add(Message.GUI_NOTBOUND.getString());
			skulllores.add(Message.GUI_CLICKBOUND.getString());
		} else {
			skulllores.add(Message.GUI_BBSID.getString().replaceAll("%BBSID%", poster.getBbsname()));
			skulllores.add(Message.GUI_POSTTIMES.getString().replaceAll("%TIMES%", "" + poster.getTopStates().size()));
			skulllores.add(Message.GUI_CLICKREBOUND.getString());
		}
		skullmeta.setLore(skulllores);
		skullmeta.setCustomModelData(95274);
		skull.setItemMeta(skullmeta);
		inv.setItem(12, skull);
//		ItemStack sunflower;
//		try {
//			sunflower = new ItemStack(Material.DOUBLE_PLANT);
//		} catch (NoSuchFieldError e) {// 某些高版本服务端不兼容旧版写法
//			sunflower = new ItemStack(Material.getMaterial("SUNFLOWER"));
//		}

		// 领取奖励
		ItemStack rewardItem = new ItemStack(Material.PAPER);
		ItemMeta rewardItemMeta = rewardItem.getItemMeta();
		rewardItemMeta.setDisplayName(Message.GUI_REWARDS.getString());
		List<String> sunflowerlores = new ArrayList<String>(Message.GUI_REWARDSINFO.getStringList());// 自定义奖励信息
		if (sunflowerlores.isEmpty()) {// 如果没有自定义奖励信息
			sunflowerlores.addAll(Option.REWARD_COMMANDS.getStringList());// 直接显示命令
			if (Option.REWARD_INCENTIVEREWARD_ENABLE.getBoolean()) {
				sunflowerlores.add(Message.GUI_INCENTIVEREWARDS.getString());// 激励奖励
				sunflowerlores.addAll(Option.REWARD_INCENTIVEREWARD_COMMANDS.getStringList());// 激励奖励命令
			}
			if (Option.REWARD_OFFDAYREWARD_ENABLE.getBoolean()) {
				sunflowerlores.add(Message.GUI_OFFDAYREWARDS.getString());// 休息日奖励
				sunflowerlores.addAll(Option.REWARD_OFFDAYREWARD_COMMANDS.getStringList()); // 休息日奖励命令
			}
		}
		sunflowerlores.add(Message.GUI_CLICKGET.getString());
		sunflowerlores = PlaceholderAPI.setPlaceholders(player, sunflowerlores);// 替换占位符
		rewardItemMeta.setLore(sunflowerlores);
		rewardItemMeta.setCustomModelData(95271);
		rewardItem.setItemMeta(rewardItemMeta);
		inv.setItem(13, rewardItem);

		// 排行榜
		ItemStack top = new ItemStack(Material.PAPER);
		ItemMeta topMeta = top.getItemMeta();
		topMeta.setDisplayName(Message.GUI_TOPS.getString());
		List<String> starlores = new ArrayList<String>();
		List<Poster> listposter = sql.getTopPosters();
		for (int i = 0; i < listposter.size(); i++) {
			if (i >= Option.GUI_TOPPLAYERS.getInt()) {
                break;
            }
			starlores.add(Message.POSTERPLAYER.getString() + ":" + listposter.get(i).getName() + " "
					+ Message.POSTERID.getString() + ":" + listposter.get(i).getBbsname() + " "
					+ Message.POSTERNUM.getString() + ":" + listposter.get(i).getCount());
		}
		topMeta.setLore(starlores);
		topMeta.setCustomModelData(95282);
		top.setItemMeta(topMeta);
		inv.setItem(14, top);

		// 信息
		ItemStack info = new ItemStack(Material.PAPER);
		ItemMeta infoMeta = info.getItemMeta();
		infoMeta.setDisplayName(Message.GUI_PAGESTATE.getString());
		List<String> compasslores = new ArrayList<String>();
		compasslores.add(Message.GUI_PAGEID.getString().replaceAll("%PAGEID%", Option.MCBBS_URL.getString()));
		Crawler crawler = new Crawler();
		if (crawler.visible) {// 如果帖子可视，就获取帖子最近一次顶贴
			if (!crawler.Time.isEmpty()) { // 如果从没有人顶帖，就以“----”代替上次顶帖时间(原来不加判断直接get会报索引范围错误)
				compasslores.add(Message.GUI_LASTPOST.getString().replaceAll("%TIME%", crawler.Time.get(0)));
			} else {
				compasslores.add(Message.GUI_LASTPOST.getString().replaceAll("%TIME%", "----"));
			}
		} else {
			compasslores.add(Message.GUI_PAGENOTVISIBLE.getString());
		}
		String extra = Util.getExtraReward(crawler);
		if (extra != null) {
			String extrarewards = Message.GUI_EXTRAREWARDS.getString().replaceAll("%EXTRA%", extra);
			compasslores.add(extrarewards);
		}
		compasslores.add(Message.GUI_CLICKOPEN.getString());
		infoMeta.setLore(compasslores);
		infoMeta.setCustomModelData(95280);
		info.setItemMeta(infoMeta);
		inv.setItem(22, info);
	}
	
	public ItemStack getRandomPane() {// 获取随机一种颜色的玻璃板
//		short data = (short)(Math.random()* 16);// 这会随机取出0-15的数据值
//		while (data == 8) {// 8号亮灰色染色玻璃板根本没有颜色
//			data = (short)(Math.random()* 16);
//		}
//		ItemStack frame;
//		String[] glasspanes = {"WHITE_STAINED_GLASS_PANE", "ORANGE_STAINED_GLASS_PANE", "MAGENTA_STAINED_GLASS_PANE",
//				"LIGHT_BLUE_STAINED_GLASS_PANE", "YELLOW_STAINED_GLASS_PANE", "LIME_STAINED_GLASS_PANE", "PINK_STAINED_GLASS_PANE",
//				"GRAY_STAINED_GLASS_PANE", "LIGHT_GRAY_STAINED_GLASS_PANE", "CYAN_STAINED_GLASS_PANE", "PURPLE_STAINED_GLASS_PANE",
//				"BLUE_STAINED_GLASS_PANE", "BROWN_STAINED_GLASS_PANE", "GREEN_STAINED_GLASS_PANE", "RED_STAINED_GLASS_PANE",
//				"BLACK_STAINED_GLASS_PANE"};
		ItemStack frame = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		ItemMeta framemeta = frame.getItemMeta();
		framemeta.setDisplayName(Message.GUI_FRAME.getString());
		frame.setItemMeta(framemeta);
		return frame;
	}

	public Inventory getGui() {
		return inv;
	}

	public void setGui(Inventory inv) {
		this.inv = inv;
	}
	
	public static void setSQLer(SQLer sql) {
		GUI.sql = sql;
	}
}
