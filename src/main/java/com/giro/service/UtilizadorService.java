package com.giro.service;

import com.giro.entites.UtilizadorEntity;
import com.giro.repository.UtilizadorRepository;
import com.giro.utils.SAML20Utils;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class UtilizadorService {

    @Autowired
    private UtilizadorRepository utilizadorRepository;

    public Optional<UtilizadorEntity> findByNifNipcAndUtilizadorActivo(String nifNipc) {
        return utilizadorRepository.findByEmailAndAtivo(nifNipc, Boolean.TRUE);
    }

    public Optional<UtilizadorEntity> findByEmail(String email) {
        return utilizadorRepository.findByEmail(email);
    }

    public Boolean activateProfile(String uuid) {
        try {
            String uuidDecoded = SAML20Utils.base64Decode(uuid);
            var result = utilizadorRepository.activateProfile(uuidDecoded);
            return (result == 1) ? Boolean.TRUE : Boolean.FALSE ;
        } catch (Base64DecodingException e) {
            e.printStackTrace();
        }
       return Boolean.FALSE;
    }

    @Transactional
    public UtilizadorEntity save(UtilizadorEntity utilizadorModel) {
        return utilizadorRepository.save(utilizadorModel);
    }

}
