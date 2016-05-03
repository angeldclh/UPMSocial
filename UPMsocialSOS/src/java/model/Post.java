/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author RAFAEL
 */
@Entity
@Table(name = "POST")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Post.findAll", query = "SELECT p FROM Post p"),
    @NamedQuery(name = "Post.findByIdpost", query = "SELECT p FROM Post p WHERE "
            + "p.idpost = :idpost"),
    @NamedQuery(name = "Post.findByTexto", query = "SELECT p FROM Post p WHERE "
            + "p.texto = :texto"),
    @NamedQuery(name = "Post.findByUser", query = "SELECT p FROM Post p WHERE "
            + "p.nombreusuario = :nombreusuario"),
    @NamedQuery(name = "Post.findByFechahora", query = "SELECT p FROM Post p "
            + "WHERE p.fechahora >= :fechahora"),
    @NamedQuery(name = "Post.findByUserAndDate", query = "SELECT p FROM Post p "
            + "WHERE p.nombreusuario = :nombreusuario AND p.fechahora >= :fechahora"),
    @NamedQuery(name = "Post.findByUserAndTwoDates", query = "SELECT p FROM Post p "
            + "WHERE p.nombreusuario = :nombreusuario AND p.fechahora >= :fechahora "
            + "AND p.fechahora < :fechahora1"),
    @NamedQuery(name = "Post.getTimeline", query = "SELECT p FROM Post p "
            + "WHERE p.nombreusuario IN :listaamigos "
            + "ORDER BY p.fechahora DESC")
})

public class Post implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 90)
    @Column(name = "IDPOST")
    private String idpost;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 140)
    @Column(name = "TEXTO")
    private String texto;
    @Basic(optional = false)
    @NotNull
    @Column(name = "FECHAHORA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechahora;
    @JoinColumn(name = "NOMBREUSUARIO", referencedColumnName = "NOMBREUSUARIO")
    @ManyToOne
    private Usuario nombreusuario;

    public Post() {
    }

    public Post(String idpost) {
        this.idpost = idpost;
    }
  
    public Post(String idpost, String texto, Date fechahora) {
        this.idpost = idpost;
        this.texto = texto;
        this.fechahora = fechahora;
    }

    public String getIdpost() {
        return idpost;
    }

    public void setIdpost(String idpost) {
        this.idpost = idpost;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Date getFechahora() {
        return fechahora;
    }

    public void setFechahora(Date fechahora) {
        this.fechahora = fechahora;
    }

    public Usuario getNombreusuario() {
        return nombreusuario;
    }

    public void setNombreusuario(Usuario nombreusuario) {
        this.nombreusuario = nombreusuario;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idpost != null ? idpost.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Post)) {
            return false;
        }
        Post other = (Post) object;
        if ((this.idpost == null && other.idpost != null) || (this.idpost != null && !this.idpost.equals(other.idpost))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.Post[ idpost=" + idpost + " ]";
    }
    
}
