package ru.antisessa.CarRefuels.util.car;

public class CarNotFoundException extends RuntimeException{
    public CarNotFoundException(String message){
        super(message);
    }
    public CarNotFoundException(){
        super("Машина не найдена");
    }
}
