package in.twizmwaz.cardinal.module.modules.teamPicker;

import com.google.common.base.Optional;
import in.twizmwaz.cardinal.GameHandler;
import in.twizmwaz.cardinal.chat.ChatConstant;
import in.twizmwaz.cardinal.chat.LocalizedChatMessage;
import in.twizmwaz.cardinal.match.MatchState;
import in.twizmwaz.cardinal.module.Module;
import in.twizmwaz.cardinal.module.modules.blitz.Blitz;
import in.twizmwaz.cardinal.module.modules.classModule.ClassModule;
import in.twizmwaz.cardinal.module.modules.team.TeamModule;
import in.twizmwaz.cardinal.util.Items;
import in.twizmwaz.cardinal.util.MiscUtil;
import in.twizmwaz.cardinal.util.Teams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class TeamPicker implements Module {

    protected TeamPicker() {
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
    }


    public Inventory getTeamPicker(Player player) {
        int size = (((GameHandler.getGameHandler().getMatch().getModules().getModules(TeamModule.class).size() + (Teams.getTeamByPlayer(player).isPresent() && Teams.getTeamByPlayer(player).get().isObserver() ? 0 : 1 )) / 9) + 1) * 9;
        int classesSize = ((GameHandler.getGameHandler().getMatch().getModules().getModules(ClassModule.class).size() + 8) / 9) * 9;
        Inventory picker = Bukkit.createInventory(null, size + classesSize, ChatColor.DARK_RED + new LocalizedChatMessage(ChatConstant.UI_TEAM_PICK).getMessage(player.getLocale()));
        int item = 0;

        int maxPlayers = 0;
        int totalPlayers = 0;
        for (TeamModule team : GameHandler.getGameHandler().getMatch().getModules().getModules(TeamModule.class)) {
            if (!team.isObserver()) {
                maxPlayers += team.getMax();
                totalPlayers += team.size();
            }
        }
        ItemStack autoJoin = Items.createItem(Material.CHAINMAIL_HELMET, 1, (short) 0, ChatColor.GRAY + "" + ChatColor.BOLD + new LocalizedChatMessage(ChatConstant.UI_TEAM_JOIN_AUTO).getMessage(player.getLocale()), Arrays.asList((totalPlayers >= maxPlayers ? ChatColor.RED + "" : ChatColor.GREEN + "") + totalPlayers + ChatColor.GOLD + " / " + ChatColor.RED + "" + maxPlayers, ChatColor.AQUA + new LocalizedChatMessage(ChatConstant.UI_TEAM_JOIN_AUTO_LORE).getMessage(player.getLocale())));
        picker.setItem(item, autoJoin);
        item++;
        for (TeamModule team : GameHandler.getGameHandler().getMatch().getModules().getModules(TeamModule.class)) {
            if (!team.isObserver()) {
                ItemStack teamStack = Items.createLeatherArmor(Material.LEATHER_HELMET, 1, team.getColor() + "" + ChatColor.BOLD + team.getName(), Arrays.asList((team.size() >= team.getMax() ? ChatColor.RED + "" : ChatColor.GREEN + "") + team.size() + ChatColor.GOLD + " / " + ChatColor.RED + "" + team.getMax(), ChatColor.GREEN + new LocalizedChatMessage(ChatConstant.UI_TEAM_CAN_PICK).getMessage(player.getLocale())), MiscUtil.convertChatColorToColor(team.getColor()));
                picker.setItem(item, teamStack);
                item++;
            }
        }
        item = size;
        if (!(Teams.getTeamByPlayer(player).isPresent() && Teams.getTeamByPlayer(player).get().isObserver())){
            ItemStack leave = Items.createItem(Material.LEATHER_BOOTS, 1, (short) 0, ChatColor.GREEN + "" + ChatColor.BOLD + new LocalizedChatMessage(ChatConstant.UI_TEAM_LEAVE).getMessage(player.getLocale()), Arrays.asList(ChatColor.DARK_PURPLE + new LocalizedChatMessage(ChatConstant.UI_TEAM_LEAVE_LORE).getMessage(player.getLocale())));
            picker.setItem(item - 1, leave);
        }
        for (ClassModule classModule : GameHandler.getGameHandler().getMatch().getModules().getModules(ClassModule.class)) {
            ItemStack classStack = Items.createItem(classModule.getIcon(), 1, (short) 0, ChatColor.GREEN + classModule.getName(), Arrays.asList(ChatColor.GOLD + classModule.getLongDescription()));
            ItemMeta classMeta = classStack.getItemMeta();
            if (classModule.equals(ClassModule.getClassByPlayer(player))) {
                classStack.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
            }
            classStack.setItemMeta(classMeta);
            picker.setItem(item, classStack);
            item++;
        }
        return picker;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();
        if (item != null) {
            Optional<TeamModule> team = Teams.getTeamByPlayer(player);
            if (team.isPresent() && team.get().isObserver() || !GameHandler.getGameHandler().getMatch().isRunning()) {
                if (event.getInventory().getName().equals(ChatColor.DARK_RED + new LocalizedChatMessage(ChatConstant.UI_TEAM_PICK).getMessage(player.getLocale()))) {
                    if (item.getType().equals(Material.CHAINMAIL_HELMET)) {
                        if (item.hasItemMeta()) {
                            if (item.getItemMeta().hasDisplayName()) {
                                if (item.getItemMeta().getDisplayName().equals(ChatColor.GRAY + "" + ChatColor.BOLD + new LocalizedChatMessage(ChatConstant.UI_TEAM_JOIN_AUTO).getMessage(player.getLocale()))) {
                                    event.setCancelled(true);
                                    player.closeInventory();
                                    player.playSound(player.getLocation(), Sound.CLICK, 1, 2);
                                    Bukkit.dispatchCommand(player, "join");
                                }
                            }
                        }
                    } else if (item.getType().equals(Material.LEATHER_BOOTS)) {
                        if (item.hasItemMeta()) {
                            if (item.getItemMeta().hasDisplayName()) {
                                if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "" + ChatColor.BOLD + new LocalizedChatMessage(ChatConstant.UI_TEAM_LEAVE).getMessage(player.getLocale()))) {
                                    event.setCancelled(true);
                                    player.closeInventory();
                                    player.playSound(player.getLocation(), Sound.CLICK, 1, 2);
                                    Bukkit.dispatchCommand(player, "leave");
                                }
                            }
                        }
                    } else if (item.getType().equals(Material.LEATHER_HELMET)) {
                        if (item.hasItemMeta()) {
                            if (item.getItemMeta().hasDisplayName()) {
                                if (Teams.getTeamByName(ChatColor.stripColor(item.getItemMeta().getDisplayName())) != null) {
                                    event.setCancelled(true);
                                    player.closeInventory();
                                    player.playSound(player.getLocation(), Sound.CLICK, 1, 2);
                                    Bukkit.dispatchCommand(player, "join " + ChatColor.stripColor(item.getItemMeta().getDisplayName()));
                                } else {
                                    event.setCancelled(true);
                                    player.closeInventory();
                                    player.playSound(player.getLocation(), Sound.CLICK, 1, 2);
                                    Bukkit.dispatchCommand(player, "class " + ChatColor.stripColor(item.getItemMeta().getDisplayName()));
                                }
                            }
                        }
                    } else {
                        if (item.hasItemMeta()) {
                            if (item.getItemMeta().hasDisplayName()) {
                                if (ClassModule.getClassByName(ChatColor.stripColor(item.getItemMeta().getDisplayName())) != null) {
                                    event.setCancelled(true);
                                    player.closeInventory();
                                    player.playSound(player.getLocation(), Sound.CLICK, 1, 2);
                                    Bukkit.dispatchCommand(player, "class " + ChatColor.stripColor(item.getItemMeta().getDisplayName()));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!GameHandler.getGameHandler().getMatch().getState().equals(MatchState.ENDED) && !(Blitz.matchIsBlitz() && GameHandler.getGameHandler().getMatch().getState().equals(MatchState.PLAYING)) &&
                (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) &&
                event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType().equals(Material.LEATHER_HELMET) &&
                event.getPlayer().getItemInHand().hasItemMeta() && event.getPlayer().getItemInHand().getItemMeta().hasDisplayName() &&
                (event.getPlayer().getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.GREEN + "" + ChatColor.BOLD + new LocalizedChatMessage(ChatConstant.UI_TEAM_SELECTION).getMessage(event.getPlayer().getLocale())) ||
                        ChatColor.stripColor(event.getPlayer().getItemInHand().getItemMeta().getDisplayName()).equals(new LocalizedChatMessage(ChatConstant.UI_TEAM_CLASS_SELECTION).getMessage(event.getPlayer().getLocale())))) {
                    event.getPlayer().openInventory(getTeamPicker(event.getPlayer()));
        }
    }

    @EventHandler
    public void onPlayerLocaleChange(PlayerLocaleChangeEvent event) {
        if (event.getOldLocale() == null) return;
        for (ItemStack item : event.getPlayer().getInventory().getContents()) {
            if (item != null) {
                if (item.getType().equals(Material.LEATHER_HELMET)) {
                    if (item.hasItemMeta()) {
                        if (item.getItemMeta().hasDisplayName()) {
                            ItemMeta meta = item.getItemMeta();

                            StringBuilder name = new StringBuilder();
                            name.append(ChatColor.GREEN);
                            name.append(ChatColor.BOLD);

                            StringBuilder lore = new StringBuilder();
                            lore.append(ChatColor.DARK_PURPLE);
                            lore.append(new LocalizedChatMessage(ChatConstant.UI_TEAM_JOIN_TIP).getMessage(event.getPlayer().getLocale()));

                            if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "" + ChatColor.BOLD + new LocalizedChatMessage(ChatConstant.UI_TEAM_SELECTION).getMessage(event.getOldLocale()))) {
                                name.append(new LocalizedChatMessage(ChatConstant.UI_TEAM_SELECTION).getMessage(event.getNewLocale()));
                                meta.setDisplayName(name.toString());
                                meta.setLore(Arrays.asList(lore.toString()));
                            } else if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "" + ChatColor.BOLD + new LocalizedChatMessage(ChatConstant.UI_TEAM_CLASS_SELECTION).getMessage(event.getOldLocale()))) {
                                name.append(new LocalizedChatMessage(ChatConstant.UI_TEAM_CLASS_SELECTION).getMessage(event.getNewLocale()));
                                meta.setDisplayName(name.toString());
                                meta.setLore(Arrays.asList(lore.toString()));
                            }
                            item.setItemMeta(meta);
                        }
                    }
                }
            }
        }
    }
}
