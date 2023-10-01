package ru.antisessa.CarRefuels.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.antisessa.CarRefuels.DTO.RefuelDTO;
import ru.antisessa.CarRefuels.models.Car;
import ru.antisessa.CarRefuels.models.Refuel;
import ru.antisessa.CarRefuels.services.CarService;
import ru.antisessa.CarRefuels.services.RefuelService;
import ru.antisessa.CarRefuels.util.car.CarErrorResponse;
import ru.antisessa.CarRefuels.util.car.CarNotFoundException;
import ru.antisessa.CarRefuels.util.refuel.*;

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
    // Найти все записи о заправках с их краткими данными
    @GetMapping()
    public List<RefuelDTO.Response.GetRefuel> allRefuel(){
        return refuelService.findAll().stream().
                map(this::convertToDTO).collect(Collectors.toList());
    }
    // Найти все записи о заправках с их полными данными
    @GetMapping("/full")
    public List<RefuelDTO.Response.GetRefuelFullInfo> allRefuelFullInfo(){
        return refuelService.findAll().stream().
                map(this::convertToDTOFullInfo).collect(Collectors.toList());
    }

    // Найти заправку по ID
    @GetMapping("/{id}")
    public RefuelDTO.Response.GetRefuel findOneById(@PathVariable("id") int id){
        return convertToDTO(refuelService.findOne(id));
    }

    // Найти заправку по ID c полной информацией
    @GetMapping("/{id}/full")
    public RefuelDTO.Response.GetRefuelFullInfo findOneByIdFullInfo(@PathVariable("id") int id){
        return convertToDTOFullInfo(refuelService.findOne(id));
    }

    ////////////////// POST End-points //////////////////
    // Регистрация заправки
    @PostMapping("/add")
    public ResponseEntity<HttpStatus> create(@RequestBody @Valid RefuelDTO.Request.CreateRefuel request,
                                             BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            throw new RefuelNotCreatedException(errorMessageBuilder(errors));
        }

        //В метод save передаем refuel с верно вложенным объектом car
        refuelService.save(convertToRefuel(request));
        return ResponseEntity.ok(HttpStatus.OK);
        // TODO внутри ответа ResponseEntity добавить информационные поля по добавленной заправке
    }

    ////////////////// UPDATE End-points //////////////////
    @PatchMapping("/update")
    public ResponseEntity<HttpStatus> updateLastRefuel(@RequestBody @Valid RefuelDTO.Request.UpdateRefuel request,
                                                       BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            throw new RefuelNotUpdatedException(errorMessageBuilder(errors));
        }

        // В метод update передаем
        refuelService.updateLastRefuel(convertToRefuel(request));
        return ResponseEntity.ok(HttpStatus.OK);
    }


    ////////////////// DELETE End-points //////////////////

    //Удаление последней заправки
    @DeleteMapping("/delete")
    public ResponseEntity<HttpStatus> deleteLastRefuel(@RequestBody @Valid RefuelDTO.Response.DeleteLastRefuel response,
                                                       BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            throw new RefuelNotDeletedException(errorMessageBuilder(errors));
        }

        refuelService.deleteLastRefuel(response.getCarName());
        return ResponseEntity.ok(HttpStatus.OK);
        // TODO внутри ответа ResponseEntity добавить информационные поля по текущей последней заправке у машины
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

    // Обработка RefuelValidate для метода create
    @ExceptionHandler
    private ResponseEntity<RefuelErrorResponse> handleException(RefuelValidateException e){
        RefuelErrorResponse response = new RefuelErrorResponse(e.getMessage(), System.currentTimeMillis());

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // Обработка RefuelNotCreated для метода create
    @ExceptionHandler
    private ResponseEntity<RefuelErrorResponse> handleException(RefuelNotCreatedException e){
        RefuelErrorResponse response = new RefuelErrorResponse(e.getMessage(), System.currentTimeMillis());

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    ////////////////// Utility //////////////////

    // Convert GetRefuel to Refuel
    private RefuelDTO.Response.GetRefuel convertToDTO(Refuel refuel) {
        RefuelDTO.Response.GetRefuel refuelDTO = modelMapper.map(refuel, RefuelDTO.Response.GetRefuel.class);
        refuelDTO.setCarName(refuel.getCar().getName());
        return refuelDTO;
    }

    // Convert GetRefuelFullInfo to Refuel
    private RefuelDTO.Response.GetRefuelFullInfo convertToDTOFullInfo(Refuel refuel) {
        RefuelDTO.Response.GetRefuelFullInfo refuelDTO = modelMapper.map(refuel, RefuelDTO.Response.GetRefuelFullInfo.class);
        refuelDTO.setCarName(refuel.getCar().getName());
        return refuelDTO;
    }

    // Convert CreateRefuel to Refuel
    private Refuel convertToRefuel(RefuelDTO.Request.CreateRefuel request){
        Refuel refuel = modelMapper.map(request, Refuel.class);
        Car foundCar = carService.findByNameIgnoreCase(request.getCarName());
        refuel.setCar(foundCar);
        return refuel;
    }

    // Convert UpdateRefuel to Refuel
    private Refuel convertToRefuel(RefuelDTO.Request.UpdateRefuel request){
        Refuel refuel = modelMapper.map(request, Refuel.class);
        Car foundCar = carService.findByNameIgnoreCase(request.getCarName());
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
