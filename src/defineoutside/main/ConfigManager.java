package defineoutside.main;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class ConfigManager {
    FileConfiguration mapsConfig;
    List<String> mapsList;

    static HashMap<String, List<String>> gamemodeToMaps = new HashMap<>();
    static HashMap<String, List<String>> gamemodeToKits = new HashMap<>();
    static HashMap<String, List<String>> gamemodeToKitSelectors = new HashMap<>();

    static HashMap<String, ItemStack[]> listOfKits = new HashMap<>();

    static HashMap<String, List<List<Location>>> listOfSpawns = new HashMap<>();
    static HashMap<String, List<List<String>>> listOfKitSelectors = new HashMap<>();
    static HashMap<String, HashMap<String, List<Location>>> listOfSpecial = new HashMap<>();

    static FileConfiguration mainConfig;

    Random rand = new Random();

    public void loadConfigs() {
        try {
            // Loads all the map names that should be played in each gamemode
            for (Iterator<File> it = FileUtils.iterateFiles(new File(MainAPI.getPlugin().getDataFolder() + File.separator + "configs"), null, true); it.hasNext(); ) {
                File file = it.next();
                File parentFile = new File(file.getParent());

                mapsConfig = YamlConfiguration.loadConfiguration(file);

                if (file.getName().equalsIgnoreCase("arenas.yml")) {

                    mapsList = mapsConfig.getStringList("maps");

                    gamemodeToMaps.put(parentFile.getName(), mapsList);

                    for (String map : mapsList) {
                        FileConfiguration mapConfig;
                        File testwarMap = new File(MainAPI.getPlugin().getDataFolder() + File.separator + "arenas" + File.separator + map + File.separator + "info.yml");

                        if (testwarMap.exists()) {
                            mapConfig = YamlConfiguration.loadConfiguration(testwarMap);
                            List<List<Location>> teamsPositions = new ArrayList<>();
                            List<List<String>> teamsKits = new ArrayList<>();

                            for (String teamsList : mapConfig.getStringList(("teams"))) {
                                List<Location> spawns = new ArrayList<>();
                                List<String> teamKit = new ArrayList<>();

                                for (String parse : mapConfig.getStringList("spawnlocations." + teamsList)) {

                                    String x = parse.substring(0, parse.indexOf(","));

                                    String y = parse.substring(parse.indexOf(",") + 1, parse.lastIndexOf(","));

                                    String z = parse.substring(parse.lastIndexOf(",") + 1);

                                    spawns.add(new Location(Bukkit.getWorld("World"), Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z)));
                                }

                                for (String kitSelectors : mapConfig.getStringList("kitselector." + teamsList)) {
                                    teamKit.add(kitSelectors);
                                }

                                teamsKits.add(teamKit);
                                teamsPositions.add(spawns);
                            }

                            // Special positions such as bombsites
                            for (String specialFunction : mapConfig.getStringList("specialpositions")) {

                                MainAPI.getPlugin().getLogger().log(Level.INFO, "specialFunction is " + specialFunction);
                                List<Location> specialPositions = new ArrayList<Location>();

                                for (String location : mapConfig.getStringList(specialFunction)) {
                                    MainAPI.getPlugin().getLogger().log(Level.INFO, "location being parsed is " + location);

                                    String x = location.substring(0, location.indexOf(","));

                                    String y = location.substring(location.indexOf(",") + 1, location.lastIndexOf(","));

                                    String z = location.substring(location.lastIndexOf(",") + 1);

                                    specialPositions.add(new Location(Bukkit.getWorld("World"), Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z)));
                                }

                                if (!listOfSpecial.containsKey(map)) {
                                    listOfSpecial.put(map, new HashMap<>());
                                }

                                listOfSpecial.get(map).put(specialFunction, specialPositions);
                            }

                            listOfSpawns.put(map, teamsPositions);
                            listOfKitSelectors.put(map, teamsKits);

                        } else {
                            List<List<Location>> teamsPositions = new ArrayList<>();
                            List<Location> spawns = new ArrayList<>();

                            spawns.add(new Location(Bukkit.getWorld("world"), 0, 70, 0));
                            teamsPositions.add(spawns);

                            listOfSpawns.put(file.getName(), teamsPositions);
                            MainAPI.getPlugin().getLogger().log(Level.WARNING, "We have initialized a world without a config.  Assuming world world and spawn location 0, 70, 0");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // Loads all the kit names that should be played in each gamemode
            for (Iterator<File> it = FileUtils.iterateFiles(new File(MainAPI.getPlugin().getDataFolder() + File.separator + "kits"), null, true); it.hasNext(); ) {
                File file = it.next();

                mapsConfig = YamlConfiguration.loadConfiguration(file);

                if (file.getName().equalsIgnoreCase("kits.yml")) {
                    mapsList = mapsConfig.getStringList("kits");
                    gamemodeToKits.put(new File(file.getParent()).getName(), mapsList);

                    mapsList = mapsConfig.getStringList("kitselector");
                    gamemodeToKitSelectors.put(new File(file.getParent()).getName(), mapsList);
                } else {
                    MainAPI.getPlugin().getLogger().log(Level.INFO, "Parsing kit file " + file.getName());
                    listOfKits.put(file.getName(), parseKitConfig(mapsConfig));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            File file = new File(MainAPI.getPlugin().getDataFolder() + File.separator + "main.yml");
            mainConfig = YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Location> getSpecialPositionsList(String map, String specialFunction) {
        return new ArrayList<>(listOfSpecial.get(map).get(specialFunction));
    }

    public File getRandomMap(String gameType) {
        List<String> availableMaps = gamemodeToMaps.get(gameType);

        String selectedMap = availableMaps.get(rand.nextInt(availableMaps.size()));

        return new File(MainAPI.getPlugin().getDataFolder() + File.separator + "arenas" + File.separator + selectedMap);
    }

    public String getRandomKitName(String gameType) {
        try {
            List<String> availableMaps = gamemodeToKits.get(gameType);

            return availableMaps.get(rand.nextInt(availableMaps.size()));
        } catch (Exception e) {
            e.printStackTrace();
            return "blank";
        }
    }

    public ItemStack[] getRandomKit(String gameType) {
        try {
            List<String> availableMaps = gamemodeToKits.get(gameType);

            return listOfKits.get(availableMaps.get(rand.nextInt(availableMaps.size())));
        } catch (Exception e) {
            MainAPI.getPlugin().getLogger().log(Level.WARNING, gameType + " has no kits");
            return new ItemStack[41];
        }
    }

    public String getRandomKitSelectorName(String gameType) {
        try {
            List<String> availableMaps = gamemodeToKitSelectors.get(gameType);

            return availableMaps.get(rand.nextInt(availableMaps.size()));
        } catch (Exception e) {
            return null;
        }
    }

    public ItemStack[] getRandomKitSelector(String gameType, String map, int teamID) {
        try {
            List<String> availableMaps = gamemodeToKitSelectors.get(gameType);

            if (listOfKitSelectors.containsKey(map)) {
                availableMaps = listOfKitSelectors.get(map).get(teamID);
            }

            return listOfKits.get(availableMaps.get(rand.nextInt(availableMaps.size())));
        } catch (Exception e) {

            return new ItemStack[41];
        }
    }

    public boolean containsKit(String gameType, String map, int teamID, String kit) {
        try {
            List<String> availableMaps = gamemodeToKitSelectors.get(gameType);

            if (listOfKitSelectors.containsKey(map)) {
                availableMaps = listOfKitSelectors.get(map).get(teamID);
            }

            return availableMaps.contains(kit);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public File getMap(String gameType, String map) {
        List<String> availableMaps = gamemodeToKits.get(gameType);

        return new File(MainAPI.getPlugin().getDataFolder() + File.separator + "arenas" + File.separator + map);
    }

    public ItemStack[] getKit(String kit) {

        return listOfKits.get(kit);
    }

    public List<List<Location>> getListOfSpawns(String world) {
        if (listOfSpawns.get(world) == null) {
            List<List<Location>> totalLocations = new ArrayList<>();
            List<Location> singleLocation = new ArrayList<>();
            singleLocation.add(new Location(Bukkit.getWorld("world"), 0, 70, 0));
            totalLocations.add(singleLocation);

            return totalLocations;
        }
        return listOfSpawns.get(world);
    }

    public ItemStack[] parseKitConfig(FileConfiguration kitConfig) {
        ItemTag itemTag = new ItemTag();
        ItemStack[] itemStack = new ItemStack[41];

        for (int i = 0; i < 41; i++) {
            ItemStack itemStackParsed = new ItemStack(Material.AIR, 1);

            int amountInt = kitConfig.getInt("items." + i + ".amount");
            String materialString = kitConfig.getString("items." + i + ".item");
            String name = kitConfig.getString("items." + i + ".name");
            List<String> enchants = kitConfig.getStringList("items." + i + ".enchants");
            String potionType = kitConfig.getString("items." + i + ".effect");
            boolean extended = kitConfig.getBoolean("items." + i + ".extended");
            boolean upgraded = kitConfig.getBoolean("items." + i + ".upgraded");
            List<String> loreList = kitConfig.getStringList("items." + i + ".lore");
            List<String> customTags = kitConfig.getStringList("items." + i + ".tags");

            // Set special item stuff
            if (materialString != null) {
                try {
                    itemStackParsed.setType(Material.matchMaterial(materialString.toUpperCase()));

                    if (potionType != null) {
                        potionType = potionType.toUpperCase();

                        PotionMeta potionMeta = (PotionMeta) itemStackParsed.getItemMeta();

                        // TODO: Error correcting/Idiot proof

                        potionMeta.setBasePotionData(new PotionData(PotionType.valueOf(potionType), extended, upgraded));

                        itemStackParsed.setItemMeta(potionMeta);
                    }

                    ItemMeta itemMeta = itemStackParsed.getItemMeta();

                    if (enchants != null) {
                        for (String enchant : enchants) {
                            Enchantment enchantType = Enchantment.getByKey(NamespacedKey.minecraft(enchant.substring(0, enchant.indexOf(" "))));
                            int enchantLevel = Integer.parseInt(enchant.substring(enchant.indexOf(" ") + 1));

                            itemMeta.addEnchant(enchantType, enchantLevel, true);
                        }
                    }

                    if (loreList != null) {
                        for (int j = 0; j < loreList.size(); j++) {
                            loreList.set(j, ChatColor.RESET + "" + ChatColor.GRAY + loreList.get(j));
                        }
                        itemMeta.setLore(loreList);
                    }

                    if (name != null) {
                        itemMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GREEN + name);
                    }

                    itemStackParsed.setItemMeta(itemMeta);

                    if (customTags != null) {
                        for (String customTag : customTags) {
                            // For example, this -
                            // Queue: Testwars - will result in -
                            // setTag(itemStackParsed, "Queue", "Testwars");
                            itemStackParsed = itemTag.setTag(itemStackParsed, customTag.substring(0, customTag.indexOf(":")), customTag.substring(customTag.indexOf(":") + 2));
                        }
                    }

                    // Zero means not specified
                    if (amountInt != 0) {
                        itemStackParsed.setAmount(amountInt);
                    }
                } catch (Exception e) {
                    MainAPI.getPlugin().getLogger().log(Level.WARNING, "The above file has an error in slot " + i);
                    e.printStackTrace();
                }
            }
            itemStack[i] = itemStackParsed;
        }
        return itemStack;
    }

    public FileConfiguration getMainConfig() {
        return mainConfig;
    }
}
