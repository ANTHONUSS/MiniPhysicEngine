package fr.anthonus.simplePhysics.objects;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class PhysicCircle extends Circle implements PhysicObject {
    public static final double DEFAULT_RADIUS = 15;

    private double velocityX = 0;
    private double velocityY = 0;
    private boolean grabbed = false;

    private double lastMouseX, lastMouseY;

    public PhysicCircle(double x, double y, Color color) {
        super(x, y, DEFAULT_RADIUS, color);

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

            velocityX = event.getSceneX() - lastMouseX;
            velocityY = event.getSceneY() - lastMouseY;

            lastMouseX = event.getSceneX();
            lastMouseY = event.getSceneY();
        });

        setOnMouseReleased(_ -> {
            grabbed = false;
        });
    }

    @Override
    public double getVelocityX() {
        return velocityX;
    }
    @Override
    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }
    @Override
    public void addVelocityX(double velocityX) {
        this.velocityX += velocityX;
    }
    @Override
    public double getVelocityY() {
        return velocityY;
    }
    @Override
    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }
    @Override
    public void addVelocityY(double velocityY) {
        this.velocityY += velocityY;
    }

    @Override
    public boolean isGrabbed() {
        return grabbed;
    }

    @Override
    public void setCoordX(double x) {
        setCenterX(x);
    }
    @Override
    public void setCoordY(double y) {
        setCenterY(y);
    }
    @Override
    public double getCoordX() {
        return getCenterX();
    }
    @Override
    public double getCoordY() {
        return getCenterY();
    }

    @Override
    public double getSize() {
        return DEFAULT_RADIUS;
    }
}
