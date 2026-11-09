package com.eventos.mstickets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;

@Service
public class TicketS3Service {

    private final S3Client s3;
    private final String bucket;

    public TicketS3Service(
            @Value("${S3_BUCKET}") String bucket,
            @Value("${AWS_REGION:us-east-1}") String region) {

        this.bucket = bucket;

        this.s3 = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public String subir(Ticket ticket, Path archivo) {

        String usuario = ticket.usuarioId;

        if (usuario == null || !usuario.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException(
                    "Identificador de usuario no válido para S3"
            );
        }

        String key = "eventos/evento-" + ticket.eventoId
                + "/" + usuario
                + "/ticket-" + ticket.id + ".pdf";

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("application/pdf")
                .build();

        s3.putObject(request, archivo);

        return key;
    }

    public byte[] descargar(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("El ticket no tiene PDF en S3");
        }

        return s3.getObjectAsBytes(
            software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()
        ).asByteArray();
    }

    public void eliminar(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        s3.deleteObject(
            software.amazon.awssdk.services.s3.model.DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()
        );
    }
}
