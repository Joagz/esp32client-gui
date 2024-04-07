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

class ESPDataPacket {
    char head;
    int len;
    String data;
    char foot;

    public char[] serialize() {
        if(this.len <= 0) return null;

        int totalSize = this.len + 2 + 4;

        char[] arr = new char[totalSize];

        int j = 0;
        arr[j] = this.head;
        
        j++;

        int number = this.len;
        arr[j++] = (char) ((number >> 24) & 0xFF);
        arr[j++] = (char) ((number >> 16) & 0xFF);
        arr[j++] = (char) ((number >> 8) & 0xFF);
        arr[j++] = (char) (number & 0xFF);

        for(int i = 0; i < this.len; i++)
        {
            arr[j] = this.data.charAt(i);
            j++;
        }

        arr[j] = this.foot;

        System.out.println(arr);
        return arr;
    }


}

public class Main {

    private static final Character HEAD = 0xEE;
    private static final Character FOOT = 0xFF;
    private static final String ADDR = "";
    private static final Integer PORT = 0;

    private static boolean sendData(Socket socket, String data) {
        try (OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream())) {
            ESPDataPacket packet = new ESPDataPacket();
            packet.data = data;
            packet.len = data.length();
            packet.foot = FOOT;
            packet.head = HEAD;

            writer.write(packet.serialize());
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static Socket connectToServer() {
        try {
            Socket socket = new Socket(ADDR, PORT);

            ESPConnectionPacket packet = new ESPConnectionPacket();
            packet.head = HEAD;
            packet.foot = FOOT;
            packet.code = "MyESP32Code";

            OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
            
            writer.write(packet.serialize());
            writer.flush();

            InputStreamReader reader = new InputStreamReader(socket.getInputStream());            
            char[] buffer = new char[128];
            int bytes_read = 0;
            
            StringBuilder sb = new StringBuilder();

            while((bytes_read = reader.read(buffer, bytes_read, 128)) > 0){
                sb.append(buffer);
            }

            String read = sb.toString();

            System.err.println(read);

            writer.close();
            reader.close();
            
            return socket;
        } catch (IOException e) {
            e.printStackTrace();
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

                if(text.isBlank() || text.isEmpty())
                {
                    return;   
                }

                System.out.println(text);

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