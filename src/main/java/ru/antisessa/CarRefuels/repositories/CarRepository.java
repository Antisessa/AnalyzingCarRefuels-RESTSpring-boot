package ru.antisessa.CarRefuels.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.antisessa.CarRefuels.models.Car;

import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Integer> {
    Optional<Car> findById(int id);
    Optional<Car> findByName(String name);
    Optional<Car> findByNameIgnoreCase(String name);
}
