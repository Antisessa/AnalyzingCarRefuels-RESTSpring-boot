package ru.antisessa.CarRefuels.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.antisessa.CarRefuels.models.Car;
import ru.antisessa.CarRefuels.models.Refuel;
import ru.antisessa.CarRefuels.repositories.CarRepository;
import ru.antisessa.CarRefuels.repositories.RefuelRepository;
import ru.antisessa.CarRefuels.util.car.CarNotFoundException;
import ru.antisessa.CarRefuels.util.refuel.RefuelNotDeletedException;
import ru.antisessa.CarRefuels.util.refuel.RefuelNotFoundException;
import ru.antisessa.CarRefuels.util.refuel.RefuelValidateException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RefuelService {
    private final RefuelRepository refuelRepository;
    private final CarRepository carRepository;

    @Autowired
    public RefuelService(RefuelRepository refuelRepository, CarRepository carRepository) {
        this.refuelRepository = refuelRepository;
        this.carRepository = carRepository;
    }

    ////////////// Методы для поиска //////////////
    public Refuel findOne(int id){
        Optional<Refuel> foundRefuel = refuelRepository.findById(id);
        return foundRefuel.orElseThrow(RefuelNotFoundException::new);
    }

    public List<Refuel> findAll(){
        return refuelRepository.findAll();
    }

    public List<Refuel> findByCar(Car car){
        Optional<List<Refuel>> refuels = refuelRepository.findByCar(car);
        return refuels.orElseThrow(RefuelNotFoundException::new);
    }

    ////////////// Методы для сохранения //////////////
    @Transactional
    public void save(Refuel refuel){
        Optional<Car> optionalCar = carRepository.findByNameIgnoreCase(refuel.getCar().getName());
        if(optionalCar.isEmpty())
            throw new CarNotFoundException("Машина не найдена (from refuel save)");

        Car foundCar = optionalCar.get();
        double calculatedConsumption = calculateAndValidate(refuel, foundCar);

        refuel.setCalculatedConsumption(calculatedConsumption);
        refuel.setPreviousConsumption(foundCar.getLastConsumption());
        refuel.setPreviousOdometerRecord(foundCar.getOdometer());

        refuel.setCar(foundCar);

        foundCar.setLastConsumption(calculatedConsumption);
        foundCar.setOdometer(refuel.getOdometerRecord());
        foundCar.getRefuels().add(refuel);
        //TODO пройтись глазами по двум методам save and delete и выполнить их проверку

        carRepository.save(foundCar);
        refuelRepository.save(refuel);
    }

    ////////////// Методы для обновления //////////////
    @Transactional
    public void updateLastRefuel(Refuel updatedRefuel, int previousId){
        updatedRefuel.setId(previousId);
        refuelRepository.save(updatedRefuel);
    }

    @Transactional
    public void deleteLastRefuel(Refuel refuelToDelete){
        Optional<Car> optionalCar = carRepository.findByNameIgnoreCase(refuelToDelete.getCar().getName());
        if(optionalCar.isEmpty())
            throw new CarNotFoundException("Ошибка поиска машины по заправке (from delete method)");

        Car foundCar = optionalCar.get();
        List<Refuel> refuelList = foundCar.getRefuels();

        if(refuelList.get(refuelList.size()-1).getId() != refuelToDelete.getId())
            throw new RefuelNotDeletedException("Удалить можно только последнюю заправку");

        foundCar.setOdometer(refuelToDelete.getPreviousOdometerRecord());
        foundCar.setLastConsumption(refuelToDelete.getPreviousConsumption());
        foundCar.getRefuels().remove(refuelList.size()-1);

        refuelRepository.delete(refuelToDelete);
        carRepository.save(foundCar);

    }

    public double calculateAndValidate(Refuel refuel, Car car){
        if(refuel.getOdometerRecord() < car.getOdometer())
            throw new RefuelValidateException("Указанное значение спидометра меньше текущего");

        refuel.setDateTime(LocalDateTime.now());

        double distanceBefore = car.getOdometer();
        System.out.println("km before: " + distanceBefore);

        double distanceAfter = refuel.getOdometerRecord();
        System.out.println("km after: " + distanceAfter);

        System.out.println("volume: " + refuel.getVolume());

        double distanceTraveled = refuel.getOdometerRecord() - car.getOdometer();
        System.out.println("traveled distance: " + distanceTraveled);

        double calculatedConsumption = refuel.getVolume() / distanceTraveled * 100;
        System.out.println("consumption: " + calculatedConsumption);

        refuel.setCalculatedConsumption(refuel.getVolume() / distanceTraveled * 100);
        return calculatedConsumption;
    }


}
