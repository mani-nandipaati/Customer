package com.cts.cloudpoc;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.swagger.annotations.Api;

@CrossOrigin
@Controller
@RequestMapping(path = "/customer")
@Api(value = "CustomerControllerAPI", produces = MediaType.APPLICATION_JSON_VALUE)
public class CustomerController {

	@Autowired
	CustomerRepository customerRepository;
	
	/*@Autowired
	private DiscoveryClient discoveryClient;*/
	
	@Autowired
	private LoadBalancerClient loadBalancer;

	@Autowired
	RestTemplate restTemplate;

	@GetMapping(path = "/test")
	public @ResponseBody String test() {
		String status = "success";
		return status;
	}

	@GetMapping(path = "/login")
	public @ResponseBody Map<String, Object> loginCustomer(
			@RequestParam("userName") String userName,
			@RequestParam("password") String password) {
		
		Map<String, Object> model = new HashMap<String, Object>();
		String status = "SUCCESS";
		model.put("status", status);
		Customer customer = customerRepository.findByUserNameAndPassword(
				userName, password);
		if (customer == null) {
			model.put("status", "FAIL");
		}
		model.put("customerId", customer.getId());
		return model;
	}
	
	@GetMapping(path = "/customerdetails")
	public @ResponseBody Map<String, Object> loginCustomer(
			@RequestParam String customerId) {
		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("status", "SUCCESS");
		Customer customer = customerRepository.findOne(Long.valueOf(customerId));
		if (customer == null) {
			model.put("status", "FAIL");
		}
		else{
			model.put("customer", customer);
			model.put("loggedTime", Calendar.getInstance().getTime());
			/*List<ServiceInstance> instances = discoveryClient.getInstances("account");
			ServiceInstance serviceInstance = instances.get(0);
			String baseUrl = serviceInstance.getUri().toString();*/
			ServiceInstance serviceInstance=loadBalancer.choose("account");
			System.out.println("serviceInstance.getUri()"+serviceInstance.getUri());
			System.out.println("serviceInstance.getServiceId()"+serviceInstance.getServiceId());
			System.out.println("serviceInstance.getMetadata()"+serviceInstance.getMetadata());
			System.out.println("serviceInstance.getHost()"+serviceInstance.getHost());
			System.out.println("serviceInstance.getPort()"+serviceInstance.getPort());
			System.out.println("serviceInstance.hashCode()"+serviceInstance.hashCode());
			System.out.println("serviceInstance.toString()"+serviceInstance.toString());
			String baseUrl=serviceInstance.getUri().toString();
			try{
				String acctNumberUrl = baseUrl +"/account/customer";
				UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(acctNumberUrl).
						queryParam("customerId", customer.getId());
				Account account =restTemplate.getForObject(builder.toUriString(), Account.class);
				model.put("account", account);
			}
			catch(Exception e){
				model.put("status", "FAIL");
				e.printStackTrace();
			}
		}
		return model;
	}

	@PostMapping(path = "/add")
	public @ResponseBody Map<String, Object> add(@RequestBody Customer customer) {
		Map<String, Object> model = new HashMap<String, Object>();
		try {
			System.out.println(" Call made to Add method : " + customer);
			Customer c = new Customer();
			c.setPassword(customer.getPassword());
			c.setUserName(customer.getUserName());
			c.setFirstName(customer.getFirstName());
			c.setLastName(customer.getLastName());
			c.setPhoneNo(customer.getPhoneNo());
			c.setEmailId(customer.getEmailId());
			c.setAddress1(customer.getAddress1());
			c.setAddress2(customer.getAddress2());
			c.setDate(new Date());
			System.out.println(customer.getDate());
			customer = customerRepository.save(customer);
			model.put("status", "SUCCESS");
			model.put("customerId", customer.getId());
		} catch (Exception e) {
			model.put("status", "failed");
			e.printStackTrace();
		}

		return model;
	}

	@GetMapping(path = "/all")
	public @ResponseBody Iterable<Customer> getAll() {
		Iterable<Customer> i = customerRepository.findAll();
		Iterator<Customer> ii = i.iterator();
		while (ii.hasNext()) {
			Customer c = ii.next();
			System.out.println(c.getDate());
		}
		return i;
	}
	
	@GetMapping(path = "/find")
	public @ResponseBody List<Customer> findCustomer(@RequestParam Long id,
			@RequestParam String from, @RequestParam String to) {
		SimpleDateFormat sd = new SimpleDateFormat("DD-MMM-YYYY");
		System.out.println("from " + from);
		System.out.println("to " + to);

		Date fromDate = null;
		Date toDate = null;
		try {
			fromDate = sd.parse(from);
			toDate = sd.parse(to);
		} catch (Exception e) {
			System.out.println(e);
		}

		System.out.println("from Date" + fromDate);
		System.out.println("to Date" + toDate);

		return customerRepository.findByIdAndDateBetween(id, fromDate, toDate);
	}
	
	@DeleteMapping(path="/delete")
	@ResponseBody
	public String delete() {
		try {
			customerRepository.deleteAll();
		}
		catch(Exception e) {
			return "FAIL";
		}
		return "SUCCESS";
	}
}
