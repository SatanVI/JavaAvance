package projetjava.observer;

public class ConsoleLogger implements IObserver {
    @Override
    public void update(String message) {
        System.out.println("[LOG SYSTEM] " + message);
    }
}