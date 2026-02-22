package ru.yandex.practicum.service;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.*;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.avro.specific.SpecificRecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ComplexObjectConverter {

    public static <T extends SpecificRecord> T convertComplexPojoToAvro(Object pojo, Class<T> targetClass) throws IOException {
        // 1. Получаем схему на основе класса целевого Avro-объекта
        Schema schema = ReflectData.get().getSchema(targetClass);

        // 2. Сериализуем исходный POJO в байты с помощью ReflectDatumWriter
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BinaryEncoder binaryEncoder = EncoderFactory.get().binaryEncoder(byteArrayOutputStream, null);
        ReflectDatumWriter<Object> reflectDatumWriter = new ReflectDatumWriter<>(schema);
        reflectDatumWriter.write(pojo, binaryEncoder);
        binaryEncoder.flush();

        // 3. Десериализуем байты обратно в целевой Avro-объект (SpecificRecord)
        BinaryDecoder binaryDecoder = DecoderFactory.get().binaryDecoder(byteArrayOutputStream.toByteArray(), null);
        GenericDatumReader<T> genericDatumReader = new GenericDatumReader<>(schema);
        T avroRecord = genericDatumReader.read(null, binaryDecoder);

        return avroRecord;
    }
}