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
@Path("/users/{userid}/posts")
public class PostFacadeREST extends AbstractFacade<Post> {

    @PersistenceContext(unitName = "UPMsocialSOSPU")
    private EntityManager em;

    public PostFacadeREST() {
        super(Post.class);
    }

    //Crear un POST
    //Se recibe solo texto plano. El usuario se obtiene de la URI y la fecha es la actual
    @POST
    @Consumes({"text/plain"})
    public Response create(@PathParam("userid") String id, String texto, @Context UriInfo uriInfo) {
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
        super.create(entity);
        //Para la response
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(entity.getIdpost());
        return Response.created(builder.build()).build();
    }

    //Editar un POST (solo el texto y obviamente la fecha)
    @PUT
    @Path("{postid}")
    @Consumes({"text/plain"})
    public Response edit(@PathParam("userid") String userid, @PathParam("postid") String postid,
            String texto, @Context UriInfo uriInfo) {
        Post entity = find(postid);
        //Error 404 si el idpost no existe o si el userid de la URI no se corresponde con el autor del post
        if (entity == null || !entity.getNombreusuario().getNombreusuario().equals(userid)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        entity.setTexto(texto);
        entity.setFechahora(new Date());
        //El id no cambia ya que es la clave primaria
        super.edit(entity);
        //Cabecera Location + no content
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        return Response.status(Response.Status.NO_CONTENT).location(builder.build()).build();
    }

    //Eliminar un POST pasándole su id
    @DELETE
    @Path("{postid}")
    public Response remove(@PathParam("postid") String id) {
        Post p = super.find(id);
        if (p == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        super.remove(super.find(id));
        return Response.status(Response.Status.NO_CONTENT).build();
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
    //En la URI se especifica la fecha en formato YYYY-MM-DD
    @GET
    @Produces({"application/xml"})
    public Response showPosts(@PathParam("userid") String userid,
            @QueryParam("date") String fecha, @QueryParam("from") Integer from,
            @QueryParam("to") Integer to) {

        //Obtener usuario u (autor de los posts) a partir de su id
        Usuario u;
        try {
            u = (Usuario) em.createNamedQuery("Usuario.findByNombreusuario")
                    .setParameter("nombreusuario", userid)
                    .getSingleResult();
        } catch (javax.persistence.NoResultException ex) {
            //Si no hay ningún usuario con nombreusuario userid, 404
            Logger.getLogger(PostFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<Post> list;
        if (fecha != null) {
            //Parsear la fecha de la URI a un objeto Date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date;
            try {
                date = sdf.parse(fecha);
            } catch (ParseException ex) {
                //La fecha de la URI no se parsea correctamente (formato incorrecto) = 400
                Logger.getLogger(PostFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            list = em.createNamedQuery("Post.findByUserAndDate")
                    .setParameter("nombreusuario", u)
                    .setParameter("fechahora", date)
                    .getResultList();

        } else { //No se especifica fecha -> todos los posts del usuario especificado
            list = em.createNamedQuery("Post.findByUser")
                    .setParameter("nombreusuario", u)
                    .getResultList();
        }

        //Filtrar por cantidad de información
        if (from != null && to != null) {
            //Query sintácticamente correcta, pero semánticamente no
            if (from > list.size() || to > list.size() || from > to) {
                return Response.status(422).build();
            }

            //Para que p ej devuelva el primer post en vez de nada si from=0 y to=0 
            list = list.subList(from, to + 1);
        }

        GenericEntity<List<Post>> entity = new GenericEntity<List<Post>>(list) {
        };

        return Response.ok(entity).build();

    }

    //Obtener el número de post publicados por mi en la red social 
    //en un determinado periodo(fecha de inicio y fin)
    //Las fechas incluyen como hora 00:00, por lo que si from=to va a devolver 0
    @GET
    @Path("nPosts")
    @Produces({"text/plain"})
    public Response nPosts(@PathParam("userid") String userid, @QueryParam("date1") String date1,
            @QueryParam("date2") String date2) {
        //usuario autor de los posts
        Usuario u;
        List<Post> list;
        try {
            u = (Usuario) em.createNamedQuery("Usuario.findByNombreusuario")
                    .setParameter("nombreusuario", userid)
                    .getSingleResult();
        } catch (javax.persistence.NoResultException ex) {
            Logger.getLogger(PostFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (date1 != null && date2 != null) {
            //Parsear la fecha de la URI a un objeto Date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date fecha1;
            Date fecha2;
            try {
                fecha1 = sdf.parse(date1);
                fecha2 = sdf.parse(date2);
            } catch (ParseException ex) {
                Logger.getLogger(PostFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            list = em.createNamedQuery("Post.findByUserAndTwoDates")
                    .setParameter("nombreusuario", u)
                    .setParameter("fechahora", fecha1)
                    .setParameter("fechahora1", fecha2)
                    .getResultList();
        } else {
            list = em.createNamedQuery("Post.findByUser")
                    .setParameter("nombreusuario", u)
                    .getResultList();
        }

        return Response.ok(list.size()).build();
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
