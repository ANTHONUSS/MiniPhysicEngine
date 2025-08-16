package fr.anthonus.simplePhysics.objects;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PhysicsRectangle extends Rectangle implements PhysicObject {
    public static final double DEFAULT_SIZE = 30;

    private double velocityX = 0;
    private double velocityY = 0;
    private boolean grabbed = false;

    private double lastMouseX, lastMouseY;

    public PhysicsRectangle(double x, double y, Color color) {
        super(x, y, DEFAULT_SIZE, DEFAULT_SIZE);
        setFill(color);

        setOnMousePressed(event -> {
            grabbed = true;

            velocityX = 0;
            velocityY = 0;

            lastMouseX = event.getSceneX();
            lastMouseY = event.getSceneY();
        });

        setOnMouseDragged(event -> {
            setCoordX(event.getSceneX());
            setCoordY(event.getSceneY());

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
        setX(x - DEFAULT_SIZE / 2);
    }
    @Override
    public double getCoordX() {
        return getX() + DEFAULT_SIZE / 2;
    }
    @Override
    public void setCoordY(double y) {
        setY(y - DEFAULT_SIZE / 2);
    }
    @Override
    public double getCoordY() {
        return getY() + DEFAULT_SIZE / 2;
    }

    @Override
    public double getSize() {
        return DEFAULT_SIZE / 2;
    }


}
