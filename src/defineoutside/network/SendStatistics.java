package defineoutside.network;

import defineoutside.creator.Game;
import defineoutside.games.GameLobby;
import defineoutside.main.GameManager;
import defineoutside.main.MainAPI;
import org.bukkit.Bukkit;

import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashSet;
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

                        NetworkInfo networkInfo = createConnectionToMainframe.connectToMainframe("192.168.1.196", 27469, MainAPI.getInternalServerIdentifier() + "#Statistics");

                        long usedMemory;
                        long maxMemory;

                        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
                        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();

                        while (true) {
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(networkInfo.getDataOutputStream());

                            usedMemory = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize() / 1000000;
                            maxMemory = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize() / 1000000;

                            HashSet<ServerInfo> gamesList = new HashSet<>();

                            for (UUID gameID : gm.getGamesHashMap().keySet()) {
                                String lobbyQueue = null;
                                if (gm.getGamesHashMap().get(gameID) instanceof GameLobby) {
                                    lobbyQueue = ((GameLobby) gm.getGamesHashMap().get(gameID)).getLobbyForGametype();
                                }


                                Game currentGame = gm.getGamesHashMap().get(gameID);

                                //Bukkit.getLogger().log(Level.WARNING, "Size of game " + currentGame.getUuidParticipating().size());

                                ServerInfo currentInfo = new ServerInfo(
                                        gameID.toString(),
                                        MainAPI.internalServerIdentifier,
                                        currentGame.getGameType(),
                                        lobbyQueue,
                                        currentGame.getUuidParticipating().size());

                                gamesList.add(currentInfo);
                            }

                            DataStatistics dataStatistics = new DataStatistics(sparkCPU.CpuMonitor.processLoad10SecAvg() * 100,
                                    sparkCPU.CpuMonitor.systemLoad10SecAvg() * 100, heapUsage.getUsed() / 1000000, heapUsage.getMax() / 1000000,
                                    maxMemory - usedMemory, maxMemory, gamesList);

                            objectOutputStream.writeObject(dataStatistics);

                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    } catch (Exception e) {
                        //e.printStackTrace();

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
