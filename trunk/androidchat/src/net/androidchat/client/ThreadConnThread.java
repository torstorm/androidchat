package net.androidchat.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class ThreadConnThread implements Runnable {

    private String defchan = "#android";

    private Socket socket;
    private String server;
    private String nick;

    public ThreadConnThread(String serv, String ni, Socket s)
    {
        socket = s;
        server = serv;
        nick = ni;
    }

    public void run()
    {
        try {
            socket = new Socket(server, 6667);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ServiceIRCService.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ServiceIRCService.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ServiceIRCService.state = 1; // logging in

        try {
            ServiceIRCService.writer.write("NICK " + nick + "\r\n");
            ServiceIRCService.writer.write("USER " + nick + " 8 * : Android Chat Client\r\n");
            ServiceIRCService.writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String line = null;
        try {
            while ((line = ServiceIRCService.reader.readLine()) != null) {
                if (line.indexOf("004") >= 0) {
                	ServiceIRCService.state = 2;// logged in
                    break;
                } else if (line.indexOf("433") >= 0) {
                	ServiceIRCService.state = -1;// nick in use
                    ServiceIRCService.writer.write("NICK " + nick + "-\r\n");
                    break;
                } else if (line.startsWith("PING ")) {
                    ServiceIRCService.writer.write("PONG " + line.substring(5) + "\r\n");
                    ServiceIRCService.writer.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ServiceIRCService.state = 3;// autojoin
        try {
            ServiceIRCService.writer.write("JOIN " + defchan + "\r\n");
            ServiceIRCService.writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ServiceIRCService.state = 10; // connected and handling
        try {
            while ((line = ServiceIRCService.reader.readLine()) != null) {
                if (line.startsWith("PING ")) {
                    ServiceIRCService.writer.write("PONG " + line.substring(5) + "\r\n");
                    ServiceIRCService.writer.flush();
                } else {
                    // handle incoming text here
                    ServiceIRCService.GetLine(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
