package ar.edu.utn.dds.k3003.repositories;

import java.util.List;
import java.util.Optional;

public interface Repository<K> {
    List<K> findAll();

    Optional<K> findById(String id);

    K save(K entity);

    K deleteById(String id);

    K update(K entity);
}
