package ru.antisessa.CarRefuels.DTO;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class CarDTO_deprecated {
    private int id;

    @NotNull(message = "Идентификатор машины не должен быть пустым")
    private String name;

    private double lastConsumption;

    @Min(value = 0, message = "Показание одометра должно быть больше 0")
    private int odometer;

    @NotNull(message = "Объем бака должен быть указан")
    @Min(value = 0, message = "Объем бака должен быть больше 0")
    private int gasTankVolume;

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "jackson_id")
    private List<RefuelDTO_deprecated> refuels;
}
