package fr.anthonus.hangingPhysics;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

public class HangingPhysicsMain extends Application {
    private static final double SCENE_W = 900;
    private static final double SCENE_H = 600;

    // Physique globale
    private static final double GRAVITY = 1600.0;      // px/s^2
    private static final double LIN_DAMPING = 0.995;   // amortissement vitesse linéaire par frame
    private static final double ANG_DAMPING = 0.995;   // amortissement vitesse angulaire par frame
    private static final double RESTITUTION = 0.35;    // rebond contre murs/sol

    // “Ressort” souris
    private static final double SPRING_K = 9000.0;     // raideur
    private static final double SPRING_D = 140.0;      // amortissement visqueux

    // Résolution de contacts
    private static final int CONTACT_ITER = 8;       // itérations séquentielles
    private static final double PEN_CORRECT_PCT = 0.6; // Baumgarte
    private static final double PEN_SLOP = 0.5;      // marge morte sur la pénétration

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage stage) {
        Group root = new Group();

        PhysicsBox box = new PhysicsBox(180, 120);
        box.x = SCENE_W * 0.5;
        box.y = SCENE_H * 0.3;
        root.getChildren().add(box.node);

        Scene scene = new Scene(root, SCENE_W, SCENE_H, Color.web("#1b1d23"));
        stage.setScene(scene);
        stage.setTitle("Physique grab localisé – collisions par coins");
        stage.show();

        // Souris
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (box.containsScenePoint(e.getSceneX(), e.getSceneY())) box.beginGrab(e.getSceneX(), e.getSceneY());
        });
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> { if (box.grabbed) box.updateGrabTarget(e.getSceneX(), e.getSceneY()); });
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> box.endGrab());

        final long[] last = {System.nanoTime()};
        new AnimationTimer() {
            @Override public void handle(long now) {
                double dt = (now - last[0]) * 1e-9;
                if (dt > 1.0/30.0) dt = 1.0/30.0; // sécurité
                last[0] = now;

                box.stepForces(dt);
                // Itérations de contact pour bien propager les impulsions
                for (int i = 0; i < CONTACT_ITER; i++) {
                    box.resolveContactsWithBounds(root.getScene().getWidth(), root.getScene().getHeight());
                }
                box.integrate(dt);
                box.render();
            }
        }.start();
    }



    static class PhysicsBox {
        final double w, h;
        double x, y;       // centre
        double angle;      // rad
        double vx, vy;     // vitesses linéaires
        double omega;      // rad/s

        final double mass;
        final double invMass;
        final double inertia;
        final double invInertia;

        final Group node;
        final Rectangle rect;

        boolean grabbed = false;
        Point2D grabLocal = null;
        Point2D grabTarget = null;

        PhysicsBox(double w, double h) {
            this.w = w; this.h = h;
            this.mass = (w*h)/10000.0;
            this.invMass = 1.0 / mass;
            this.inertia = (mass/12.0) * (w*w + h*h);
            this.invInertia = 1.0 / inertia;

            node = new Group();
            rect = new Rectangle(-w/2.0, -h/2.0, w, h);
            rect.setArcWidth(8); rect.setArcHeight(8);
            rect.setFill(Color.web("#e6eaef"));
            rect.setStroke(Color.web("#9aa4b2")); rect.setStrokeWidth(1.5);
            node.getChildren().add(rect);
        }

        boolean containsScenePoint(double sx, double sy) {
            try {
                Transform inv = node.getLocalToSceneTransform().createInverse();
                Point2D pLocal = inv.transform(sx, sy);
                return rect.contains(pLocal);
            } catch (Exception ex) {
                return false;
            }
        }

        void beginGrab(double sx, double sy) {
            grabbed = true;
            try {
                Transform inv = node.getLocalToSceneTransform().createInverse();
                grabLocal = inv.transform(sx, sy);
            } catch (Exception ex) {
                grabLocal = new Point2D(0,0);
            }
            grabTarget = new Point2D(sx, sy);
        }
        void updateGrabTarget(double sx, double sy) { grabTarget = new Point2D(sx, sy); }
        void endGrab() { grabbed = false; grabLocal = null; grabTarget = null; }

        // --------- Physique ---------

        void stepForces(double dt) {
            // Gravité
            vy += GRAVITY * dt;

            // Ressort de grab appliqué au point saisi -> force + couple
            if (grabbed && grabLocal != null && grabTarget != null) {
                Point2D grabWorld = localToWorld(grabLocal);
                double rx = grabWorld.getX() - x;
                double ry = grabWorld.getY() - y;

                // Erreur ressort
                double ex = (grabTarget.getX() - grabWorld.getX());
                double ey = (grabTarget.getY() - grabWorld.getY());

                // Vitesse du point saisi
                double vpx = vx + (-omega * ry);
                double vpy = vy + ( omega * rx);

                // Force ressort + amortisseur
                double fx = SPRING_K * ex + SPRING_D * (0 - vpx);
                double fy = SPRING_K * ey + SPRING_D * (0 - vpy);

                // Appliquer linéaire
                vx += fx * invMass * dt;
                vy += fy * invMass * dt;
                // Couple τ = r × F
                double tau = rx * fy - ry * fx;
                omega += tau * invInertia * dt;
            }

            // Amortissement
            vx *= LIN_DAMPING;
            vy *= LIN_DAMPING;
            omega *= ANG_DAMPING;
        }

        void integrate(double dt) {
            x += vx * dt;
            y += vy * dt;
            angle += omega * dt;
        }

        void render() {
            node.setTranslateX(x);
            node.setTranslateY(y);
            node.setRotate(Math.toDegrees(angle));
        }

        Point2D localToWorld(Point2D local) {
            double c = Math.cos(angle), s = Math.sin(angle);
            double wx = x + c*local.getX() - s*local.getY();
            double wy = y + s*local.getX() + c*local.getY();
            return new Point2D(wx, wy);
        }

        // Coins locaux
        Point2D[] localCorners() {
            return new Point2D[]{
                    new Point2D(-w/2, -h/2),
                    new Point2D( w/2, -h/2),
                    new Point2D( w/2,  h/2),
                    new Point2D(-w/2,  h/2)
            };
        }

        // Impulsion de contact au point r (depuis centre), normale n=(nx,ny), profondeur pen (>0)
        void resolveContactAtPoint(double rx, double ry, double nx, double ny, double pen) {
            // Correction de pénétration (Baumgarte)
            double corr = Math.max(pen - PEN_SLOP, 0.0) * PEN_CORRECT_PCT;
            if (corr > 0) {
                x += nx * corr;
                y += ny * corr;
            }

            // Vitesse du point
            double vpx = vx + (-omega * ry);
            double vpy = vy + ( omega * rx);

            // Vitesse normale (vers “extérieur”)
            double vn = vpx * nx + vpy * ny;
            if (vn < 0) {
                // Denom = invM + (r × n)^2 * invI
                double rCrossN = rx * ny - ry * nx;
                double denom = invMass + (rCrossN * rCrossN) * invInertia;
                double j = -(1.0 + RESTITUTION) * vn / denom;

                // Appliquer l’impulsion
                double jx = j * nx, jy = j * ny;
                vx += jx * invMass;
                vy += jy * invMass;
                omega += (rx * jy - ry * jx) * invInertia;
            }
        }

        // Résolution contact par COINS contre les 4 bords axis-aligned
        void resolveContactsWithBounds(double W, double H) {
            double c = Math.cos(angle), s = Math.sin(angle);
            for (Point2D lc : localCorners()) {
                // position monde du coin
                double wx = x + c*lc.getX() - s*lc.getY();
                double wy = y + s*lc.getX() + c*lc.getY();
                // vecteur r = coin - centre
                double rx = wx - x;
                double ry = wy - y;

                // Gauche (x=0), normale vers +X
                if (wx < 0) {
                    double pen = -wx;
                    resolveContactAtPoint(rx, ry, +1, 0, pen);
                }
                // Droite (x=W), normale vers -X
                if (wx > W) {
                    double pen = wx - W;
                    resolveContactAtPoint(rx, ry, -1, 0, pen);
                }
                // Haut (y=0), normale vers +Y (vers le bas)
                if (wy < 0) {
                    double pen = -wy;
                    resolveContactAtPoint(rx, ry, 0, +1, pen);
                }
                // Bas (y=H), normale vers -Y (vers le haut)
                if (wy > H) {
                    double pen = wy - H;
                    resolveContactAtPoint(rx, ry, 0, -1, pen);
                }
            }
        }
    }
}
