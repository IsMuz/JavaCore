package src.less14.race;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;


public class MainClass {
    public static final int CARS_COUNT = 4;

    public static void main(String[] args) {
        CyclicBarrier start = new CyclicBarrier(CARS_COUNT, () -> {
            System.out.println("������ ���������� >>> ����� ��������!!!");
        });
        CountDownLatch finish = new CountDownLatch(CARS_COUNT);

        System.out.println("������ ���������� >>> ����������!!!");
        Race race = new Race(new Road(60), new Tunnel(), new Road(40));
        Car[] cars = new Car[CARS_COUNT];
        for (int i = 0; i < cars.length; i++) {
            cars[i] = new Car(race, 20 + (int) (Math.random() * 10), start, finish);
        }
        for (int i = 0; i < cars.length; i++) {
            new Thread(cars[i]).start();
        }

        try {
            finish.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("������ ���������� >>> ����� �����������!!!");
    }
}

class Car implements Runnable {
    private static int CARS_COUNT;
    private CyclicBarrier start;
    private CountDownLatch finish;

    static {
        CARS_COUNT = 0;
    }

    private Race race;
    private int speed;
    private String name;

    public String getName() {
        return name;
    }

    public int getSpeed() {
        return speed;
    }

    public Car(Race race, int speed, CyclicBarrier start, CountDownLatch finish) {
        this.race = race;
        this.speed = speed;
        this.finish = finish;
        CARS_COUNT++;
        this.name = "�������� #" + CARS_COUNT;
        this.start = start;
        this.finish = finish;
    }

    @Override
    public void run() {
        try {
            System.out.println(this.name + " ���������");
            Thread.sleep(500 + (int) (Math.random() * 800));
            System.out.println(this.name + " �����");
            start.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < race.getStages().size(); i++) {
            race.getStages().get(i).go(this);
        }
        finish.countDown();
    }
}

abstract class Stage {
    protected int length;
    protected String description;

    public String getDescription() {
        return description;
    }

    public abstract void go(Car c);
}

class Road extends Stage {
    public Road(int length) {
        this.length = length;
        this.description = "������ " + length + " ������";
    }

    @Override
    public void go(Car c) {
        try {
            System.out.println(c.getName() + " ����� ����: " + description);
            Thread.sleep(length / c.getSpeed() * 1000);
            System.out.println(c.getName() + " �������� ����: " + description);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Tunnel extends Stage {
    Semaphore sem = new Semaphore(MainClass.CARS_COUNT / 2);

    public Tunnel() {
        this.length = 80;
        this.description = "������� " + length + " ������";

    }

    @Override
    public void go(Car c) {
        try {
            try {
                System.out.println(c.getName() + " ��������� � �����(����): " + description);
                sem.acquire();
                System.out.println(c.getName() + " ����� ����: " + description);
                Thread.sleep(length / c.getSpeed() * 1000);
                sem.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println(c.getName() + " �������� ����: " + description);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Race {
    private List<Stage> stages;

    public List<Stage> getStages() {
        return stages;
    }

    public Race(Stage... stages) {
        this.stages = new ArrayList<>(Arrays.asList(stages));
    }
}
