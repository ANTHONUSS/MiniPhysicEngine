package fr.anthonus.simplePhysics;

import fr.anthonus.simplePhysics.objects.PhysicCircle;
import fr.anthonus.simplePhysics.objects.PhysicsRectangle;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.concurrent.ThreadLocalRandom;

public class SimplePhysicsMain extends Application {
    private static final double SCREEN_WIDTH = 800;
    private static final double SCREEN_HEIGHT = 800;

    public static final Pane root = new Pane();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        root.setPrefSize(SCREEN_WIDTH, SCREEN_HEIGHT);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Mini physics engine");

        stage.show();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        double screenWidth = root.getWidth();
        double screenHeight = root.getHeight();

        for(int i = 0; i < 50; i++) {
            double radius = PhysicCircle.DEFAULT_RADIUS;
            double x = random.nextDouble(radius, screenWidth - radius);
            double y = random.nextDouble(radius, screenHeight - radius);
            PhysicCircle ball = new PhysicCircle(x, y, Color.color(random.nextDouble(), random.nextDouble(), random.nextDouble()));
            PhysicsEngine.addPhysicObject(ball);
            root.getChildren().add(ball);

            radius = PhysicsRectangle.DEFAULT_SIZE;
            x = random.nextDouble(radius, screenWidth - radius);
            y = random.nextDouble(radius, screenHeight - radius);
            PhysicsRectangle rectangle = new PhysicsRectangle(x, y, Color.color(random.nextDouble(), random.nextDouble(), random.nextDouble()));
            PhysicsEngine.addPhysicObject(rectangle);
            root.getChildren().add(rectangle);
        }

        PhysicsEngine.run();

    }
}