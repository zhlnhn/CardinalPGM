package in.twizmwaz.cardinal;

import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
import in.twizmwaz.cardinal.chat.LocaleHandler;
import in.twizmwaz.cardinal.command.*;
import in.twizmwaz.cardinal.permissions.Setting;
import in.twizmwaz.cardinal.permissions.SettingValue;
import in.twizmwaz.cardinal.rotation.exception.RotationLoadException;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jdom2.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class Cardinal extends JavaPlugin {

    private static Cardinal instance;
    private static GameHandler gameHandler;
    private static LocaleHandler localeHandler;
    private CommandsManager<CommandSender> commands;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        try {
            this.commands.execute(cmd.getName(), args, sender, sender);
        } catch (CommandPermissionsException e) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.sendMessage(ChatColor.RED + "Number expected, string received instead.");
            } else {
                sender.sendMessage(ChatColor.RED + "An error has occurred. See console.");
                e.printStackTrace();
            }
        } catch (CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
        return true;
    }

    private void setupCommands() {
        this.commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender sender, String perm) {
                return sender instanceof ConsoleCommandSender || sender.hasPermission(perm);
            }
        };
        CommandsManagerRegistration cmdRegister = new CommandsManagerRegistration(this, this.commands);
        cmdRegister.register(CycleCommand.class);
        cmdRegister.register(MapCommands.class);
        cmdRegister.register(MatchCommand.class);
        cmdRegister.register(StartAndEndCommand.class);
        cmdRegister.register(JoinCommand.class);
        cmdRegister.register(RotationCommands.class);
        cmdRegister.register(CancelCommand.class);
        cmdRegister.register(TeamCommand.class);
        cmdRegister.register(ModesCommand.class);
        cmdRegister.register(ClassCommands.class);
        cmdRegister.register(CardinalCommand.class);
        /*for (String cmd : AdminChat.commands) {
            getCommand(cmd).setExecutor(new AdminChat());
        }
        for (String cmd : GlobalChat.commands) {
            getCommand(cmd).setExecutor(new GlobalChat());
        }
        for (String cmd : TeamChannel.commands) {
            getCommand(cmd).setExecutor(new TeamChannel());
        }*/
    }

    public void onEnable() {
        FileConfiguration config = getConfig();
        if (!config.contains("deleteMaches")) {
            config.addDefault("deleteMaches", "true");
        }
        if (config.contains("deleteMaches")) {
            if (Boolean.parseBoolean(config.getString("deleteMatches"))) {
                Bukkit.getLogger().log(Level.INFO, "[CardianlPGM] Deleting match files, this can be disabled via the configuration");
                File matches = new File("matches/");
                try {
                    FileUtils.deleteDirectory(matches);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        instance = this;
        try {
            localeHandler = new LocaleHandler(this);
        } catch (IOException | JDOMException e) {
            e.printStackTrace();
            this.setEnabled(false);
            return;
        }
        if (!config.contains("settings")) {
//            config.addDefault("settings", Arrays.asList("deathmessages"));
        }
        config.options().copyDefaults(true);
        saveDefaultConfig();

        if (config.contains("settings")) {
            for (String settingName : config.getStringList("settings")) {
                List<String> names = new ArrayList<>();
                Set<SettingValue> values = new HashSet<>();
                names.add(settingName.trim());
                if (config.contains("setting." + settingName + ".aliases")) {
                    for (String alias : config.getStringList("setting." + settingName + ".aliases")) {
                        names.add(alias.trim());
                    }
                }
                if (config.contains("setting." + settingName + ".values")) {
                    for (String valueName : config.getStringList("setting." + settingName + ".values")) {
                        if (valueName.contains("[default]")) values.add(new SettingValue(valueName.replaceAll("[default]", "").trim(), true));
                        else values.add(new SettingValue(valueName.trim(), false));
                    }
                }
                new Setting(names, values);
            }
        }
        try {
            gameHandler = new GameHandler(this);
        } catch (RotationLoadException e) {
            Bukkit.getLogger().log(Level.SEVERE, "CardinalPGM failed to initialize because of an invalid rotation configuration.");
            setEnabled(false);
            return;
        }
        setupCommands();
    }

    public GameHandler getGameHandler() {
        return gameHandler;
    }

    public static LocaleHandler getLocaleHandler() {
        return localeHandler;
    }

    public JavaPlugin getPlugin() {
        return this;
    }

    public static Cardinal getInstance() {
        return instance;
    }
}
