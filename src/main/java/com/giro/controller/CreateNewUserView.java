package com.giro.controller;

import com.giro.config.CryptPassword;
import com.giro.config.FacesUtils;
import com.giro.entites.UtilizadorEntity;
import com.giro.entites.email.EmailModel;
import com.giro.entites.enums.EmailStatusEnum;
import com.giro.service.EmailService;
import com.giro.service.UtilizadorService;
import com.giro.utils.SAML20Utils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.UUID;

@Data
@Slf4j
@Controller
@ViewScoped
public class CreateNewUserView implements Serializable {

    private static final long serialVersionUID = 1L;

    private UtilizadorEntity utilizadorEntity;
    private String passwordConfirm;
    private String param;
    private Boolean renderPassword;
    private Boolean nifNipcReadOnly;
    private Boolean renderButtonSave;
    private String sede;

    @Autowired
    private UtilizadorService utilizadorService;

    @Autowired
    private FacesUtils facesUtils;

    @Autowired
    private EmailService emailService;

    @PostConstruct
    public void init() {
        renderButtonSave = Boolean.TRUE;
        nifNipcReadOnly = Boolean.FALSE;
        renderPassword = Boolean.TRUE;
        utilizadorEntity = new UtilizadorEntity();
    }

    public String save() {

        if (this.validarUsuario()) {

            if (this.renderPassword.equals(Boolean.TRUE)) {

                if (!utilizadorEntity.getPassword().equals(passwordConfirm)) {
                    this.facesUtils.exibeErro("Erro ao Salvar!");
                    this.facesUtils.update(":frm-new-user:growl");
                    log.error("Tentativa de salvar com senha diferentes");
                    return null;
                }

                utilizadorEntity.setAtivo(Boolean.FALSE);
                utilizadorEntity.setPinAtivacao(UUID.randomUUID().toString());
                utilizadorEntity.setPassword(encode(utilizadorEntity.getPassword()));
                utilizadorEntity = utilizadorService.save(utilizadorEntity);

                var emailModel = emailService.sendEmail(prepareEmailForSending(utilizadorEntity));

                if (emailModel != null && emailModel.getId() != null) {
                    if (emailModel.getEmailStatus().equals(EmailStatusEnum.SEND)) {
                        return "verificarEmail.xhtml";
                    } else {
                        return null;
                    }
                }

            } else {
                utilizadorEntity.setAtivo(Boolean.TRUE);
                utilizadorEntity = utilizadorService.save(utilizadorEntity);
                return "index.xhtml?faces-redirect=true";
            }
        }
        return null;
    }

    private EmailModel prepareEmailForSending(UtilizadorEntity utilizadorEntity) {
        EmailModel emailModel = new EmailModel();
        emailModel.setOwnerRef(utilizadorEntity.getNome());
        emailModel.setEmailFrom("giro@giro.pt");
        emailModel.setEmailTo(utilizadorEntity.getEmail());
        emailModel.setSubject("Ativar seu perfil");
        var uuidEncoded = SAML20Utils.base64Encode(utilizadorEntity.getPinAtivacao());
        emailModel.setText(" Seu cadastro foi realizado com <b>sucesso</b>.<br/><br/> Aceda ao link abaixo para <b>ativar</b> seu perfil. <br/><br/>" + "<a href='https://dev.impic.pt/areareservada/activate/" + uuidEncoded + "' target='_blank'>https://dev.impic.pt/areareservada/activate</a>");
        return emailModel;
    }

    private String encode(CharSequence plainTextPassword) {
        return CryptPassword.encrypt(plainTextPassword.toString(), "123456789");
    }

    public Boolean validarUsuario() {

        var utilizadorOptional = utilizadorService.findByEmail(utilizadorEntity.getEmail());

        if (utilizadorOptional.isPresent()) {

            var msg = new String();

            if (utilizadorOptional.get().getAtivo() == Boolean.FALSE) {
                msg = "Usuário já cadastrado, aceda ao email cadastrado e ative sua conta!";
            } else {
                msg = "Usuário já cadastrado!";
            }

            this.facesUtils.exibeErro(msg);
            this.facesUtils.update(":frm-new-user:growl");
            return Boolean.FALSE;
        }

        return Boolean.TRUE;

    }

    public void onLoad() {

        if (StringUtils.isNotBlank(param)) {

            var paramDecoded = decodeParam();

            if (paramDecoded == null) {
                this.renderButtonSave = Boolean.FALSE;
                return;
            }

            this.renderButtonSave = Boolean.FALSE;
            return;

        }

    }

    private String[] decodeParam() {

        try {

            var decoded = new String(Base64.decode(param));
            var splited = decoded.split("&");

            if (splited.length == 2) {
                if (StringUtils.isNotBlank(splited[0]) && StringUtils.isNotBlank(splited[1])) {
                    return splited;
                }
            }

        } catch (Base64DecodingException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

}
