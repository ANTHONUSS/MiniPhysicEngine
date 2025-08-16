package fr.anthonus.physicsObject;

import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class Square extends Rectangle {
    public static final double DEFAULT_SIZE = 30;

    private final Pane root;
    private final List<Square> allSquares;

    private double velocityX = 0;
    private double velocityY = 0;
    private boolean grabbed = false;

    private double lastMouseX, lastMouseY;

    // constantes physiques
    private static final double GRAVITY = 0.3;
    private static final double FRICTION = 0.98; // perte d'énergie
    private static final double BOUNCE = 0.5;    // restitution sur les murs

    public Square(Pane root, double size, Color color, double centerX, double centerY, List<Square> allSquares) {
        super(size, size, color);
        setX(centerX);
        setY(centerY);
        setStrokeWidth(0);
        this.root = root;
        this.allSquares = allSquares;

        setOnMousePressed(event -> {
            grabbed = true;
            velocityX = 0;
            velocityY = 0;
            lastMouseX = event.getSceneX();
            lastMouseY = event.getSceneY();
        });

        setOnMouseDragged(event -> {
            setX(event.getSceneX());
            setY(event.getSceneY());

            velocityX = (event.getSceneX() - lastMouseX) * 2;
            velocityY = (event.getSceneY() - lastMouseY) * 2;

            lastMouseX = event.getSceneX();
            lastMouseY = event.getSceneY();
        });

        setOnMouseReleased(_ -> {
            grabbed = false;
        });

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                doPhysics();
                doCollision();
            }
        };
        timer.start();
    }

    private void doPhysics() {
        if (!grabbed) {
            velocityY += GRAVITY;

            setX(getX() + velocityX);
            setY(getY() + velocityY);

            velocityX *= FRICTION;
            velocityY *= FRICTION;
        }
    }

    private void doCollision() {
        double screenWidth = root.getWidth();
        double screenHeight = root.getHeight();

        double speed = Math.hypot(velocityX, velocityY);
        double relativeBounce = Math.min(BOUNCE, speed / 5.0);

        // ---- 1. collisions avec les murs ----
        if (getX() - DEFAULT_SIZE < 0) {
            setX(DEFAULT_SIZE);
            velocityX = -velocityX * relativeBounce;
        }
        if (getX() + DEFAULT_SIZE > screenWidth) {
            setX(screenWidth - DEFAULT_SIZE);
            velocityX = -velocityX * relativeBounce;
        }
        if (getY() - DEFAULT_SIZE < 0) {
            setY(DEFAULT_SIZE);
            velocityY = -velocityY * relativeBounce;
        }
        if (getY() + DEFAULT_SIZE > screenHeight) {
            setY(screenHeight - DEFAULT_SIZE);
            velocityY = -velocityY * relativeBounce;
        }

        for (Square square : allSquares) {
            if (this != square) {
                double dx = this.getX() - square.getX();
                double dy = this.getY() - square.getY();
                double distance = Math.sqrt(dx * dx + dy * dy);
                double minDist = this.getWidth() + square.getWidth();

                if (distance < minDist - 0.5 && distance > 0) {
                    // vecteur normalisé
                    double nx = dx / distance;
                    double ny = dy / distance;

                    // profondeur du chevauchement
                    double overlap = minDist - distance;

                    // séparation équitable
                    this.setX(this.getX() + nx * overlap / 2);
                    this.setY(this.getY() + ny * overlap / 2);
                    square.setX(square.getX() - nx * overlap / 2);
                    square.setY(square.getY() - ny * overlap / 2);

                    // vecteur tangent
                    double tx = -ny;
                    double ty = nx;

                    // projection des vitesses
                    double v1n = this.velocityX * nx + this.velocityY * ny;
                    double v1t = this.velocityX * tx + this.velocityY * ty;
                    double v2n = square.velocityX * nx + square.velocityY * ny;
                    double v2t = square.velocityX * tx + square.velocityY * ty;

                    // nouvelles vitesses normales (masses égales, restitution)
                    double restitution = 0.3; // amorti, ajustez 0.3–0.8
                    double v1nAfter = v2n * restitution;
                    double v2nAfter = v1n * restitution;

                    // reconstruction des vitesses finales
                    this.velocityX = v1nAfter * nx + v1t * tx;
                    this.velocityY = v1nAfter * ny + v1t * ty;
                    square.velocityX = v2nAfter * nx + v2t * tx;
                    square.velocityY = v2nAfter * ny + v2t * ty;
                }
            }
        }

    }
}
