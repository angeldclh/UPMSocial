/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
import javax.ws.rs.core.GenericEntity;
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
@Path("users")
public class UsuarioFacadeREST extends AbstractFacade<Usuario> {

    @PersistenceContext(unitName = "UPMsocialSOSPU")
    private EntityManager em;

    public UsuarioFacadeREST() {
        super(Usuario.class);
    }

    //Crea un nuevo USUARIO
    @POST
    @Consumes({"application/xml"})
    public Response create(Usuario entity, @Context UriInfo uriInfo) {
        //Almacenar en la BD el hash de la contraseña, no la contraseña en claro
        entity.setPassword(String.valueOf(entity.getPassword().hashCode()));
        super.create(entity);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(entity.getNombreusuario());
        return Response.created(builder.build()).build();
    }

    //Modifica el PERFIL de USUARIO
    @PUT
    @Path("{id}")
    @Consumes({"application/xml"})
    public Response edit(@PathParam("id") String id, Usuario entity, @Context UriInfo uriInfo) {
        //Devolver not found si el id de la URI no corresponde a ningún usuario
        Usuario u;
        if ((u = super.find(id)) == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        //Devolver forbidden si se intenta cambiar el nombre de usuario (PK)
        if (!u.getNombreusuario().equals(entity.getNombreusuario())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        //Almacenar en la BD el hash de la contraseña, no la contraseña en claro
        entity.setPassword(String.valueOf(entity.getPassword().hashCode()));
        super.edit(entity);
        //Cabecera Location
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        return Response.status(Response.Status.NO_CONTENT).location(builder.build()).build();

    }

    //Elimina un USUARIO o su "perfil". 
    @DELETE
    @Path("{id}")
    public Response remove(@PathParam("id") String id) {
        Usuario u;
        //404 si el usuario que se quiere eliminar no existe
        if ((u = super.find(id)) == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        super.remove(u);
        return Response.status(Response.Status.NO_CONTENT).build();

    }

    //Buscar usuarios (posibles amigos) por patrón
    //Si la URI es /search devuelve todos los usuarios de la red
    //Devuelve siempre 200 OK aunque la lista sea vacía (Twitter y Facebook)
    @GET
    @Path("search")
    @Produces({"application/xml"})
    public List<Usuario> findPattern(@QueryParam("id") String id) {
        if (id == null) {
            id = "";
        }
        List results = em.createNamedQuery("Usuario.findByPattern")
                .setParameter("pattern", "%" + id + "%")
                .getResultList();
        return results;
    }

    //Obtener lista de amigos y filtrarla por nombre o limitar la cantidad de 
    //información obtenida por número de amigos   
    //Añadir Responses
    @GET
    @Path("{id}/friends")
    @Produces({"application/xml"})
    public Response findFriends(@PathParam("id") String id,
            @QueryParam("from") Integer from, @QueryParam("to") Integer to) {
        //El id de la URI no corresponde a ningún usuario -> 404 not found
        if (find(id) == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<Usuario> results = em.createNamedQuery("Usuario.getFriends")
                .setParameter("nombreusuario", id)
                //.setParameter("pattern", id + "%") //Búsqueda an: salen angel y ana, pero no manuel
                .getResultList();

        if (from != null && to != null) {
            results = results.subList(from, to + 1); //Revisar estos índices
        }

        //Devolver OK 200 + lista amigos en XML
        GenericEntity<List<Usuario>> entity = new GenericEntity<List<Usuario>>(results) {
        };
        return Response.ok(entity).build();

    }

    //Añadir un usuario a la lista de amigos: se le pasa text/plain con su nombreusuario (PK)
    @POST
    @Path("{id}/friends")
    @Consumes({"text/plain"})
    public Response addFriend(@PathParam("id") String id, String friendid,
            @Context UriInfo uriInfo) {
        Usuario user = super.find(id);
        Usuario amigo = super.find(friendid);
        //Si el usuario de la URI o el nuevo amigo no existen, error 404
        if (user == null || amigo == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Collection<Usuario> lista = user.getUsuarioCollection();
        //Si ya es amigo del amigo de entrada
        if (lista.contains(amigo)) {
            return Response.status(Response.Status.CONFLICT).build();
        }
        lista.add(amigo);
        user.setUsuarioCollection(lista);
        getEntityManager().merge(user);
        //Crear URI para el nuevo amigo en la lista (para luego poder borrarlo)
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(friendid);
        return Response.status(Response.Status.NO_CONTENT).location(builder.build()).build();
    }

    //Eliminar usuario id2 de la lista de amigos de id
    @POST
    @Path("{id}/friends/{id2}/")
    public Response deleteFriend(@PathParam("id") String id, @PathParam("id2") String id2) {

        Usuario user = super.find(id);
        Usuario amigo = super.find(id2);
        if (user == null || amigo == null || !user.getUsuarioCollection().contains(amigo)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Collection<Usuario> lista = user.getUsuarioCollection();
        lista.remove(amigo);
        user.setUsuarioCollection(lista);
        getEntityManager().merge(user);

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    //Obtener los posts de los amigos de un usuario
    //En el XML aparece primero el post más reciente
    @GET
    @Path("{id}/timeline")
    public Response getTimeline(@PathParam("id") String id) {
        //El id de la URI no corresponse a ningún usuario -> 404 not found
        if (find(id) == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<Usuario> listaAmigos = em.createNamedQuery("Usuario.getFriends")
                .setParameter("nombreusuario", id)
                .getResultList();
        
        List<Post> timeline = em.createNamedQuery("Post.getTimeline")
                .setParameter("listaamigos", listaAmigos)
                .getResultList();
        
        GenericEntity<List<Post>> entity = new GenericEntity<List<Post>>(timeline) {
        };

        return Response.ok(entity).build();
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

    
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
