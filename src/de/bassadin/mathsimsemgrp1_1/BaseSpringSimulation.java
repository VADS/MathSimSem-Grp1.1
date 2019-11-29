package de.bassadin.mathsimsemgrp1_1;

import de.bassadin.mathsimsemgrp1_1.Objects2D.*;
import de.bassadin.mathsimsemgrp1_1.utils.Constants;
import de.bassadin.mathsimsemgrp1_1.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;

public class BaseSpringSimulation extends JFrame {
    public float mass1;
    public float mass2;

    public float springConstant;
    public float equilibriumDistance;
    public float startPosition1;
    public float startPosition2;
    public float startSpeed1;
    public float startSpeed2;

    private ArrayList<Object2D> sceneObjects = new ArrayList<Object2D>();

    private float reducedMass;
    private double sqrtDDividedByMu;

    private float a;
    private double b;

    private float equilibriumSpeedAtT0, equilibriumPositionAtT0;

    private int body1SideLength, body2SideLength;

    private QuadBody quadBody1, quadBody2;

    private int springWidth;
    private Quad spring;

    private int centerOfMassSideLength;
    private Circle centerOfMass;

    private BufferedImage backBuffer;

    public BaseSpringSimulation(float mass1, float mass2, float springConstant, float equilibriumDistance, float startPosition1, float startPosition2, float startSpeed1, float startSpeed2) {
        //Set vars
        this.mass1 = mass1;
        this.mass2 = mass2;
        this.springConstant = springConstant;
        this.equilibriumDistance = equilibriumDistance;
        this.startPosition1 = startPosition1;
        this.startPosition2 = startPosition2;
        this.startSpeed1 = startSpeed1;
        this.startSpeed2 = startSpeed2;

        //Drawing stuff
        body1SideLength = Utils.widthAndHeightForMass(mass1);
        body2SideLength = Utils.widthAndHeightForMass(mass2);

        //Where the objects are placed on the y axis
        int objectHeight = 300;

        quadBody1 = new QuadBody(0, objectHeight - body1SideLength / 2, body1SideLength, body1SideLength, mass1);
        quadBody2 = new QuadBody(0, objectHeight - body2SideLength / 2, body2SideLength, body2SideLength, mass2);

        springWidth = (int) (quadBody1.getHeight() * 0.16);
        spring = new Quad(0, objectHeight - springWidth / 2, 0, springWidth); //Position will be updated in draw

        centerOfMassSideLength = (int) (quadBody1.getHeight() * 0.3);
        centerOfMass = new Circle(0, objectHeight - centerOfMassSideLength / 2, centerOfMassSideLength, centerOfMassSideLength);

        //Formula stuff
        reducedMass = Utils.reducedMass(mass1, mass2);
        sqrtDDividedByMu = Math.sqrt(springConstant / reducedMass);

        a = startPosition2 - startPosition1 - equilibriumDistance;
        b = (1 / sqrtDDividedByMu) * (startSpeed2 - startSpeed1);

        equilibriumSpeedAtT0 = 1 / (mass1 + mass2) * ((mass1 * startSpeed1) + (mass2 * startSpeed2));
        equilibriumPositionAtT0 = ((mass1 * startPosition1) + (mass2 * startPosition2)) / (mass1 + mass2);

        //Object Setup
        sceneObjects.add(quadBody1);
        quadBody2.setColor(Color.RED);
        sceneObjects.add(quadBody2);
        sceneObjects.add(spring);

        centerOfMass.setColor(Color.CYAN);
        sceneObjects.add(centerOfMass);

        quadBody1.setPosX(quadBody1.getPosX() + 120);

        //Window Setup
        setTitle("Harmonische Bewegung zweier gekoppelter Massen");
        setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        backBuffer = new BufferedImage(Constants.WINDOW_WIDTH,Constants.WINDOW_HEIGHT, BufferedImage.TYPE_INT_RGB);

        this.setVisible(true);
        double deltaTime = 0;
        while (true) {
            this.draw(deltaTime);
            try {
                Thread.sleep((int) (Constants.TPF * 1000));
                deltaTime += Constants.TPF;
            } catch (InterruptedException e) {
            }
        }
    }

    void draw(double deltaTime) {
        Graphics g = getGraphics();
        Graphics bbg = backBuffer.getGraphics();

        //Fill Scene Background
        bbg.setColor(Color.WHITE);
        bbg.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        //Draw Grid
        bbg.setColor(Color.LIGHT_GRAY);
        for(int x = 100; x <= Constants.WINDOW_WIDTH; x += 100) {
            bbg.drawLine(x, 0, x, Constants.WINDOW_HEIGHT);
            bbg.drawString(Integer.toString(x), x + 5, 50);
        }
        for(int y = 100; y <= Constants.WINDOW_WIDTH; y += 100) {
            bbg.drawLine(0, y, Constants.WINDOW_WIDTH, y);
            bbg.drawString(Integer.toString(y), 10, y + 13);
        }

        //Center of Mass Point
        float equilibriumPositionAtT = equilibriumPositionAtT0 + (equilibriumSpeedAtT0 * (float)deltaTime);
        centerOfMass.setPosX(equilibriumPositionAtT - centerOfMass.getWidth() / 2);

        double sVonT = (a * Math.cos(sqrtDDividedByMu * deltaTime)) + (b * Math.sin(sqrtDDividedByMu * deltaTime));

        //Body positions
        double body1Position = ((mass1 + mass2) * equilibriumPositionAtT - mass2 * (sVonT + equilibriumDistance)) / (mass1 + mass2);
        double body2Position = equilibriumPositionAtT - ((mass1 * (-sVonT - equilibriumDistance)) / (mass1 + mass2));

        quadBody1.setPosX(body1Position - quadBody1.getWidth() / 2);
        quadBody2.setPosX(body2Position - quadBody2.getWidth() / 2);

        spring.setPosX(quadBody1.getPosX() + quadBody1.getWidth());
        spring.setWidth((int) (quadBody2.getPosX() - quadBody1.getPosX() - quadBody1.getWidth() + 2));

        if (spring.getWidth() < 0) {
            spring.setPosX(spring.getPosX() + spring.getWidth() + quadBody2.getWidth());
            spring.setWidth(spring.getWidth() * -1 - quadBody1.getWidth());
        }

        drawSceneObjects(bbg);

        //Mass numbers
        bbg.setColor(Color.white);
        bbg.drawString("1", (int)(quadBody1.getPosX() + quadBody1.getWidth() / 2), (int)(quadBody1.getPosY() + quadBody1.getHeight() / 2));
        bbg.drawString("2", (int)(quadBody2.getPosX() + quadBody2.getWidth() / 2), (int)(quadBody2.getPosY() + quadBody2.getHeight() / 2));
        g.drawImage(backBuffer,0, 0, null);
    }

    void drawSceneObjects(Graphics g) {
        Iterator<Object2D> iterator = sceneObjects.iterator();
        while (iterator.hasNext()) {
            Object2D object = iterator.next();
            object.draw(g);
        }
    }
}


