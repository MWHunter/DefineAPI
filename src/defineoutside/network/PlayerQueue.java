package defineoutside.network;

import defineoutside.main.MainAPI;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class PlayerQueue {
    Thread connectionToMain;

    static NetworkInfo networkInfo;

    CreateConnectionToMainframe createConnectionToMainframe = new CreateConnectionToMainframe();

    static List<QueueData> queueData = new ArrayList<>();

    public void ConnectToMainframe(String host) {
        Runnable connect = () -> {
            while (true) {
                ObjectOutputStream objectOutputStream = null;
                try {
                    networkInfo = createConnectionToMainframe.connectToMainframe(host, 27469, MainAPI.getInternalServerIdentifier() + "#Output");
                    MainAPI.getPlugin().getLogger().log(Level.INFO, "Connected to the central server");

                    objectOutputStream = new ObjectOutputStream(networkInfo.getDataOutputStream());
                } catch (IOException e) {
                    //e.printStackTrace();
                    // it restarts later
                }

                while (true) {
                    try {
                        // Other threads add to this list, must be synchronized for reliability
                        synchronized (queueData) {
                            for (QueueData queueData : queueData) {
                                System.out.println("sent something!");
                                objectOutputStream.writeObject(queueData);
                            }

                            objectOutputStream.reset();
                            queueData.clear();
                        }
                        Thread.sleep(50);

                    } catch (Exception e) {
                        e.printStackTrace();

                        MainAPI.getPlugin().getLogger().log(Level.WARNING, "Disconnected from the central server");

                        // Loop again in 10 seconds!
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException ex) {
                            // nothing
                        }

                        // Do another loop!
                        break;
                    }
                }
            }
        };

        Thread thread = new Thread(connect);
        thread.start();
    }

    public void addToQueue(QueueData queueData) {
        synchronized (PlayerQueue.queueData) {
            PlayerQueue.queueData.add(queueData);
        }
    }
}
