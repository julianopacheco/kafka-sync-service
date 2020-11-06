# Kafka Synchronous Service

Projeto em spring boot para executar requisições síncronas

## Composiçao do projeto
- Spring Boot (spring web, spring kafka e devtools)
- Três containers Docker contendo:
    - zookeeper
    - kafka
    - kafka-sync-service (aplicação cliente que faz a integração entre as requisições rest e as mensagens do kafka)

## Requisitos
Possuir o [docker](https://docs.docker.com/get-docker/) instalado

## Rodando o projeto
Para rodar o projeto basta, dentro da pasta do projeto, executar o seguinte comando:

```
docker-compose up
```

## Executando o teste
...

## Producer e Listener 
O projeto basicamente é composto de um serviço reste, disponível no path /process, é um listener o qual fica consumindo o tópico configurado no arquivo application.yml.