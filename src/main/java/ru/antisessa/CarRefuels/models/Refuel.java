package ru.antisessa.CarRefuels.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "refuel")
public class Refuel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @NotNull(message = "Значение заправленного объема не может быть пустым")
    @DecimalMax(value = "99.99", message = "Значение заправленного объема должно быть меньше 100 литров")
    @DecimalMin(value = "1.00", message = "Значение заправленного объема должно быть больше 1 литра")
    @Column(name = "volume")
    private double volume;

    @Column(name = "cost")
    @NotNull(message = "Значение цены за заправленный объем не может быть пустым")
    private double cost;

    @Column(name = "odometer_record")
    @Min(value = 0, message = "Показание одометра должно быть положительным")
    private int odometerRecord;

    @Column(name = "created_at")
    private LocalDateTime dateTime;

    @Column(name = "consumption")
//    @NotNull(message = "Значение среднего расхода топлива не может быть пустым")
    @DecimalMax(value = "99.99", message = "Значение должно быть меньше 100 литров")
    @DecimalMin(value = "1.00", message = "Значение должно быть больше 1 литра")
    private double calculatedConsumption;

    @ManyToOne
    @JoinColumn(name = "car_id", referencedColumnName = "id")
    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
    private Car car;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Refuel{");
        sb.append("id=").append(id);
        sb.append(", volume=").append(volume);
        sb.append(", cost=").append(cost);
        sb.append(", odometerRecord=").append(odometerRecord);
        sb.append(", dateTime=").append(dateTime);
        sb.append(", calculatedConsumption=").append(calculatedConsumption);
        sb.append(", car=").append(car);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Refuel refuel = (Refuel) o;

        return getId() == refuel.getId();
    }

    @Override
    public int hashCode() {
        return getId();
    }
}
