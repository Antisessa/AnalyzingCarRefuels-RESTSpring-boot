package ru.antisessa.CarRefuels.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.antisessa.CarRefuels.models.Car;
import ru.antisessa.CarRefuels.repositories.CarRepository;
import ru.antisessa.CarRefuels.util.car.CarAlreadyCreatedException;
import ru.antisessa.CarRefuels.util.car.CarNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class CarService {
    private final CarRepository carRepository;

    @Autowired
    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }
    ////////////// Методы для поиска //////////////
    public List<Car> findAll(){
        return carRepository.findAll();
    }

    public Car findOne(int id){
        Optional<Car> foundCar = carRepository.findById(id);
        return foundCar.orElseThrow(CarNotFoundException::new);
    }

    public Car findByName(String name){
        Optional<Car> foundCar = carRepository.findByName(name);
        return foundCar.orElseThrow(CarNotFoundException::new);
    }

    ////////////// Методы для сохранения //////////////
    @Transactional
    public void save(Car car){
        Optional<Car> checkCreatedCar = carRepository.findByNameIgnoreCase(car.getName());
        if(checkCreatedCar.isPresent())
            throw new CarAlreadyCreatedException("Машина с таким идентификатором уже существует.");
        carRepository.save(car);
    }

    ////////////// Методы для обновления //////////////
    @Transactional
    public void updateCar(Car updatedCar, int previousId){
        updatedCar.setId(previousId);
        carRepository.save(updatedCar);
    }
}
