package module.java;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class App {
    public static void main(String[] args) {
        CashBox cashBox = new CashBox(500);
        Queue<BankRequest> queue = new ConcurrentLinkedQueue<>();

        for (int i = 1; i <= 4; i++) {
            new Thread(new Client(i, queue)).start();
            new Thread(new AccountManager(i, queue, cashBox)).start();
        }

        new Thread(new Observer(cashBox)).start();
    }
}

class CashBox {
    private final int capacity;
    private int currentValue;

    public CashBox(int capacity){
        this.capacity = capacity;
        currentValue = 0;
    }

    public synchronized void addMoney(int money, int id){
        while(isFull(money)){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        currentValue+= money;
        System.out.printf("Account manager %s put %s dollars in the vault, current vault: %s\n", id, money, currentValue);
        notifyAll();
    }

    public synchronized void withdrawMoney(int money, int id) {
        while (isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        currentValue -= money;
        System.out.printf("Account manager %s withdraw %s dollars in the vault, current vault: %s\n", id, money, currentValue);
        notifyAll();
    }

    public synchronized int getVault() {
        return this.currentValue;
    }

    public int getCapacity() {
        return capacity;
    }

    public synchronized boolean isFull(int money) {
        return currentValue + money >= capacity;
    }

    public synchronized boolean isEmpty() {
        return currentValue <= 100;
    }
}

class AccountManager implements Runnable {

    private final int id;
    private Queue<BankRequest> queue;
    private CashBox cashBox;

    public AccountManager(int id, Queue<BankRequest> queue, CashBox cashBox) {
        this.id = id;
        this.queue = queue;
        this.cashBox = cashBox;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            BankRequest request = queue.poll();
            if (request != null && request.isWithdrawal()) {
                cashBox.withdrawMoney(request.getAmount(), id);
            } else {
                cashBox.addMoney(request.getAmount(), id);
            }
        }
    }
}

class Client implements Runnable{
    private final int id;
    private Random random = new Random();
    private Queue<BankRequest> queue;

    Client(int id, Queue<BankRequest> queue) {
        this.id = id;
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(170);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int amount = random.nextInt(100);
            boolean isWithdrawal = random.nextInt(2) == 0;
            BankRequest request = new BankRequest(amount, isWithdrawal);
            queue.add(request);
            System.out.printf("Client %s added request %s\n", id, request);
        }
    }
}

class Observer implements Runnable {
    private CashBox cashbox;

    public Observer(CashBox cashbox) {
        this.cashbox = cashbox;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(cashbox.isFull(100)){
                int vault = cashbox.getVault();
                cashbox.withdrawMoney(vault / 2, -1);
                System.out.println("!Account manager withdrawn money!");
            } else if (cashbox.isEmpty()) {
                cashbox.addMoney(cashbox.getCapacity() / 2, -1);
                System.out.println("!Account manager added money!");
            }
        }
    }
}

class BankRequest {
    private final int amount;
    private final boolean isWithdrawal;

    public BankRequest(int amount, boolean isWithdrawal) {
        this.amount = amount;
        this.isWithdrawal = isWithdrawal;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isWithdrawal() {
        return isWithdrawal;
    }

    @Override
    public String toString() {
        return "(Request with " + "amount = " + amount +
                ", isWithdrawal=" + isWithdrawal + ")";
    }
}