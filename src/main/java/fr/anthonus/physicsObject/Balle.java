package fr.anthonus.physicsObject;

import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;

public class Balle extends Circle {
    public static final double DEFAULT_RADIUS = 15;

    private final Pane root;
    private final List<Balle> allBalls;

    private double velocityX = 0;
    private double velocityY = 0;
    private boolean grabbed = false;

    private double lastMouseX, lastMouseY;

    // constantes physiques
    private static final double GRAVITY = 0.3;
    private static final double FRICTION = 0.98; // perte d'énergie
    private static final double BOUNCE = 0.5;    // restitution sur les murs

    public Balle(Pane root, double radius, Color color, double centerX, double centerY, List<Balle> allBalls) {
        super(centerX, centerY, radius, color);
        this.root = root;
        this.allBalls = allBalls;

        setOnMousePressed(event -> {
            grabbed = true;
            velocityX = 0;
            velocityY = 0;
            lastMouseX = event.getSceneX();
            lastMouseY = event.getSceneY();
        });

        setOnMouseDragged(event -> {
            setCenterX(event.getSceneX());
            setCenterY(event.getSceneY());

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

            setCenterX(getCenterX() + velocityX);
            setCenterY(getCenterY() + velocityY);

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
        if (getCenterX() - DEFAULT_RADIUS < 0) {
            setCenterX(DEFAULT_RADIUS);
            velocityX = -velocityX * relativeBounce;
        }
        if (getCenterX() + DEFAULT_RADIUS > screenWidth) {
            setCenterX(screenWidth - DEFAULT_RADIUS);
            velocityX = -velocityX * relativeBounce;
        }
        if (getCenterY() - DEFAULT_RADIUS < 0) {
            setCenterY(DEFAULT_RADIUS);
            velocityY = -velocityY * relativeBounce;
        }
        if (getCenterY() + DEFAULT_RADIUS > screenHeight) {
            setCenterY(screenHeight - DEFAULT_RADIUS);
            velocityY = -velocityY * relativeBounce;
        }

        for (Balle balle : allBalls) {
            if (this != balle) {
                double dx = this.getCenterX() - balle.getCenterX();
                double dy = this.getCenterY() - balle.getCenterY();
                double distance = Math.sqrt(dx * dx + dy * dy);
                double minDist = this.getRadius() + balle.getRadius();

                if (distance < minDist - 0.5 && distance > 0) {
                    // vecteur normalisé
                    double nx = dx / distance;
                    double ny = dy / distance;

                    // profondeur du chevauchement
                    double overlap = minDist - distance;

                    // séparation équitable
                    this.setCenterX(this.getCenterX() + nx * overlap / 2);
                    this.setCenterY(this.getCenterY() + ny * overlap / 2);
                    balle.setCenterX(balle.getCenterX() - nx * overlap / 2);
                    balle.setCenterY(balle.getCenterY() - ny * overlap / 2);

                    // vecteur tangent
                    double tx = -ny;
                    double ty = nx;

                    // projection des vitesses
                    double v1n = this.velocityX * nx + this.velocityY * ny;
                    double v1t = this.velocityX * tx + this.velocityY * ty;
                    double v2n = balle.velocityX * nx + balle.velocityY * ny;
                    double v2t = balle.velocityX * tx + balle.velocityY * ty;

                    // nouvelles vitesses normales (masses égales, restitution)
                    double restitution = 0.3; // amorti, ajustez 0.3–0.8
                    double v1nAfter = v2n * restitution;
                    double v2nAfter = v1n * restitution;

                    // reconstruction des vitesses finales
                    this.velocityX = v1nAfter * nx + v1t * tx;
                    this.velocityY = v1nAfter * ny + v1t * ty;
                    balle.velocityX = v2nAfter * nx + v2t * tx;
                    balle.velocityY = v2nAfter * ny + v2t * ty;
                }
            }
        }

    }
}
