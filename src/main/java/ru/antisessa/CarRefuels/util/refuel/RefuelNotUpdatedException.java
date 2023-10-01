package ru.antisessa.CarRefuels.util.refuel;

public class RefuelNotUpdatedException extends RuntimeException {
    public RefuelNotUpdatedException(String message){
        super(message);
    }
}
