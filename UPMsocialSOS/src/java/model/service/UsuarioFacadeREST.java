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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import model.Usuario;

/**
 *
 * @author RAFAEL
 */
@Stateless
@Path("users")
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

    //Crea un nuevo USUARIO
    @POST
    @Consumes({"application/xml"})
    public Response create2(Usuario entity, @Context UriInfo uriInfo) {
        //Almacenar en la BD el hash de la contraseña, no la contraseña en claro
        entity.setPassword(String.valueOf(entity.getPassword().hashCode()));
        create(entity);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(entity.getNombreusuario());
        return Response.created(builder.build()).build();
    }

    
    //Modifica el PERFIL de USUARIO
    @PUT
    @Path("{id}")
    @Consumes({"application/xml"})
    public Response edit(@PathParam("id") String id, Usuario entity, @Context UriInfo uriInfo) {
        //Almacenar en la BD el hash de la contraseña, no la contraseña en claro
        entity.setPassword(String.valueOf(entity.getPassword().hashCode()));
        super.edit(entity);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        return Response.created(builder.build()).build();
        
    }

    //Elimina un USUARIO o su "perfil"
    @DELETE
    @Path("{id}")
    public Response remove(@PathParam("id") String id) {
        /*Usuario u;
        if((u = super.find(id)) == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        super.remove(u);
        return Response.status(Response.Status.NO_CONTENT).build();*/
        super.remove(super.find(id));
        return Response.status(Response.Status.NO_CONTENT).build();
    }

  
    //Buscar usuarios (posibles amigos) por patrón
    //Si la URI es /search devuelve todos los usuarios de la red
    @GET
    @Path("search")
    @Produces({"application/xml"})
    public List<Usuario> findPattern(@QueryParam("id") String id) {
        if(id==null) id = "";
        List results = em.createNamedQuery("Usuario.findByPattern")
                .setParameter("pattern", "%"+id+"%")
                .getResultList();
        return results;
    }

    //Obtener lista de amigos y filtrarla por nombre o limitar la cantidad de 
    //información obtenida por número de amigos
    
    //Tira un bonito Null Pointer por el tema del many to many
    @GET
    @Path("{id}/friends")
    @Produces({"application/xml"})
    public List<Usuario> findFriend(@QueryParam("id") String id, @QueryParam("from")
    Integer from, @QueryParam("to") Integer to){
        if(id==null) id = "";
        List results = em.createNamedQuery("Usuario.findFriends")
                .setParameter("pattern", "%"+id+"%") //Búsqueda an: salen angel y ana, pero no manuel
                .getResultList();
        
        if(from != 0 && to != 0){
            results = results.subList(from-1, to+1); //Revisar estos índices
        }
        
        return results;
    }
    
    
    
    
    //Añadir un usuario a la lista de amigos: se le pasa text/plain con su nombreusuario (PK)
    @POST
    @Path("{id}/friends")
    @Consumes({"text/plain"})
    public Response addFriend(@PathParam("id") String id, String entity) {
        Usuario user = super.find(id);
        if (user == null || super.find(entity) == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Collection<Usuario> lista = user.getUsuarioCollection();
        Usuario amigo = super.find(entity);
        //Si ya es amigo del amigo de entrada
        if (lista.contains(amigo)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        lista.add(amigo);
        user.setUsuarioCollection(lista);
        getEntityManager().merge(user);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    //Eliminar usuario de la lista de amigos
    @POST
    @Path("{id}/friends/{id2}/")
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
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    
    

    //Buscar usuario por id. Devuelve un XML con su perfil
    @GET
    @Path("{id}")
    @Produces({"application/xml"})
    public Usuario find(@PathParam("id") String id) {
        return super.find(id);
    }

    //Devuelve una lista con todos los usuarios
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
