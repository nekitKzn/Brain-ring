package com.nekitvp.brain;

import java.util.HashMap;
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
    private static final String COMIC_SANS_FONT = "Comic Sans MS";
    private static final String COURIER_NEW_FONT = "Courier New";

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
    private Stage mainStage;

    public static void main(String[] args) {
        launch(args);
    }

    private final Map<KeyCode, Runnable> keyActions = createCombinedMap();

    private Map<KeyCode, Runnable> createCombinedMap() {
        // Создаём изменяемую карту
        Map<KeyCode, Runnable> combinedMap = new HashMap<>();

        // Добавляем элементы из первой карты
        combinedMap.putAll(Map.of(
                KeyCode.S, () -> mainStage.setFullScreen(true),
                KeyCode.C, this::startKey,
                KeyCode.N, this::newGameKey,
                KeyCode.R, () -> teamAction(Team.RED),
                KeyCode.G, () -> teamAction(Team.GREEN),
                KeyCode.DIGIT1, () -> updateScore(Team.RED, -1),
                KeyCode.DIGIT4, () -> updateScore(Team.RED, 1),
                KeyCode.DIGIT2, () -> updateScore(Team.GREEN, -1),
                KeyCode.DIGIT5, () -> updateScore(Team.GREEN, 1),
                KeyCode.DIGIT0, this::resetScores
        ));

        // Добавляем обработку цифровой клавиатуры
        combinedMap.putAll(Map.of(
                KeyCode.NUMPAD0, this::resetScores,
                KeyCode.NUMPAD1, () -> updateScore(Team.RED, -1),
                KeyCode.NUMPAD4, () -> updateScore(Team.RED, 1),
                KeyCode.NUMPAD2, () -> updateScore(Team.GREEN, -1),
                KeyCode.NUMPAD5, () -> updateScore(Team.GREEN, 1)
        ));

        combinedMap.putAll(Map.of(
                KeyCode.BACK_SPACE, () -> updateScore(Team.GREEN, 1)
        ));

        return combinedMap;
    }

    @Override
    public void start(Stage stage) {

        startSound = loadSound("/sounds/start.wav");
        endSound = loadSound("/sounds/endTime.wav");
        answerSound = loadSound("/sounds/answer.wav");
        falseStartSound = loadSound("/sounds/falseStart.wav");

        logoImageView = createImageView();

        timerLabel = createLabel(INITIAL_TIME_STRING, 200, COURIER_NEW_FONT);
        resultLabel = createLabel("", 45, COMIC_SANS_FONT);
        answerLabel = createLabel("", 25, COMIC_SANS_FONT);

        redScoreLabel = createLabel("КРАСНЫЕ", 13, COMIC_SANS_FONT);
        redScoreValue = createLabel("0", 50, COMIC_SANS_FONT);
        greenScoreLabel = createLabel("ЗЕЛЕНЫЕ", 13, COMIC_SANS_FONT);
        greenScoreValue = createLabel("0", 50, COMIC_SANS_FONT);



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
        layout.setPrefSize(800, 526);

        root = new Pane();
        root.setStyle("-fx-background-color: black;");
        root.getChildren().add(layout);
        scene = new Scene(root, 800, 526);

        mainStage = stage;

        scene.heightProperty()
                .addListener((obs, oldVal, newVal) -> resizeElements(scene.getWidth(), newVal.doubleValue()));
        scene.setOnKeyPressed(event -> keyActions.getOrDefault(event.getCode(), () -> {}).run());
        stage.setScene(scene);
        stage.setTitle("Brain Ring");
        stage.show();
    }

    private ImageView createImageView() {
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(225);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-effect: dropshadow(gaussian, white, 100, 0.8, -1, 0);");
        return imageView;
    }

    private Label createLabel(String text, Integer fontSize, String fontFamily) {
        Label label = new Label(text);
        setFontSize(label, fontFamily, fontSize);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private MediaPlayer loadSound(String path) {
        return new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource(path)).toString()));
    }

    private void resizeElements(double width, double height) {
        logoImageView.setFitWidth(height * 0.43);

        setFontSize(timerLabel, COURIER_NEW_FONT, height * 0.38);
        setFontSize(resultLabel, COMIC_SANS_FONT, height * 0.086);
        setFontSize(answerLabel, COMIC_SANS_FONT, height * 0.048);
        setFontSize(redScoreValue, COMIC_SANS_FONT, height * 0.1);
        setFontSize(greenScoreValue, COMIC_SANS_FONT, height * 0.1);
        setFontSize(redScoreLabel, COMIC_SANS_FONT, height * 0.025);
        setFontSize(greenScoreLabel, COMIC_SANS_FONT, height * 0.025);

        layout.setPrefSize(width, height);
    }

    private void setFontSize(Label label, String font, double size) {
        label.setStyle(String.format("-fx-font-size: %s; -fx-font-family: '%s'; -fx-text-fill: white;", size, font));
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

    private void teamAction(Team team) {
        if (timerRunning) {
            play(answerSound);
            stopTimer(true);
            changeBackgroundColor(team.getColor());
            answerLabel.setText("Отвечает " + team.getTextForAnswer() + " команда");
            checkFirstSecond();
        } else if (!falseStart && isInitialTime()) {
            play(falseStartSound);
            falseStart = true;
            changeBackgroundColor(team.getColor());
            resultLabel.setText("ФАЛЬСТАРТ " + team.getTextForFail() + " КОМАНДЫ");
        }
    }

    private void updateScore(Team team, int delta) {
        if (Team.RED.equals(team)) {
            redScore = Math.max(0, redScore + delta);
            redScoreValue.setText(String.valueOf(redScore));
        } else if (Team.GREEN.equals(team)) {
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
                    stopTimer(false);
                }
            }
        };
        timer.start();
    }

    // Метод для остановки таймера
    private void stopTimer(Boolean isTeam) {
        timerRunning = false;
        elapsedPauseTime = System.currentTimeMillis() - startTime; // Запоминаем, сколько прошло времени

        if (timer != null) {
            timer.stop();
        }

        if (Boolean.FALSE.equals(isTeam)) { // если таймер законончился
            resultLabel.setText("ВРЕМЯ ЗАКОНЧИЛОСЬ");
            answerLabel.setText("");
            changeBackgroundColor("#486fb6");
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