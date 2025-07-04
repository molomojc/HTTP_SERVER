package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Server {
  
	
	public void test()   {
	
		try {
			ServerSocket serverSocket = new ServerSocket(8080);
			while (true) {
             Socket sClient = serverSocket.accept();
                if(sClient.isConnected()) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(sClient.getInputStream()));

                    StringBuilder requestBuilder = new StringBuilder();
                    String line;
                    while (!(line = br.readLine()).isBlank()) {
                        requestBuilder.append(line + "\r\n");
                    }

                
                    String request = requestBuilder.toString();
                    String[] requestsLines = request.split("\r\n");
                    String[] requestLine = requestsLines[0].split(" ");
                    String method = requestLine[0];
                    String path = requestLine[1];
                    String version = requestLine[2];
                    String host = requestsLines[1].split(" ")[1];

                    List<String> headers = new ArrayList<>();
                    for (int h = 2; h < requestsLines.length; h++) {
                        String header = requestsLines[h];
                        headers.add(header);
                    }

                    String accessLog = String.format("Client %s, method %s, path %s, version %s, host %s, headers %s",
                    		sClient.toString(), method, path, version, host, headers.toString());
                    System.out.println(accessLog);
                    
                    Path filePath = getFilePath(path);
                    if (Files.exists(filePath)) {
                        // file exist
                        String contentType = guessContentType(filePath);
                        sendResponse(sClient, "200 OK", contentType, Files.readAllBytes(filePath));
                    } else {
                        // 404
                        byte[] notFoundContent = "data/Durban.html".getBytes();
                        sendResponse(sClient, "404 Not Found", "text/html", notFoundContent);
                    }
                }
            }
        
		} catch (IOException e) {
			
			e.printStackTrace();
		}
           
	}
	private static void sendResponse(Socket client, String status, String contentType, byte[] content) throws IOException {
        OutputStream clientOutput = client.getOutputStream();
        clientOutput.write(("HTTP/1.1 \r\n" + status).getBytes());
        clientOutput.write(("ContentType: " + contentType + "\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(content);
        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        client.close();
    }

    private static Path getFilePath(String path) {
        if ("/".equals(path)) {
            path = "/Durban.html";
        }

        return Paths.get("/data", path);
    }

    private static String guessContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }

}

