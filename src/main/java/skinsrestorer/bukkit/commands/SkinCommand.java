package skinsrestorer.bukkit.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import skinsrestorer.bukkit.SkinsRestorer;
import skinsrestorer.bukkit.storage.Locale;
import skinsrestorer.bukkit.storage.SkinStorage;
import skinsrestorer.bukkit.utils.MojangAPI;
import skinsrestorer.bukkit.utils.MojangAPI.SkinRequestException;
import skinsrestorer.shared.storage.CooldownStorage;

import java.util.concurrent.TimeUnit;

public class SkinCommand implements CommandExecutor {

    FileConfiguration config = SkinsRestorer.getInstance().getConfig();

    //Method called for the commands help.
    public void help(Player p) {
        p.sendMessage(Locale.SR_LINE.toString());
        p.sendMessage(Locale.HELP_PLAYER.toString().replace("%ver%", SkinsRestorer.getInstance().getVersion()));
        if (p.hasPermission("skinsrestorer.cmds"))
            p.sendMessage(Locale.HELP_SR.toString());
        p.sendMessage(Locale.SR_LINE.toString());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(Locale.TITLE.toString() + Locale.NOT_PLAYER);
            return true;
        }

        final Player p = (Player) sender;

        // Skin Help
        if (args.length == 0 || args.length > 2) {
            if (config.getBoolean("SkinWithoutPerm") == false) {
                if (p.hasPermission("skinsrestorer.playercmds")) {
                    help(p);
                } else {
                    p.sendMessage(Locale.TITLE.toString() + Locale.PLAYER_HAS_NO_PERMISSION);
                }
            }
            else {
                help(p);
            }
        }

        // Skin Clear and Skin (name)
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("clear")) {
                Object props = null;

                SkinStorage.removePlayerSkin(p.getName());
                props = SkinStorage.createProperty("textures", "", "");
                SkinsRestorer.getInstance().getFactory().applySkin(p, props);
                SkinsRestorer.getInstance().getFactory().updateSkin(p);
                p.sendMessage(Locale.TITLE.toString() + Locale.SKIN_CLEAR_SUCCESS);

                return true;
            } else {

                StringBuilder sb = new StringBuilder();
                sb.append(args[0]);

                final String skin = sb.toString();

                if (config.getBoolean("DisabledSkins.Enabled") == true)
                    if (!p.hasPermission("skinsrestorer.bypassdisabled")) {
                        for (String dskin : config.getStringList("DisabledSkins.Names"))
                            if (skin.equalsIgnoreCase(dskin)) {
                                p.sendMessage(Locale.TITLE.toString() + Locale.SKIN_DISABLED);
                                return true;
                            }
                    }

                if (p.hasPermission("skinsrestorer.bypasscooldown")) {

                } else {
                    if (CooldownStorage.hasCooldown(p.getName())) {
                        p.sendMessage(Locale.TITLE + Locale.SKIN_COOLDOWN_NEW.toString().replace("%s", "" + CooldownStorage.getCooldown(p.getName())));
                        return true;
                    }
                }

                CooldownStorage.resetCooldown(p.getName());
                CooldownStorage.setCooldown(p.getName(), config.getInt("SkinChangeCooldown"), TimeUnit.SECONDS);

                Bukkit.getScheduler().runTaskAsynchronously(SkinsRestorer.getInstance(), () -> {
                    try {
                        MojangAPI.getUUID(skin);

                        SkinStorage.setPlayerSkin(p.getName(), skin);
                        SkinsRestorer.getInstance().getFactory().applySkin(p,
                                SkinStorage.getOrCreateSkinForPlayer(p.getName()));
                        p.sendMessage(Locale.TITLE.toString() + Locale.SKIN_CHANGE_SUCCESS);
                        return;
                    } catch (SkinRequestException e) {
                        p.sendMessage(e.getReason());
                        return;
                    }
                });
                return true;
            }
        }

        // Skin Set
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("set")) {

                StringBuilder sb = new StringBuilder();
                sb.append(args[1]);

                final String skin = sb.toString();

                if (config.getBoolean("DisabledSkins.Enabled") == true)
                    if (!p.hasPermission("skinsrestorer.bypassdisabled") && !p.isOp()) {
                        for (String dskin : config.getStringList("DisabledSkins.Names"))
                            if (skin.equalsIgnoreCase(dskin)) {
                                p.sendMessage(Locale.TITLE.toString() + Locale.SKIN_DISABLED);
                                return true;
                            }
                    }

                if (p.hasPermission("skinsrestorer.bypasscooldown")) {

                } else {
                    if (CooldownStorage.hasCooldown(p.getName())) {
                        p.sendMessage(Locale.TITLE + Locale.SKIN_COOLDOWN_NEW.toString().replace("%s", "" + CooldownStorage.getCooldown(p.getName())));
                        return true;
                    }
                }

                CooldownStorage.resetCooldown(p.getName());
                CooldownStorage.setCooldown(p.getName(), config.getInt("SkinChangeCooldown"), TimeUnit.SECONDS);

                Bukkit.getScheduler().runTaskAsynchronously(SkinsRestorer.getInstance(), () -> {
                    try {
                        MojangAPI.getUUID(skin);

                        SkinStorage.setPlayerSkin(p.getName(), skin);
                        SkinsRestorer.getInstance().getFactory().applySkin(p, SkinStorage.getOrCreateSkinForPlayer(p.getName()));
                        p.sendMessage(Locale.TITLE.toString() +Locale.SKIN_CHANGE_SUCCESS);
                        return;
                    } catch (SkinRequestException e) {
                        p.sendMessage(e.getReason());
                        return;
                    }
                });
                return true;
            } else {
                help(p);
                return true;
            }
        }
        return true;
    }
}
