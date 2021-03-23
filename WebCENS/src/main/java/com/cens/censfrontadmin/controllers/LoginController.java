/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cens.censfrontadmin.controllers;

import com.cens.censfrontadmin.requestDTO.LoginRequestDTO;
import com.cens.censfrontadmin.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.ws.rs.POST;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.json.JSONObject;

/**
 *
 * @author apaez
 */
@ManagedBean
@ViewScoped
public class LoginController implements Serializable {

    private LoginRequestDTO requestDTO = new LoginRequestDTO();

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String loginRequest() {
        String redirect = null;
        try {
            URL url = new URL("http://localhost:80/loginWeb");
            HttpURLConnection conection = (HttpURLConnection) url.openConnection();
            conection.setDoOutput(true);
            conection.setRequestMethod("POST");
            conection.setRequestProperty("Content-Type", "application/json");
            ObjectMapper mapper = new ObjectMapper();
            String string = mapper.writeValueAsString(requestDTO);
            OutputStream os = conection.getOutputStream();
            os.write(string.getBytes());
            os.flush();

            if (conection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conection.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conection.getInputStream())));
            String output;
            JSONObject response = null;
            while ((output = br.readLine()) != null) {
                response = new JSONObject(output);
                if (Constants.CODEOK == response.getInt("code")) {
                    redirect = "/usuarioDTO/List?faces-redirect=true";
                    //agregar sesion is logged
                    ExternalContext context = FacesContext.getCurrentInstance().getExternalContext(); 
                    context.getSessionMap().put("IS_LOGIN", "OK");

                } else {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Credenciales incorrectas / Acceso no disponible para el usuario"));
                }
            }
            conection.disconnect();

        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        return redirect;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String resetRequest() {
        String redirect = null;
        try {
            if (requestDTO.getUsername().isEmpty() || requestDTO.getUsername().equals("") || requestDTO.getUsername() == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Primero ingrese el usuario para resetear"));
            }
            URL url = new URL("http://localhost:80/resetPassword/" + requestDTO.getUsername());
            StringBuilder result = new StringBuilder();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            for (String line; (line = reader.readLine()) != null;) {
                result.append(line);
            }
            
          

             JSONObject response = new JSONObject(result.toString());
                if (Constants.CODEOK == response.getInt("code")) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso", "Se reseteo la clave para el usuario "+ requestDTO.getUsername()));
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "No se pudo resetear la clave"));
                }


        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        return redirect;
    }
    public LoginRequestDTO getRequestDTO() {
        return requestDTO;
    }

    public void setRequestDTO(LoginRequestDTO requestDTO) {
        this.requestDTO = requestDTO;
    }
    
    public void logout() {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.getSessionMap().put("IS_LOGIN", "");
        System.out.print("Sesion terminada para el usuario");

        ExternalContext ec = FacesContext.getCurrentInstance()
                .getExternalContext();
        try {
            ec.redirect(ec.getRequestContextPath()
                    + "");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    

}
