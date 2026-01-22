package projetjava.observer;
import java.util.ArrayList;
import java.util.List;

public class NotificationManager {
    private List<IObserver> subscribers = new ArrayList<>();

    public void subscribe(IObserver observer) {
        subscribers.add(observer);
    }

    public void notifyAll(String message) {
        for (IObserver observer : subscribers) {
            observer.update(message);
        }
    }
}