package com.service.other.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.service.other.entity.GarageFavorite;

public interface GarageFavoriteRepository extends JpaRepository<GarageFavorite, Integer> {
	List<GarageFavorite> findByGarageOwnerId(Integer garageOwnerId);

	boolean existsByGarageOwnerIdAndFavoriteGarageId(Integer ownerId, Integer favoriteId);
}