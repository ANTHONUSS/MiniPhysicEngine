package fr.anthonus.simplePhysics;

import fr.anthonus.simplePhysics.objects.PhysicObject;
import javafx.animation.AnimationTimer;

import java.util.ArrayList;
import java.util.List;

public class PhysicsEngine {
    private static final double GRAVITY = 0.3;
    private static final double FRICTION = 0.98;
    private static final double BOUNCE = 0.5;

    private static final List<PhysicObject> physicObjects = new ArrayList<>();

    private static final AnimationTimer physicsTimer = new AnimationTimer() {;
        @Override
        public void handle(long now) {
            doPhysics();
            doCollision();
        }
    };

    public static void run(){
        physicsTimer.start();
    }

    public static void stop(){
        physicsTimer.stop();
    }

    private static void doPhysics(){
        for(PhysicObject object : physicObjects){
            if (object.isGrabbed()) continue;

            object.addVelocityY(GRAVITY);

            object.setCoordX(object.getCoordX() + object.getVelocityX());
            object.setCoordY(object.getCoordY() + object.getVelocityY());

            object.setVelocityX(object.getVelocityX() * FRICTION);
            object.setVelocityY(object.getVelocityY() * FRICTION);
        }
    }

    private static void doCollision() {
        double screenWidth = SimplePhysicsMain.root.getWidth();
        double screenHeight = SimplePhysicsMain.root.getHeight();

        for(PhysicObject object : physicObjects){
            double x = object.getCoordX();
            double y = object.getCoordY();
            double size = object.getSize();

            if (x - size < 0) {
                object.setCoordX(size);
                object.setVelocityX(-object.getVelocityX() * BOUNCE);
            }
            if (x + size > screenWidth) {
                object.setCoordX(screenWidth - size);
                object.setVelocityX(-object.getVelocityX() * BOUNCE);
            }
            if (y - size < 0) {
                object.setCoordY(size);
                object.setVelocityY(-object.getVelocityY() * BOUNCE);
            }
            if (y + size > screenHeight) {
                object.setCoordY(screenHeight - size);
                object.setVelocityY(-object.getVelocityY() * BOUNCE);
            }

            for (PhysicObject other : physicObjects) {
                if (object.equals(other)) continue;
                double dx = object.getCoordX() - other.getCoordX();
                double dy = object.getCoordY() - other.getCoordY();
                double distance = Math.sqrt(dx * dx + dy * dy);
                double minDist = object.getSize() + other.getSize();

                if (distance < minDist - 0.5 && distance > 0) {
                    double overlap = minDist - distance;
                    double nx = dx / distance;
                    double ny = dy / distance;

                    object.setCoordX(object.getCoordX() + nx * overlap / 2);
                    object.setCoordY(object.getCoordY() + ny * overlap / 2);
                    other.setCoordX(other.getCoordX() - nx * overlap / 2);
                    other.setCoordY(other.getCoordY() - ny * overlap / 2);

                    double tx = -ny;
                    double ty = nx;

                    double v1n = object.getVelocityX() * nx + object.getVelocityY() * ny;
                    double v1t = object.getVelocityX() * tx + object.getVelocityY() * ty;
                    double v2n = other.getVelocityX() * nx + other.getVelocityY() * ny;
                    double v2t = other.getVelocityX() * tx + other.getVelocityY() * ty;

                    double v1nAfter = v2n;
                    double v2nAfter = v1n;

                    object.setVelocityX(v1nAfter * nx + v1t * tx);
                    object.setVelocityY(v1nAfter * ny + v1t * ty);
                    other.setVelocityX(v2nAfter * nx + v2t * tx);
                    other.setVelocityY(v2nAfter * ny + v2t * ty);
                }

            }

        }



    }

    public static void addPhysicObject(PhysicObject obj) {
        physicObjects.add(obj);
    }

    public static void removePhysicObject(PhysicObject obj) {
        physicObjects.remove(obj);
    }
}
