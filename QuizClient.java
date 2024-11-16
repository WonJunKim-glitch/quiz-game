import java.io.*;
import java.net.*;

public class QuizClient {
    private static final String SERVER_ADDRESS = "localhost"; // 서버 주소 (기본 localhost)
    private static final int SERVER_PORT = 1234; // 서버 포트 번호

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            String serverMessage;
            while (true) {
                // 서버 메시지 수신
                serverMessage = in.readLine();
                if (serverMessage == null) {
                    System.out.println("Server connection closed.");
                    break;
                }

                // 서버 메시지 출력
                System.out.println("Server: " + serverMessage);

                // 종료 메시지 확인
                if (serverMessage.startsWith("Your final score:") || serverMessage.startsWith("Thank you")) {
                    break;
                }

                // 사용자 입력
                if (serverMessage.startsWith("Question")) { // 질문이 전송된 경우에만 답변 요청
                    System.out.print("Your answer: ");
                    String answer = userInput.readLine();
                    out.println(answer);
                }
            }
        } catch (IOException e) {
            System.out.println("Error connecting to the server: " + e.getMessage());
        }
    }
}
