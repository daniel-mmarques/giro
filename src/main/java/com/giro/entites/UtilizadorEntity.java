package com.giro.entites;

import com.giro.config.Model;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.io.Serializable;

@Entity
@Data
@Model
@Table(name = "UTILIZADOR")
public class UtilizadorEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "NOME")
    private String nome;

    @Email
    @Column(name = "EMAIL")
    private String email;

    @Column(name = "ATIVO")
    private Boolean ativo;

    @Column(name = "PINACTIVACAO")
    private String pinAtivacao;

    public String getShortName() {
        if (StringUtils.isNotBlank(this.nome)) {
            if (this.nome.length() >= 15) {
                return this.nome.substring(0, 15);
            }
        }
        return this.nome;
    }

    public UtilizadorEntity() {
    }

    public UtilizadorEntity(String nome, Boolean ativo) {
        this.nome = nome;
        this.ativo = ativo;
    }

}