package ru.antisessa.CarRefuels.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.antisessa.CarRefuels.models.Car;
import ru.antisessa.CarRefuels.models.Refuel;

import java.util.List;
import java.util.Optional;

public interface RefuelRepository extends JpaRepository<Refuel, Integer> {
    Optional<Refuel> findById(int id);
    Optional<List<Refuel>> findByCar(Car car);
    Optional<Refuel> findTopByCar(Car car);

    void deleteById(Integer integer);
}
