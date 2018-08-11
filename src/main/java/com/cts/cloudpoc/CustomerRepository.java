package com.cts.cloudpoc;

import java.util.Date;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends CrudRepository<Customer, Long>{
	public Customer findByUserNameAndPassword(String userName, String password);
	public List<Customer> findByIdAndDateBetween(Long id, Date from, Date to);

}
