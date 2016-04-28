/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import model.Post;
import model.Usuario;

/**
 *
 * @author RAFAEL
 */
@Stateless
@Path("/users/{userid}/posts")
public class PostFacadeREST extends AbstractFacade<Post> {

    @PersistenceContext(unitName = "UPMsocialSOSPU")
    private EntityManager em;

    public PostFacadeREST() {
        super(Post.class);
    }

    //Crear un POST
    @Override
    public void create(Post entity) {
        super.create(entity);
    }

    //Se recibe solo texto plano. El usuario se obtiene de la URI y la fecha es la actual
    @POST
    @Consumes({"text/plain"})
    public Response create2(@PathParam("userid") String id, String texto, @Context UriInfo uriInfo) {
        Post entity = new Post();
        Usuario u = (Usuario) em.createNamedQuery("Usuario.findByNombreusuario")
                .setParameter("nombreusuario", id)
                .getSingleResult();
        entity.setNombreusuario(u);
        entity.setFechahora(new Date());
        //El id del post es la concatenación del nombre de usuario, el string "Post" y el hash de la fecha
        entity.setIdpost(entity.getNombreusuario().getNombreusuario()
                + "Post" + entity.getFechahora().toString().hashCode());
        entity.setTexto(texto);
        create(entity);
        //Para la response
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(entity.getIdpost());
        return Response.created(builder.build()).build();
    }

    //Editar un POST (solo el texto y obviamente la fecha)
    @PUT
    @Path("{postid}")
    @Consumes({"text/plain"})
    public void edit(@PathParam("postid") String id, String texto) {
        Post entity = find(id);
        entity.setTexto(texto);
        entity.setFechahora(new Date());
        super.edit(entity);
    }

    //Eliminar un POST pasándole su id
    @DELETE
    @Path("{postid}")
    public void remove(@PathParam("postid") String id) {
        super.remove(super.find(id));
    }

    //Devuelve un Post dado su id
    @GET
    @Path("{postid}")
    @Produces({"application/xml"})
    public Post find(@PathParam("postid") String id) {
        return super.find(id);
    }

    //Devuelve todos los posts de UPMSocial (para debug)
    @GET
    @Override
    @Path("all")
    @Produces({"application/xml"})
    public List<Post> findAll() {
        return super.findAll();
    }

    //Obtener los posts de un usuario y filtrar la lista por fecha o limitar la
    //cantidad de información obtenida por número de posts
    //En la URI se especifica la fecha en formato YYYYMMDD
    @GET
    @Produces({"application/xml"})
    public List<Post> showPosts(@PathParam("userid") String userid,
            @QueryParam("date") String fecha) {
        
        //Obtener usuario u (autor de los posts) a partir de su id
        Usuario u = (Usuario) em.createNamedQuery("Usuario.findByNombreusuario")
                .setParameter("nombreusuario", userid)
                .getSingleResult();
        if (fecha != null) {
            //Parsear la fecha de la URI a un objeto Date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = null;
            try {
                date = sdf.parse(fecha);
            } catch (ParseException ex) {
                Logger.getLogger(PostFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            }
            return em.createNamedQuery("Post.findByUserAndDate")
                    .setParameter("nombreusuario", u)
                    .setParameter("fechahora", date)
                    .getResultList();
        }
        return em.createNamedQuery("Post.findByUser")
                    .setParameter("nombreusuario", u)
                    .getResultList();

        //A esta query hay que pasarle una variable de la clase Usuario (u)
        /*return em.createNamedQuery("Post.findByUser")
                .setParameter("nombreusuario", u)
                .getResultList();*/
       
    }

///////////////////////////////
    //Devuelve los posts de todos los usuarios en el intervalo (from, to). Orden cronológico
    @GET
    @Path("{from}/{to}")
    @Produces({"application/xml"})
    public List<Post> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    //Devuelve el número de posts
    @GET
    @Path("count")
    @Produces("text/plain")
    public String countREST() {
        return String.valueOf(super.count());
    }

    //Necesario para hacer consultas
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
