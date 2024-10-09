package com.nekitvp.brain;

import java.util.Objects;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;


public class HelloApplication extends Application {

    private static final long INITIAL_TIME_MS = 20000;
    private static final String INITIAL_TIME_STRING = "20.00";

    private Label timerLabel;
    private Label resultLabel;
    private Label answerLabel;

    private MediaPlayer endSound;
    private MediaPlayer falseStartSound;
    private MediaPlayer startSound;
    private MediaPlayer answerSound;

    private long startTime;
    private long elapsedPauseTime = 0; // Время, на которое был остановлен таймер
    private boolean timerRunning = false;
    private boolean falseStart = false;
    private AnimationTimer timer;
    private Pane root;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        // Загружаем звуки
        startSound = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("/sounds/start.wav")).toString()));
        endSound = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("/sounds/endTime.wav")).toString()));
        answerSound = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("/sounds/answer.wav")).toString()));
        falseStartSound = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("/sounds/falseStart.wav")).toString()));


        Image logoImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
        var logoImageView = new ImageView(logoImage);
        logoImageView.setFitWidth(450);
        logoImageView.setPreserveRatio(true);
        logoImageView.setStyle("-fx-effect: dropshadow(gaussian, white, 100, 0.8, 0, 0);");

        timerLabel = new Label(INITIAL_TIME_STRING);
        timerLabel.setStyle("-fx-font-size: 400px; -fx-font-family: 'Courier New'; -fx-text-fill: white;");
        timerLabel.setMaxWidth(Double.MAX_VALUE);
        timerLabel.setAlignment(Pos.CENTER);

        resultLabel = new Label("");
        resultLabel.setStyle("-fx-font-family: 'Comic Sans MS'; -fx-font-size: 90px; -fx-text-fill: white;");
        resultLabel.setMaxWidth(Double.MAX_VALUE);
        resultLabel.setAlignment(Pos.CENTER);

        answerLabel = new Label("");
        answerLabel.setStyle("-fx-font-family: 'Comic Sans MS'; -fx-font-size: 50px; -fx-text-fill: white;");
        answerLabel.setMaxWidth(Double.MAX_VALUE);
        answerLabel.setAlignment(Pos.CENTER);

        VBox layout = new VBox(5, answerLabel, resultLabel, timerLabel, logoImageView);
        layout.setAlignment(Pos.CENTER);

        root = new Pane();
        root.setStyle("-fx-background-color: black;");

        root.getChildren().add(layout);

        Scene scene = getScene();

        stage.fullScreenProperty().addListener((obs, wasFullScreen, isFullScreen) -> {
            if (Boolean.TRUE.equals(isFullScreen)) {
                layout.setPrefWidth(stage.getWidth());
                layout.setPrefHeight(stage.getHeight());
                layout.setAlignment(Pos.CENTER);
            }
        });

        stage.setScene(scene);
        stage.setTitle("Brain Ring");
        stage.show();
    }

    private void play(MediaPlayer player) {
        player.stop();
        player.seek(Duration.ZERO);
        player.play();
    }

    private Scene getScene() {
        Scene scene = new Scene(root);
        scene.setOnKeyPressed(e -> {

            String key = e.getText().toLowerCase();

            if (isResetKey(key)) { // кнопка ведущего "НОВАЯ ИГРА"
                resetTimer();
                changeBackgroundColor("black");

            } else if (!timerRunning && !falseStart && isStartKey(key)){ // кнопка ведущего "ЗАПУСТИТЬ"
                play(startSound);
                startTimer();
                changeBackgroundColor("black");
                resultLabel.setText("");
                answerLabel.setText("");

            } else if (timerRunning && isRedTeamKey(key)) { // кнопка КРАСНОЙ команды
                play(answerSound);
                stopTimer("КРАСНАЯ команда");
                changeBackgroundColor("#ad3333");
                checkFirstSecond();
                answerLabel.setText("Отвечает КРАСНАЯ команда");

            } else if (timerRunning && isGreenTeamKey(key)) { // кнопка ЗЕЛЕНОЙ команды
                play(answerSound);
                stopTimer("ЗЕЛЕНАЯ команда");
                changeBackgroundColor("#4f6b34");
                checkFirstSecond();
                answerLabel.setText("Отвечает ЗЕЛЕНАЯ команда");

            } else if (!timerRunning && !falseStart && isInitialTime() && isRedTeamKey(key)) { // фальтстарт КРАСНОЙ команды
                play(falseStartSound);
                falseStart = true;
                changeBackgroundColor("#ad3333");
                resultLabel.setText("ФАЛЬСТАРТ КРАСНОЙ КОМАНДЫ");

            } else if (!timerRunning && !falseStart && isInitialTime() && isGreenTeamKey(key)) { // фальтстарт ЗЕЛЕНОЙ команды
                play(falseStartSound);
                falseStart = true;
                changeBackgroundColor("#4f6b34");
                resultLabel.setText("ФАЛЬСТАРТ ЗЕЛЕНОЙ КОМАНДЫ");
            }
        });

        return scene;
    }

    private boolean isStartKey(String key) {
        return (key.equals("c") || key.equals("с"));
    }

    private boolean isResetKey(String key) {
        return key.equals("n") || key.equals("т");
    }

    private boolean isGreenTeamKey(String key) {
        return key.equals("g") || key.equals("п");
    }

    private boolean isRedTeamKey(String key) {
        return key.equals("r") || key.equals("к");
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

        if (team == null) {
            resultLabel.setText("ВРЕМЯ ЗАКОНЧИЛОСЬ");
            answerLabel.setText("");
            changeBackgroundColor("red"); // Красим экран в красный цвет
            play(endSound);
        }
    }

    private void changeBackgroundColor(String color) {
        root.setStyle("-fx-background-color: " + color + ";");
    }

    private boolean isInitialTime() {
        return INITIAL_TIME_STRING.equals(timerLabel.getText());
    }

    private void checkFirstSecond() {
        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = INITIAL_TIME_MS - elapsed;

        if (remaining <= INITIAL_TIME_MS && remaining > (INITIAL_TIME_MS - 1000)) {
            resultLabel.setText("ОТВЕТ НА ПЕРВОЙ СЕКУНДЕ!");
        }
    }
}