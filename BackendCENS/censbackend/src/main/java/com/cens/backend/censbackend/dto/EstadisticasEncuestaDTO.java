package com.cens.backend.censbackend.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EstadisticasEncuestaDTO  implements Serializable {

	private static final long serialVersionUID = 2415336925455211424L;
	
	private String pregunta;
	private String tipo;
	private Integer numeroRespuestas;
	private Map<String, Integer> respuestasXNumero;
	public String getPregunta() {
		return pregunta;
	}
	public void setPregunta(String pregunta) {
		this.pregunta = pregunta;
	}
	public Integer getNumeroRespuestas() {
		return numeroRespuestas;
	}
	public void setNumeroRespuestas(Integer numeroRespuestas) {
		this.numeroRespuestas = numeroRespuestas;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public Map<String, Integer> getRespuestasXNumero() {
		return respuestasXNumero;
	}
	public void setRespuestasXNumero(Map<String, Integer> respuestasXNumero) {
		this.respuestasXNumero = respuestasXNumero;
	}
	

}
