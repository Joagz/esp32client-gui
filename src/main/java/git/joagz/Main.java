package git.joagz;
import javax.swing.*;

import java.awt.event.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * ESPConnectionPacket
 */
class ESPConnectionPacket {
    char head;
    String code;
    char foot;

    public char[] serialize() {
        int s_code = this.code.length();
        if(s_code == 0) return null;

        int totalSize = s_code + 2;

        char[] arr = new char[totalSize];

        int j = 0;
        arr[j] = this.head;
        
        j++;
        for(int i = 0; i < s_code; i++)
        {
            arr[j] = this.code.charAt(i);
            j++;
        }

        arr[j] = this.foot;        

        System.out.println(arr);
        return arr;
    }

}

class ESPDataPacket extends ESPConnectionPacket{
    int len;
}

public class Main {

    private static final Character HEAD = 0x01;
    private static final Character FOOT = 0x04;
    private static final String ADDR = "192.168.100.205";
    private static final Integer PORT = 4321;

    private static boolean sendData(Socket socket, String data) {
        try {
        	OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
        	ESPDataPacket packet = new ESPDataPacket();
            packet.code = data;
            packet.len = data.length();
            packet.foot = FOOT;
            packet.head = HEAD;

            writer.write(packet.serialize());
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static Socket connectToServer() {
        Socket socket = null;
        try {
            socket = new Socket(ADDR, PORT);

            ESPConnectionPacket packet = new ESPConnectionPacket();
            packet.head = HEAD;
            packet.foot = FOOT;
            packet.code = "ABC123";
            
            OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
            
            writer.write(packet.serialize());
            writer.flush();

            // Verify socket connection before returning
            if (socket.isConnected()) {
                return socket;
            } else {
                throw new IOException("Failed to connect to server.");
            }
        } catch (IOException e) {
            // Handle connection errors gracefully
            e.printStackTrace();
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return null;
    }


    private static void makeWindow() {        
        Socket esp32 = connectToServer();
        
        JFrame f = new JFrame();
        
        JTextArea textArea = new JTextArea();
        textArea.setBounds(250/2, 100, 250, 100);
        textArea.setRows(6);
        
        ActionListener sendMsg = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {

            	String text = textArea.getText();

                if (text.isBlank() || text.isEmpty()) {
                    return;   
                }

                sendData(esp32, text);
            }
        };
        
        JButton btn = new JButton("Send Message");
        btn.setBounds((500-100)/2, 250, 100, 40);
        btn.addActionListener(sendMsg);        

        f.setResizable(false);

        f.add(btn);
        f.add(textArea);
        
        f.setSize(500, 500);
        f.setLayout(null);
        f.setVisible(true);
    }

    public static void main(String[] args) {
        makeWindow();
    }
}