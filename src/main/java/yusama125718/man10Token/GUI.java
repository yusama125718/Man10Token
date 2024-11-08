package yusama125718.man10Token;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static yusama125718.man10Token.Man10Token.items;

public class GUI {
    // 通常メニュー展開
    public static void OpenMenu(Player p, Boolean isEdit, Integer page){
        String title;
        if(isEdit) title = "[Man10Token] 編集メニュー " + page;
        else title = "[Man10Token] メインメニュー " + page;
        Inventory inv = Bukkit.createInventory(null,54, Component.text(title));
        for (int i = 51; i < 54; i++){
            inv.setItem(i,GetItem(Material.BLUE_STAINED_GLASS_PANE, "次のページへ",1));
            inv.setItem(i - 3,GetItem(Material.WHITE_STAINED_GLASS_PANE, "",1));
            inv.setItem(i - 6,GetItem(Material.RED_STAINED_GLASS_PANE, "前のページへ",1));
        }
        for (int i = 0; (i < 45 && items.size() > i + 45 * (page - 1)); i++){
            Man10Token.TradeItem t = items.get(i + 45 * (page - 1));
            inv.setItem(i, CreateSample(t.item.clone(), t.cost));
        }
        p.openInventory(inv);
    }

    // 通常メニュー交換画面
    public static void OpenTradeMenu(Player p, Integer index){
        Man10Token.TradeItem t = items.get(index);
        Inventory inv = Bukkit.createInventory(null,36, Component.text("[Man10Token] 交換画面 " + index));
        for (int i = 0; i < 36; i++){
            switch (i){
                // 戻る
                case 0:
                    inv.setItem(i, GetItem(Material.BLACK_STAINED_GLASS_PANE, "戻る",1));
                    break;

                // アイテム
                case 13:
                    inv.setItem(i, CreateSample(t.item.clone(), t.cost));
                    break;

                // 交換ボタン
                case 31:
                    if(t.state) inv.setItem(i, GetItem(Material.EMERALD_BLOCK, "交換する", 1));
                    else inv.setItem(i, GetItem(Material.REDSTONE_BLOCK, "交換停止中", 1));
                    break;

                default:
                    inv.setItem(i, GetItem(Material.WHITE_STAINED_GLASS_PANE, "",1));
                    break;
            }

        }
        p.openInventory(inv);
    }


    public static void OpenEditMenu(Player p, Integer id){
        Man10Token.TradeItem t = items.get(id);
        Inventory inv = Bukkit.createInventory(null,36, Component.text("[Man10Token] 編集画面 " + id));
        for (int i = 0; i < 36; i++){
            switch (i){
                // 戻る
                case 0:
                    inv.setItem(i, GetItem(Material.BLACK_STAINED_GLASS_PANE, "戻る",1));
                    break;

                // アイテム変更
                case 13:
                    ItemStack d_item = t.item.clone();
                    ItemMeta d_meta = d_item.getItemMeta();
                    if(d_meta.hasLore()) d_meta.lore().add(Component.text("[クリックで編集]"));
                    else {
                        d_meta.lore(new ArrayList<>() {{
                            add(Component.text("[クリックで編集]"));
                        }});
                    }
                    d_item.setItemMeta(d_meta);
                    inv.setItem(i, d_item);
                    break;

                // 全体制限変更
                case 15:
                    inv.setItem(i, GetItem(Material.BLUE_STAINED_GLASS_PANE, "全体での交換数の上限を変更する　設定値："+ t.max_all, 1));
                    break;

                // 個人制限変更
                case 24:
                    inv.setItem(i, GetItem(Material.BLUE_STAINED_GLASS_PANE, "個人での交換数の上限を変更する　設定値："+ t.max_personal, 1));
                    break;

                // 交換ボタン
                case 31:
                    if(t.state) inv.setItem(i, GetItem(Material.EMERALD_BLOCK, "交換中 [クリックで交換停止]", 1));
                    else inv.setItem(i, GetItem(Material.REDSTONE_BLOCK, "交換停止中 [クリックで交換開始]", 1));
                    break;

                // token
                case 33:
                    inv.setItem(i, GetItem(Material.EMERALD, "必要トークンを編集する 必要トークン：" + t.cost, 1));
                    break;

                // delete
                case 35:
                    inv.setItem(i, GetItem(Material.RED_STAINED_GLASS, "削除する", 1));
                    break;

                default:
                    inv.setItem(i, GetItem(Material.WHITE_STAINED_GLASS_PANE, "",1));
                    break;
            }

        }
        p.openInventory(inv);
    }

    public static void OpenCreateGUI(Player p, String name, Integer cost){
        Inventory inv = Bukkit.createInventory(null,36, Component.text("[Man10TokenEdit] 新規作成 " + name));
        for (int i = 0; i < 36; i++){
            switch (i){
                // アイテム
                case 13:
                    break;

                // 決定ボタン
                case 31:
                    inv.setItem(i, GetItem(Material.EMERALD_BLOCK, "保存", 1));
                    break;

                case 35:
                    inv.setItem(i, GetItem(Material.WHITE_STAINED_GLASS_PANE, cost.toString(), 1));
                    break;

                default:
                    inv.setItem(i, GetItem(Material.WHITE_STAINED_GLASS_PANE, "",1));
                    break;
            }

        }
        p.openInventory(inv);
    }

    // mode = <display/cost/item>
    public static void OpenEditItemGUI(Player p, Integer id){
        Man10Token.TradeItem t = items.get(id);
        Inventory inv = Bukkit.createInventory(null,36, Component.text("[Man10TokenEdit] アイテム編集 " + id ));
        for (int i = 0; i < 36; i++){
            switch (i){
                // 戻る
                case 0:
                    inv.setItem(i, GetItem(Material.BLACK_STAINED_GLASS_PANE, "戻る",1));
                    break;

                // アイテムスロット
                case 13:
                    ItemStack item;
                    item = t.item;
                    inv.setItem(i, item);
                    break;

                // 決定ボタン
                case 31:
                    inv.setItem(i, GetItem(Material.EMERALD_BLOCK, "保存", 1));
                    break;

                default:
                    inv.setItem(i, GetItem(Material.WHITE_STAINED_GLASS_PANE, "",1));
                    break;
            }

        }
        p.openInventory(inv);
    }

    public static void OpenDeleteGUI(Player p, int id){
        Man10Token.TradeItem t = items.get(id);
        Inventory inv = Bukkit.createInventory(null,36, Component.text("[Man10Token] トレード削除 " + id ));
        for (int i = 0; i < 36; i++){
            switch (i){
                // 戻る
                case 2:
                    inv.setItem(i, GetItem(Material.RED_STAINED_GLASS_PANE, "削除",1));
                    break;

                case 6:
                    inv.setItem(i, GetItem(Material.BLUE_STAINED_GLASS_PANE, "キャンセル",1));
                    break;

                default:
                    inv.setItem(i, GetItem(Material.WHITE_STAINED_GLASS_PANE, "",1));
                    break;
            }

        }
        p.openInventory(inv);
    }

    public static ItemStack GetItem(Material mate, String name, Integer cmd){
        ItemStack item = new ItemStack(mate, 1);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        meta.setCustomModelData(cmd);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack CreateSample(ItemStack item, int cost){
        ItemMeta meta = item.getItemMeta();
        List<Component> lore = new ArrayList<>();
        if (meta.hasLore()) lore = meta.lore();
        lore.add(Component.text("[Man10Token] 必要コスト：" + cost));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
