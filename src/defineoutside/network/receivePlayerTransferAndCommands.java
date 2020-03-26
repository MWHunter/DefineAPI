package defineoutside.network;

import defineoutside.main.MainAPI;
import org.bukkit.Bukkit;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Level;

public class receivePlayerTransferAndCommands {

    static NetworkInfo networkInfo;

    CreateConnectionToMainframe createConnectionToMainframe = new CreateConnectionToMainframe();

    public void ConnectToMainframe(String host) {
        Runnable connect = () -> {
            while (true) {
                ObjectInputStream objectInputStream = null;
                try {
                    networkInfo = createConnectionToMainframe.connectToMainframe(host, 27469, MainAPI.getInternalServerIdentifier() + "#Input");
                    MainAPI.getPlugin().getLogger().log(Level.INFO, "Connected to the central server #Input");
                    objectInputStream = new ObjectInputStream(networkInfo.getDataInputStream());
                } catch (IOException e) {
                    // it restarts later
                }

                while (true) {
                    try {
                        // Now listen for players being sent
                        try {
                            Object received = objectInputStream.readObject();

                            if (received instanceof ServerCommand) {
                                ServerCommand serverCommand = (ServerCommand) received;
                                //MainAPI.getPlugin().getLogger().log(Level.SEVERE, serverCommand.getCommand() + " has been received");

                                //MainAPI.getPlugin().getServer().getScheduler().runTask(MainAPI.getPlugin(), () -> MainAPI.getPlugin().getServer().dispatchCommand(Bukkit.getConsoleSender(), serverCommand.getCommand())
                                System.out.println(serverCommand.getCommand() + " has been received");

                                MainAPI.getPlugin().getServer().getScheduler().runTask(MainAPI.getPlugin(), () -> MainAPI.getPlugin().getServer().dispatchCommand(Bukkit.getConsoleSender(), serverCommand.getCommand())
                                );

                                //System.out.println("We got " + serverCommand.getCommand()););
                            }
                        } catch (EOFException e) {
                            MainAPI.getPlugin().getLogger().log(Level.INFO, "EOFException thrown receivePlayerTransferAndCommands:43");
                            Thread.sleep(50);
                        }

                    } catch (Exception e) {

                        //e.printStackTrace();
                        MainAPI.getPlugin().getLogger().log(Level.WARNING, "Disconnected from the central server");

                        // Loop again!
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException ex) {
                            // nothing
                        }

                        break;
                    }
                }
            }

        };

        Thread thread = new Thread(connect);
        thread.start();
    }
}
