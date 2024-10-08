package it.springscheduled.endtripfinder.repo;

import it.springscheduled.endtripfinder.repo.entities.TripEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
@Component
public interface TripRepository extends CrudRepository<TripEntity, Integer>, TripRepositoryExtension {

}
