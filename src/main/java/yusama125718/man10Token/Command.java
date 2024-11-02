package yusama125718.man10Token;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

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

            case 2:
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
                    if (!system){
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
                    Thread th = new Thread(() -> {
                        MySQLManager mysql = new MySQLManager(mtoken, "man10_token");
                        ResultSet res = mysql.query("SELECT id, update_at, mcid, uuid, value FROM token_data WHERE uuid = '"+ ((Player) sender).getUniqueId() +"' AND token_name = '"+ token_charge +"' LIMIT 1;");
                        if (res == null){
                            sender.sendMessage(Component.text(prefix + "DBの接続に失敗しました"));
                            mysql.close();
                            return;
                        }
                        try {
                            Player p = (Player) sender;
                            // データなかった時処理
                            if (!res.next()){
                                mysql.close();
                                LocalDateTime time = LocalDateTime.now();
                                if (!mysql.execute("INSERT INTO token_data (create_at, update_at, token_name, mcid, uuid, value) VALUES ('"+ time +"', '"+ time +"', '"+ token_charge +"', '"+ p.getName() + "', '"+ p.getUniqueId() +"', "+ amount +");")){
                                    sender.sendMessage(Component.text(prefix + "DBの保存に失敗しました"));
                                    Bukkit.broadcast(Component.text(prefix + "作成失敗："+ p.getName() +" トークン："+ amount), "mtoken.op");
                                    return;
                                }
                                sender.sendMessage(Component.text(prefix + "ユーザーデータを作成し、追加しました"));
                                return;
                            }
                            if (!res.getString("mcid").equals(p.getName())){
                                
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                    });
                    th.start();
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return null;
    }
}
