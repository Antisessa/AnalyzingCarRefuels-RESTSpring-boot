package ru.antisessa.CarRefuels.DTO;

import lombok.Getter;
import lombok.Setter;
import ru.antisessa.CarRefuels.models.Car;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public enum RefuelDTO {
    ;

    private interface id {
        @NotNull
        int getId();
    }

    private interface volume {
        @NotNull(message = "Значение заправленного объема не может быть пустым")
        @DecimalMax(value = "99.99", message = "Значение заправленного объема должно быть меньше 100 литров")
        @DecimalMin(value = "1.00", message = "Значение заправленного объема должно быть больше 1 литра")
        double getVolume();
    }

    private interface cost{
        @NotNull
        double getCost();
    }

    private interface odometerRecord {
        @Min(value = 0, message = "Показание одометра должно быть положительным")
        int getOdometerRecord();
    }

    private interface previousOdometerRecord {
        @Min(value = 0, message = "Показание одометра должно быть положительным")
        int getPreviousOdometerRecord();
    }

    private interface dateTime {
        LocalDateTime getDateTime();
    }

    private interface calculatedConsumption {
        @NotNull(message = "Значение среднего расхода топлива не может быть пустым")
        @DecimalMax(value = "99.99", message = "Значение должно быть меньше 100 литров")
        @DecimalMin(value = "1.00", message = "Значение должно быть больше 1 литра")
        double getCalculatedConsumption();
    }

    private interface previousConsumption {
        @NotNull(message = "Значение среднего расхода топлива не может быть пустым")
        @DecimalMax(value = "99.99", message = "Значение должно быть меньше 100 литров")
        @DecimalMin(value = "1.00", message = "Значение должно быть больше 1 литра")
        double getPreviousConsumption();
    }

    private interface car{
        Car getCar();
    }

    private interface carName{
        @NotNull
        String getCarName();
    }

    public enum Request {
        ;

        @Getter @Setter
        public static class CreateRefuel implements
                volume, cost, odometerRecord, carName{

            double volume;
            double cost;
            int odometerRecord;
            String carName;
        }
    }

    public enum Response {
        ;

        public static class T{}
        @Getter @Setter
        public static class DeleteLastRefuel extends T implements carName{
            String carName;
        }

        @Getter @Setter
        public static class GetRefuel extends DeleteLastRefuel implements
                id, volume, cost, dateTime, calculatedConsumption{

            int id;
            LocalDateTime dateTime;
            double volume;
            double cost;
            double calculatedConsumption;
        }

        @Getter @Setter
        public static class GetRefuelFullInfo extends GetRefuel implements
                previousOdometerRecord, odometerRecord{

            int previousOdometerRecord;
            int odometerRecord;
        }

//        @Getter @Setter
//        public static class GetRefuelFullInfo implements
//                id, volume, cost, previousOdometerRecord,
//                odometerRecord, dateTime, calculatedConsumption, carName{
//            int id;
//            String carName;
//            LocalDateTime dateTime;
//            double volume;
//            double cost;
//            double calculatedConsumption;
//            int previousOdometerRecord;
//            int odometerRecord;
//        }
    }
}
