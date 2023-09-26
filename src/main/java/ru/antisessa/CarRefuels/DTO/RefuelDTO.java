package ru.antisessa.CarRefuels.DTO;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;
import ru.antisessa.CarRefuels.models.Car;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class RefuelDTO {
    private int id;

    @NotNull(message = "Укажите объем заправленного топлива")
    @DecimalMax(value = "99.99", message = "Значение объема должно быть меньше 100 литров")
    @DecimalMin(value = "1.00", message = "Значение объема должно быть больше 1 литра")
    private double volume;

    @NotNull(message = "Укажите сумму, потраченную на заправку")
    private double cost;

    @NotNull(message = "Запишите показания одометра")
    private int odometerRecord;

    @NotNull(message = "Укажите идентификатор машины")
    private String carName;
}
