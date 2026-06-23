package com.digitalqueue.service;

import com.digitalqueue.exception.RecursoNoEncontradoException;
import com.digitalqueue.repository.FilaRepository;
import com.digitalqueue.repository.TurnoRepository;
import com.digitalqueue.security.AdminPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAccessService {

    private final FilaRepository filaRepository;
    private final TurnoRepository turnoRepository;

    public Long resolverFilaId(Long filaIdSolicitado) {
        Long localId = principalActual().localId();

        if (filaIdSolicitado == null) {
            return filaRepository.findFirstByLocalIdOrderByIdAsc(localId)
                    .map(fila -> fila.getId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("El local no tiene una fila configurada"));
        }

        if (!filaRepository.existsByIdAndLocalId(filaIdSolicitado, localId)) {
            throw new AccessDeniedException("La fila no pertenece al administrador autenticado");
        }
        return filaIdSolicitado;
    }

    public void validarTurno(Long turnoId) {
        if (!turnoRepository.existsByIdAndFilaLocalId(turnoId, principalActual().localId())) {
            throw new AccessDeniedException("El turno no pertenece al administrador autenticado");
        }
    }

    public void validarLocal(Long localId) {
        if (!principalActual().localId().equals(localId)) {
            throw new AccessDeniedException("El local no pertenece al administrador autenticado");
        }
    }

    private AdminPrincipal principalActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AdminPrincipal principal)) {
            throw new AccessDeniedException("Administrador no autenticado");
        }
        return principal;
    }
}
