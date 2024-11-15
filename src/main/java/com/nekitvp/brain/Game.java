package com.nekitvp.brain;

import java.util.Map;
import java.util.Objects;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;


public class Game extends Application {

    private static final long INITIAL_TIME_MS = 20000;
    private static final String INITIAL_TIME_STRING = "20.00";
    private static final String END_TIME_STRING = "00.00";

    private int redScore = 0;
    private int greenScore = 0;

    private Label redScoreLabel;
    private Label greenScoreLabel;
    private Label redScoreValue;
    private Label greenScoreValue;
    private Label timerLabel;
    private Label resultLabel;
    private Label answerLabel;
    private MediaPlayer endSound;
    private MediaPlayer falseStartSound;
    private MediaPlayer startSound;
    private MediaPlayer answerSound;
    private ImageView logoImageView;

    private long startTime;
    private long elapsedPauseTime = 0; // Время, на которое был остановлен таймер
    private boolean timerRunning = false;
    private boolean falseStart = false;
    private AnimationTimer timer;
    private Pane root;

    private VBox redVBox;
    private VBox greenVBox;
    private VBox layout;
    private HBox scoreLayout;
    private Scene scene;

    public static void main(String[] args) {
        launch(args);
    }

    private final Map<KeyCode, Runnable> keyActions = Map.of(
            KeyCode.C, this::startKey,
            KeyCode.N, this::newGameKey,
            KeyCode.R, () -> teamAction("КРАСНАЯ", "#ad3333"),
            KeyCode.G, () -> teamAction("ЗЕЛЕНАЯ", "#4f6b34"),
            KeyCode.DIGIT1, () -> updateScore("RED", -1),
            KeyCode.DIGIT4, () -> updateScore("RED", 1),
            KeyCode.DIGIT2, () -> updateScore("GREEN", -1),
            KeyCode.DIGIT5, () -> updateScore("GREEN", 1),
            KeyCode.DIGIT0, this::resetScores
    );

    @Override
    public void start(Stage stage) {

        startSound = loadSound("/sounds/start.wav");
        endSound = loadSound("/sounds/endTime.wav");
        answerSound = loadSound("/sounds/answer.wav");
        falseStartSound = loadSound("/sounds/falseStart.wav");

        logoImageView = createImageView("/logo.png", 450);

        timerLabel = createLabel(INITIAL_TIME_STRING, "400px", "Courier New");
        resultLabel = createLabel("", "90px", "Comic Sans MS");
        answerLabel = createLabel("", "50px", "Comic Sans MS");

        redScoreLabel = createLabel("КРАСНЫЕ", "25px", "Comic Sans MS");
        redScoreValue = createLabel("0", "100px", "Comic Sans MS");
        greenScoreLabel = createLabel("ЗЕЛЕНЫЕ", "25px", "Comic Sans MS");
        greenScoreValue = createLabel("0", "100px", "Comic Sans MS");

        redVBox = new VBox(redScoreLabel, redScoreValue);
        redVBox.setAlignment(Pos.CENTER);
        greenVBox = new VBox(greenScoreLabel, greenScoreValue);
        greenVBox.setAlignment(Pos.CENTER);

        // Размещаем элементы в горизонтальном контейнере
        scoreLayout = new HBox(30, redVBox, logoImageView, greenVBox);
        scoreLayout.setAlignment(Pos.CENTER);

        // Обновляем основной макет
        layout = new VBox(5, answerLabel, resultLabel, timerLabel, scoreLayout);
        layout.setAlignment(Pos.CENTER);

        root = new Pane();
        root.setStyle("-fx-background-color: black;");
        root.getChildren().add(layout);



        //stage.setFullScreen(true);

        scene = new Scene(root);
        scene.heightProperty()
                .addListener((obs, oldVal, newVal) -> resizeElements(scene.getWidth(), newVal.doubleValue()));
        scene.setOnKeyPressed(event -> keyActions.getOrDefault(event.getCode(), () -> {}).run());
        stage.setScene(scene);
        stage.setTitle("Brain Ring");
        stage.show();
    }

    private ImageView createImageView(String path, double fitWidth) {
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(fitWidth);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-effect: dropshadow(gaussian, white, 100, 0.8, 0, 0);");
        return imageView;
    }

