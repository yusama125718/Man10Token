package yusama125718.man10Token;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.lang.Integer.parseInt;
import static yusama125718.man10Token.Man10Token.*;

public class Command implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("mtoken.p")) return true;
        switch (args.length){
            case 0:
                if (!system || !trade){
                    sender.sendMessage(prefix + "交換停止中です");
                    return true;
                }
                GUI.OpenMenu((Player) sender, false, 1);
                return true;

            case 1:
                if (args[0].equals("token")){
                    if (!system){
                        sender.sendMessage(prefix + "停止中です");
                        return true;
                    }
                    Thread th = new Thread(() -> {
                        MySQLManager mysql = new MySQLManager(mtoken, "mtoken");
                        try{
                            ResultSet res = mysql.query("SELECT value FROM token_data WHERE uuid = '"+ ((Player) sender).getUniqueId() +"' AND token_name = '"+ token_trade +"' LIMIT 1;");
                            if (!res.next()){
                                mysql.close();
                                sender.sendMessage(Component.text(prefix + "データがありません"));
                                return;
                            }
                            int value = res.getInt("value");
                            sender.sendMessage(prefix + "現在の交換に使用できるトークンは"+ value +"です");
                        }
                        catch (SQLException ex) {
                            sender.sendMessage(Component.text(prefix + "DBの取得に失敗しました"));
                            mysql.close();
                            throw new RuntimeException(ex);
                        }
                    });
                    th.start();
                    return true;
                }
                if (sender.hasPermission("mtoken.op") && args[0].equals("tokenop")){
                    String conditions = "uuid = "+ ((Player) sender).getUniqueId();
                    SendTokens(sender, conditions);
                    return true;
                }
                if (sender.hasPermission("mtoken.op") && args[0].equals("on")){
                    if (system){
                        sender.sendMessage(prefix + "既にonです");
                        return true;
                    }
                    system = true;
                    mtoken.getConfig().set("system", system);
                    mtoken.saveConfig();
                    sender.sendMessage(prefix + "onにしました");
                    return true;
                }
                if (sender.hasPermission("mtoken.op") && args[0].equals("off")){
                    if (!system){
                        sender.sendMessage(prefix + "既にoffです");
                        return true;
                    }
                    system = false;
                    mtoken.getConfig().set("system", system);
                    mtoken.saveConfig();
                    sender.sendMessage(prefix + "offにしました");
                    return true;
                }
                if (sender.hasPermission("mtoken.op") && args[0].equals("edit")){
                    if (system && trade){
                        sender.sendMessage(prefix + "トレードをオフにしてから編集してください");
                        return true;
                    }
                    GUI.OpenMenu((Player) sender, true, 1);
                    return true;
                }
                if (args[0].equals("help")) {
                    sender.sendMessage(prefix + "/mtoken　交換メニューを開きます");
                    sender.sendMessage(prefix + "/mtoken　token 交換に使用するトークンの残高を表示します");
                    if (sender.hasPermission("mtoken.op")) {
                        sender.sendMessage(prefix + "/mtoken　tokenop 自分が所有する全てのトークンを表示します");
                        sender.sendMessage(prefix + "/mtoken [on/off] システムをON/OFFします");
                        sender.sendMessage(prefix + "/mtoken　tokenop [MCID/UUID] 指定したプレイヤーが所有する全てのトークンを表示します");
                        sender.sendMessage("※プレイヤーがオンラインの場合、MCIDで実行した場合でもUUIDで検索します");
                        sender.sendMessage(prefix + "/mtoken　edit 編集メニューを開きます");
                        sender.sendMessage(prefix + "/mtoken　trade [on/off] 交換をON/OFFします");
                        sender.sendMessage(prefix + "/mtoken　charge [on/off] チャージをON/OFFします");
                        sender.sendMessage(prefix + "/mtoken　charge [数字] 自分のトークンを増減させます");
                        sender.sendMessage(prefix + "/mtoken　charge [MCID/UUID] [数字] 指定したプレイヤーのトークンを増減させます");
                        sender.sendMessage(prefix + "/mtoken　create [name] [cost] 交換先を作成します");
                    }
                    return true;
                }
                break;

            case 2:
                if (sender.hasPermission("mtoken.op") && args[0].equals("tokenop")){
                    String conditions = "";
                    // 16文字以上はUUIDとする
                    if (args[1].length() > 16) conditions = "uuid = "+ args[1];
                    else {
                        Player p = Bukkit.getPlayer(args[1]);
                        if (p == null) conditions = "mcid = "+ args[1];
                        else conditions = "uuid = "+ p.getUniqueId();
                    }
                    SendTokens(sender, conditions);
                    return true;
                }
                if (sender.hasPermission("mtoken.op") && args[0].equals("trade")){
                    if (args[1].equals("on")){
                        if (trade){
                            sender.sendMessage(prefix + "既にonです");
                            return true;
                        }
                        trade = true;
                        mtoken.getConfig().set("trade", trade);
                        mtoken.saveConfig();
                        sender.sendMessage(prefix + "onにしました");
                        return true;
                    }
                    if (args[1].equals("off")){
                        if (!trade){
                            sender.sendMessage(prefix + "既にoffです");
                            return true;
                        }
                        trade = false;
                        mtoken.getConfig().set("trade", trade);
                        mtoken.saveConfig();
                        sender.sendMessage(prefix + "offにしました");
                        return true;
                    }
                }
                if (sender.hasPermission("mtoken.op") && args[0].equals("charge")){
                    if (args[1].equals("on")){
                        if (charge){
                            sender.sendMessage(prefix + "既にonです");
                            return true;
                        }
                        charge = true;
                        mtoken.getConfig().set("charge", charge);
                        mtoken.saveConfig();
                        sender.sendMessage(prefix + "onにしました");
                        return true;
                    }
                    if (args[1].equals("off")){
                        if (!charge){
                            sender.sendMessage(prefix + "既にoffです");
                            return true;
                        }
                        charge = false;
                        mtoken.getConfig().set("charge", charge);
                        mtoken.saveConfig();
                        sender.sendMessage(prefix + "offにしました");
                        return true;
                    }
                    if (!system || !charge){
                        sender.sendMessage(Component.text(prefix + "システムは停止中です"));
                        return true;
                    }
                    int amount;
                    try {
                        amount = parseInt(args[1]);
                    } catch (Exception e){
                        sender.sendMessage(Component.text(prefix + "数値が不正です"));
                        return true;
                    }
                    Charge((Player) sender, (Player) sender, amount);
                    return true;
                }
                break;

            case 3:
                if (sender.hasPermission("mtoken.op") && args[0].equals("charge")){
                    if (!system || !charge){
                        sender.sendMessage(Component.text(prefix + "システムは停止中です"));
                        return true;
                    }
                    Player p;
                    // 16文字まではMCIDとして扱う
                    if (args[1].length() <= 16) p = Bukkit.getPlayerExact(args[2]);
                    else p = Bukkit.getPlayer(UUID.fromString(args[2]));
                    if (p == null){
                        sender.sendMessage(Component.text(prefix + "プレイヤーが見つかりませんでした"));
                        return true;
                    }
                    int amount;
                    try {
                        amount = parseInt(args[2]);
                    } catch (Exception e){
                        sender.sendMessage(Component.text(prefix + "数値が不正です"));
                        return true;
                    }
                    Charge((Player) sender, p, amount);
                    return true;
                }
                if (sender.hasPermission("mtoken.op") && args[0].equals("create")){
                    if (args[1].length() > 16){
                        sender.sendMessage(prefix + "16文字以下にしてください");
                        return true;
                    }
                    int cost;
                    try {
                        cost = parseInt(args[2]);
                    } catch (Exception e){
                        sender.sendMessage(Component.text(prefix + "数値が不正です"));
                        return true;
                    }
                    for(TradeItem t: items){
                        if(t.name.equals(args[1] + ".yml")){
                            sender.sendMessage(Component.text(prefix + args[1] + "は既に存在しています"));
                            return true;
                        }
                    }
                    GUI.OpenCreateGUI((Player) sender, args[1], cost);
                    return true;
                }
                if (sender.hasPermission("mtoken.op") && args[0].equals("cost")){
                    int id;
                    int cost;
                    try {
                        id = parseInt(args[1]);
                        cost = parseInt(args[2]);
                    } catch (Exception e){
                        sender.sendMessage(Component.text(prefix + "数値が不正です"));
                        return true;
                    }
                    if (items.size() <= id){
                        sender.sendMessage(Component.text(prefix + "指定したIDは存在しません"));
                        return true;
                    }
                    TradeItem target = items.get(id);
                    target.cost = cost;
                    File file = new File(configfile + File.separator + target.name);
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    config.set("cost", cost);
                    try {
                        config.save(file);
                    } catch (IOException e) {
                        sender.sendMessage(Component.text(prefix + "保存に失敗しました"));
                        return true;
                    }
                    sender.sendMessage(Component.text(prefix + "変更しました"));
                    return true;
                }
                if (sender.hasPermission("mtoken.op") && args[1].equals("max_global")){
                    int id;
                    int max;
                    try {
                        id = parseInt(args[1]);
                        max = parseInt(args[2]);
                    } catch (Exception e){
                        sender.sendMessage(Component.text(prefix + "数値が不正です"));
                        return true;
                    }
                    if (items.size() <= id){
                        sender.sendMessage(Component.text(prefix + "指定したIDは存在しません"));
                        return true;
                    }
                    if (max < 0){
                        sender.sendMessage(Component.text(prefix + "0未満は指定できません"));
                        return true;
                    }
                    TradeItem target = items.get(id);
                    target.max_all = max;
                    File file = new File(configfile + File.separator + target.name);
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    config.set("max_all", max);
                    try {
                        config.save(file);
                    } catch (IOException e) {
                        sender.sendMessage(Component.text(prefix + "保存に失敗しました"));
                        return true;
                    }
                    sender.sendMessage(Component.text(prefix + "変更しました"));
                    return true;
                }
                if (sender.hasPermission("mtoken.op") && args[1].equals("max_personal")){
                    int id;
                    int max;
                    try {
                        id = parseInt(args[1]);
                        max = parseInt(args[2]);
                    } catch (Exception e){
                        sender.sendMessage(Component.text(prefix + "数値が不正です"));
                        return true;
                    }
                    if (items.size() <= id){
                        sender.sendMessage(Component.text(prefix + "指定したIDは存在しません"));
                        return true;
                    }
                    if (max < 0){
                        sender.sendMessage(Component.text(prefix + "0未満は指定できません"));
                        return true;
                    }
                    TradeItem target = items.get(id);
                    target.max_personal = max;
                    File file = new File(configfile + File.separator + target.name);
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    config.set("max_personal", max);
                    try {
                        config.save(file);
                    } catch (IOException e) {
                        sender.sendMessage(Component.text(prefix + "保存に失敗しました"));
                        return true;
                    }
                    sender.sendMessage(Component.text(prefix + "変更しました"));
                    return true;
                }
                break;
        }
        sender.sendMessage(Component.text(prefix + "/mtoken help でhelpを表示"));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return null;
    }

    private static void Charge(Player owner, Player target, Integer amount){
        Thread th = new Thread(() -> {
            MySQLManager mysql = new MySQLManager(mtoken, "man10_token");
            try {
                ResultSet res = mysql.query("SELECT id, update_at, mcid, uuid, value FROM token_data WHERE uuid = '"+ target.getUniqueId() +"' AND token_name = '"+ token_charge +"' LIMIT 1;");
                if (res == null){
                    owner.sendMessage(Component.text(prefix + "DBの取得に失敗しました"));
                    mysql.close();
                    return;
                }
                LocalDateTime time = LocalDateTime.now();
                // データなかった時処理
                if (!res.next()){
                    mysql.close();
                    if (!mysql.execute("START TRANSACTION;" +
                            "INSERT INTO token_data (create_at, update_at, token_name, mcid, uuid, value) VALUES ('"+ time +"', '"+ time +"', '"+ token_charge +"', '"+ target.getName() + "', '"+ target.getUniqueId() +"', "+ amount +");" +
                            "INSERT INTO token_logs (time, token_data_id, mcid, uuid, diff, note) VALUES ('"+ time +"', (SELECT id FROM token_data WHERE uuid = '"+ target.getUniqueId() +"' AND token_name = '"+ token_charge +"' LIMIT 1), '"+ target.getName() +"', '"+ target.getUniqueId() +"', '"+ amount +"', '"+ owner.getName() +"による実行');" +
                            "COMMIT;")){
                        owner.sendMessage(Component.text(prefix + "DBの保存に失敗しました"));
                        return;
                    }
                    owner.sendMessage(Component.text(prefix + "ユーザーデータを作成し、追加しました"));
                    target.sendMessage(Component.text(prefix + "トークンを"+ amount +"獲得しました"));
                    return;
                }
                int id = res.getInt("id");
                int value = res.getInt("value");
                value += amount;
                mysql.close();
                // トランザクション処理
                if (!mysql.execute("START TRANSACTION;" +
                        "UPDATE token_data SET update_at = '"+ time +"', mcid = '"+ target.getName() +"', value = "+ value +" WHERE id = "+ id +";" +
                        "INSERT INTO token_logs (time, token_data_id, mcid, uuid, diff, note) VALUES ('"+ time +"', '"+ id +"', '"+ target.getName() +"', '"+ target.getUniqueId() +"', '"+ amount +"', '"+ owner.getName() +"による実行');" +
                        "COMMIT;")){
                    owner.sendMessage(Component.text(prefix + "更新に失敗しました"));
                    return;
                }
                owner.sendMessage(Component.text(prefix + "更新しました"));
                target.sendMessage(Component.text(prefix + "トークンを"+ amount +"獲得しました"));
            } catch (SQLException e) {
                owner.sendMessage(Component.text(prefix + "DBの取得に失敗しました"));
                mysql.close();
                throw new RuntimeException(e);
            }
        });
        th.start();
    }

    private static void SendTokens(CommandSender sender, String conditions){
        Thread th = new Thread(() -> {
            MySQLManager mysql = new MySQLManager(mtoken, "mtoken");
            try{
                ResultSet res = mysql.query("SELECT mcid, token_name, value FROM token_data WHERE "+ conditions +";");
                if (!res.next()){
                    mysql.close();
                    sender.sendMessage(Component.text(prefix + "データがありません"));
                    return;
                }
                sender.sendMessage(prefix + sender.getName() +"のトークン一覧");
                do {
                    int value = res.getInt("value");
                    String name = res.getNString("token_name");
                    String mcid = res.getString("mcid");
                    sender.sendMessage(mcid +"："+ name +"："+ value);
                } while (res.next());
            }
            catch (SQLException ex) {
                sender.sendMessage(Component.text(prefix + "DBの取得に失敗しました"));
                mysql.close();
                throw new RuntimeException(ex);
            }
        });
        th.start();
    }
}
