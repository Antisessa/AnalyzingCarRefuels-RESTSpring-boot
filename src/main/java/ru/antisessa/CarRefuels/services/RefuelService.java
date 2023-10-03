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
import java.util.stream.Collectors;

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
        refuel.setPreviousConsumption(refuel.getCar().getLastConsumption());
        refuel.setPreviousOdometerRecord(refuel.getCar().getOdometer());

        // Назначаем новые поля для одометра у машины после заправки
        boundCar.setOdometer(refuel.getOdometerRecord());

        // Если заправка до полного бака, то добавляем ей значение вычисленного расхода
        if(refuel.isFullTankRefuel())
            refuel.setCalculatedConsumption(calculatedConsumption);

        // Выстраиваем обратную связь для синхронности кэша + расчета последующего среднего расхода
        boundCar.getRefuels().add(refuel);

        // Если заправка была до полного бака, то нужно обновить средний расход у машины
        if(refuel.isFullTankRefuel()){
            List<Refuel> fullRefuels =
                    boundCar.getRefuels().stream()
                            .filter(a -> a.getCalculatedConsumption() > 0)
                            .collect(Collectors.toList());

            //Вводим переменную аккумулятор
            double consumptionSum = 0;

            for (Refuel refuelIterator : fullRefuels )
                consumptionSum+= refuelIterator.getCalculatedConsumption();

            System.out.println("Количество полных заправок: " + fullRefuels.size());
            System.out.println("Вычисленная сумма всех значений расходов: " + consumptionSum);
            System.out.println("Вычисленное значение среднего расхода: " + consumptionSum / fullRefuels.size());
            boundCar.setLastConsumption(consumptionSum / fullRefuels.size());
        }

        carRepository.save(boundCar);
        refuelRepository.save(refuel);
    }

    ////////////// Методы для обновления //////////////
    @Transactional
    public void updateLastRefuel(Refuel updatedRefuel){
        // В метод приходит Refuel с верно вложенной машиной

        Car boundCar = updatedRefuel.getCar();
        Refuel lastRefuel = boundCar.getRefuels().get(boundCar.getRefuels().size() - 1);

        boundCar.setOdometer(lastRefuel.getPreviousOdometerRecord());
        boundCar.setLastConsumption(lastRefuel.getPreviousConsumption());

        // Удаляем старую заправку из БД
        refuelRepository.delete(lastRefuel);

        //Удаляем старую заправку у машины для синхронности кэша
        boundCar.getRefuels().remove(lastRefuel);

        // Обновляем машину в БД
        carRepository.save(boundCar);

        // Вызываем метод для записи новой заправки, передаем туда обновленную заправку
        save(updatedRefuel);
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
        if(refuel.getOdometerRecord() <= car.getOdometer())
            throw new RefuelValidateException("Значение спидометра после заправки должно быть больше последнего показателя у машины");

        refuel.setDateTime(LocalDateTime.now());

        //Достаем список заправок из привязанной машины
        List<Refuel> refuels = car.getRefuels();

        //Если заправка ДО полного бака
        if (refuel.isFullTankRefuel()){
            // инициализируем счетчик для пройденных километров и заправленных литров
            System.out.println("Добавляем заправку до полного бака");
            int reachedKm = refuel.getOdometerRecord() - car.getOdometer();
            System.out.println("Пройдено км с последней заправки: " + reachedKm);
            double filledVolume = refuel.getVolume();

            // Проходимся по всему списку заправок с конца - [0, 1, 2]
            for (int i = refuels.size() -1; i >= 0; i--) {
                //Вводим переменную-сущность-итератор
                Refuel refuelIterator = refuels.get(i);

                //Как только найдется заправка до полного бака - выходим из цикла
                if(refuelIterator.isFullTankRefuel()) {
                    System.out.println("Пройдено км с последней полной заправки: " + reachedKm);
                    System.out.println("Заправлено литров с последней полной заправки: " + filledVolume);
                    break;
                }
                //Если итератор не является заправкой до полного бака, то добавляем значения к счетчику
                reachedKm+= refuelIterator.getOdometerRecord() - refuelIterator.getPreviousOdometerRecord();
                filledVolume+= refuelIterator.getVolume();
            }
            //Как только выходим из цикла - возвращаем полученный расход
            return filledVolume / reachedKm * 100;
        }

        //Если заправка НЕ до полного бака, то просто возвращаем предыдущий расход
        return car.getLastConsumption();
    }


}