    private Label createLabel(String text, String fontSize, String fontFamily) {
        Label label = new Label(text);
        label.setStyle(String.format("-fx-font-size: %s; -fx-font-family: '%s'; -fx-text-fill: white;", fontSize, fontFamily));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private MediaPlayer loadSound(String path) {
        return new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource(path)).toString()));
    }

    private void resizeElements(double width, double height) {
        logoImageView.setFitWidth(height * 0.43);

        setFontSize(timerLabel, height * 0.38);
        setFontSize(resultLabel, height * 0.086);
        setFontSize(answerLabel, height * 0.048);
        setFontSize(redScoreValue, height * 0.1);
        setFontSize(greenScoreValue, height * 0.1);
        setFontSize(redScoreLabel, height * 0.025);
        setFontSize(greenScoreLabel, height * 0.025);

        layout.setPrefSize(width, height);
    }

    private void setFontSize(Label label, double size) {
        label.setStyle(label.getStyle().replaceFirst("-fx-font-size: \\d+px", "-fx-font-size: " + size + "px"));
    }

    private void play(MediaPlayer player) {
        player.stop();
        player.seek(Duration.ZERO);
        player.play();
    }

    private void startKey() {
        if (!timerRunning && !falseStart && !isEndTime()) {
            play(startSound);
            startTimer();
            changeBackgroundColor("black");
            resultLabel.setText("");
            answerLabel.setText("");
        }
    }

    private void newGameKey() {
        resetTimer();
        changeBackgroundColor("black");
    }

    private void resetScores() {
        redScore = greenScore = 0;
        redScoreValue.setText("0");
        greenScoreValue.setText("0");
    }

    private void teamAction(String team, String color) {
        if (timerRunning) {
            play(answerSound);
            stopTimer(team + " команда");
            changeBackgroundColor(color);
            answerLabel.setText("Отвечает " + team + " команда");
            checkFirstSecond();
        } else if (!falseStart && isInitialTime()) {
            play(falseStartSound);
            falseStart = true;
            changeBackgroundColor(color);
            resultLabel.setText("ФАЛЬСТАРТ " + team + " КОМАНДЫ");
        }
    }

    private void updateScore(String team, int delta) {
        if ("RED".equals(team)) {
            redScore = Math.max(0, redScore + delta);
            redScoreValue.setText(String.valueOf(redScore));
        } else if ("GREEN".equals(team)) {
            greenScore = Math.max(0, greenScore + delta);
            greenScoreValue.setText(String.valueOf(greenScore));
        }
    }

    private void resetTimer() {
        startTime = 0;
        elapsedPauseTime = 0;
        timerLabel.setText(INITIAL_TIME_STRING);
        resultLabel.setText("");
        answerLabel.setText("");
        timerRunning = false;
        falseStart = false;
        if (timer != null) {
            timer.stop();
        }
    }

    private void startTimer() {
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        } else {
            startTime = System.currentTimeMillis() - elapsedPauseTime;
        }

        timerRunning = true;
        falseStart = false;

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsed = System.currentTimeMillis() - startTime;
                long remaining = INITIAL_TIME_MS - elapsed;

                if (remaining >= 0) {
                    long seconds = remaining / 1000;
                    long milliseconds = remaining % 1000;
                    timerLabel.setText(String.format("%02d.%02d", seconds, milliseconds / 10));
                } else {
                    stopTimer(null);
                }
            }
        };
        timer.start();
    }

    // Метод для остановки таймера
    private void stopTimer(String team) {
        timerRunning = false;
        elapsedPauseTime = System.currentTimeMillis() - startTime; // Запоминаем, сколько прошло времени

        if (timer != null) {
            timer.stop();
        }

        if (team == null) { // если таймер законончился
            resultLabel.setText("ВРЕМЯ ЗАКОНЧИЛОСЬ");
            answerLabel.setText("");
            changeBackgroundColor("red"); // Красим экран в красный цвет
            timerLabel.setText(END_TIME_STRING);
            play(endSound);
        }
    }

    private void changeBackgroundColor(String color) {
        root.setStyle("-fx-background-color: " + color + ";");
        scoreLayout.setStyle("-fx-background-color: " + color + ";");
        redVBox.setStyle("-fx-background-color: " + color + ";");
        greenVBox.setStyle("-fx-background-color: " + color + ";");
    }

    private boolean isInitialTime() {
        return INITIAL_TIME_STRING.equals(timerLabel.getText());
    }

    private boolean isEndTime() {
        return END_TIME_STRING.equals(timerLabel.getText());
    }

    private void checkFirstSecond() {
        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = INITIAL_TIME_MS - elapsed;

        if (remaining < INITIAL_TIME_MS && remaining >= (INITIAL_TIME_MS - 1010)) {
            resultLabel.setText("ОТВЕТ НА ПЕРВОЙ СЕКУНДЕ!");
        }
    }
}