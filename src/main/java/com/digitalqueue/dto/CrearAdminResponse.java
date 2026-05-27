package com.digitalqueue.dto;

import com.digitalqueue.model.enums.RolAdmin;
import com.digitalqueue.model.enums.TipoOperacionLocal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearAdminResponse {

    private Boolean creado;
    private Boolean emailExistente;
    private String mensaje;
    private Long usuarioId;
    private Long localId;
    private Long filaId;
    private String nombre;
    private String email;
    private RolAdmin rol;
    private String nombreLocal;
    private String direccionLocal;
    private String linkImagenLogoLocal;
    private TipoOperacionLocal tipoOperacionLocal;
    private String codigoPublico;
}
