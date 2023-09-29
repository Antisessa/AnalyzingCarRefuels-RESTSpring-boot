package ru.antisessa.CarRefuels.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.antisessa.CarRefuels.DTO.CarDTO;
import ru.antisessa.CarRefuels.DTO.RefuelDTO_deprecated;
import ru.antisessa.CarRefuels.DTO.RefuelDTO;
import ru.antisessa.CarRefuels.models.Car;
import ru.antisessa.CarRefuels.models.Refuel;
import ru.antisessa.CarRefuels.services.CarService;
import ru.antisessa.CarRefuels.services.RefuelService;
import ru.antisessa.CarRefuels.util.car.CarErrorResponse;
import ru.antisessa.CarRefuels.util.car.CarNotFoundException;
import ru.antisessa.CarRefuels.util.refuel.RefuelErrorResponse;
import ru.antisessa.CarRefuels.util.refuel.RefuelNotCreatedException;
import ru.antisessa.CarRefuels.util.refuel.RefuelNotDeletedException;
import ru.antisessa.CarRefuels.util.refuel.RefuelNotFoundException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController //@Controller + @ResponseBody над каждым методом для Jackson
@RequestMapping("/acr/refuel")
public class RefuelController {
    private final RefuelService refuelService;
    private final CarService carService;
    private final ModelMapper modelMapper;

    @Autowired
    public RefuelController(RefuelService refuelService, CarService carService, ModelMapper modelMapper) {
        this.refuelService = refuelService;
        this.carService = carService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello from ACR app - refuel controller";
    }

    ////////////////// GET End-points //////////////////
    // Найти все записи о заправках
    @GetMapping()
    public List<RefuelDTO.Response.GetRefuel> allRefuel(){
        return refuelService.findAll().stream().
                map(this::convertToDTO).collect(Collectors.toList());
    }
    // Найти все записи о заправках с полной информацией
    @GetMapping("/full")
    public List<RefuelDTO.Response.GetRefuelFullInfo> allRefuelFullInfo(){
        return refuelService.findAll().stream().
                map(this::convertToDTOFullInfo).collect(Collectors.toList());
    }

    // Найти DTO заправку по ID
    @GetMapping("/{id}")
    public RefuelDTO.Response.GetRefuel findOneById(@PathVariable("id") int id){
        return convertToDTO(refuelService.findOne(id));
    }

    // Найти DTO заправку по ID c полной информацией
    @GetMapping("/{id}/full")
    public RefuelDTO.Response.GetRefuelFullInfo findOneByIdFullInfo(@PathVariable("id") int id){
        return convertToDTOFullInfo(refuelService.findOne(id));
    }

    ////////////////// POST End-points //////////////////
    // Регистрация заправки
    @PostMapping("/add")
    public ResponseEntity<HttpStatus> create(@RequestBody @Valid RefuelDTO_deprecated refuelDTODeprecated,
                                             BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            throw new RefuelNotCreatedException(errorMessageBuilder(errors));
        }
        //В метод save передаем refuel с верно вложенным объектом car
        refuelService.save(convertToRefuel(refuelDTODeprecated));
        return ResponseEntity.ok(HttpStatus.OK);
    }

    ////////////////// UPDATE End-points //////////////////
    // TODO написать метод для обновления машины


    ////////////////// DELETE End-points //////////////////
    //Удаление заправки
    @DeleteMapping("/delete")
    public ResponseEntity<HttpStatus> deleteLastRefuel(@RequestBody @Valid RefuelDTO.Response.DeleteLastRefuel response,
                                                       BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            throw new RefuelNotDeletedException(errorMessageBuilder(errors));
        }

        refuelService.deleteLastRefuel(response.getCarName());
        return ResponseEntity.ok(HttpStatus.OK);
    }

    // Обработка NotFoundRefuel для метода findOneById
    @ExceptionHandler
    private ResponseEntity<RefuelErrorResponse> handleException(RefuelNotFoundException e){
        RefuelErrorResponse response = new RefuelErrorResponse(e.getMessage(), System.currentTimeMillis());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // Обработка CarNotFound для метода deleteLastRefuel
    @ExceptionHandler
    private ResponseEntity<CarErrorResponse> handleException(CarNotFoundException e){
        CarErrorResponse response = new CarErrorResponse(e.getMessage(), System.currentTimeMillis());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


    // Обработка RefuelNotDeleted для метода deleteLastRefuel
    @ExceptionHandler
    private ResponseEntity<RefuelErrorResponse> handleException(RefuelNotDeletedException e){
        RefuelErrorResponse response = new RefuelErrorResponse(e.getMessage(), System.currentTimeMillis());

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    //////////// Utility ////////////
//    private RefuelDTO.Response.GetRefuel convertToDTO(Refuel refuel) {
//        RefuelDTO.Response.GetRefuel refuelDTO = modelMapper.map(refuel, RefuelDTO.Response.GetRefuel.class);
//        refuelDTO.setCarName(refuel.getCar().getName());
//        return refuelDTO;
//    }

    private RefuelDTO.Response.GetRefuel convertToDTO(Refuel refuel) {
        RefuelDTO.Response.GetRefuel refuelDTO = modelMapper.map(refuel, RefuelDTO.Response.GetRefuel.class);
        refuelDTO.setCarName(refuel.getCar().getName());
        return refuelDTO;
    } //TODO Правильно параметризовать методы чтобы не было повторяющегося кода

    private RefuelDTO.Response.GetRefuelFullInfo convertToDTOFullInfo(Refuel refuel) {
        RefuelDTO.Response.GetRefuelFullInfo refuelDTO = modelMapper.map(refuel, RefuelDTO.Response.GetRefuelFullInfo.class);
        refuelDTO.setCarName(refuel.getCar().getName());
        return refuelDTO;
    }

    private Refuel convertToRefuel(RefuelDTO_deprecated refuelDTODeprecated){
        Refuel refuel = modelMapper.map(refuelDTODeprecated, Refuel.class);
        Car foundCar = carService.findByNameIgnoreCase(refuelDTODeprecated.getCarName());
        refuel.setCar(foundCar);
        return refuel;
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
