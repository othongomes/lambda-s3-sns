package br.com.validador;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.Map;

public class Handler implements RequestHandler<S3Event , String> {

    @Override
    public String handleRequest(S3Event  event, Context context) {
        LambdaLogger logger = context.getLogger();

        var record = event.getRecords().get(0);
        String nomeObjeto = record.getS3().getObject().getUrlDecodedKey();
        String bucket = record.getS3().getBucket().getName();

        logger.log("URL OBJECT " + nomeObjeto);
        logger.log("BUCKET: " + bucket);

        String[] tipos = System.getenv().get("tipos").split(",");
        var tipoObejto = nomeObjeto.split("\\.")[1].toLowerCase();
        boolean existe = Arrays.stream(tipos).anyMatch(tipoObejto::equals);

        if (!existe) {
            try {
                AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
                DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucket, nomeObjeto);
                s3Client.deleteObject(deleteObjectRequest);
                logger.log("========== OBJETO INVALIDO =========");
                logger.log("OBJETO EXCLUIDO COM SUCESSO!");
                return "Arquivo " + nomeObjeto + " excluido do s3 pois é invalido";
            } catch (Exception e) {
                logger.log(e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            logger.log("========== OBJETO VALIDO =========");
        }

        return "Arquivo " + nomeObjeto + " salvo com sucesso";
    }
}
