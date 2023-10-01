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

    public Car findByNameIgnoreCase(String name){
        Optional<Car> optionalCar = carRepository.findByNameIgnoreCase(name);
        return optionalCar.orElseThrow(CarNotFoundException::new);
    }

    ////////////// Методы для сохранения //////////////
    @Transactional
    public void save(Car car){
        Optional<Car> checkCreatedCar = carRepository.findByNameIgnoreCase(car.getName());
        if(checkCreatedCar.isPresent())
            throw new CarAlreadyCreatedException("Машина с таким идентификатором уже существует.");

        //Проверяем значение расхода, если оно не указано пользователем то назначаем дефолтное чтобы пропустила БД
        if(car.getLastConsumption() <= 0)
            car.setLastConsumption(1.01);

        carRepository.save(car);
    }

    @Transactional
    public void updateCar(Car updatedCar){
        // Ищем машину по переданному id
        Optional<Car> optionalCar = carRepository.findById(updatedCar.getId());
        Car previousCar = optionalCar.orElseThrow(CarNotFoundException::new);

        // Назначаем обновленной машине значения полей старой машины
        updatedCar.setLastConsumption(previousCar.getLastConsumption());
        updatedCar.setOdometer(previousCar.getOdometer());
        updatedCar.setGasTankVolume(previousCar.getGasTankVolume());
        updatedCar.setRefuels(previousCar.getRefuels());

        carRepository.save(updatedCar);
    }

    ////////////// Метод для удаления //////////////
    @Transactional
    public void delete(String name){
        Optional<Car> optionalCar = carRepository.findByNameIgnoreCase(name);
        if(optionalCar.isEmpty())
            throw new CarNotFoundException("Машины с таким идентификатором не существует.");
        carRepository.delete(optionalCar.get());

    }
}
