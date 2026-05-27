package com.digitalqueue.service;

import com.digitalqueue.dto.CrearAdminRequest;
import com.digitalqueue.dto.CrearAdminResponse;
import com.digitalqueue.model.Fila;
import com.digitalqueue.model.Local;
import com.digitalqueue.model.PuntoAcceso;
import com.digitalqueue.model.UsuarioAdmin;
import com.digitalqueue.model.enums.EstadoFila;
import com.digitalqueue.model.enums.QueueStatus;
import com.digitalqueue.model.enums.RolAdmin;
import com.digitalqueue.model.enums.TipoAcceso;
import com.digitalqueue.model.enums.TipoDia;
import com.digitalqueue.model.enums.TipoOperacionLocal;
import com.digitalqueue.repository.FilaRepository;
import com.digitalqueue.repository.LocalRepository;
import com.digitalqueue.repository.PuntoAccesoRepository;
import com.digitalqueue.repository.UsuarioAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UsuarioAdminService {

    private final UsuarioAdminRepository usuarioAdminRepository;
    private final LocalRepository localRepository;
    private final FilaRepository filaRepository;
    private final PuntoAccesoRepository puntoAccesoRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CrearAdminResponse crearCuentaAdministrador(CrearAdminRequest request) {
        String nombre = normalizarNombre(request.getNombre());
        String email = normalizarEmail(request.getEmail());

        if (usuarioAdminRepository.existsByEmailIgnoreCase(email)) {
            return CrearAdminResponse.builder()
                    .creado(false)
                    .emailExistente(true)
                    .mensaje("Ya existe una cuenta administradora con ese email.")
                    .nombre(nombre)
                    .email(email)
                    .build();
        }

        Local local = crearLocal(request);
        Fila fila = crearFilaInicial(local);
        PuntoAcceso puntoAcceso = crearPuntoAccesoPublico(local, fila);

        UsuarioAdmin usuarioAdmin = UsuarioAdmin.builder()
                .local(local)
                .nombre(nombre)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(RolAdmin.ADMIN_LOCAL)
                .activo(true)
                .build();

        UsuarioAdmin usuarioGuardado = usuarioAdminRepository.save(usuarioAdmin);

        return CrearAdminResponse.builder()
                .creado(true)
                .emailExistente(false)
                .mensaje("Cuenta administradora creada correctamente.")
                .usuarioId(usuarioGuardado.getId())
                .localId(local.getId())
                .filaId(fila.getId())
                .nombre(usuarioGuardado.getNombre())
                .email(usuarioGuardado.getEmail())
                .rol(usuarioGuardado.getRol())
                .nombreLocal(local.getNombre())
                .direccionLocal(local.getDireccion())
                .linkImagenLogoLocal(local.getLinkImagenLogo())
                .tipoOperacionLocal(local.getTipoOperacion())
                .codigoPublico(puntoAcceso.getCodigoPublico())
                .build();
    }

    private Local crearLocal(CrearAdminRequest request) {
        Local local = Local.builder()
                .nombre(normalizarNombre(request.getNombreLocal()))
                .direccion(normalizarNombre(request.getDireccionLocal()))
                .linkImagenLogo(normalizarOpcional(request.getLinkImagenLogoLocal()))
                .activo(true)
                .tipoOperacion(tipoOperacionPara(request))
                .capacidadMaxima(capacidadMaximaPara(request))
                .personasActuales(0)
                .build();

        return localRepository.save(local);
    }

    private Fila crearFilaInicial(Local local) {
        Fila fila = Fila.builder()
                .local(local)
                .nombre("Fila principal")
                .estado(EstadoFila.ABIERTA)
                .queueStatus(QueueStatus.NORMAL)
                .tipoDia(TipoDia.NORMAL)
                .tiempoPromedioAtencionMinutos(3)
                .build();

        return filaRepository.save(fila);
    }

    private PuntoAcceso crearPuntoAccesoPublico(Local local, Fila fila) {
        PuntoAcceso puntoAcceso = PuntoAcceso.builder()
                .fila(fila)
                .codigoPublico(generarCodigoPublico(local))
                .tipoAcceso(TipoAcceso.QR)
                .activo(true)
                .build();

        return puntoAccesoRepository.save(puntoAcceso);
    }

    private TipoOperacionLocal tipoOperacionPara(CrearAdminRequest request) {
        if (request.getTipoOperacionLocal() == null) {
            return TipoOperacionLocal.ATENCION_RAPIDA;
        }
        return request.getTipoOperacionLocal();
    }

    private Integer capacidadMaximaPara(CrearAdminRequest request) {
        if (request.getCapacidadMaxima() == null) {
            return 20;
        }
        return request.getCapacidadMaxima();
    }

    private String generarCodigoPublico(Local local) {
        String base = Normalizer.normalize(local.getNombre(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");

        if (!StringUtils.hasText(base)) {
            base = "local";
        }
        return base + "-" + local.getId();
    }

    private String normalizarNombre(String nombre) {
        return nombre.trim();
    }

    private String normalizarOpcional(String valor) {
        if (!StringUtils.hasText(valor)) {
            return null;
        }
        return valor.trim();
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
