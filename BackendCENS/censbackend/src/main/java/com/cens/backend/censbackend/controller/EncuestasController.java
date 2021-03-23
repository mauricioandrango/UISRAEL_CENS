package com.cens.backend.censbackend.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cens.backend.censbackend.dto.EncuestaDTO;
import com.cens.backend.censbackend.dto.EstadisticasEncuestaDTO;
import com.cens.backend.censbackend.dto.GenericResponseDTO;
import com.cens.backend.censbackend.entities.Encuesta;
import com.cens.backend.censbackend.entities.Pregunta;
import com.cens.backend.censbackend.entities.Respuesta;
import com.cens.backend.censbackend.entities.Usuario;
import com.cens.backend.censbackend.repository.EncuestaRepository;
import com.cens.backend.censbackend.repository.RespuestaRepository;
import com.cens.backend.censbackend.repository.UsuarioRepository;
import com.cens.backend.censbackend.utils.ConstantsUtils;
import com.cens.backend.censbackend.utils.FirebaseUtils;
import org.springframework.web.bind.annotation.RequestMethod;


@RestController
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST})

public class EncuestasController {
	
	@Autowired // This means to get the bean called userRepository// Which is auto-generated by// Spring, we will use it to handle the data
	private EncuestaRepository encuestaRepository;
	@Autowired // This means to get the bean called userRepository// Which is auto-generated by// Spring, we will use it to handle the data
	private UsuarioRepository usuarioRepository;
	@Autowired // This means to get the bean called userRepository// Which is auto-generated by// Spring, we will use it to handle the data
	private RespuestaRepository respuestaRepository;
	
	@PostMapping(path = "/createEncuesta") // Map ONLY POST Requests
	public @ResponseBody Encuesta createEncuesta(@RequestBody Encuesta encuesta) {
		try {
			// for some reason is decrease one day 
		/*	Date fechaInicio = new Date(encuesta.getFechaInicio().getTime() - (1000 * 60 * 60 * 24));
			Date fechaFin = new Date(encuesta.getFechaFin().getTime() - (1000 * 60 * 60 * 24));
			encuesta.setFechaInicio(fechaInicio);
			encuesta.setFechaFin(fechaFin);*/
			
			encuestaRepository.save(encuesta);
			//FIREBASE NOTIFICACION AL CREAR ENCUESTA
			FirebaseUtils.sendPushNotification("Se creo la encuesta: "+ encuesta.getNombre());
			
			return encuesta; // new GenericResponseDTO(ConstantsUtils.CODE_200_OK, "Creado correctamente", obj);

		} catch (Exception e) {
			return new Encuesta();// new GenericResponseDTO(ConstantsUtils.CODE_500_ERROR, e.getMessage(), null);
		}
	}

	@GetMapping(path = "/allEncuestas")
	public @ResponseBody GenericResponseDTO getAll() {
	
		
		try {

			// This returns a JSON or XML with the elements
			Iterable<Encuesta> iterableUsuarios = encuestaRepository.findAll();
			
			//Hibernate.initialize(iterableUsuarios);
			return new GenericResponseDTO(ConstantsUtils.CODE_200_OK, "OK", iterableUsuarios);

		} catch (Exception e) {
			return new GenericResponseDTO(ConstantsUtils.CODE_500_ERROR,
					"Error obteniendo la lista de encuestas: " + e.getMessage(), null);
		}
	}
	
	@GetMapping(path = "/allEncuestasActivas")
	public @ResponseBody GenericResponseDTO getAllEncuestasActivas() {
	
		
		try {

			// This returns a JSON or XML with the elements
			List<Encuesta> listEncuestasActivas = encuestaRepository.findEncuestasActivas(new Date());
			
			//Hibernate.initialize(iterableUsuarios);
			return new GenericResponseDTO(ConstantsUtils.CODE_200_OK, "OK", listEncuestasActivas);

		} catch (Exception e) {
			return new GenericResponseDTO(ConstantsUtils.CODE_500_ERROR,
					"Error obteniendo la lista de encuestas: " + e.getMessage(), null);
		}
	}
	
