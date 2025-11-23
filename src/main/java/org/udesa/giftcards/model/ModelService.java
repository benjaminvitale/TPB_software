package org.udesa.giftcards.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// Esta es la clase que te falta. Es el PADRE de tus Servicios (UserService, GiftCardService).
public abstract class ModelService<T extends ModelEntity, R extends JpaRepository<T, Long>> {

    @Autowired
    protected R repository;

    @Transactional
    public T add(T entity) {
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<T> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<T> findAll() {
        return repository.findAll();
    }

    // Este método abstracto obliga a tus servicios a definir cómo actualizar datos
    protected abstract void updateData(T existingObject, T updatedObject);
}