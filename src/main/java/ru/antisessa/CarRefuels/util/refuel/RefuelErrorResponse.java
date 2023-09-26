package ru.antisessa.CarRefuels.util.refuel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RefuelErrorResponse {
    private String message;
    private long timestamp;
}
