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
    private int port;
    private byte[] bytes;

    FileTransferThread(ClientThread parent, Socket socket, String filename, String filetarget, int port) {
        super();
        this.socket = socket;
        this.parent = parent;
        this.filename = filename;
        this.filetarget = filetarget;
        this.port = port;
    }

    @Override
    public void run() {
        parent.sendReturnMessage("SFILE " + filename + " " + port);

        InputStream is = null;
        OutputStream os = null;
        boolean init = true;

        try {
            is = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            init = false;
        }

        if(init) {
            try {
                bytes = is.readAllBytes();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        init = true;

        port = parent.parent.getNewPort();
        parent.parent.sendPort(port, filetarget, filename);
        Socket socket2 = null;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            socket2 = serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
            init = false;
        }

        if(init) {
            try {
                os = socket2.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                init = false;
            }
        }

        if(init){
            try {
                os.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
