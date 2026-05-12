CREATE KEYSPACE IF NOT EXISTS digital_queue_metrics
WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': 1
};

USE digital_queue_metrics;

CREATE TABLE IF NOT EXISTS metricas_inscripcion_por_fila_dia (
    fila_id bigint,
    fecha date,
    created_at timestamp,
    turno_id bigint,
    cantidad_integrantes int,
    personas_adelante bigint,
    queue_status text,
    tiempo_estimado_informado int,
    tiempo_estimado_minimo int,
    tiempo_estimado_maximo int,
    dia_semana text,
    franja_horaria int,
    PRIMARY KEY ((fila_id, fecha), created_at, turno_id)
) WITH CLUSTERING ORDER BY (created_at DESC, turno_id ASC);

CREATE TABLE IF NOT EXISTS metricas_llamado_por_fila_dia (
    fila_id bigint,
    fecha date,
    called_at timestamp,
    turno_id bigint,
    tiempo_real_espera int,
    error_prediccion int,
    PRIMARY KEY ((fila_id, fecha), called_at, turno_id)
) WITH CLUSTERING ORDER BY (called_at DESC, turno_id ASC);

CREATE TABLE IF NOT EXISTS metricas_llegadas_por_franja (
    fila_id bigint,
    dia_semana text,
    franja_horaria int,
    fecha date,
    cantidad counter,
    PRIMARY KEY ((fila_id, dia_semana, franja_horaria), fecha)
);
