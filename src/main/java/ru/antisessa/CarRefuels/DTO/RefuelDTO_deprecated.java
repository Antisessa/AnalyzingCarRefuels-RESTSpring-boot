package ru.antisessa.CarRefuels.DTO;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class RefuelDTO_deprecated {
    private int id;

    @NotNull(message = "Укажите объем заправленного топлива")
    @DecimalMax(value = "99.99", message = "Значение объема должно быть меньше 100 литров")
    @DecimalMin(value = "1.00", message = "Значение объема должно быть больше 1 литра")
    private double volume;

    @NotNull(message = "Укажите сумму, потраченную на заправку")
    private double cost;

    @NotNull(message = "Запишите показания одометра после заправки")
    private int odometerRecord;

    @NotNull(message = "Укажите идентификатор машины")
    private String carName;
}
