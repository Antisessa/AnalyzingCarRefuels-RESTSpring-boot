package ru.antisessa.CarRefuels.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.antisessa.CarRefuels.DTO.CarDTO;
import ru.antisessa.CarRefuels.DTO.RefuelDTO;
import ru.antisessa.CarRefuels.models.Car;
import ru.antisessa.CarRefuels.models.Refuel;
import ru.antisessa.CarRefuels.services.CarService;
import ru.antisessa.CarRefuels.util.car.CarAlreadyCreatedException;
import ru.antisessa.CarRefuels.util.car.CarErrorResponse;
import ru.antisessa.CarRefuels.util.car.CarNotCreatedException;
import ru.antisessa.CarRefuels.util.car.CarNotFoundException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController // @Controller + @ResponseBody над каждым методом для Jackson
@RequestMapping("/acr")
public class CarController {
    private final CarService carService;
    private final ModelMapper modelMapper;

    @Autowired
    public CarController(CarService carService, ModelMapper modelMapper) {
        this.carService = carService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/car/hello")
    public String sayHello() {
        return "Hello from ACR app - cars controller";
    }

    // Найти все машины
    @GetMapping("/cars")
    public List<CarDTO> allCars(){
        return carService.findAll().stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    // Найти машину по ID
    @GetMapping("/cars/{id}")
    public CarDTO findOneById(@PathVariable("id") int id){
        return convertToDTO(carService.findOne(id));
    }

    // Регистрация машины
    @PostMapping("/cars/registration")
    public ResponseEntity<HttpStatus> create(@RequestBody @Valid CarDTO carDTO,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            throw new CarNotCreatedException(errorMessageBuilder(errors));
          }
        carService.save(convertToCar(carDTO));
        return ResponseEntity.ok(HttpStatus.OK);
        }



    // Обработка NotFound для метода findOneById
    @ExceptionHandler
    private ResponseEntity<CarErrorResponse> handleException(CarNotFoundException e){
        CarErrorResponse response = new CarErrorResponse(e.getMessage(), System.currentTimeMillis());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // Обработка NotCreated для метода create
    @ExceptionHandler
    private ResponseEntity<CarErrorResponse> handleException(CarNotCreatedException e){
        CarErrorResponse response = new CarErrorResponse(e.getMessage(), System.currentTimeMillis());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Обработка AlreadyCreated для метода create
    @ExceptionHandler
    private ResponseEntity<CarErrorResponse> handleException(CarAlreadyCreatedException e){
        CarErrorResponse response = new CarErrorResponse(e.getMessage(), System.currentTimeMillis());

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }


    //////////// Utility ////////////
    private CarDTO convertToDTO(Car car) {
        CarDTO carDTO = modelMapper.map(car, CarDTO.class);
        carDTO.setRefuels(car.getRefuels().stream()
                .map(this::convertToDTO).collect(Collectors.toList()));

        return carDTO;
    }

    private RefuelDTO convertToDTO(Refuel refuel) {
        RefuelDTO refuelDTO = modelMapper.map(refuel, RefuelDTO.class);
        refuelDTO.setCarName(refuel.getCar().getName());
        return refuelDTO;
    }

    private Car convertToCar(CarDTO carDTO){
        Car mappedCar = modelMapper.map(carDTO, Car.class);
        mappedCar.setLastConsumption(1.01);
        return mappedCar;
    }

    private String errorMessageBuilder(List<FieldError> errors){
        StringBuilder errorMsg = new StringBuilder();
        for (FieldError error : errors) {
            errorMsg.append(error.getField()).append(" - ")
                    .append(error.getDefaultMessage()).append(";");
        }
        return errorMsg.toString();
    }
}
