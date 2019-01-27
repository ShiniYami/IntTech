import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FileTransferThread implements Runnable {

    private Socket socket;
    private ClientThread parent;
    private String filename;
    private String filetarget;
    private int port2;
    private byte[] bytes = new byte[1024];

    FileTransferThread(ClientThread parent, Socket socket, String filename, String filetarget) {
        super();
        this.socket = socket;
        this.parent = parent;
        this.filename = filename;
        this.filetarget = filetarget;
    }

    @Override
    public void run() {


        InputStream is = null;
        OutputStream os = null;
        boolean init = true;

        try {
            is = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            init = false;
        }
        port2 = parent.parent.getNewPort();
        parent.parent.sendPort(port2, filetarget, filename);
        Socket socket2 = null;
        try {
            ServerSocket serverSocket = new ServerSocket(port2);
            socket2 = serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
            init = false;
        }

        if (init) {
            try {
                os = socket2.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                init = false;
            }
        }

        if (init) {
            try {
                while (is.available() != 0) {
                    if (is.read(bytes) > 0) {
                        os.write(bytes);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        try {
            is.close();
            os.close();
            socket.close();
            socket2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
