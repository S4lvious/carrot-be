package com.carrot.Carrot.repository;

import com.carrot.Carrot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findFirstBy();
    Optional<User> findFirstByOrderByIdAsc();
    Optional<User> findByUsername(String Username);
    Optional<User> findByEmail(String email);
    Optional<User> findByRequisitionId(String requisitionId);
    Optional<User> findByGoCardlessRef(String goCardlessRef);

}
