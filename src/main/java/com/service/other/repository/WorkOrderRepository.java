package com.service.other.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.service.other.entity.WorkOrder;
import com.service.other.util.Status;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Integer> {
	List<WorkOrder> findByStatus(Status status);

	List<WorkOrder> findByGarageId(Integer garageId);
//	List<WorkOrder> findByJobCardGarageId(Integer garageId);
}