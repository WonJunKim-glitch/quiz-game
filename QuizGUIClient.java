import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class QuizGUIClient {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 1234;

    private JFrame frame;
    private JTextArea textArea;
    private JTextField answerField;
    private JButton submitButton;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private List<String> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;

    public QuizGUIClient() {
        setupGUI();

        // 서버 연결 시도
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 서버에서 메시지를 수신하는 스레드 실행
            new Thread(this::listenToServer).start();

        } catch (IOException e) {
            showMessage("Error connecting to the server: " + e.getMessage());
            submitButton.setEnabled(false); // 서버 연결 실패 시 버튼 비활성화
        }
    }

    private void setupGUI() {
        frame = new JFrame("Quiz Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        // TextArea: 서버 메시지 출력
        textArea = new JTextArea(10, 30);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);

        // Answer field: 사용자 입력
        answerField = new JTextField(20);
        answerField.setPreferredSize(new Dimension(250, 30));

        // Submit button
        submitButton = new JButton("Submit Answer");
        submitButton.setEnabled(false); // 시작 시 비활성화
        submitButton.addActionListener(e -> handleSubmit());

        // Layout 설정
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(answerField, BorderLayout.SOUTH);
        panel.add(submitButton, BorderLayout.EAST);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void listenToServer() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                processServerMessage(message);
            }
        } catch (IOException e) {
            showMessage("Connection lost: " + e.getMessage());
        }
    }

    private void processServerMessage(String message) {
        // 서버 메시지가 질문인지 결과인지에 따라 처리
        if (message.startsWith("Question")) {
            questions.add(message); // 질문 저장
            currentQuestionIndex = questions.size() - 1;
            showMessage(message);
            enableSubmitButton();
        } else if (message.startsWith("Your final score")) {
            showMessage(message);
            disableSubmitButton();
        } else {
            // 기타 메시지 (정답 여부 등)
            showMessage(message);
        }
    }

    private void handleSubmit() {
        String answer = answerField.getText().trim();
        if (answer.isEmpty()) {
            showMessage("Please enter an answer.");
            return;
        }

        // 사용자 답변 서버로 전송
        out.println(answer);
        answerField.setText("");
        disableSubmitButton(); // 다음 질문 수신 대기
    }

    private void enableSubmitButton() {
        SwingUtilities.invokeLater(() -> submitButton.setEnabled(true));
    }

    private void disableSubmitButton() {
        SwingUtilities.invokeLater(() -> submitButton.setEnabled(false));
    }

    private void showMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(message + "\n");
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        new QuizGUIClient();
    }
}
