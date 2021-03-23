package com.cens.backend.censbackend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.cens.backend.censbackend.entities.Usuario;

@Repository
public interface UsuarioRepository extends CrudRepository<Usuario, Integer>  {
	
	
	@Query(value = "SELECT u FROM Usuario u WHERE u.mail  = ?1  or u.username  = ?1 ")
	public Optional<Usuario> findByLogin(String mail);
	
	@Query(value = "SELECT u FROM Usuario u WHERE u.username  = ?1  OR u.mail = ?1")
	public Optional<Usuario> findByUsername(String username);
	
	@Query(value = "SELECT u FROM Usuario u WHERE u.mail  = ?1 ")
	public Optional<Usuario> findByMail(String mail);
	 
}
