/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.resources;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.Usuario;

/**
 *
 * @author angel
 */
@Stateless
@Path("usuario")
public class UsuarioFacadeREST extends AbstractFacade<Usuario> {

    @PersistenceContext(unitName = "UPMSocialNBPU")
    private EntityManager em;

    public UsuarioFacadeREST() {
        super(Usuario.class);
    }

    //Añadir usuario: se le da el XML sin el id (clave primaria)
    @POST
    @Override
    @Consumes({MediaType.APPLICATION_XML})
    public void create(Usuario entity) {
        super.create(entity);
    }

    //Modificar el perfil de un usuario con id {id}
    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML})
    public void edit(@PathParam("id") Integer id, Usuario entity) {
        super.edit(entity);
    }

    //Eliminar usuario
    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Integer id) {
        super.remove(super.find(id));
    }

    
    //Obtener el XML de un usuario
    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML})
    public Response find(@PathParam("id") Integer id) {
        if (super.find(id) == null)
                return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(super.find(id)).build();
    }

    //Obtener lista de todos los usuarios
    @GET
    @Produces({MediaType.APPLICATION_XML})
    public Response findAll2() {
        List<Usuario> lista = super.findAll();
        if (lista.isEmpty())
            return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(super.findAll()).build();
    }

    //
    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML})
    public List<Usuario> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    //Obtener el número de usuarios de la red
    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(super.count());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
}
