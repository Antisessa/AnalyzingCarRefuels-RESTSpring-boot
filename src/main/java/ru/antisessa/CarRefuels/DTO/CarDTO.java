package ru.antisessa.CarRefuels.DTO;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import ru.antisessa.CarRefuels.models.Refuel;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/*
Используется паттерн DTO interface,
В главном Enum представлены интерфейсы, названные одноименно с полями класса Car
При создании нужного класса для DTO (пр. Response GetCar) мы указываем поля внутри этого класса
и реализуем нужные интерфейсы из главного enum, реализация происходит с помощью аннотации @Getter из Lombok
Валидация происходит на уровне интерфейсов, если в Request передается JSON ез нужного поля то Hibernate Validator его не пропустит
 */

// Enum для всех CarDTO
public enum CarDTO {
    ;

    private interface id {
        @NotNull
        int getId();
    }
    private interface name {
        @NotNull
        String getName();
    }
    private interface lastConsumption {
        @NotNull(message = "Значение среднего расхода топлива не может быть пустым")
        @DecimalMax(value = "99.99", message = "Значение должно быть меньше 100 литров")
        @DecimalMin(value = "1.00", message = "Значение должно быть больше 1 литра")
        double getLastConsumption();
    }
    private interface odometer {
        @Min(value = 0, message = "Показание одометра должно быть положительным")
        int getOdometer();
    }
    private interface gasTankVolume {
        @NotNull(message = "Объем бака должен быть указан")
        @Min(value = 0, message = "Объем бака положительная величина")
        int getGasTankVolume();
    }
    private interface car_name {
        String getCar_name();
    }

    private interface refuels {
        @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "jackson_id")
        List<Refuel> getRefuels();
    }

    //Enums Requests для CarController
    public enum Request {
        ; // Пустой enum


        @Getter @Setter
        public static class CreateCar implements name, odometer, gasTankVolume {
            String name;
            int odometer;
            int gasTankVolume;
        }
    }

    public enum Response {
        ; // Пустой enum

        @Getter @Setter
        public static class GetCar implements name, odometer, gasTankVolume{
            String name;
            int odometer;
            int gasTankVolume;

            @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "jackson_id")
            List<RefuelDTO_deprecated> refuels;

        }
    }
}
