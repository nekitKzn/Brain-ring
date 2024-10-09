module com.nekitvp.brain {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens com.nekitvp.brain to javafx.fxml;
    exports com.nekitvp.brain;
}