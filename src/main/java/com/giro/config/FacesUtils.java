package com.giro.config;

import org.primefaces.PrimeFaces;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

@Component
public class FacesUtils {

    public void exibeErro(String mensagem) {
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", mensagem);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.addMessage(":frm-growl-msg:growl", facesMessage);
    }

    public void exibeErro(String summary, String mensagem) {
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, mensagem);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.addMessage(":frm-growl-msg:growl", facesMessage);
    }

    public void exibeInfo(String mensagem) {
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, "", mensagem);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.addMessage(":frm-growl-msg:growl", facesMessage);
    }

    public void exibeWarn(String mensagem) {
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_WARN, "", mensagem);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.addMessage(":frm-growl-msg:growl", facesMessage);
    }

    public void exibeInfo(String summary, String mensagem) {
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, mensagem);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.addMessage(":frm-growl-msg:growl", facesMessage);
    }

    public void exibeMensagemErro(String mensagem, String idComponente) {
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", mensagem);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.addMessage(idComponente, facesMessage);
    }

    public void update(String... componenteIdExpressaoAbsoluta) {
        PrimeFaces.current().ajax().update(componenteIdExpressaoAbsoluta);
    }

    public void updateGrowl() {
        PrimeFaces.current().ajax().update(":frm-growl-msg:growl");
    }

    public void dialogInfo(String summary, String detail) {
        FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_INFO, summary, detail);
        PrimeFaces.current().dialog().showMessageDynamic(message);
    }

    public void hide(String componenteIdExpressaoAbsoluta) {
        PrimeFaces.current().executeScript("PF('"+componenteIdExpressaoAbsoluta+"').hide();");
    }

    public void resetInput(String componenteIdExpressaoAbsoluta) {
        PrimeFaces.current().resetInputs(componenteIdExpressaoAbsoluta);
    }



}
