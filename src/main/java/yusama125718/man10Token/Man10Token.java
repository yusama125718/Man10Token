package yusama125718.man10Token;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Man10Token extends JavaPlugin {

    public static JavaPlugin mtoken;
    public static Boolean system = false;
    public static Boolean trade = false;
    public static Boolean charge = false;
    public static String prefix;
    public static String token_charge = "unknown";
    public static String token_trade = "unknown";
    public static List<TradeItem> items = new ArrayList<>();
    public static File configfile;

    private static File folder;

    @Override
    public void onEnable() {
        mtoken = this;
        new Event(this);
        getCommand("mtoken").setExecutor(new Command());
        SetupConfig();
        MySQLManager mysql = new MySQLManager(mtoken, "man10_token");
        mysql.execute("create table if not exists token_logs(id int auto_increment, time varchar(35), token_data_id int, mcid varchar(16), uuid varchar(36), diff int, note varchar(90), primary key(id))");
        mysql.execute("create table if not exists trade_logs(id int auto_increment, time varchar(35), token_data_id int, token_logs_id int, item_name varchar(20), mcid varchar(16), uuid varchar(36), primary key(id))");
        mysql.execute("create table if not exists token_data(id int auto_increment, create_at varchar(35), update_at varchar(35), token_name varchar(50), mcid varchar(16), uuid varchar(36), value int, primary key(id))");
    }

    public static class TradeItem{
        public String name;
        public Boolean state;
        public ItemStack item;
        public Integer cost;
        public Integer max_personal;
        public Integer max_all;

        public TradeItem(String NAME, Boolean STATE, ItemStack ITEM, Integer COST, Integer MAX_P, Integer MAX_A){
            name = NAME;
            state = STATE;
            item = ITEM;
            cost = COST;
            max_personal = MAX_P;
            max_all = MAX_A;
        }
    }

    private static void SetupConfig(){
        mtoken.saveDefaultConfig();
        folder = new File(mtoken.getDataFolder().getAbsolutePath() + File.separator + "items");
        system = mtoken.getConfig().getBoolean("system");
        trade = mtoken.getConfig().getBoolean("trade");
        charge = mtoken.getConfig().getBoolean("charge");
        prefix = mtoken.getConfig().getString("prefix");
        token_charge = mtoken.getConfig().getString("token_name.charge");
        token_trade = mtoken.getConfig().getString("token_name.trade");
        if (mtoken.getDataFolder().listFiles() != null){
            for (File file : Objects.requireNonNull(mtoken.getDataFolder().listFiles())) {
                if (file.getName().equals("items")) {
                    configfile = file;
                    GetItems();
                    return;
                }
            }
        }
        if (folder.mkdir()) {
            Bukkit.broadcast(Component.text(prefix + "アイテムフォルダを作成しました"), "mtoken.op");
            configfile = folder;
        } else {
            Bukkit.broadcast(Component.text(prefix + "アイテムフォルダの作成に失敗しました"), "mtoken.op");
        }
    }

    private static void GetItems(){
        if (configfile.listFiles() != null){
            for (File file : configfile.listFiles()){
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                if (!config.isString("token") || !config.isItemStack("item") || !config.isBoolean("state") || !config.isInt("cost") || !config.isInt("max_personal") || !config.isInt("max_all")){
                    Bukkit.broadcast(Component.text(prefix + file.getName() + "の読み込みに失敗しました"), "mtoken.op");
                    continue;
                }
                items.add(new TradeItem(file.getName(), config.getBoolean("state"), config.getItemStack("item"), config.getInt("cost"), config.getInt("max_personal"), config.getInt("max_all")));
            }
        }
    }
}
