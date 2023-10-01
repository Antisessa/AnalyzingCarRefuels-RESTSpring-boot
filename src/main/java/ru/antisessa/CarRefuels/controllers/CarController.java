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
import ru.antisessa.CarRefuels.util.car.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController // @Controller + @ResponseBody над каждым методом для Jackson
@RequestMapping("/acr/cars")
public class CarController {
    private final CarService carService;
    private final ModelMapper modelMapper;

    @Autowired
    public CarController(CarService carService, ModelMapper modelMapper) {
        this.carService = carService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("hello")
    public String sayHello() {
        return "Hello from ACR app - cars controller";
    }

    ////////////////// GET End-points //////////////////
    // Найти все машины
    @GetMapping()
    public List<CarDTO.Response.GetCar> allCars(){
        return carService.findAll().stream()
                .map(this::carToDTO).collect(Collectors.toList());
    }

    @GetMapping("/full")
    public List<CarDTO.Response.GetCarFullInfo> allCarsFullInfo(){
        return carService.findAll().stream()
                .map(this::carToDTOFullInfo).collect(Collectors.toList());
    }

    // Найти машину с краткими данными по ID
    @GetMapping("/{id}")
    public CarDTO.Response.GetCar findOneById(@PathVariable("id") int id){
        return carToDTO(carService.findOne(id));
    }

    //Найти машину со всеми данными по ID
    @GetMapping("{id}/full")
    public CarDTO.Response.GetCarFullInfo findOneByIdFullInfo(@PathVariable("id") int id){
        return carToDTOFullInfo(carService.findOne(id));
    }

    ////////////////// POST End-points //////////////////
    // Регистрация машины
    @PostMapping("/registration")
    public ResponseEntity<HttpStatus> create(@RequestBody @Valid CarDTO.Request.CreateCar request,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            throw new CarNotCreatedException(errorMessageBuilder(errors));
          }
        carService.save(convertToCar(request));
        return ResponseEntity.ok(HttpStatus.OK);
        }

    ////////////////// UPDATE End-points //////////////////

    @PatchMapping("/update")
    public ResponseEntity<HttpStatus> update(@RequestBody @Valid CarDTO.Request.UpdateCar request,
                                             BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            throw new CarNotUpdatedException(errorMessageBuilder(errors));
        }

        carService.updateCar(convertToCar(request));
        return ResponseEntity.ok(HttpStatus.OK);
    }

    ////////////////// DELETE End-points //////////////////
    @DeleteMapping("/delete")
    public ResponseEntity<HttpStatus> delete(@RequestBody @Valid CarDTO.Request.DeleteCar request,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            throw new CarNotCreatedException(errorMessageBuilder(errors));
        }
        carService.delete(request.getName());
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


    /////// Code Block for convert Model to DTO ///////
    // Convert Car to GetCar
    private CarDTO.Response.GetCar carToDTO(Car car) {
        CarDTO.Response.GetCar getCar = modelMapper.map(car, CarDTO.Response.GetCar.class);

        getCar.setCountRefuels(car.getRefuels().size());

        return getCar;
    }

    // Convert Car to GetCarFullInfo
    private CarDTO.Response.GetCarFullInfo carToDTOFullInfo(Car car) {
        CarDTO.Response.GetCarFullInfo getCar = modelMapper.map(car, CarDTO.Response.GetCarFullInfo.class);

        getCar.setCountRefuels(car.getRefuels().size());

        getCar.setRefuels(car.getRefuels().stream()
                .map(this::refuelToDTO).collect(Collectors.toList()));

        return getCar;
    }

    // Convert Refuel to GetRefuel
    private RefuelDTO.Response.GetRefuel refuelToDTO(Refuel refuel) {
        return modelMapper.map(refuel, RefuelDTO.Response.GetRefuel.class);
    }




    /////// Code Block for convert DTO to Model ///////
    //  Convert CreateCar to Car
    private Car convertToCar(CarDTO.Request.CreateCar createCar){
        return modelMapper.map(createCar, Car.class);
    }

    // Convert UpdateCar to Car
    private Car convertToCar(CarDTO.Request.UpdateCar updateCar){
        return modelMapper.map(updateCar, Car.class);
    }




    private String errorMessageBuilder(List<FieldError> errors){
        StringBuilder errorMsg = new StringBuilder();
        for (FieldError error : errors) {
            errorMsg.append(error.getField()).append(" - ")
                    .append(error.getDefaultMessage()).append("; ");
        }
        return errorMsg.toString();
    }
}
