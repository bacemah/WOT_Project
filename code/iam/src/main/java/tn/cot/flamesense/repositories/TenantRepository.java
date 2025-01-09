package tn.cot.flamesense.repositories;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;
import tn.cot.flamesense.entities.Tenant;


@Repository
public interface TenantRepository extends CrudRepository<Tenant, String> {
    Tenant findByName(String name);
}


























