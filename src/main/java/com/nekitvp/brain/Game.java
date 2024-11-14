package com.nekitvp.brain;

import java.util.Objects;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    @Override
    public void start(Stage stage) {

        // Загружаем звуки
        startSound = new MediaPlayer(
                new Media(Objects.requireNonNull(getClass().getResource("/sounds/start.wav")).toString()));
        endSound = new MediaPlayer(
                new Media(Objects.requireNonNull(getClass().getResource("/sounds/endTime.wav")).toString()));
        answerSound = new MediaPlayer(
                new Media(Objects.requireNonNull(getClass().getResource("/sounds/answer.wav")).toString()));
        falseStartSound = new MediaPlayer(
                new Media(Objects.requireNonNull(getClass().getResource("/sounds/falseStart.wav")).toString()));

        Image logoImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
        logoImageView = new ImageView(logoImage);
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

        redScoreLabel = new Label("КРАСНЫЕ");
        redScoreLabel.setStyle("-fx-font-family: 'Comic Sans MS'; -fx-font-size: 25px; -fx-text-fill: white;");
        redScoreValue = new Label("0");
        redScoreValue.setStyle("-fx-font-family: 'Comic Sans MS'; -fx-font-size: 100px; -fx-text-fill: white;");

        greenScoreLabel = new Label("ЗЕЛЕНЫЕ");
        greenScoreLabel.setStyle("-fx-font-family: 'Comic Sans MS'; -fx-font-size: 25px; -fx-text-fill: white;");
        greenScoreValue = new Label("0");
        greenScoreValue.setStyle("-fx-font-family: 'Comic Sans MS'; -fx-font-size: 100px; -fx-text-fill: white;");

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

        scene = getScene();

        scene.heightProperty()
                .addListener((obs, oldVal, newVal) -> resizeElements(scene.getWidth(), newVal.doubleValue()));

        //stage.setFullScreen(true);

        stage.setScene(scene);
        stage.setTitle("Brain Ring");
        stage.show();
    }

    // Метод для изменения размеров элементов
    private void resizeElements(double width, double height) {
        // Пример логики: адаптируем размеры в зависимости от ширины и высоты окна
        double logoWidth = height * 0.43; // 43% от высоты окна
        logoImageView.setFitWidth(logoWidth);

        double timerFontSize = height * 0.38; // 38% от высоты окна для шрифта таймера
        timerLabel.setStyle(
                "-fx-font-size: " + timerFontSize + "px; -fx-font-family: 'Courier New'; -fx-text-fill: white;");

        double resultFontSize = height * 0.086; // 8.6% от высоты окна для шрифта результата
        resultLabel.setStyle(
                "-fx-font-size: " + resultFontSize + "px; -fx-font-family: 'Comic Sans MS'; -fx-text-fill: white;");

        double answerFontSize = height * 0.048; // 4.8% от высоты окна для шрифта ответа
        answerLabel.setStyle(
                "-fx-font-size: " + answerFontSize + "px; -fx-font-family: 'Comic Sans MS'; -fx-text-fill: white;");

        double scoreValueSize = height * 0.1; // 10% от высоты окна для баллов
        redScoreValue.setStyle(
                "-fx-font-size: " + scoreValueSize + "px; -fx-font-family: 'Comic Sans MS'; -fx-text-fill: white;");
        greenScoreValue.setStyle(
                "-fx-font-size: " + scoreValueSize + "px; -fx-font-family: 'Comic Sans MS'; -fx-text-fill: white;");

        double scoreLabelSize = height * 0.025; // 2.5% от высоты окна для баллов
        redScoreLabel.setStyle(
                "-fx-font-size: " + scoreLabelSize + "px; -fx-font-family: 'Comic Sans MS'; -fx-text-fill: white;");
        greenScoreLabel.setStyle(
                "-fx-font-size: " + scoreLabelSize + "px; -fx-font-family: 'Comic Sans MS'; -fx-text-fill: white;");

        layout.setPrefWidth(width);
        layout.setPrefHeight(height);
        layout.setAlignment(Pos.CENTER);
    }

    private void play(MediaPlayer player) {
        player.stop();
        player.seek(Duration.ZERO);
        player.play();
    }

    private Scene getScene() {
        scene = new Scene(root);
        scene.setOnKeyPressed(e -> {

            String key = e.getText().toLowerCase();

            if (isResetKey(key)) { // кнопка ведущего "НОВАЯ ИГРА"
                resetTimer();
                changeBackgroundColor("black");

            } else if (!timerRunning && !falseStart && !isEndTime() && isStartKey(key)) { // кнопка ведущего "ЗАПУСТИТЬ"
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

            } else if (!timerRunning && !falseStart && isInitialTime() && isRedTeamKey(
                    key)) { // фальтстарт КРАСНОЙ команды
                play(falseStartSound);
                falseStart = true;
                changeBackgroundColor("#ad3333");
                resultLabel.setText("ФАЛЬСТАРТ КРАСНОЙ КОМАНДЫ");

            } else if (!timerRunning && !falseStart && isInitialTime() && isGreenTeamKey(key)) { // фальтстарт ЗЕЛЕНОЙ
                play(falseStartSound);
                falseStart = true;
                changeBackgroundColor("#4f6b34");
                resultLabel.setText("ФАЛЬСТАРТ ЗЕЛЕНОЙ КОМАНДЫ");

            } else if (key.equals("1")) { // Уменьшаем баллы красной команды
                redScore = Math.max(0, redScore - 1);
                redScoreValue.setText(String.valueOf(redScore));

            } else if (key.equals("4")) { // Увеличиваем баллы красной команды
                redScore += 1;
                redScoreValue.setText(String.valueOf(redScore));

            } else if (key.equals("2")) { // Уменьшаем баллы зеленой команды
                greenScore = Math.max(0, greenScore - 1);
                greenScoreValue.setText(String.valueOf(greenScore));

            } else if (key.equals("5")) { // Увеличиваем баллы зеленой команды
                greenScore += 1;
                greenScoreValue.setText(String.valueOf(greenScore));

            } else if (key.equals("0")) { // Обнуляем баллы обеих команд
                redScore = 0;
                greenScore = 0;
                redScoreValue.setText("0");
                greenScoreValue.setText("0");
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