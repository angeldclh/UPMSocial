/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.service;

import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
@Path("/usuarios/{user}/posts")
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

    @POST
    @Consumes({"application/xml"})
    public Response create2(@PathParam("user") String id, Post entity, @Context UriInfo uriInfo) {
        Usuario u = (Usuario) em.createNamedQuery("Usuario.findByNombreusuario")
                .setParameter("nombreusuario", id)
                .getSingleResult();
        entity.setNombreusuario(u);
        entity.setFechahora(new Date());
        //El id del post es la concatenación del nombre de usuario, el string "Post" y el hash de la fecha
        entity.setIdpost(entity.getNombreusuario().getNombreusuario() 
                + "Post" + entity.getFechahora().toString().hashCode());
        create(entity);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(entity.getIdpost());
        return Response.created(builder.build()).build();
    }

    //Editar un POST
    @PUT
    @Path("{id}")
    @Consumes({"application/xml"})
    public void edit(@PathParam("id") String id, Post entity) {
        entity.setFechahora(new Date());
        super.edit(entity);
    }

    //Eliminar un POST
    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") String id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces({"application/xml"})
    public Post find(@PathParam("id") String id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces({"application/xml"})
    public List<Post> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({"application/xml"})
    public List<Post> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces("text/plain")
    public String countREST() {
        return String.valueOf(super.count());
    }

    
    //Obtener los posts de un usuario y filtrar la lista por fecha o limitar la
    //cantidad de información obtenida por número de posts
    @GET
    @Path("search")
    
    
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
