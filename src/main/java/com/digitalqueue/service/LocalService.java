package com.digitalqueue.service;

import com.digitalqueue.dto.ActualizarLogoLocalRequest;
import com.digitalqueue.dto.ActualizarLogoLocalResponse;
import com.digitalqueue.exception.RecursoNoEncontradoException;
import com.digitalqueue.model.Local;
import com.digitalqueue.repository.LocalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LocalService {

    private final LocalRepository localRepository;

    @Transactional
    public ActualizarLogoLocalResponse actualizarLogo(Long localId, ActualizarLogoLocalRequest request) {
        Local local = localRepository.findById(localId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Local no encontrado."));

        local.setLinkImagenLogo(normalizarOpcional(request.getLinkImagenLogo()));
        localRepository.save(local);

        return new ActualizarLogoLocalResponse(true, "Imagen del local actualizada correctamente.");
    }

    private String normalizarOpcional(String valor) {
        if (!StringUtils.hasText(valor)) {
            return null;
        }
        return valor.trim();
    }
}
