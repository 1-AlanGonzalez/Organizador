package com.gymmanager.gym_manager.services.exports;


import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.gymmanager.gym_manager.entity.Anotation.ColumnLabel;

public class ExportMapper {
    public static List<Map<String, Object>> mapearEntidad(Object entidad, List<String> atributos) {

    List<Map<String, Object>> filas = new ArrayList<>();
    filas.add(new LinkedHashMap<>()); // fila base vacía

    for (String atributo : atributos) {
        String nombreColumna = obtenerLabel(entidad, atributo);
        List<Map<String, Object>> nuevasFilas = new ArrayList<>();

        for (Map<String, Object> filaActual : filas) {

            List<Object> valores = resolverRutaExpandida(entidad, atributo);

            if (valores.isEmpty()) {
                filaActual.put(nombreColumna, null);
                nuevasFilas.add(filaActual);
            } else {
                for (Object valor : valores) {
                    Map<String, Object> copia = new LinkedHashMap<>(filaActual);
                    copia.put(nombreColumna, valor);
                    nuevasFilas.add(copia);
                }
            }
        }

        filas = nuevasFilas;
    }

    return filas;
}

private static List<Object> resolverRutaExpandida(Object objeto, String ruta) {

    String[] partes = ruta.split("\\.");
    return resolverRecursivo(objeto, partes, 0);
}

private static List<Object> resolverRecursivo(Object actual, String[] partes, int index) {

    List<Object> resultados = new ArrayList<>();

    if (actual == null) {
        return resultados;
    }

    if (index >= partes.length) {
        resultados.add(actual);
        return resultados;
    }

    try {

        Field field = actual.getClass().getDeclaredField(partes[index]);
        field.setAccessible(true);
        Object valor = field.get(actual);

        if (valor == null) {
            return resultados;
        }

        // 🔥 Si es colección → expandimos
        if (valor instanceof Collection<?>) {

            for (Object elemento : (Collection<?>) valor) {
                resultados.addAll(
                        resolverRecursivo(elemento, partes, index + 1)
                );
            }

        } else {
            resultados.addAll(
                    resolverRecursivo(valor, partes, index + 1)
            );
        }

    } catch (Exception e) {
        // ignorar
    }

    return resultados;
}

private static String obtenerLabel(Object entidad, String ruta) {

    try {

        String[] partes = ruta.split("\\.");
        Class<?> claseActual = entidad.getClass();
        Field field = null;

        for (String parte : partes) {

            field = claseActual.getDeclaredField(parte);
            field.setAccessible(true);

            Class<?> tipo = field.getType();

            // 🔥 SI ES COLECCIÓN, OBTENER TIPO GENERICO
            if (Collection.class.isAssignableFrom(tipo)) {

                ParameterizedType genericType =
                        (ParameterizedType) field.getGenericType();

                claseActual = (Class<?>) genericType.getActualTypeArguments()[0];

            } else {
                claseActual = tipo;
            }
        }

        if (field != null && field.isAnnotationPresent(ColumnLabel.class)) {
            return field.getAnnotation(ColumnLabel.class).value();
        }

    } catch (Exception e) {
        // ignorar
    }

    return ruta;
}
    // private static boolean esTipoSimple(Class<?> tipo) {

    //     return tipo.isPrimitive()
    //             || tipo.equals(String.class)
    //             || Number.class.isAssignableFrom(tipo)
    //             || tipo.equals(Boolean.class)
    //             || tipo.equals(java.time.LocalDate.class)
    //             || tipo.equals(java.time.LocalDateTime.class);
    // }

    // private static Object obtenerCampoRepresentativo(Object entidadRelacionada) {

    //     try {

    //         // Si tiene campo "nombre", usamos ese
    //         Field nombreField = entidadRelacionada.getClass().getDeclaredField("nombre");
    //         nombreField.setAccessible(true);
    //         return nombreField.get(entidadRelacionada);

    //     } catch (Exception e) {
    //         // Si no tiene "nombre", intentamos con "descripcion"
    //         try {
    //             Field descField = entidadRelacionada.getClass().getDeclaredField("descripcion");
    //             descField.setAccessible(true);
    //             return descField.get(entidadRelacionada);
    //         } catch (Exception ex) {
    //             // Si no encontramos nada representativo, devolvemos toString()
    //             return entidadRelacionada.toString();
    //         }
    //     }
    // }

    // private static Object resolverRuta(Object objeto, String ruta) {

    //     try {   

    //         String[] partes = ruta.split("\\.");
    //         Object valorActual = objeto;

    //         for (String parte : partes) {

    //             if (valorActual == null) {
    //                 return null;
    //             }

    //             Field field = valorActual.getClass().getDeclaredField(parte);
    //             field.setAccessible(true);

    //             valorActual = field.get(valorActual);
    //         }

    //         return valorActual;

    //     } catch (Exception e) {
    //         return null;
    //     }
    // }
}
