package defineoutside.network;

import defineoutside.main.MainAPI;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;

import static defineoutside.main.MainAPI.internalServerIdentifier;
import static org.bukkit.Bukkit.getServer;

public class RegisterServer {
    public static void sendHostname() {
        MainAPI.getPlugin().getServer().getScheduler().runTaskAsynchronously(MainAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        //MainAPI.getPlugin().getLogger().log(Level.WARNING, "Sending this server to the mainframe");

                        Socket s = new Socket(MainAPI.hostName, 27469);
                        DataInputStream dis = new DataInputStream(s.getInputStream());

                        String msg = dis.readUTF();

                        if (msg.equals("CRva7SfCPaiBrS7cZh6bNXuupO0qnfOYrgOCZceQFWcFjbiksI1mgcUyhO31AZtz10k6Kj8Ji5XQ0pMObC2BXEKg2XptcVjFdGf")) {
                            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                            dos.writeUTF("4v2NZ8RTar54k4PoYEsnjxpL0IObNMgediJQP65QwUwmm9hBw1hQCJvxcSo6tIDwiHY2RkYzmVMWIpN8Oe4rrmPxVum2PBwBnL6");
                            dos.writeUTF(internalServerIdentifier + "#Register");
                        }

                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(s.getOutputStream());
                        objectOutputStream.writeObject(new AddNonSubServer(true, MainAPI.lobbyType.equalsIgnoreCase("lobby"),
                                internalServerIdentifier, InetAddress.getByName(getServer().getIp()), getServer().getPort()));

                        s.close();

                        // Resend every minute to handle bungeecord sometimes going down
                        Thread.sleep(60 * 1000);

                    } catch (Exception e) {
                        MainAPI.getPlugin().getLogger().log(Level.WARNING, "Registration disconnected");

                        try {
                            Thread.sleep(60 * 1000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        //e.printStackTrace();
                    }
                }
            }
        });
    }
}
