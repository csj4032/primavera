package com.genius.primavera.testcontainers.bean;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;

public class ElasticsearchBeanCreator implements BeanCreator {

    @Override
    public Object createBean(ContainerInfo containerInfo) {
        RestClient restClient = RestClient.builder(new HttpHost(containerInfo.getHost(), containerInfo.getMappedPort(), "http")).build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        ElasticsearchClient client = new ElasticsearchClient(transport);
        SimpleElasticsearchMappingContext mappingContext = new SimpleElasticsearchMappingContext();
        ElasticsearchConverter converter = new MappingElasticsearchConverter(mappingContext);
        return new ElasticsearchTemplate(client, converter);
    }

    @Override
    public ContainerType getSupportedType() {
        return ContainerType.ELASTICSEARCH;
    }
}