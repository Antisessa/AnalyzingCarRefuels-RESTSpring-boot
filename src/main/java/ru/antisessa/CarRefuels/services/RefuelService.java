package ru.antisessa.CarRefuels.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.antisessa.CarRefuels.models.Car;
import ru.antisessa.CarRefuels.models.Refuel;
import ru.antisessa.CarRefuels.repositories.CarRepository;
import ru.antisessa.CarRefuels.repositories.RefuelRepository;
import ru.antisessa.CarRefuels.util.car.CarNotFoundException;
import ru.antisessa.CarRefuels.util.refuel.RefuelNotFoundException;

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
            throw new CarNotFoundException("Машина не найдена (from validate)");

        Car foundCar = optionalCar.get();
        validate(refuel, foundCar);

        refuel.setCar(foundCar);
        foundCar.getRefuels().add(refuel);
        foundCar.setLastConsumption(refuel.getCalculatedConsumption());
        foundCar.setOdometer(refuel.getOdometerRecord());

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

        /*TODO
           1.выполнить проверку List<Refuel> size = 1
           2.добавить в таблицу Refuel столбец previous odometer record и previous consumption
           таким образом при удалении единственной заправки данные на этих полях будут восстановлены
           до момента когда пользователь регистрировал машину
           3.После удаления заправки назначить машине эти поля
           */
        Refuel previousRefuel = refuelList.get(refuelList.size()-2);

        foundCar.setLastConsumption(previousRefuel.getCalculatedConsumption());
        foundCar.setOdometer(previousRefuel.getOdometerRecord());

        foundCar.getRefuels().remove(refuelList.size()-1);

        refuelRepository.delete(refuelToDelete);
        carRepository.save(foundCar);
    }

    public Refuel validate(Refuel refuel, Car car){
        refuel.setDateTime(LocalDateTime.now());

        double distanceBefore = car.getOdometer();
        System.out.println("km before: " + distanceBefore);

        double distanceAfter = refuel.getOdometerRecord();
        System.out.println("km after: " + distanceAfter);

        System.out.println("volume: " + refuel.getVolume());

        double distanceTraveled = refuel.getOdometerRecord() - car.getOdometer();
        System.out.println("traveled distance: " + distanceTraveled);

        double consumption = refuel.getVolume() / distanceTraveled * 100;
        System.out.println("consumption: " + consumption);

        refuel.setCalculatedConsumption(refuel.getVolume() / distanceTraveled * 100);

        return refuel;
    }

}
