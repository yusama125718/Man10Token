package yusama125718.man10Token;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static java.lang.Integer.parseInt;
import static net.kyori.adventure.text.event.ClickEvent.suggestCommand;
import static yusama125718.man10Token.Man10Token.*;

public class Event implements Listener {
    public Event(Man10Token plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void GUIClick(InventoryClickEvent e) throws IOException {
        Component component = e.getView().title();
        String title = "";
        if (component instanceof TextComponent text) title = text.content();
        if (title.startsWith("[Man10Token]")){
            if (e.getClick().equals(ClickType.NUMBER_KEY) || e.getClick().equals(ClickType.SWAP_OFFHAND)){
                e.setCancelled(true);
                return;
            }
            e.setCancelled(true);
            if (title.startsWith("[Man10Token] メインメニュー ")){
                if (!system || !trade){
                    e.getWhoClicked().sendMessage(Component.text(prefix + "現在システムはOffです"));
                    e.getWhoClicked().closeInventory();
                    return;
                }
                int page = parseInt(title.substring(21));
                if (51 <= e.getRawSlot() && e.getRawSlot() <= 53){    //次のページへ
                    if (items.size() > 45 * page) GUI.OpenMenu((Player) e.getWhoClicked(), false,page + 1);
                    return;
                }
                if (45 <= e.getRawSlot() && e.getRawSlot() <= 47){     //前のページへ
                    if (page != 1) GUI.OpenMenu((Player) e.getWhoClicked(), false, page - 1);
                    return;
                }
                if (e.getRawSlot() < 45 && e.getCurrentItem() != null){
                    int index = 45 * (page - 1) + e.getRawSlot();
                    if (items.size() > index) GUI.OpenTradeMenu((Player) e.getWhoClicked(), index);
                    return;
                }
            }
            else if (title.startsWith("[Man10Token] 交換画面 ")){
                if (!system || !trade){
                    e.getWhoClicked().sendMessage(Component.text(prefix + "現在システムはOffです"));
                    e.getWhoClicked().closeInventory();
                    return;
                }
                int id = parseInt(title.substring(19));
                Man10Token.TradeItem target = items.get(id);
                if (e.getRawSlot() == 31){
                    if (!target.state){
                        e.getWhoClicked().sendMessage(Component.text(prefix + "そのアイテムは現在交換停止中です"));
                        e.getWhoClicked().closeInventory();
                        return;
                    }
                    Thread th = new Thread(() -> {
                        MySQLManager mysql = new MySQLManager(mtoken, "man10_token");
                        try {
                            // トークン残高確認
                            ResultSet res = mysql.query("SELECT id, update_at, mcid, uuid, value FROM token_data WHERE uuid = '" + e.getWhoClicked().getUniqueId() + "' AND token_name = '" + token_charge + "' LIMIT 1;");
                            if (res == null) {
                                e.getWhoClicked().sendMessage(Component.text(prefix + "DBの取得に失敗しました"));
                                mysql.close();
                                return;
                            }
                            if (!res.next()) {
                                e.getWhoClicked().sendMessage(Component.text(prefix + "トークンデータが存在しません"));
                                mysql.close();
                                return;
                            }
                            int token_id = res.getInt("id");
                            int value = res.getInt("value");
                            mysql.close();
                            if (value < target.cost){
                                e.getWhoClicked().sendMessage(Component.text(prefix + "トークンが不足しています"));
                                e.getWhoClicked().closeInventory();
                                return;
                            }
                            // 最大取引数が設定されている場合取引数を確認
                            if (target.max_all != 0 || target.max_personal != 0){
                                res = mysql.query("SELECT COUNT(item_name = '"+ target.name +"' AND uuid = '"+ e.getWhoClicked().getUniqueId() +"') AS personal, COUNT(item_name = '"+ target.name +"') AS global FROM trade_logs;");
                                if (res != null && res.next()){
                                    int global = res.getInt("global");
                                    int personal = res.getInt("personal");
                                    mysql.close();
                                    if ((target.max_all != 0 && global >= target.max_all) || (target.max_personal != 0 && personal >= target.max_personal)){
                                        e.getWhoClicked().sendMessage(Component.text(prefix + "取引数が最大数に達しています"));
                                        e.getWhoClicked().closeInventory();
                                        return;
                                    }
                                }
                                else {
                                    e.getWhoClicked().sendMessage(Component.text(prefix + "DBの取得に失敗しました"));
                                    mysql.close();
                                    return;
                                }
                            }
                            value -= target.cost;
                            LocalDateTime time = LocalDateTime.now();
                            // トランザクション処理で各データおよびログを保存
                            if (!mysql.execute("START TRANSACTION;" +
                                    "UPDATE token_data SET update_at = '"+ time +"', mcid = '"+ e.getWhoClicked().getName() +"', value = "+ value +" WHERE id = "+ token_id +";" +
                                    "INSERT INTO token_logs (time, token_data_id, mcid, uuid, diff, note) VALUES ('"+ time +"', "+ token_id +", '"+ e.getWhoClicked().getName() +"', '"+ e.getWhoClicked().getUniqueId() +"', '"+ target.cost +"', '"+ target.name +"の交換');" +
                                    "INSERT INTO trade_logs (time, token_data_id, mcid, uuid, token_logs_id, item_name) VALUES ('"+ time +"', "+ token_id +", '"+ e.getWhoClicked().getName() +"', '"+ e.getWhoClicked().getUniqueId() +"', '(SELECT id FROM token_logs WHERE uuid = '"+ e.getWhoClicked().getUniqueId() +"' AND token_id = '"+ token_id +"' AND time = '"+ time +"' LIMIT 1)', '"+ target.name +"');" +
                                    "COMMIT;")) {
                                e.getWhoClicked().sendMessage(Component.text(prefix + "DBの保存に失敗しました"));
                                e.getWhoClicked().closeInventory();
                                return;
                            }
                            // アイテム付与
                            Bukkit.getScheduler().runTask(mtoken, () -> e.getWhoClicked().getInventory().addItem(target.item.clone()));
                            e.getWhoClicked().sendMessage(Component.text(prefix + "交換しました"));
                            return;
                        }
                        catch (SQLException ex) {
                            e.getWhoClicked().sendMessage(Component.text(prefix + "DBの取得に失敗しました"));
                            mysql.close();
                            throw new RuntimeException(ex);
                        }
                    });
                    th.start();
                }
                else if (e.getRawSlot() == 0){
                    e.getWhoClicked().closeInventory();
                    GUI.OpenMenu((Player) e.getWhoClicked(), false, (id + 46) / 45);
                    return;
                }
            }
            else if (title.startsWith("[Man10Token] 編集メニュー ")){
                int page = parseInt(title.substring(20));
                if (51 <= e.getRawSlot() && e.getRawSlot() <= 53){    //次のページへ
                    if (items.size() > 45 * page) GUI.OpenMenu((Player) e.getWhoClicked(), true,page + 1);
                    return;
                }
                else if (45 <= e.getRawSlot() && e.getRawSlot() <= 47){     //前のページへ
                    if (page != 1) GUI.OpenMenu((Player) e.getWhoClicked(), true, page - 1);
                    return;
                }
                else if (e.getRawSlot() < 45 && e.getCurrentItem() != null){
                    int index = 45 * (page - 1) + e.getRawSlot();
                    if (items.size() > index) {
                        e.getWhoClicked().closeInventory();
                        GUI.OpenEditMenu((Player) e.getWhoClicked(), index);
                    }
                    return;
                }
            }
            else if (title.startsWith("[Man10Token] 編集画面 ")){
                int id = parseInt(title.substring(18));
                Man10Token.TradeItem target = items.get(id);
                if (e.getRawSlot() == 0){
                    e.getWhoClicked().closeInventory();
                    GUI.OpenMenu((Player) e.getWhoClicked(), true, (id + 46) / 45);
                    return;
                }
                else if (e.getRawSlot() == 13) {
                    e.getWhoClicked().closeInventory();
                    GUI.OpenEditItemGUI((Player) e.getWhoClicked(), id);
                    return;
                }
                else if (e.getRawSlot() == 15) {
                    e.getWhoClicked().closeInventory();
                    e.getWhoClicked().sendMessage(Component.text(prefix + "全体での交換数の上限を編集する§e§l[ここをクリックで自動入力する]").clickEvent(suggestCommand("/mtoken max_global "+ id +" ")));
                    e.getWhoClicked().sendMessage(Component.text(prefix + "↑をクリックして[上限]を入力で編集（0で無制限）"));
                    return;
                }
                else if (e.getRawSlot() == 24) {
                    e.getWhoClicked().closeInventory();
                    e.getWhoClicked().sendMessage(Component.text(prefix + "個人での交換数の上限を編集する§e§l[ここをクリックで自動入力する]").clickEvent(suggestCommand("/mtoken max_personal "+ id +" ")));
                    e.getWhoClicked().sendMessage(Component.text(prefix + "↑をクリックして[上限]を入力で編集（0で無制限）"));
                    return;
                }
                else if (e.getRawSlot() == 31) {
                    if (target.state){
                        target.state = false;
                        e.getInventory().setItem(31, GUI.GetItem(Material.REDSTONE_BLOCK, "交換停止中 [クリックで交換開始]", 1));
                    }
                    else {
                        target.state = true;
                        e.getInventory().setItem(31, GUI.GetItem(Material.EMERALD_BLOCK, "交換中 [クリックで交換停止]", 1));
                    }
                    File file = new File(configfile.getAbsoluteFile() + File.separator + target.name);
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    config.set("state", target.state);
                    config.save(file);
                    e.getWhoClicked().sendMessage(Component.text(prefix + "販売状態を変更しました。現在の状態：" + target.state));
                    return;
                }
                else if (e.getRawSlot() == 33){
                    e.getWhoClicked().closeInventory();
                    e.getWhoClicked().sendMessage(Component.text(prefix + "要求トークン数を編集する§e§l[ここをクリックで自動入力する]").clickEvent(suggestCommand("/mtoken cost "+ id +" ")));
                    e.getWhoClicked().sendMessage(Component.text(prefix + "↑をクリックして[コスト]を入力で編集"));
                    return;
                }
                else if (e.getRawSlot() == 35){
                    e.getWhoClicked().closeInventory();
                    GUI.OpenDeleteGUI((Player) e.getWhoClicked(), id);
                    return;
                }
            }
            else if (title.startsWith("[Man10Token] トレード削除 ")){
                int id = parseInt(title.substring(20));
                Man10Token.TradeItem target = items.get(id);
                if (e.getRawSlot() == 2){
                    if (new File(configfile + File.separator + target.name).delete()){
                        items.remove(target);
                        e.getWhoClicked().closeInventory();
                        e.getWhoClicked().sendMessage(Component.text(prefix + "削除しました"));
                    }
                    else {
                        e.getWhoClicked().closeInventory();
                        e.getWhoClicked().sendMessage(Component.text(prefix + "削除に失敗しました"));
                    }
                    return;
                }
                else if (e.getRawSlot() == 6){
                    e.getWhoClicked().closeInventory();
                    GUI.OpenEditMenu((Player) e.getWhoClicked(), id);
                    return;
                }
            }
        }
        else if (title.startsWith("[Man10TokenEdit]")){
            if ((e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta() || !e.getCurrentItem().getItemMeta().hasCustomModelData() || e.getCurrentItem().getItemMeta().getCustomModelData() != 1)
                    || (!e.getCurrentItem().getType().equals(Material.EMERALD_BLOCK) && !e.getCurrentItem().getType().equals(Material.WHITE_STAINED_GLASS_PANE))) return;
            if (e.getClick().equals(ClickType.NUMBER_KEY) || e.getClick().equals(ClickType.SWAP_OFFHAND)){
                e.setCancelled(true);
                return;
            }
            if (title.startsWith("[Man10TokenEdit] 新規作成 ")){
                String name = title.substring(22);
                switch (e.getRawSlot()){
                    case 13:
                        return;

                    case 31:
                        e.setCancelled(true);
                        Inventory inv = e.getInventory();
                        if (inv.getItem(13) == null){
                            e.getWhoClicked().sendMessage(Component.text(prefix + "アイテムが不足しています！"));
                            return;
                        }
                        Component c = inv.getItem(35).displayName();
                        String s = "";
                        if (c instanceof TextComponent text) s = text.content();
                        int cost = parseInt(s);
                        File folder = new File(configfile.getAbsolutePath() + File.separator + name + ".yml");
                        YamlConfiguration yml = new YamlConfiguration();
                        yml.set("cost", cost);
                        yml.set("item", inv.getItem(13));
                        yml.set("max_personal", 0);
                        yml.set("max_all", 0);
                        yml.set("state", false);
                        yml.save(folder);
                        items.add(new TradeItem(name + ".yml", false, inv.getItem(15), cost,  0, 0));
                        e.getWhoClicked().sendMessage(Component.text(prefix + "作成しました"));
                        e.getWhoClicked().closeInventory();
                        return;

                    default:
                        e.setCancelled(true);
                        return;
                }
            }
            else if (title.startsWith("[Man10TokenEdit] アイテム編集 ")){
                int id = parseInt(title.substring(24));
                Man10Token.TradeItem target = items.get(id);
                switch(e.getRawSlot()){
                    case 0:
                        e.setCancelled(true);
                        e.getWhoClicked().closeInventory();
                        GUI.OpenEditMenu((Player) e.getWhoClicked(), id);
                        return;

                    case 13:
                        return;

                    case 31:
                        e.setCancelled(true);
                        if (e.getInventory().getItem(13) == null){
                            e.getWhoClicked().sendMessage(Component.text(prefix + "アイテムが不足しています"));
                            return;
                        }
                        File file = new File(configfile + File.separator + target.name);
                        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                        config.set("item", e.getInventory().getItem(13));
                        config.save(file);
                        target.item = e.getInventory().getItem(13);
                        e.getWhoClicked().closeInventory();
                        e.getWhoClicked().sendMessage(Component.text(prefix + "変更しました"));
                        return;

                    default:
                        e.setCancelled(true);
                        return;
                }
            }
        }
    }
}
