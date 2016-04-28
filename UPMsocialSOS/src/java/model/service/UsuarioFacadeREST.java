/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.service;

import java.util.Collection;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import model.Usuario;

/**
 *
 * @author RAFAEL
 */
@Stateless
@Path("usuarios")
public class UsuarioFacadeREST extends AbstractFacade<Usuario> {

    @PersistenceContext(unitName = "UPMsocialSOSPU")
    private EntityManager em;

    public UsuarioFacadeREST() {
        super(Usuario.class);
    }

    //Guarda bbdd un nuevo USUARIO
    @Override
    public void create(Usuario entity) {
        super.create(entity);
    }

    //    Crea un nuevo USUARIO
    @POST
    @Consumes({"application/xml"})
    public Response create2(Usuario entity, @Context UriInfo uriInfo) {
        create(entity);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(entity.getNombreusuario());
        return Response.created(builder.build()).build();
    }

    //Añadir un usuario a la lista de amigos
    @POST
    @Path("{id}/amigos")
    @Consumes({"text/plain"})
    public Response addFriend(@PathParam("id") String id, String entity) {
        Usuario user = super.find(id);
        if (user == null || super.find(entity) == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Collection<Usuario> lista = user.getUsuarioCollection();  
        Usuario amigo = super.find(entity);
        //Si ya es amigo del amigo de entrada*/
        if(lista.contains(amigo)){
         return Response.status(Response.Status.UNAUTHORIZED).build();
        }   
        lista.add(amigo);
        user.setUsuarioCollection(lista);
        getEntityManager().merge(user);
        return Response.noContent().build();
    }

    //Eliminar usuario de la lista de amigos
    @POST
    @Path("{id}/amigos/{id2}/")
    public Response deleteFriend(@PathParam("id") String id, @PathParam("id2") String id2) {

        Usuario user = super.find(id);
        Usuario amigo = super.find(id2);
        if (user == null || amigo == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        
        Collection<Usuario> lista = user.getUsuarioCollection();
        lista.remove(amigo);
        user.setUsuarioCollection(lista);
        getEntityManager().merge(user);
        return Response.noContent().build();
    }

    //Modifica el PERFIL de USUARIO
    @PUT
    @Path("{id}")
    @Consumes({"application/xml"})
    public void edit(@PathParam("id") String id, Usuario entity) {
        super.edit(entity);
    }

    //Elimina un USUARIO o su "perfil"
    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") String id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces({"application/xml"})
    public Usuario find(@PathParam("id") String id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces({"application/xml"})
    public List<Usuario> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({"application/xml"})
    public List<Usuario> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces("text/plain")
    public String countREST() {
        return String.valueOf(super.count());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
