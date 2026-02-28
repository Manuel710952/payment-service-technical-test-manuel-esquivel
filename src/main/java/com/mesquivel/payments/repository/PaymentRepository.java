package com.mesquivel.payments.repository;

import com.mesquivel.payments.model.Payment;
import com.mesquivel.payments.model.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findByEstatus(PaymentStatus estatus);
    List<Payment> findByPagador(String pagador);
    List<Payment> findByBeneficiario(String beneficiario);
}
