package fr.anthonus;

import fr.anthonus.physicsObject.Balle;
import fr.anthonus.physicsObject.Square;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Main extends Application {
    private static final List<Balle> balls = new ArrayList<>();
    private static final List<Square> squares = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Pane root = new Pane();
        root.setPrefSize(800, 600);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Mini physics engine");

        stage.show();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        double screenWidth = root.getWidth();
        double screenHeight = root.getHeight();

        for(int i = 0; i < 100; i++) {
            double x = random.nextDouble(Balle.DEFAULT_RADIUS, screenWidth - Balle.DEFAULT_RADIUS);
            double y = random.nextDouble(Balle.DEFAULT_RADIUS, screenHeight - Balle.DEFAULT_RADIUS);

            Balle ball = new Balle(root, Balle.DEFAULT_RADIUS, Color.DODGERBLUE, x, y, balls);
            balls.add(ball);

            Square square = new Square(root, Square.DEFAULT_SIZE, Color.DODGERBLUE, x, y, squares);
            squares.add(square);
        }

        root.getChildren().addAll(balls);
        root.getChildren().addAll(squares);

    }
}