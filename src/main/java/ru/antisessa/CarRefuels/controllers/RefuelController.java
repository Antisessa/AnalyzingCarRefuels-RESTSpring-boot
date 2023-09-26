package ru.antisessa.CarRefuels.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.antisessa.CarRefuels.DTO.RefuelDTO;
import ru.antisessa.CarRefuels.models.Refuel;
import ru.antisessa.CarRefuels.services.RefuelService;
import ru.antisessa.CarRefuels.util.refuel.RefuelErrorResponse;
import ru.antisessa.CarRefuels.util.refuel.RefuelNotCreatedException;
import ru.antisessa.CarRefuels.util.refuel.RefuelNotFoundException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController //@Controller + @ResponseBody над каждым методом для Jackson
@RequestMapping("/acr/refuel")
public class RefuelController {
    private final RefuelService refuelService;
    private final ModelMapper modelMapper;

    @Autowired
    public RefuelController(RefuelService refuelService, ModelMapper modelMapper) {
        this.refuelService = refuelService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello from ACR app - refuel controller";
    }

    // Найти все записи о заправках
    @GetMapping()
    public List<RefuelDTO> allRefuel(){
        return refuelService.findAll().stream().
                map(this::convertToDTO).collect(Collectors.toList());
    }

    // Найти DTO заправку по ID
    @GetMapping("/{id}")
    public RefuelDTO findOneById(@PathVariable("id") int id){
        return convertToDTO(refuelService.findOne(id));
    }

    // Регистрация заправки
    @PostMapping("/add")
    public ResponseEntity<HttpStatus> create(@RequestBody @Valid RefuelDTO refuelDTO,
                                             BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            throw new RefuelNotCreatedException(errorMessageBuilder(errors));
        }
        refuelService.save(convertToRefuel(refuelDTO));
        return ResponseEntity.ok(HttpStatus.OK);
    }

    // Обработка NotFound для метода findOneById
    @ExceptionHandler
    private ResponseEntity<RefuelErrorResponse> handleException(RefuelNotFoundException e){
        RefuelErrorResponse response = new RefuelErrorResponse(e.getMessage(), System.currentTimeMillis());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    //////////// Utility ////////////
    private RefuelDTO convertToDTO(Refuel refuel) {
        RefuelDTO refuelDTO = modelMapper.map(refuel, RefuelDTO.class);
        refuelDTO.setCarName(refuel.getCar().getName());
        return refuelDTO;
    }

    private Refuel convertToRefuel(RefuelDTO refuelDTO){
        return modelMapper.map(refuelDTO, Refuel.class);
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
