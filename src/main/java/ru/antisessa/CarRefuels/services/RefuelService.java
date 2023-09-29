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
        // В метод save приходит refuel с верной машиной внутри
        Car boundCar = refuel.getCar();

        // Проводим валидацию и вычисляем расход по показателям заправки
        double calculatedConsumption = calculateAndValidate(refuel, boundCar);

        // Берем старые показатели из машины и присваиваем их полям refuel для возможности отката после удаления
        refuel.setCalculatedConsumption(calculatedConsumption);
        refuel.setPreviousConsumption(refuel.getCar().getLastConsumption());
        refuel.setPreviousOdometerRecord(refuel.getCar().getOdometer());

        // Назначаем новые поля для расхода и одометра у машины после заправки
        boundCar.setLastConsumption(calculatedConsumption);
        boundCar.setOdometer(refuel.getOdometerRecord());

        // Выстраиваем обратную связь для синхронности кэша
        boundCar.getRefuels().add(refuel);

        carRepository.save(boundCar);
        refuelRepository.save(refuel);
    }

    ////////////// Методы для обновления //////////////
    @Transactional
    public void updateLastRefuel(Refuel updatedRefuel, int previousId){
        updatedRefuel.setId(previousId);
        refuelRepository.save(updatedRefuel);
    }

    @Transactional
    public void deleteLastRefuel(String carName){
        Optional<Car> optionalCar = carRepository.findByNameIgnoreCase(carName);
        if(optionalCar.isEmpty())
            throw new CarNotFoundException("Ошибка поиска машины по заправке (from delete method)");

        Car foundCar = optionalCar.get();
        List<Refuel> refuelList = foundCar.getRefuels();

        if(refuelList.isEmpty())
            throw new RefuelNotDeletedException("Не найдено заправок у этой машины");

        // Переменная для заправки которую удаляем
        Refuel refuelToDelete = refuelList.get(refuelList.size() - 1);

        // Зависимой машине назначаем поля предыдущих значений до последней заправки
        foundCar.setOdometer(refuelToDelete.getPreviousOdometerRecord());
        foundCar.setLastConsumption(refuelToDelete.getPreviousConsumption());

        // Выполняет синхронизацию кэшей
        foundCar.getRefuels().remove(refuelToDelete);

        refuelRepository.delete(refuelToDelete); // Удаляем запись о заправке
        carRepository.save(foundCar); // Обновляем запись по этой машине

    }

    public double calculateAndValidate(Refuel refuel, Car car){
        if(refuel.getOdometerRecord() < car.getOdometer())
            throw new RefuelValidateException("Указанное значение спидометра после заправки меньше последнего показателя у машины");

        refuel.setDateTime(LocalDateTime.now());

        double distanceBefore = car.getOdometer();
        System.out.println("km before: " + distanceBefore);

        double distanceAfter = refuel.getOdometerRecord();
        System.out.println("km after: " + distanceAfter);

        System.out.println("volume: " + refuel.getVolume());

        double distanceTraveled = refuel.getOdometerRecord() - car.getOdometer();
        System.out.println("traveled distance: " + distanceTraveled);

        // volume / distance * 100
        double calculatedConsumption = refuel.getVolume() / distanceTraveled * 100;
        System.out.println("consumption: " + calculatedConsumption);
        return calculatedConsumption;
    }


}
