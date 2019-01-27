import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class KeyTransferThread implements Runnable {


    private ClientThread parent;
    private String type;
    private byte[] targetKey;
    private byte[] publicKey = new byte[1024];
    private byte[] whisperMessage = new byte[128];
    private String messageTarget;

    KeyTransferThread(ClientThread parent, String type, byte[] targetKey, String messageTarget) {
        super();
        this.parent = parent;
        this.type = type;
        this.targetKey = targetKey;
        this.messageTarget = messageTarget;
    }

    KeyTransferThread(ClientThread parent, String type) {
        super();
        this.parent = parent;
        this.type = type;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        Socket socket;
        InputStream in = null;
        OutputStream out = null;
        int port = parent.parent.getNewPort();
        try {
            serverSocket = new ServerSocket(port);

        } catch (IOException e) {
            e.printStackTrace();
        }


        if (type.equals("send")) {
            parent.sendReturnMessage("KEYPR " + port);
            try {
                socket = serverSocket.accept();
                out = socket.getOutputStream();
                in = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                out.write(targetKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (in != null) {
                    int sPort = parent.parent.getNewPort();
                    parent.parent.sendWhisperPort(sPort, messageTarget);
                    ServerSocket sSocket = null;
                    try {
                        sSocket = new ServerSocket(sPort);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    Socket s = sSocket.accept();
                    out = s.getOutputStream();
                    while (in.available() == 0) {

                    }
                    while (in.available() != 0) {
                        if (in.read(whisperMessage) > 0) {
                            out.write(whisperMessage);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (type.equals("receive")) {
            if (serverSocket != null) {
                parent.sendReturnMessage("KEYPS " + port);
                try {
                    socket = serverSocket.accept();
                    in = socket.getInputStream();
                    while (in.available() == 0) {
                    }
                    while (in.available() != 0) {
                        if (in.read(publicKey) > 0) {
                            parent.setPublicKey(publicKey);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
