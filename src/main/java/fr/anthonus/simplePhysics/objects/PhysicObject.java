package fr.anthonus.simplePhysics.objects;

public interface PhysicObject {
    double getVelocityX();
    void setVelocityX(double velocityX);
    void addVelocityX(double velocityX);
    double getVelocityY();
    void setVelocityY(double velocityY);
    void addVelocityY(double velocityY);

    boolean isGrabbed();

    void setCoordX(double x);
    void setCoordY(double y);
    double getCoordX();
    double getCoordY();

    double getSize();
}
