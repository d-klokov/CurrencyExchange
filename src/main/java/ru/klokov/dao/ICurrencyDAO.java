package ru.klokov.dao;

import ru.klokov.exception.DatabaseException;
import ru.klokov.exception.ResourceNotFoundException;
import ru.klokov.model.Currency;

import java.util.List;
import java.util.Optional;

public interface ICurrencyDAO {
    List<Currency> findAll() throws DatabaseException;
    Optional<Currency> findById(Long id) throws DatabaseException;
    Optional<Currency> findByCode(String code) throws DatabaseException;
    Currency save(Currency currency)  throws DatabaseException;
}
