package defineoutside.network;

import defineoutside.creator.Game;
import defineoutside.main.GameManager;
import defineoutside.main.MainAPI;
import org.bukkit.Bukkit;

import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class SendStatistics {
    Thread connectionToMain;

    public void startNetworkMonitoring(String host) {
        try {
            Runnable myRunnable = () -> {
                while (true) {
                    try {
                        CreateConnectionToMainframe createConnectionToMainframe = new CreateConnectionToMainframe();

                        GameManager gm = new GameManager();

                        NetworkInfo networkInfo = createConnectionToMainframe.connectToMainframe("192.168.1.196", 27469, MainAPI.getInternalServerIdentifier());

                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(networkInfo.getDataOutputStream());

                        long usedMemory;
                        long maxMemory;

                        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
                        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();

                        while (true) {
                            usedMemory = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize() / 1000000;
                            maxMemory = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize() / 1000000;

                            HashMap<UUID, List<UUID>> playersAndGames = new HashMap<>();
                            HashMap<UUID, String> gamesAndGametypes = new HashMap<>();

                            for (UUID gameID : gm.getGamesHashMap().keySet()) {
                                Game game = gm.getGamesHashMap().get(gameID);
                                playersAndGames.put(gameID, game.getUuidParticipating());
                                gamesAndGametypes.put(gameID, game.getGameType());
                            }

                            DataStatistics dataStatistics = new DataStatistics(sparkCPU.CpuMonitor.processLoad10SecAvg() * 100,
                                    sparkCPU.CpuMonitor.systemLoad10SecAvg() * 100, heapUsage.getUsed() / 1000000, heapUsage.getMax() / 1000000,
                                    maxMemory - usedMemory, maxMemory, playersAndGames, gamesAndGametypes);

                            objectOutputStream.writeObject(dataStatistics);

                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    } catch (Exception e) {
                        Bukkit.getLogger().log(Level.WARNING, "Statistics unable to connect to the game manager with host " + host + " and port 27469.  Restarting connection in 10 seconds!");

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException ex) {
                            //
                        }
                    }
                }
            };

            connectionToMain = new Thread(myRunnable);
            connectionToMain.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