	@GetMapping(path = "/allEncuestasByUser/{userid}")
	public @ResponseBody GenericResponseDTO allEncuestasByUser(@PathVariable int userid) {
	
		
		try {

			// This returns a JSON or XML with the elements
			List<Encuesta> iterableEncuestas = encuestaRepository.findEncuestasActivas(new Date());
			//Lista de Encuesta que el usuario ha respondido 
			List<Encuesta> encuestasUsuario = new ArrayList<Encuesta>();
			
			Optional<Usuario> optUsuario = usuarioRepository.findById(userid);
			if(optUsuario.isPresent()) {
				Usuario usuario = optUsuario.get();
				// TODO: agregar encuestas respondidads por el usuario
				List<Respuesta> respuestas = respuestaRepository.findPreguntasByUserId(usuario.getId());
				//respuestas.
				for(Respuesta respuesta: respuestas) {
					Encuesta encuesta = respuesta.getPregunta().getEncuesta();
					if(!encuestasUsuario.contains(encuesta)) {
						encuestasUsuario.add(encuesta);
					}	
				}				
			}
			List<Encuesta> allEncuestas= toList(iterableEncuestas);
			//eliminar encuestas que ya haya respondido el usuario
				
			allEncuestas.removeAll(encuestasUsuario);
			
			
			//Hibernate.initialize(iterableUsuarios);
			return new GenericResponseDTO(ConstantsUtils.CODE_200_OK, "OK", allEncuestas);

		} catch (Exception e) {
			return new GenericResponseDTO(ConstantsUtils.CODE_500_ERROR,
					"Error obteniendo la lista de encuestas por usuario: " + e.getMessage(), null);
		}
	}

	
	@RequestMapping(path = "/crearDummy") // Map ONLY POST Requests
	public @ResponseBody Encuesta crearSimple() {
		try {
			Encuesta obj = new Encuesta();
			//obj.setId(1);
			obj.setNombre("encuesta1");
			obj.setDescripcion("encuesta1");
			
			encuestaRepository.save(obj);
			return obj; // new GenericResponseDTO(ConstantsUtils.CODE_200_OK, "Creado correctamente", obj);

		} catch (Exception e) {
			return new Encuesta();// new GenericResponseDTO(ConstantsUtils.CODE_500_ERROR, e.getMessage(), null);
		}
	}
	@GetMapping("findEncuestaById/{id}")

	public @ResponseBody GenericResponseDTO findRespuestaById(@PathVariable int id) {
		try {

			Optional<Encuesta> respuestaOptional = encuestaRepository.findById(id);
			return new GenericResponseDTO(ConstantsUtils.CODE_200_OK, "Encuesta encontrada", respuestaOptional.get());

		} catch (Exception e) {
			return new GenericResponseDTO(ConstantsUtils.CODE_500_ERROR,
					"Encuesta no Encontrada: " + e.getMessage(), null);
		}
	}
	
	@GetMapping("findEncuestaConPreguntasById/{id}")

	public @ResponseBody GenericResponseDTO findEncuestaConPreguntasById(@PathVariable int id) {
		try {

			Optional<Encuesta> respuestaOptional = encuestaRepository.findById(id);
			Encuesta encuestaEncontrada = respuestaOptional.get();
			//Crear el DTO
			EncuestaDTO  encuestaDTO = new EncuestaDTO();
			encuestaDTO.setId(id);
			encuestaDTO.setNombre(encuestaEncontrada.getNombre());
			encuestaDTO.setDescripcion(encuestaEncontrada.getDescripcion());
			encuestaDTO.setPreguntas(encuestaEncontrada.getPreguntas());
			
			return new GenericResponseDTO(ConstantsUtils.CODE_200_OK, "Encuesta encontrada", encuestaDTO);

		} catch (Exception e) {
			return new GenericResponseDTO(ConstantsUtils.CODE_500_ERROR,
					"Encuesta no Encontrada: " + e.getMessage(), null);
		}
	}
	@GetMapping("obteneEstadisticaEncuesta/{id}")

