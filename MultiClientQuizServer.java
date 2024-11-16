import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class MultiClientQuizServer {
    private static final int PORT = 1234;
    private static List<String> QUESTIONS = new ArrayList<>();
    private static List<String> ANSWERS = new ArrayList<>();

    public static void main(String[] args) {
        // 질문과 답변을 파일에서 읽어오기
        loadQuestionsAndAnswers();

        ExecutorService threadPool = Executors.newFixedThreadPool(10); // 최대 10개의 클라이언트를 처리

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is waiting for client connections...");
            while (true) {
                // 클라이언트 연결 대기
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected!");

                // 클라이언트 처리용 별도의 스레드를 생성하여 실행
                threadPool.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    // 클라이언트를 처리할 클래스
    static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                // 클라이언트에게 인사 메시지 전송
                out.println("Welcome to the Quiz Game! Please answer the following questions.");
                int score = 0;

                // 각 질문에 대해 클라이언트와 통신
                for (int i = 0; i < QUESTIONS.size(); i++) {
                    out.println("Question " + (i + 1) + ": " + QUESTIONS.get(i)); // 질문 전송

                    // 시간 제한 설정: 10초
                    String clientAnswer = null;
                    long startTime = System.currentTimeMillis();
                    while (System.currentTimeMillis() - startTime < 10000) { // 10초 대기
                        if (in.ready()) {
                            clientAnswer = in.readLine();
                            break;
                        }
                    }

                    // 시간 초과 처리
                    if (clientAnswer == null) {
                        out.println("Time's up! The correct answer is: " + ANSWERS.get(i));
                    } else {
                        // 정답 비교
                        if (clientAnswer.equalsIgnoreCase(ANSWERS.get(i))) {
                            out.println("Correct!");
                            score++;
                        } else {
                            out.println("Incorrect! The correct answer is: " + ANSWERS.get(i));
                        }
                    }
                }

                // 최종 점수 전송
                out.println("Your final score: " + score + "/" + QUESTIONS.size());
                out.println("Thank you for playing!");

                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            }
        }
    }

    // 파일에서 질문과 답변을 읽어오는 메서드
    private static void loadQuestionsAndAnswers() {
        String questionFilePath = "C:/Users/user/.vscode/intellij/test/src/questions.txt";
        String answerFilePath = "C:/Users/user/.vscode/intellij/test/src/answers.txt";

        try (BufferedReader questionReader = new BufferedReader(new FileReader(questionFilePath));
             BufferedReader answerReader = new BufferedReader(new FileReader(answerFilePath))) {

            String question;
            String answer;

            // 파일에서 질문과 답변을 읽어 리스트에 추가
            while ((question = questionReader.readLine()) != null && (answer = answerReader.readLine()) != null) {
                QUESTIONS.add(question);
                ANSWERS.add(answer);
            }
            System.out.println("Questions and answers loaded successfully!");
        } catch (IOException e) {
            System.out.println("Error loading questions and answers: " + e.getMessage());
            System.out.println("Check file paths: ");
            System.out.println("Questions file: " + questionFilePath);
            System.out.println("Answers file: " + answerFilePath);
        }
    }
}

