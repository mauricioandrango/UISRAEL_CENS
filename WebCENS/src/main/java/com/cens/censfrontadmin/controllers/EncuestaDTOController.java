package com.cens.censfrontadmin.controllers;

import com.cens.censfrontadmin.dto.EncuestaDTO;
import com.cens.censfrontadmin.controllers.util.JsfUtil;
import com.cens.censfrontadmin.controllers.util.PaginationHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import static javax.ws.rs.client.Entity.entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.json.JSONArray;
import org.json.JSONObject;

@ManagedBean
@SessionScoped
public class EncuestaDTOController implements Serializable {

    private EncuestaDTO selected;
    private Client wsClient;
    private WebTarget wgetTarget;
    private static String URL_BASE = "http://localhost:80/";
    private DataModel items = null;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    
    private Date fechaInicio;
    private Date fechaFin;

    public EncuestaDTOController() {
        wsClient = ClientBuilder.newClient();
       verificarSesionUsuario();

    }
    
    public void verificarSesionUsuario(){
         // check if user is logged
        //agregar sesion is logged
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        String usuarioLogeado = context.getSessionMap().get("IS_LOGIN").toString();
        System.err.println("USUARIO LOGEADO: " + usuarioLogeado);
        if (usuarioLogeado == null || usuarioLogeado.isEmpty()) {
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

    public EncuestaDTO getSelected() {
        if (selected == null) {
            selected = new EncuestaDTO();
            selectedItemIndex = -1;
        }
        return selected;
    }

    public void setSelected(EncuestaDTO selected) {
         this.selected = selected;
        try {
            // when selected change update dates mus be  updated
            //Date date1=new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
            fechaInicio = new SimpleDateFormat("yyyy-MM-dd").parse(selected.getFechaInicio());
            fechaFin = new SimpleDateFormat("yyyy-MM-dd").parse(selected.getFechaFin());
        } catch (ParseException ex) {
            Logger.getLogger(EncuestaDTOController.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    return findAll().size();
                    //return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(findAll());
                    //return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        selected = (EncuestaDTO) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        selected = new EncuestaDTO();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            createWS(selected);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundletwo").getString("EncuestaDTOCreated"));
            return prepareCreate();
        } catch (Exception e) {
            e.printStackTrace();
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundletwo").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        selected = (EncuestaDTO) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            edit(selected);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundletwo").getString("EncuestaDTOUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundletwo").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        //selected = (EncuestaDTO) getItems().getRowData();
        //selected = pagination.getPageFirstItem() + getItems().gUetRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            remove(selected);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundletwo").getString("EncuestaDTODeleted"));
            // JsfUtil.addSuccessMessage("Encuesta Borrada");

        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundletwo").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            //TODO current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(findAll(), true);
    }

    public EncuestaDTO getEncuestaDTO(java.lang.Long id) {
        return find(id);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    private void edit(EncuestaDTO current) {
        try {
            
             //agregar fecha de inicio y de fin 
            Date initDate = new Date();
            Date finishDate = addDays(initDate, 30);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            
            String strInitDate = formatter.format(getFechaInicio());
            String strEndDate = formatter.format(getFechaFin());
            current.setFechaInicio(strInitDate);
            current.setFechaFin(strEndDate);
            
            
            URL url = new URL("http://localhost:80/updateEncuesta");
            HttpURLConnection conection = (HttpURLConnection) url.openConnection();
            conection.setDoOutput(true);
            conection.setRequestMethod("POST");
            conection.setRequestProperty("Content-Type", "application/json");
            ObjectMapper mapper = new ObjectMapper();
            String string = mapper.writeValueAsString(current);
            System.err.println("JSON to UPDATE encuesta:"+ string);

            OutputStream os = conection.getOutputStream();
            os.write(string.getBytes());
            os.flush();

            if (conection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conection.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conection.getInputStream())));
            conection.disconnect();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
    }

    private void remove(EncuestaDTO current) {
        String id = String.valueOf(current.getId()) + "";
        WebTarget removeTarget = wsClient.target(URL_BASE).path("deleteEncuesta/" + id);
        String get = removeTarget.request().delete(String.class);
        System.out.println(get);
    }

    private int count() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private List<EncuestaDTO> findAll() {
        WebTarget findAllTarget = wsClient.target(URL_BASE).path("allEncuestas");
        String get = findAllTarget.request(MediaType.APPLICATION_JSON).get(String.class);
        JSONObject object = new JSONObject(get);
        return castToList(object.getJSONArray("data"));
    }

    public List<EncuestaDTO> castToList(JSONArray encuestas) {
        List<EncuestaDTO> list = new ArrayList<>();
        for (int i = encuestas.length() - 1; i >= 0; i--) {
            EncuestaDTO encuesta = new EncuestaDTO(encuestas.get(i).toString());
            list.add(encuesta);
        }
        return list;
    }

    private EncuestaDTO find(Long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //JSON REQUEST PARA CREAR ENCUESTA
    /*
    {
    "nombre": "encuestaTest30Enero2021v2",
    "descripcion": "encuesta7",
    "fechaInicio": "2021-01-01",
    "fechaFin": "2021-01-01"
    }
    
    */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    private void createWS(EncuestaDTO current) {
        try {

            //agregar fecha de inicio y de fin 
            Date initDate = new Date();
            Date finishDate = addDays(initDate, 30);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            
            String strInitDate = formatter.format(getFechaInicio());
            String strEndDate = formatter.format(getFechaFin());
            current.setFechaInicio(strInitDate);
            current.setFechaFin(strEndDate);

            URL url = new URL("http://localhost:80/createEncuesta");
            HttpURLConnection conection = (HttpURLConnection) url.openConnection();
            conection.setDoOutput(true);
            conection.setRequestMethod("POST");
            conection.setRequestProperty("Content-Type", "application/json");
            ObjectMapper mapper = new ObjectMapper();
            String string = mapper.writeValueAsString(current);
            System.err.println("JSON to create encuesta:"+ string);
            OutputStream os = conection.getOutputStream();
            os.write(string.getBytes());
            os.flush();

            if (conection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conection.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conection.getInputStream())));
            conection.disconnect();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
    }

    @FacesConverter(forClass = EncuestaDTO.class)
    public static class EncuestaDTOControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            EncuestaDTOController controller = (EncuestaDTOController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "encuestaDTOController");
            return controller.getEncuestaDTO(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof EncuestaDTO) {
                EncuestaDTO o = (EncuestaDTO) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + EncuestaDTO.class.getName());
            }
        }

    }
    


    public static Date addDays(Date date, int days)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
        System.out.println(fechaInicio);

    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

}