	public @ResponseBody GenericResponseDTO obteneEstadisticaEncuesta(@PathVariable int id) {
		try {

			Optional<Encuesta> respuestaOptional = encuestaRepository.findById(id);
			Encuesta encuestaEncontrada = respuestaOptional.get();
			//Crear el DTO
			EncuestaDTO  encuestaDTO = new EncuestaDTO();
			encuestaDTO.setId(id);
			encuestaDTO.setNombre(encuestaEncontrada.getNombre());
			encuestaDTO.setDescripcion(encuestaEncontrada.getDescripcion());
			encuestaDTO.setPreguntas(encuestaEncontrada.getPreguntas());
			///hacer mapa pregunta y numero
			List<EstadisticasEncuestaDTO> listEstadisticas = new ArrayList<>();
			
			for(Pregunta pregunta: encuestaEncontrada.getPreguntas()) {
				// OBTENER RESPUESTAS POR CADA PREGUNTA
				
				List<Respuesta> listRespuestas = respuestaRepository.findByPreguntaaId(pregunta.getId());
				EstadisticasEncuestaDTO estadisticasEncuestaDTO= new EstadisticasEncuestaDTO();
				estadisticasEncuestaDTO.setPregunta(pregunta.getTitulo());
				estadisticasEncuestaDTO.setNumeroRespuestas(listRespuestas.size());
				estadisticasEncuestaDTO.setTipo(pregunta.getTipo());
				
				//agregar opcionex con numero de respuestas
				if(pregunta.getTipo().equals(ConstantsUtils.TIPO_PREGUNTA_OPCIONES)) {
					
				    Map<String,Integer> mapaOpcionXNumRespuesta = new HashMap<>(); ;
				    StringTokenizer tok = new StringTokenizer(pregunta.getOpciones(),",");
				    while(tok.hasMoreElements()) {
				    	String opcionToken =tok.nextToken().toString();
				    	//obtener el numero de la opcion
				    	int numOpciones = (int) listRespuestas.stream()
			                      .filter(c -> opcionToken.equals(c.getRespuesta()))
			                      .count();
				    	
				    	mapaOpcionXNumRespuesta.put(opcionToken, numOpciones);

				    	
				    }
				    

					estadisticasEncuestaDTO.setRespuestasXNumero(mapaOpcionXNumRespuesta);
					
				}
				
				listEstadisticas.add(estadisticasEncuestaDTO);
				
			}
			
			return new GenericResponseDTO(ConstantsUtils.CODE_200_OK, "OK", listEstadisticas);

		} catch (Exception e) {
			return new GenericResponseDTO(ConstantsUtils.CODE_500_ERROR,
					"Encuesta no Encontrada: " + e.getMessage(), null);
		}
	}
	
	
	/*
	 * Solo para pruebas servicio simple
	 * 	
	@RequestMapping("/encuestas")
	String hello() {
		
		return "Hello, world";
	}
	*/
	
	@PostMapping(path = "/updateEncuesta") // Map ONLY POST Requests
	public @ResponseBody GenericResponseDTO updateEncuesta(@RequestBody Encuesta encuesta) {
		try {

			Encuesta updatedEncuesta = encuestaRepository.findById(encuesta.getId()).get();
			updatedEncuesta.setNombre(encuesta.getNombre());
			updatedEncuesta.setDescripcion(encuesta.getDescripcion());
			
			//DECCCREASE ONE DAY
			/*Date fechaInicio = new Date(encuesta.getFechaInicio().getTime() - (1000 * 60 * 60 * 24));
			Date fechaFin = new Date(encuesta.getFechaFin().getTime() - (1000 * 60 * 60 * 24));
			*/
			updatedEncuesta.setFechaInicio(encuesta.getFechaInicio());
			updatedEncuesta.setFechaFin(encuesta.getFechaFin());
			



			encuestaRepository.save(updatedEncuesta);
			return new GenericResponseDTO(ConstantsUtils.CODE_200_OK, "Actualizado correctamente", encuesta);

		} catch (Exception e) {
			return new GenericResponseDTO(ConstantsUtils.CODE_500_ERROR,
					"No se pudo crear el usuario: " + e.getMessage(), null);
		}
	}

	@DeleteMapping(path = "/deleteEncuesta/{id}") // Map ONLY POST Requests
	public @ResponseBody GenericResponseDTO delete(@PathVariable int id) {
		try {

			encuestaRepository.deleteById(id);
			return new GenericResponseDTO(ConstantsUtils.CODE_200_OK, "Eliminado correctamente", null);

		} catch (Exception e) {
			return new GenericResponseDTO(ConstantsUtils.CODE_500_ERROR, e.getMessage(), null);
		}
	}

	
	
	public static <T> List<T> toList(final Iterable<T> iterable) {
	    return StreamSupport.stream(iterable.spliterator(), false)
	                        .collect(Collectors.toList());
	}

}
