package ru.antisessa.CarRefuels.util.car;

public class CarNotUpdatedException extends RuntimeException {
    public CarNotUpdatedException(String message){
        super("CarNotUpdatedException: " + message);
    }
}
