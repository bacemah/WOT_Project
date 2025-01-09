package tn.cot.smarthydro.repositories;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;
import tn.cot.smarthydro.entities.Tenant;


@Repository
public interface TenantRepository extends CrudRepository<Tenant, String> {
    Tenant findByName(String name);
}


























