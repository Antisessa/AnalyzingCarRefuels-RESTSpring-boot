package ru.antisessa.CarRefuels.util.refuel;

public class RefuelNotFoundException extends RuntimeException{
    public RefuelNotFoundException(){
        super("Заправка не найдена (from lambda)");
    }

    public RefuelNotFoundException(String message){
        super(message);
    }
}
